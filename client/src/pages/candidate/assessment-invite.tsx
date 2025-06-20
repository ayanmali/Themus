import React, { useState } from 'react';
import { Clock, Code, BookOpen, Users, CheckCircle, AlertCircle, Play } from 'lucide-react';
import { Assessment } from '@/lib/types/assessment';
import { minutesToHours } from '@/lib/utils';
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';

// Mock data for the assessment
const assessmentData: Assessment = {
    id: "1",
    employerId: "1",
    createdAt: new Date(),
    updatedAt: new Date(),
    name: "Full Stack Developer Assessment",
    role: "Senior Full Stack Developer",
    type: "take-home",
    duration: 180,
    skills: [
        "React.js/Vue.js",
        "Node.js/Express",
        "Database Design",
        "API Development",
        "Authentication",
        "Testing",
        "Docker",
        "Kubernetes",
        "TypeScript"
    ],
    description: "This comprehensive assessment evaluates your ability to build a complete web application from scratch. You'll be tasked with creating a task management system that demonstrates your proficiency in both frontend and backend development, including user authentication, data persistence, and responsive design.",
    rules: [
        "Complete the assessment within the allocated time frame",
        "You may use any resources, documentation, or tools you normally would",
        "Write clean, maintainable, and well-documented code",
        "Include proper error handling and validation",
        "Provide a README with setup instructions"
    ],
    instructions: [
        "Read through all requirements carefully before starting",
        "Set up your development environment with your chosen stack",
        "Implement features and changes incrementally and test as you go",
        "Commit your code regularly with meaningful messages",
        "Submit your solution by submitting a pull request to your repository"
    ],
    languageOptions: [
        "React + Node.js",
        "Vue.js + Node.js",
        "React + Python (Django/Flask)",
        "Vue.js + Python (Django/Flask)",
    ],
    status: "active",
    repoLink: "https://github.com/user/repo",
};

export default function CandidateAssessmentInvite() {
    const [selectedLanguage, setSelectedLanguage] = useState('');
    const [email, setEmail] = useState('');
    const [isStarting, setIsStarting] = useState(false);

    const handleStart = () => {
        if (!selectedLanguage) {
            alert('Please select a language/framework combination before starting.');
            return;
        }
        setIsStarting(true);
        // Simulate starting the assessment
        setTimeout(() => {
            alert(`Starting assessment with ${selectedLanguage}...`);
            setIsStarting(false);
        }, 1000);
    };

    return (
        <div className="min-h-screen bg-gray-900 text-gray-100">
            {/* Header */}
            <div className="bg-gray-800 border-b border-gray-700">
                <div className="max-w-7xl mx-auto py-4">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-2xl font-bold text-white">{assessmentData.name}</h1>
                            <p className="text-gray-400 mt-1">Position: {assessmentData.role}</p>
                        </div>
                        <div className="flex items-center space-x-4">
                            <div className="flex items-center space-x-2 bg-blue-900/30 px-3 py-1 rounded-full">
                                {assessmentData.type === "take-home" ? (
                                    <BookOpen className="h-4 w-4 text-blue-400" />
                                ) : (
                                    <Code className="h-4 w-4 text-green-400" />
                                )}
                                <span className="text-sm font-medium text-blue-300">{assessmentData.type === "take-home" ? "Take-Home Assessment" : "Live Coding Assessment"}</span>
                            </div>
                            <div className="flex items-center space-x-2 text-gray-400">
                                <Clock className="h-4 w-4" />
                                <span className="text-sm">
                                    {assessmentData.type === "take-home" ? "Estimated Duration: " : "Duration: "}
                                    {minutesToHours(assessmentData.duration ?? 0)} hours
                                </span>
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
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <div className="flex items-center space-x-2 mb-4">
                                <Users className="h-5 w-5 text-purple-400" />
                                <h2 className="text-xl font-semibold text-white">Skills & Competencies</h2>
                            </div>
                            <div className={`grid gap-3 ${assessmentData.skills.length > 8 ? "grid-cols-3" : "grid-cols-2"}`}>
                                {assessmentData.skills.map((skill, index) => (
                                    <div key={index} className="flex items-center space-x-2 bg-gray-700/50 rounded-lg p-3">
                                        <CheckCircle className="h-4 w-4 text-green-400 flex-shrink-0" />
                                        <span className="text-gray-300 text-sm">{skill}</span>
                                    </div>
                                ))}
                            </div>
                        </div>

                        {/* Description */}
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <h2 className="text-xl font-semibold text-white mb-4">Assessment Description</h2>
                            <p className="text-gray-300 leading-relaxed">{assessmentData.description}</p>
                        </div>

                        {/* Rules */}
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <div className="flex items-center space-x-2 mb-4">
                                <AlertCircle className="h-5 w-5 text-yellow-400" />
                                <h2 className="text-xl font-semibold text-white">Rules & Guidelines</h2>
                            </div>
                            <ul className="space-y-3">
                                {assessmentData.rules?.map((rule, index) => (
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
                                {assessmentData.instructions?.map((instruction, index) => (
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
                    <div className="space-y-6">
                        {/* Language Selection */}
                        {assessmentData.languageOptions?.length && (
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
                                    {assessmentData.languageOptions?.map((option) => (
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
                                                {assessmentData.languageOptions?.map((option) => (
                                                    <SelectItem key={option.replace(" ", "-").toLowerCase()} value={option}>
                                                        {option}
                                                    </SelectItem>
                                                ))}
                                            </SelectGroup>
                                        </SelectContent>
                                    </Select>
                                </div>
                            </div>)}

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
                                    {assessmentData.languageOptions?.map((option) => (
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

                        {/* Start Button */}
                        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <button
                                onClick={handleStart}
                                disabled={(!selectedLanguage && !!assessmentData.languageOptions?.length) || isStarting}
                                className="w-full bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 disabled:from-gray-600 disabled:to-gray-600 disabled:cursor-not-allowed text-white font-semibold py-3 px-6 rounded-lg transition-all duration-200 flex items-center justify-center space-x-2"
                            >
                                {isStarting ? (
                                    <>
                                        <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                                        <span>Starting...</span>
                                    </>
                                ) : (
                                    <>
                                        <Play className="h-5 w-5" />
                                        <span>Start Assessment</span>
                                    </>
                                )}
                            </button>
                            {!selectedLanguage && !!assessmentData.languageOptions?.length && (
                                <p className="text-sm text-gray-400 mt-2 text-center">
                                    Please select a language combination first
                                </p>
                            )}
                        </div>

                        {/* Quick Info */}
                        {/* <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                            <h3 className="text-lg font-semibold text-white mb-4">Quick Info</h3>
                            <div className="space-y-3">
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Type:</span>
                                    <span className="text-gray-300">{assessmentData.type}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Duration:</span>
                                    <span className="text-gray-300">{assessmentData.duration}</span>
                                </div>
                                <div className="flex justify-between">
                                    <span className="text-gray-400">Skills:</span>
                                    <span className="text-gray-300">{assessmentData.skills.length} areas</span>
                                </div>
                            </div>
                        </div> */}
                    </div>
                </div>
            </div>
        </div>
    );
}