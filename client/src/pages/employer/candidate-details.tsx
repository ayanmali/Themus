import { useState } from "react";
import { ArrowLeft, Calendar, Check, Edit3, Mail, Phone, MapPin, Globe, GraduationCap, Briefcase, Trash2, X, Loader2, Info, Plus, ExternalLink, User } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { useRoute, useLocation } from "wouter";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import useApi from "@/hooks/use-api";
import { useToast } from "@/hooks/use-toast";
import { Tooltip, TooltipProvider, TooltipTrigger, TooltipContent } from "@/components/ui/tooltip";
import { PaginatedResponse } from "@/lib/types/paginated-response";

import { Candidate } from "@/lib/types/candidate";
import { CandidateAttempt } from "@/lib/types/candidate-attempt";

export default function CandidateDetails() {
    const { apiCall } = useApi();
    const { toast } = useToast();
    const queryClient = useQueryClient();
    const [, navigate] = useLocation();
    const [, params] = useRoute("/candidates/:candidateId");
    const candidateId = params?.candidateId;

    // State for editing
    const [isEditingName, setIsEditingName] = useState(false);
    const [isEditingEmail, setIsEditingEmail] = useState(false);
    const [tempName, setTempName] = useState('');
    const [tempEmail, setTempEmail] = useState('');

    // State for tracking changes
    const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
    const [pendingChanges, setPendingChanges] = useState<Partial<Candidate>>({});

    // State for metadata editing
    const [newMetadataKey, setNewMetadataKey] = useState('');
    const [newMetadataValue, setNewMetadataValue] = useState('');

    // Email dialog state
    const [isEmailDialogOpen, setIsEmailDialogOpen] = useState(false);
    const [emailSubject, setEmailSubject] = useState('');
    const [emailMessage, setEmailMessage] = useState('');

    // Attempts pagination
    const [currentAttemptsPage, setCurrentAttemptsPage] = useState(1);
    const attemptsPerPage = 10;

    // Fetch candidate data
    const { data: candidate, isLoading: candidateLoading, error: candidateError } = useQuery({
        queryKey: ['candidate', candidateId],
        queryFn: async (): Promise<Candidate> => {
            const response = await apiCall(`/api/candidates/${candidateId}`, {
                method: 'GET',
            });

            if (!response) {
                throw new Error('Failed to fetch candidate');
            }

            return response;
        },
        enabled: !!candidateId,
    });

    // Update candidate mutation
    const updateCandidateMutation = useMutation({
        mutationFn: async (updatedCandidate: Partial<Candidate>) => {
            const response = await apiCall(`/api/candidates/${candidateId}`, {
                method: 'PUT',
                body: JSON.stringify(updatedCandidate),
            });

            if (!response) {
                throw new Error('Failed to update candidate');
            }

            return response;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['candidate', candidateId] });
            queryClient.invalidateQueries({ queryKey: ['candidates'] });
            clearPendingChanges();
            toast({
                title: "Success",
                description: "Candidate updated successfully",
            });
        },
        onError: (error: any) => {
            toast({
                title: "Error",
                description: error.message || "Failed to update candidate",
                variant: "destructive",
            });
        },
    });

    // Send email mutation
    const sendEmailMutation = useMutation({
        mutationFn: async ({ candidateId, subject, text }: { candidateId: number; subject: string; text: string }) => {
            const response = await apiCall(`/api/email/send`, {
                method: 'POST',
                body: JSON.stringify({
                    candidateId: candidateId,
                    subject: subject,
                    text: text
                }),
            });
            return response;
        },
        onSuccess: () => {
            // Close dialog and reset form
            setIsEmailDialogOpen(false);
            setEmailSubject('');
            setEmailMessage('');
            
            toast({
                title: "Success",
                description: "Email sent successfully",
            });
        },
        onError: (error: any) => {
            toast({
                title: "Error",
                description: error.message || "Failed to send email",
                variant: "destructive",
            });
        },
    });

    // Fetch candidate attempts
    const { data: attemptsData, isLoading: attemptsLoading, error: attemptsError } = useQuery({
        queryKey: ['candidateAttempts', 'candidate', candidateId, currentAttemptsPage],
        queryFn: async (): Promise<PaginatedResponse<CandidateAttempt>> => {
            const response = await apiCall(`/api/attempts/filter?candidateId=${candidateId}&page=${currentAttemptsPage - 1}&size=${attemptsPerPage}`, {
                method: 'GET',
            });

            if (!response) {
                throw new Error('Failed to fetch candidate attempts');
            }

            return response;
        },
        enabled: !!candidateId,
    });

    // Extract attempts from the response
    const candidateAttempts: CandidateAttempt[] = attemptsData?.content || [];
    const totalAttempts: number = attemptsData?.totalElements || 0;
    const totalAttemptsPages: number = attemptsData?.totalPages || 0;

    // Helper function to track changes
    const addPendingChange = (field: keyof Candidate, value: any) => {
        setPendingChanges(prev => ({
            ...prev,
            [field]: value
        }));
        setHasUnsavedChanges(true);
    };

    // Helper function to clear changes
    const clearPendingChanges = () => {
        setPendingChanges({});
        setHasUnsavedChanges(false);
    };

    // Helper function to save all pending changes
    const saveAllChanges = () => {
        if (Object.keys(pendingChanges).length > 0) {
            updateCandidateMutation.mutate(pendingChanges);
            clearPendingChanges();
        }
    };

    // Helper function to get current value (including pending changes)
    const getCurrentValue = (field: keyof Candidate) => {
        if (pendingChanges[field] !== undefined) {
            return pendingChanges[field];
        }
        return candidate?.[field];
    };

    // Helper functions for specific field types
    const getCurrentName = (): string => {
        if (pendingChanges.fullName !== undefined) {
            return pendingChanges.fullName as string;
        }
        return candidate?.fullName || '';
    };

    const getCurrentEmail = (): string => {
        if (pendingChanges.email !== undefined) {
            return pendingChanges.email as string;
        }
        return candidate?.email || '';
    };

    const getCurrentMetadata = (): Record<string, string> => {
        if (pendingChanges.metadata !== undefined) {
            return pendingChanges.metadata as Record<string, string>;
        }
        return candidate?.metadata || {};
    };

    // Editing functions
    const startEditingName = () => {
        setIsEditingName(true);
        setTempName(candidate?.fullName || '');
    };

    const saveName = () => {
        if (candidate) {
            addPendingChange('fullName', tempName);
        }
        setIsEditingName(false);
    };

    const cancelNameEdit = () => {
        setIsEditingName(false);
        setTempName('');
    };

    const startEditingEmail = () => {
        setIsEditingEmail(true);
        setTempEmail(candidate?.email || '');
    };

    const saveEmail = () => {
        if (candidate) {
            addPendingChange('email', tempEmail);
        }
        setIsEditingEmail(false);
    };

    const cancelEmailEdit = () => {
        setIsEditingEmail(false);
        setTempEmail('');
    };

    // Metadata functions
    const addMetadataField = () => {
        if (newMetadataKey && newMetadataValue && candidate) {
            const updatedMetadata = {
                ...getCurrentMetadata(),
                [newMetadataKey]: newMetadataValue
            };

            addPendingChange('metadata', updatedMetadata);

            setNewMetadataKey('');
            setNewMetadataValue('');
        }
    };

    const deleteMetadataField = (key: string) => {
        if (candidate) {
            const newMetadata = { ...getCurrentMetadata() };
            delete newMetadata[key];

            addPendingChange('metadata', newMetadata);
        }
    };

    const updateMetadataField = (oldKey: string, newKey: string, newValue: string) => {
        if (candidate) {
            const newMetadata = { ...getCurrentMetadata() };
            if (oldKey !== newKey) {
                delete newMetadata[oldKey];
            }
            newMetadata[newKey] = newValue;

            addPendingChange('metadata', newMetadata);
        }
    };

    const getStatusColor = (status: string) => {
        switch (status.toLowerCase()) {
            case 'invited': return 'bg-blue-600';
            case 'started': return 'bg-yellow-600';
            case 'completed': return 'bg-green-600';
            case 'evaluated': return 'bg-purple-600';
            default: return 'bg-gray-600';
        }
    };

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    const getMetadataIcon = (key: string) => {
        const lowerKey = key.toLowerCase();
        if (lowerKey.includes('phone')) return <Phone className="w-4 h-4" />;
        if (lowerKey.includes('location') || lowerKey.includes('address')) return <MapPin className="w-4 h-4" />;
        if (lowerKey.includes('linkedin')) return <Globe className="w-4 h-4" />;
        if (lowerKey.includes('portfolio') || lowerKey.includes('website')) return <Globe className="w-4 h-4" />;
        if (lowerKey.includes('education') || lowerKey.includes('degree')) return <GraduationCap className="w-4 h-4" />;
        if (lowerKey.includes('experience') || lowerKey.includes('company') || lowerKey.includes('position')) return <Briefcase className="w-4 h-4" />;
        return <User className="w-4 h-4" />;
    };

    // Loading state
    if (candidateLoading) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-7xl mx-auto">
                    <div className="flex items-center justify-center py-12">
                        <div className="flex items-center space-x-2">
                            <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
                            <span className="text-gray-400">Loading candidate...</span>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // Error state
    if (candidateError || !candidate) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-7xl mx-auto">
                    <div className="bg-red-50 border border-red-200 rounded-md p-4">
                        <div className="flex">
                            <div className="ml-3">
                                <h3 className="text-sm font-medium text-red-800">
                                    Error loading candidate
                                </h3>
                                <div className="mt-2 text-sm text-red-700">
                                    <p>{candidateError?.message || "Candidate not found"}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-900 text-white p-6">
            <div className="max-w-7xl mx-auto">
                <div className="flex justify-between items-center mb-6">
                    <button
                        onClick={() => navigate("/candidates")}
                        className="flex items-center gap-2 text-blue-400 hover:text-blue-300 mb-6 transition-colors"
                    >
                        <ArrowLeft size={20} />
                        Back to Candidates
                    </button>
                    <span className="text-red-500" hidden={!hasUnsavedChanges}>You have unsaved changes</span>
                </div>

                <div className="text-sm grid grid-cols-1 lg:grid-cols-3 gap-6 h-[calc(100vh-8rem)]">
                    <div className="lg:col-span-2 overflow-y-auto">
                        <div className="bg-gray-800 rounded-lg p-8 shadow-xl">
                            {/* Header Section */}
                            <div className="flex justify-between items-start mb-6">
                                <div className="flex-1">
                                    <div className="mb-4">
                                        {isEditingName ? (
                                            <div className="flex items-center gap-2">
                                                <Input
                                                    type="text"
                                                    value={tempName}
                                                    onChange={(e) => setTempName(e.target.value)}
                                                    className="text-3xl font-bold bg-gray-700 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none flex-1"
                                                    autoFocus
                                                />
                                                <button
                                                    onClick={saveName}
                                                    className="p-2 text-green-400 hover:text-green-300 hover:bg-gray-700 rounded transition-colors"
                                                >
                                                    <Check size={20} />
                                                </button>
                                                <button
                                                    onClick={cancelNameEdit}
                                                    className="p-2 text-red-400 hover:text-red-300 hover:bg-gray-700 rounded transition-colors"
                                                >
                                                    <X size={16} />
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="flex items-center gap-2">
                                                <h1 className="text-3xl font-bold">{getCurrentName()}</h1>
                                                <button
                                                    onClick={startEditingName}
                                                    className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                                >
                                                    <Edit3 size={16} />
                                                </button>
                                            </div>
                                        )}
                                    </div>

                                    <div className="flex items-center gap-6 text-sm text-gray-400">
                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} />
                                            <span>Added: {formatDate(candidate.createdDate || '')}</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} />
                                            <span>Updated: {formatDate(candidate.updatedDate || '')}</span>
                                        </div>
                                    </div>
                                </div>

                                <div className="flex space-x-2">
                                    <Button
                                        onClick={() => setIsEmailDialogOpen(true)}
                                        className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg transition-colors text-sm"
                                    >
                                        <Mail size={16} />
                                        Send Email
                                    </Button>
                                </div>
                            </div>

                            {/* Contact Information */}
                            <div className="mt-6">
                                <div className="flex items-center justify-between mb-2">
                                    <div className="flex items-center gap-5">
                                        <h3 className="text-lg font-semibold">Contact Information</h3>
                                        <TooltipProvider delayDuration={100}>
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Info size={16} className="text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors" />
                                                </TooltipTrigger>
                                                <TooltipContent className="bg-gray-800 text-white border-gray-500">
                                                    <p>Primary contact information for the candidate</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        </TooltipProvider>
                                    </div>
                                </div>
                                <div className="bg-gray-700 rounded-lg p-4">
                                    <div className="space-y-3">
                                        <div className="flex items-center justify-between">
                                            <div className="flex items-center gap-3">
                                                <Mail className="w-4 h-4 text-gray-400" />
                                                <span className="text-gray-300">Email:</span>
                                            </div>
                                            <div className="flex items-center gap-2">
                                                {isEditingEmail ? (
                                                    <div className="flex items-center gap-2">
                                                        <Input
                                                            type="email"
                                                            value={tempEmail}
                                                            onChange={(e) => setTempEmail(e.target.value)}
                                                            className="bg-gray-600 text-white px-3 py-1 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                                                            autoFocus
                                                        />
                                                        <button
                                                            onClick={saveEmail}
                                                            className="p-1 text-green-400 hover:text-green-300 hover:bg-gray-600 rounded transition-colors"
                                                        >
                                                            <Check size={14} />
                                                        </button>
                                                        <button
                                                            onClick={cancelEmailEdit}
                                                            className="p-1 text-red-400 hover:text-red-300 hover:bg-gray-600 rounded transition-colors"
                                                        >
                                                            <X size={14} />
                                                        </button>
                                                    </div>
                                                ) : (
                                                    <>
                                                        <span className="text-white">{getCurrentEmail()}</span>
                                                        <button
                                                            onClick={startEditingEmail}
                                                            className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-600 rounded transition-colors"
                                                        >
                                                            <Edit3 size={14} />
                                                        </button>
                                                    </>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Metadata Section */}
                            <div className="mt-6">
                                <div className="flex items-center justify-between mb-2">
                                    <h3 className="text-lg font-semibold">Additional Information</h3>
                                </div>
                                <div className="bg-gray-700 rounded-lg p-4">
                                    <div className="space-y-4 mb-6">
                                        {Object.entries(getCurrentMetadata()).map(([key, value]) => (
                                            <div key={key} className="bg-gray-800 rounded-lg p-4 flex items-center gap-4">
                                                <div className="flex items-center gap-2 text-gray-400">
                                                    {getMetadataIcon(key)}
                                                </div>
                                                <div className="flex-1 grid grid-cols-2 gap-4">
                                                    <Input
                                                        type="text"
                                                        value={key}
                                                        onChange={(e) => updateMetadataField(key, e.target.value, value)}
                                                        className="bg-gray-700 text-white px-3 py-2 border rounded-md border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
                                                        placeholder="Field name"
                                                    />
                                                    <Input
                                                        type="text"
                                                        value={value}
                                                        onChange={(e) => updateMetadataField(key, key, e.target.value)}
                                                        className="bg-gray-700 text-white px-3 py-2 rounded-md border border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
                                                        placeholder="Field value"
                                                    />
                                                </div>
                                                <button
                                                    onClick={() => deleteMetadataField(key)}
                                                    className="p-2 text-red-400 hover:text-red-300 hover:bg-gray-600 rounded transition-colors"
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        ))}
                                    </div>

                                    <div className="bg-gray-800 rounded-lg p-4 flex items-center gap-4">
                                        <div className="flex items-center gap-2 text-gray-400">
                                            <Plus className="w-4 h-4" />
                                        </div>
                                        <div className="flex-1 grid grid-cols-2 gap-4">
                                            <Input
                                                type="text"
                                                value={newMetadataKey}
                                                onChange={(e) => setNewMetadataKey(e.target.value)}
                                                className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
                                                placeholder="New field name"
                                            />
                                            <Input
                                                type="text"
                                                value={newMetadataValue}
                                                onChange={(e) => setNewMetadataValue(e.target.value)}
                                                className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
                                                placeholder="New field value"
                                            />
                                        </div>
                                        <button
                                            onClick={addMetadataField}
                                            disabled={!newMetadataKey || !newMetadataValue}
                                            className="p-2 text-green-400 hover:text-green-300 hover:bg-gray-600 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            <Plus size={16} />
                                        </button>
                                    </div>
                                </div>
                            </div>

                            {/* Assessment History */}
                            <div className="mt-6">
                                <div className="flex items-center justify-between mb-2">
                                    <h3 className="text-lg font-semibold">Assessment History</h3>
                                </div>
                                <div className="bg-gray-700 rounded-lg p-4">
                                    {/* Loading State for Attempts */}
                                    {attemptsLoading && (
                                        <div className="flex items-center justify-center py-8">
                                            <div className="flex items-center space-x-2">
                                                <Loader2 className="h-5 w-5 animate-spin text-gray-400" />
                                                <span className="text-gray-400">Loading assessment history...</span>
                                            </div>
                                        </div>
                                    )}

                                    {/* Error State for Attempts */}
                                    {attemptsError && (
                                        <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
                                            <div className="flex">
                                                <div className="ml-3">
                                                    <h3 className="text-sm font-medium text-red-800">
                                                        Error loading assessment history
                                                    </h3>
                                                    <div className="mt-2 text-sm text-red-700">
                                                        <p>{attemptsError.message}</p>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    )}

                                    {/* Assessment History List */}
                                    {!attemptsLoading && !attemptsError && (
                                        <>
                                            <div className="mb-4">
                                                <p className="text-sm text-gray-400">
                                                    Showing {candidateAttempts.length} of {totalAttempts} assessments
                                                </p>
                                            </div>

                                            <div className="space-y-3">
                                                {candidateAttempts.length > 0 ? (
                                                    candidateAttempts.map((attempt: CandidateAttempt) => (
                                                        <div
                                                            key={attempt.id}
                                                            className="bg-gray-800 rounded-lg p-4 flex items-center justify-between hover:bg-gray-750 transition-colors"
                                                        >
                                                            <div className="flex-1">
                                                                <div className="flex items-center gap-4">
                                                                    <div>
                                                                        <h4 className="font-medium text-white">
                                                                            {attempt.assessment?.name}
                                                                        </h4>
                                                                        <p className="text-sm text-gray-400">{attempt.assessment?.role}</p>
                                                                        <div className="flex items-center gap-4 text-xs text-gray-500 mt-1">
                                                                            {attempt.startedDate && (
                                                                                <span>Started: {formatDate(attempt.startedDate)}</span>
                                                                            )}
                                                                            {attempt.completedDate && (
                                                                                <span>Completed: {formatDate(attempt.completedDate)}</span>
                                                                            )}
                                                                            {attempt.evaluatedDate && (
                                                                                <span>Evaluated: {formatDate(attempt.evaluatedDate)}</span>
                                                                            )}
                                                                        </div>
                                                                    </div>
                                                                </div>
                                                            </div>
                                                            <div className="flex items-center gap-4">
                                                                <span className={`px-3 py-1 rounded-full text-sm font-medium capitalize text-white ${getStatusColor(attempt.status?.toLowerCase())}`}>
                                                                    {attempt.status?.toLowerCase()}
                                                                </span>
                                                                <DropdownMenu>
                                                                    <DropdownMenuTrigger asChild>
                                                                        <Button variant="ghost" className="p-2 hover:bg-slate-700 hover:text-white rounded-lg transition-colors">
                                                                            <ExternalLink size={16} />
                                                                        </Button>
                                                                    </DropdownMenuTrigger>
                                                                    <DropdownMenuContent className="w-56 bg-slate-800 text-white border-slate-500" align="start">
                                                                        <DropdownMenuLabel>Actions</DropdownMenuLabel>
                                                                        <DropdownMenuGroup>
                                                                            <DropdownMenuItem 
                                                                                className="hover:bg-slate-700 transition-colors hover:text-white"
                                                                                onClick={() => navigate(`/assessments/view/${attempt.assessment?.id}`)}
                                                                            >
                                                                                View Assessment Details
                                                                            </DropdownMenuItem>
                                                                            {attempt.githubRepositoryLink && (
                                                                                <DropdownMenuItem 
                                                                                    className="hover:bg-slate-700 transition-colors hover:text-white"
                                                                                    onClick={() => window.open(attempt.githubRepositoryLink, '_blank')}
                                                                                >
                                                                                    View Repository on GitHub
                                                                                </DropdownMenuItem>
                                                                            )}
                                                                        </DropdownMenuGroup>
                                                                    </DropdownMenuContent>
                                                                </DropdownMenu>
                                                            </div>
                                                        </div>
                                                    ))
                                                ) : (
                                                    <div className="bg-gray-800 rounded-lg p-8 text-center">
                                                        <p className="text-gray-400">No assessment history found for this candidate.</p>
                                                    </div>
                                                )}
                                            </div>

                                            {totalAttemptsPages > 1 && (
                                                <div className="flex items-center justify-between mt-6">
                                                    <p className="text-sm text-gray-400">
                                                        Showing {((currentAttemptsPage - 1) * attemptsPerPage) + 1} to{' '}
                                                        {Math.min(currentAttemptsPage * attemptsPerPage, totalAttempts)} of{' '}
                                                        {totalAttempts} assessments
                                                    </p>
                                                    <div className="flex items-center gap-2">
                                                        <button
                                                            onClick={() => setCurrentAttemptsPage(prev => Math.max(prev - 1, 1))}
                                                            disabled={currentAttemptsPage === 1}
                                                            className="p-2 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                                        >
                                                            ←
                                                        </button>
                                                        <span className="text-sm text-gray-300">
                                                            {currentAttemptsPage} of {totalAttemptsPages}
                                                        </span>
                                                        <button
                                                            onClick={() => setCurrentAttemptsPage(prev => Math.min(prev + 1, totalAttemptsPages))}
                                                            disabled={currentAttemptsPage === totalAttemptsPages}
                                                            className="p-2 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                                        >
                                                            →
                                                        </button>
                                                    </div>
                                                </div>
                                            )}
                                        </>
                                    )}
                                </div>
                            </div>

                            {/* Save Changes Section */}
                            <div className="flex items-center justify-between mt-8 pt-6 border-t border-gray-700">
                                <div className="flex items-center gap-2">
                                    {hasUnsavedChanges && (
                                        <div className="flex items-center gap-2 text-yellow-400 text-sm">
                                            <span>•</span>
                                            <span>You have unsaved changes</span>
                                        </div>
                                    )}
                                </div>
                                <div className="flex items-center gap-3">
                                    {updateCandidateMutation.isPending && (
                                        <div className="flex items-center space-x-2 text-gray-400">
                                            <Loader2 className="h-4 w-4 animate-spin" />
                                            <span>Saving changes...</span>
                                        </div>
                                    )}
                                    <Button
                                        variant="outline"
                                        onClick={clearPendingChanges}
                                        disabled={!hasUnsavedChanges || updateCandidateMutation.isPending}
                                        className="text-gray-400 hover:text-gray-300 border-gray-600 hover:border-gray-500"
                                    >
                                        Cancel
                                    </Button>
                                    <Button
                                        onClick={saveAllChanges}
                                        disabled={!hasUnsavedChanges || updateCandidateMutation.isPending}
                                        className="bg-blue-600 hover:bg-blue-700 text-white"
                                    >
                                        Save Changes
                                    </Button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="lg:col-span-1">
                        <div className="h-full">
                            {/* Quick Actions Sidebar */}
                            <div className="bg-gray-800 rounded-lg p-6 shadow-xl">
                                <h3 className="text-lg font-semibold mb-4">Quick Actions</h3>
                                <div className="space-y-3">
                                    <Button
                                        onClick={() => setIsEmailDialogOpen(true)}
                                        className="w-full flex items-center gap-2 bg-slate-700 hover:bg-slate-600"
                                    >
                                        <Mail size={16} />
                                        Send Email
                                    </Button>
                                    <Button
                                        onClick={() => navigate('/assessments')}
                                        className="w-full flex items-center gap-2 bg-slate-700 hover:bg-slate-600"
                                    >
                                        <Plus size={16} />
                                        Assign to Assessment
                                    </Button>
                                    <Button
                                        variant="outline"
                                        onClick={() => navigate('/candidates')}
                                        className="w-full flex items-center gap-2 border-gray-600 text-gray-300 hover:bg-gray-700"
                                    >
                                        <ArrowLeft size={16} />
                                        Back to Candidates
                                    </Button>
                                </div>

                                {/* Candidate Stats */}
                                {/* TODO: Add stats API endpoint w/ designated SQL query */}
                                <div className="mt-6 pt-6 border-t border-gray-700">
                                    <h4 className="text-md font-semibold mb-3">Statistics</h4>
                                    <div className="space-y-2 text-sm">
                                        <div className="flex justify-between">
                                            <span className="text-gray-400">Total Assessments:</span>
                                            <span className="text-white">{totalAttempts}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="text-gray-400">Evaluated:</span>
                                            <span className="text-green-400">
                                                {candidateAttempts.filter(a => a.status === 'evaluated').length}
                                            </span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="text-gray-400">Completed:</span>
                                            <span className="text-green-400">
                                                {candidateAttempts.filter(a => a.status === 'completed' || a.status === 'evaluated').length}
                                            </span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="text-gray-400">In Progress:</span>
                                            <span className="text-yellow-400">
                                                {candidateAttempts.filter(a => a.status === 'started').length}
                                            </span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="text-gray-400">Invited:</span>
                                            <span className="text-blue-400">
                                                {candidateAttempts.filter(a => a.status === 'invited').length}
                                            </span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="text-gray-400">Expired:</span>
                                            <span className="text-red-400">
                                                {candidateAttempts.filter(a => a.status === 'expired').length}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Email Dialog */}
            <Dialog open={isEmailDialogOpen} onOpenChange={setIsEmailDialogOpen}>
                <DialogContent className="sm:max-w-[600px] bg-slate-800 text-white border-slate-500">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                            <Mail size={20} />
                            Send Email to {getCurrentName() || getCurrentEmail()}
                        </DialogTitle>
                        <DialogDescription className="text-gray-300">
                            Send an email to the candidate.
                        </DialogDescription>
                    </DialogHeader>
                    
                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <label htmlFor="email-subject" className="text-sm font-medium text-gray-300">
                                Subject
                            </label>
                            <Input
                                id="email-subject"
                                value={emailSubject}
                                onChange={(e) => setEmailSubject(e.target.value)}
                                placeholder="Enter email subject..."
                                className="bg-gray-700 text-white border-gray-600 focus:border-blue-400"
                            />
                        </div>
                        
                        <div className="space-y-2">
                            <label htmlFor="email-message" className="text-sm font-medium text-gray-300">
                                Message
                            </label>
                            <Textarea
                                id="email-message"
                                value={emailMessage}
                                onChange={(e) => setEmailMessage(e.target.value)}
                                placeholder="Enter your message..."
                                className="bg-gray-700 text-white border-gray-600 focus:border-blue-400 min-h-[200px] resize-none"
                            />
                        </div>
                    </div>
                    
                    <DialogFooter>
                        <Button
                            variant="secondary"
                            onClick={() => {
                                setIsEmailDialogOpen(false);
                                setEmailSubject('');
                                setEmailMessage('');
                            }}
                            disabled={sendEmailMutation.isPending}
                        >
                            Cancel
                        </Button>
                        <Button
                            onClick={() => {
                                if (candidate?.id && emailSubject.trim() && emailMessage.trim()) {
                                    sendEmailMutation.mutate({
                                        candidateId: candidate.id,
                                        subject: emailSubject.trim(),
                                        text: emailMessage.trim()
                                    });
                                }
                            }}
                            disabled={!emailSubject.trim() || !emailMessage.trim() || sendEmailMutation.isPending}
                            className="bg-blue-600 hover:bg-blue-700 text-white"
                        >
                            {sendEmailMutation.isPending ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Sending...
                                </>
                            ) : (
                                <>
                                    <Mail size={16} className="mr-2" />
                                    Send Email
                                </>
                            )}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}