import React, { useState } from 'react';
import { Clock, Code, BookOpen, Users, CheckCircle, AlertCircle, Play, Info } from 'lucide-react';
import { Assessment } from '@/lib/types/assessment';
import { minutesToHours } from '@/lib/utils';
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { useParams } from 'wouter';
import { useQuery } from '@tanstack/react-query';
import useApi from '@/hooks/use-api';
import { Tooltip, TooltipProvider, TooltipTrigger, TooltipContent } from '@/components/ui/tooltip';
import { toast } from '@/hooks/use-toast';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';

export default function CandidateAssessmentPreview() {
    const [selectedLanguage, setSelectedLanguage] = useState('');
    const [email, setEmail] = useState('');
    const [isStarting, setIsStarting] = useState(false);
    // set to true after the candidate clicks the start button and connects their github account if not already connected
    const [hasStarted, setHasStarted] = useState(true);
    const [password, setPassword] = useState('');
    const params = useParams();
    const { apiCall } = useApi();
    const assessmentId = Number(params.assessment_id);

    // State variable doesn't work for some reason - gets set to null upon navigation
    let attemptId: number | null = null;

    // Creates the candidate's github repository from the template
    const createCandidateRepo = async () => {
        try {
            const data: {
                id: number;
                githubRepositoryLink: string;
                languageChoice: string;
                startedDate: string;
            } = await apiCall(`/api/attempts/live/start`, {
                method: 'POST',
                body: JSON.stringify({
                    languageChoice: selectedLanguage,
                    candidateEmail: email,
                    assessmentId: assessmentId,
                }),
            });
            return data;
        }
        catch (error: any) {
            toast({
                title: "Error",
                description: error.message,
                variant: "destructive",
            });
        }
    }

    // Fetch assessment data using Tanstack Query
    const { data: assessment, isLoading, error } = useQuery({
        queryKey: ['assessment', assessmentId],
        queryFn: async (): Promise<Assessment> => {
            const response = await apiCall(`/api/attempts/live/${assessmentId}`, {
                method: 'GET',
            });

            if (!response.id) {
                throw new Error('Failed to fetch assessment');
            }

            return response;
        },
        enabled: !!assessmentId, // Only run query if assessmentId is valid
    });

    // Email validation function
    const isValidEmail = (email: string) => {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    };

    const handleStart = async () => {
        if (!email || !isValidEmail(email)) {
            alert('Please enter a valid email address before starting.');
            return;
        }

        try {
            const isAbleToTakeAssessment = await apiCall(`/api/attempts/live/${assessmentId}/can-take-assessment?email=${email}`, {
                method: 'GET',
            });
            if (!isAbleToTakeAssessment.result) {
                alert('You are not eligible to take this assessment. You either have not been invited, or have already taken the assessment. Please contact the employer to re-invite you.');
                return;
            }
            attemptId = isAbleToTakeAssessment.attemptId;
            console.log('Attempt ID:', isAbleToTakeAssessment.attemptId);
        } catch (error: any) {
            toast({
                title: "Invalid Credentials",
                description: error.message || 'An error occurred during authentication. Please try again.',
                variant: "destructive",
            });
            return;
        }

        // Github app installation URL with state parameter specifying this is a candidate installation
        try {
            const authResp = await apiCall(`/api/attempts/live/authenticate`, {
                method: 'POST',
                body: JSON.stringify({
                    candidateEmail: email,
                    plainTextPassword: password,
                    assessmentId: assessmentId
                }),
            });
            // Check if user needs to be redirected to GitHub app installation
            // Ensures that candidate has valid github connection
            //if (authResp?.requiresRedirect && authResp?.redirectUrl) {
            if (!authResp?.result) {
                const githubInstallUrlResponse: { redirectUrl: string } = await apiCall("/api/attempts/live/github/generate-install-url?email=" + email, {
                    method: "POST"
                });
                // Enter polling state immediately so UI updates and disables button
                setIsStarting(true);
                // Show loading state
                toast({
                    title: "Connecting GitHub",
                    description: "Please complete the GitHub installation in the new tab. This tab will automatically proceed once connected.",
                });
                // Open GitHub installation in new tab
                window.open(githubInstallUrlResponse.redirectUrl, '_blank');
                // Start polling for GitHub connection
                startPollingForGitHubConnection();
                return;
            }

            // if (authResp?.result === false) {
            //     alert('You are not invited to take this assessment.');
            //     return;
            // }
        } catch (error: any) {
            // Handle authentication errors
            if (error.status === 401) {
                //alert(error.message || 'Your email and password do not match. Please check your credentials and try again.');
                toast({
                    title: "Authentication Error",
                    description: 'Your email and password do not match. Please check your credentials and try again.',
                    variant: "destructive",
                });
            } else {
                //alert('An error occurred during authentication. Please try again.');
                toast({
                    title: "Authentication Error",
                    description: 'An error occurred during authentication. Please try again: ' + error.message,
                    variant: "destructive",
                });
            }
            return;
        }

        if (assessment?.languageOptions?.length && !selectedLanguage) {
            toast({
                description: 'Please select a tech stack before starting.',
                variant: "destructive",
            });
            return;
        }

        // github app flow

        //setIsStarting(true);

        // check if the candidate has a valid github token

        // redirect to starting page
        console.log('Candidate has a valid github token, redirecting to starting page');
        console.log('Attempt ID:', attemptId);
        try {
            const candidateRepoData = await createCandidateRepo();
            //await startRecording();
            window.open(candidateRepoData?.githubRepositoryLink, "_blank");
        }
        catch (error: any) {
            // if (error instanceof Error && error.name === "NotAllowedError") {
            //   setShowPermissionDialog(true);
            // }
            toast({
                title: "Error",
                description: error.message,
                variant: "destructive",
            });
        }
        setIsStarting(false);
        //navigate(`/assessments/starting/${attemptId}`);

        setHasStarted(true);

    };

    // Polling function to check if GitHub connection is established
    const startPollingForGitHubConnection = async () => {
        const maxAttempts = 60; // 5 minutes with 5-second intervals
        const pollInterval = 5000; // 5 seconds
        let attempts = 0;

        const poll = async () => {
            if (attempts >= maxAttempts) {
                console.log('GitHub connection polling timeout reached');
                setIsStarting(false);
                toast({
                    title: "Connection Timeout",
                    description: 'GitHub connection timeout. Please try again.',
                    variant: "destructive",
                });
                return;
            }

            try {
                const response = await apiCall(`/api/attempts/live/has-valid-github-token?email=${email}`, {
                    method: 'GET',
                });

                console.log('GitHub connection polling attempt', attempts + 1, ':', response);

                if (response.result) {
                    // GitHub connection established, now create the candidate repo
                    console.log('GitHub connection established, creating candidate repository...');

                    try {
                        const candidateRepoData = await createCandidateRepo();
                        window.open(candidateRepoData?.githubRepositoryLink, "_blank");
                        setIsStarting(false);
                        setHasStarted(true);
                        return;
                    } catch (error: any) {
                        console.error('Error creating candidate repository:', error);
                        setIsStarting(false);
                        toast({
                            title: "Error",
                            description: error.message,
                            variant: "destructive",
                        });
                        return;
                    }
                }

                // Continue polling
                attempts++;
                setTimeout(poll, pollInterval);
            } catch (error) {
                console.error('GitHub connection polling error:', error);
                attempts++;
                setTimeout(poll, pollInterval);
            }
        };

        // Start polling
        poll();
    };

    // Polling function to check if GitHub token is valid (legacy - keeping for backward compatibility)
    // const startPollingForGitHubToken = async () => {
    //     const maxAttempts = 60; // 5 minutes with 7-second intervals
    //     const pollInterval = 7000; // 7 seconds
    //     let attempts = 0;

    //     const poll = async () => {
    //         if (attempts >= maxAttempts) {
    //             console.log('Polling timeout reached');
    //             setIsStarting(false);
    //             alert('GitHub connection timeout. Please try again.');
    //             return;
    //         }

    //         try {
    //             const response = await apiCall(`/api/attempts/live/has-valid-github-token?email=${email}`, {
    //                 method: 'GET',
    //             });

    //             console.log('Polling attempt', attempts + 1, ':', response);

    //             if (response.result) {
    //                 // Valid token found, redirect to starting page
    //                 console.log('Valid GitHub token found, redirecting...');
    //                 setIsStarting(false);

    //                 // Extract attempt_id from the response or use a default
    //                 const attemptId = response.attemptId || 'default';
    //                 //navigate(`/assessments/starting/${attemptId}`);
    //                 setHasStarted(true);
    //                 return;
    //             }

    //             // Continue polling
    //             attempts++;
    //             setTimeout(poll, pollInterval);
    //         } catch (error) {
    //             console.error('Polling error:', error);
    //             attempts++;
    //             setTimeout(poll, pollInterval);
    //         }
    //     };

    //     // Start polling
    //     poll();
    // };

    // Show loading state
    if (isLoading) {
        return (
            <div className="min-h-screen bg-gray-900 text-gray-100 flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
                    <p className="text-gray-400">Loading assessment...</p>
                </div>
            </div>
        );
    }

    // Show error state
    if (error) {
        return (
            <div className="min-h-screen bg-gray-900 text-gray-100 flex items-center justify-center">
                <div className="text-center">
                    <AlertCircle className="h-12 w-12 text-red-500 mx-auto mb-4" />
                    <p className="text-red-400 mb-2">Failed to load assessment</p>
                    <p className="text-gray-400 text-sm">{error.message}</p>
                </div>
            </div>
        );
    }

    // Show content when assessment is loaded
    if (!assessment || assessment.status !== 'ACTIVE') {
        return (
            <div className="min-h-screen bg-gray-900 text-gray-100 flex items-center justify-center">
                <div className="text-center">
                    <p className="text-gray-400">Assessment not found</p>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-900 text-gray-100">
            {/* Header */}
            <div className="bg-gray-800 border-b border-gray-700">
                <div className="max-w-7xl mx-auto py-4">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-2xl font-bold text-white">{assessment.name}</h1>
                            <p className="text-gray-400 mt-1">Position: {assessment.role} - {assessment.employerName}</p>
                        </div>
                        <div className="flex items-center space-x-4">
                            <div className="flex items-center space-x-2 bg-blue-900/30 px-3 py-1 rounded-full">
                                <Code className="h-4 w-4 text-green-400" />
                                <span className="text-sm font-medium text-blue-300">Live Coding Assessment</span>
                            </div>
                            <div className="flex items-center space-x-2 text-gray-400">
                                <Clock className="h-4 w-4" />
                                {hasStarted ? (
                                    <span className="text-sm">
                                        Time Remaining: {`${(assessment.duration ?? 0) % 60 === 0 ? minutesToHours(assessment.duration ?? 0) : assessment.duration ?? 0} ${(assessment.duration ?? 0) % 60 === 0 ? 'hours' : 'minutes'}`}
                                    </span>

                                ) : (
                                    <span className="text-sm">
                                        Time Limit: {`${(assessment.duration ?? 0) % 60 === 0 ? minutesToHours(assessment.duration ?? 0) : assessment.duration ?? 0} ${(assessment.duration ?? 0) % 60 === 0 ? 'hours' : 'minutes'}`}
                                    </span>
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-7xl mx-auto py-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* Main Content */}
                    <div className="lg:col-span-2 space-y-8">
                        {/* Skills & Competencies */}
                        {/* <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <div className="flex items-center space-x-2 mb-4">
                                <Users className="h-5 w-5 text-purple-400" />
                                <h2 className="text-xl font-semibold text-white">Skills & Competencies</h2>
                            </div>
                            <div className={`grid gap-3 ${assessment.skills.length > 8 ? "grid-cols-3" : "grid-cols-2"}`}>
                                {assessment.skills.map((skill, index) => (
                                    <div key={index} className="flex items-center space-x-2 bg-gray-700/50 rounded-lg p-3">
                                        <CheckCircle className="h-4 w-4 text-green-400 flex-shrink-0" />
                                        <span className="text-gray-300 text-sm">{skill}</span>
                                    </div>
                                ))}
                            </div>
                        </div> */}

                        {/* Description */}
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <h2 className="text-xl font-semibold text-white mb-4">Assessment Description</h2>
                            <p className="text-gray-300 leading-relaxed">{assessment.description}</p>
                        </div>

                        {/* Rules */}
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <div className="flex items-center space-x-2 mb-4">
                                <AlertCircle className="h-5 w-5 text-yellow-400" />
                                <h2 className="text-xl font-semibold text-white">Rules & Guidelines</h2>
                            </div>
                            <ul className="space-y-3">
                                {assessment.rules?.map((rule, index) => (
                                    <li key={index} className="flex items-start space-x-3">
                                        <div className="w-2 h-2 bg-yellow-400 rounded-full mt-2 flex-shrink-0"></div>
                                        <span className="text-gray-300">{rule}</span>
                                    </li>
                                ))}
                            </ul>
                        </div>

                        {/* Instructions */}
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <div className="flex items-center space-x-2 mb-4">
                                <BookOpen className="h-5 w-5 text-blue-400" />
                                <h2 className="text-xl font-semibold text-white">Instructions</h2>
                            </div>
                            <ol className="space-y-3">
                                {assessment.instructions?.map((instruction, index) => (
                                    <li key={index} className="flex items-start space-x-3">
                                        <div className="w-6 h-6 bg-blue-600 text-white rounded-full flex items-center justify-center text-sm font-medium flex-shrink-0">
                                            {index + 1}
                                        </div>
                                        <span className="text-gray-300">{instruction}</span>
                                    </li>
                                ))}
                            </ol>
                        </div>
                    </div>

                    {/* Sidebar */}
                    {hasStarted ? (
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <h3 className="text-lg font-semibold text-white mb-4">Good luck!</h3>
                            <p className="text-gray-300">The GitHub repository has been created for you. You can start the assessment now.</p>
                            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">

                                <Dialog>
                                    <DialogTrigger asChild className="w-full flex">
                                        <button
                                            //onClick={handleFinish}
                                            //disabled={(!selectedLanguage && !!assessment.languageOptions?.length) || !email || !isValidEmail(email) || isStarting}
                                            disabled={isStarting}
                                            className="w-full  disabled:from-gray-600 disabled:to-gray-600 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-all flex items-center justify-center space-x-2"
                                        // className="w-full bg-gradient-to-r duration-1000 from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 disabled:from-gray-600 disabled:to-gray-600 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-all flex items-center justify-center space-x-2"
                                        >
                                            <CheckCircle className="h-5 w-5" />
                                            <span>I'm ready to submit</span>
                                        </button>
                                    </DialogTrigger>
                                    <DialogContent className="bg-slate-800/60 border-slate-700/50 text-slate-300 backdrop-blur-sm">
                                        <DialogHeader>
                                            <DialogTitle>Submit Assessment</DialogTitle>
                                        </DialogHeader>
                                        <DialogDescription>
                                            Ensure you have committed and pushed your changes to your branch and submitted a pull request before submitting.
                                        </DialogDescription>
                                        <DialogFooter>
                                            <Button type="submit" variant="default" className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm">Submit</Button>
                                        </DialogFooter>
                                    </DialogContent>
                                </Dialog>
                            </div>
                        </div>

                    ) : (
                        <div className="space-y-6">
                            {/* Language Selection */}
                            {assessment.languageOptions?.length && assessment.languageOptions?.length > 0 ? (
                                <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                                    <h3 className="text-lg font-semibold text-white mb-4">Choose your stack</h3>
                                    <div className="space-y-2">
                                        {/* <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Language/Framework Combination
                                    </label> */}
                                        {/* <select
                                    value={selectedLanguage}
                                    onChange={(e) => setSelectedLanguage(e.target.value)}
                                    className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Select your preferred language...</option>
                                    {assessment.languageOptions?.map((option) => (
                                        <option key={option.replace(" ", "-").toLowerCase()} value={option}>
                                            {option}
                                        </option>
                                    ))}
                                </select> */}
                                        <Select
                                            value={selectedLanguage}
                                            onValueChange={(value) => setSelectedLanguage(value)}
                                        >
                                            <SelectTrigger className="w-11/12 bg-slate-700 text-gray-100">
                                                <SelectValue placeholder="Select a language/framework" />
                                            </SelectTrigger>
                                            <SelectContent className="bg-slate-700 text-gray-100">
                                                <SelectGroup>
                                                    <SelectLabel>Languages/Frameworks</SelectLabel>
                                                    {assessment.languageOptions?.map((option) => (
                                                        <SelectItem key={option.replace(" ", "-").toLowerCase()} value={option}>
                                                            {option}
                                                        </SelectItem>
                                                    ))}
                                                </SelectGroup>
                                            </SelectContent>
                                        </Select>
                                    </div>
                                </div>) : (
                                <>
                                </>
                            )}

                            {/*Email/password*/}
                            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                                <h3 className="text-lg font-semibold text-white mb-4">Enter your email address</h3>
                                <div className="space-y-2">
                                    {/* <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Language/Framework Combination
                                    </label> */}
                                    {/* <select
                                    value={selectedLanguage}
                                    onChange={(e) => setSelectedLanguage(e.target.value)}
                                    className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Select your preferred language...</option>
                                    {assessment.languageOptions?.map((option) => (
                                        <option key={option.replace(" ", "-").toLowerCase()} value={option}>
                                            {option}
                                        </option>
                                    ))}
                                </select> */}
                                    <Input
                                        type="email"
                                        placeholder="Enter your email address"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        className="w-11/12 bg-slate-700 text-gray-100"
                                    />
                                </div>
                            </div>
                            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                                <div className="flex items-center gap-x-2">
                                    <h3 className="text-lg font-semibold text-white mb-4">Password</h3>
                                    <TooltipProvider delayDuration={100}>
                                        <Tooltip>
                                            <TooltipTrigger className="cursor-pointer">
                                                <Info className="w-4 h-4" />
                                            </TooltipTrigger>
                                            <TooltipContent className="bg-slate-700 border-slate-600 text-gray-100">
                                                <p>Enter the password provided in the email invitation to start the assessment (case-sensitive)</p>
                                            </TooltipContent>
                                        </Tooltip>
                                    </TooltipProvider>
                                </div>
                                <div className="space-y-2">
                                    {/* <label className="block text-sm font-medium text-gray-300 mb-2">
                                        Language/Framework Combination
                                    </label> */}
                                    {/* <select
                                    value={selectedLanguage}
                                    onChange={(e) => setSelectedLanguage(e.target.value)}
                                    className="w-full bg-gray-700 border border-gray-600 rounded-lg px-3 py-2 text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                                >
                                    <option value="">Select your preferred language...</option>
                                    {assessment.languageOptions?.map((option) => (
                                        <option key={option.replace(" ", "-").toLowerCase()} value={option}>
                                            {option}
                                        </option>
                                    ))}
                                </select> */}
                                    <Input
                                        type="password"
                                        placeholder="Enter your password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="w-11/12 bg-slate-700 text-gray-100"
                                    />
                                </div>
                            </div>

                            {/* Start Button */}
                            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                                <button
                                    onClick={handleStart}
                                    disabled={(!selectedLanguage && !!assessment.languageOptions?.length) || !email || !isValidEmail(email) || isStarting}
                                    className="w-full  disabled:from-gray-600 disabled:to-gray-600 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-all flex items-center justify-center space-x-2"
                                // className="w-full bg-gradient-to-r duration-1000 from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 disabled:from-gray-600 disabled:to-gray-600 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-all flex items-center justify-center space-x-2"
                                >
                                    {isStarting ? (
                                        <>
                                            <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                                            <span>Waiting for GitHub Connection...</span>
                                        </>
                                    ) : (
                                        <>
                                            <Play className="h-5 w-5" />
                                            <span>Start Assessment</span>
                                        </>
                                    )}
                                </button>
                                {!selectedLanguage && !!assessment.languageOptions?.length && (
                                    <p className="text-sm text-gray-400 mt-2 text-center">
                                        Please select a language combination first
                                    </p>
                                )}
                                {(!email || !isValidEmail(email)) && (
                                    <p className="text-sm text-gray-400 mt-2 text-center">
                                        Please enter a valid email address
                                    </p>
                                )}
                            </div>

                            {/* Quick Info */}
                            {/* <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <h3 className="text-lg font-semibold text-white mb-4">Quick Info</h3>
                            <div className="space-y-3">
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Type:</span>
                                    <span className="text-gray-300">{assessment.type}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Duration:</span>
                                    <span className="text-gray-300">{assessment.duration}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Skills:</span>
                                    <span className="text-gray-300">{assessment.skills.length} areas</span>
                                </div>
                            </div>
                        </div> */}
                        </div>)}
                </div>
            </div>
        </div>
    );
}