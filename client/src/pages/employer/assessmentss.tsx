import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "wouter";
import { AppShell } from "@/components/layout/app-shell";
import { Button } from "@/components/ui/button";
import { ArrowLeft, Calendar, Clock, ExternalLink, MoreHorizontal, Pause, Play, Plus, Search, Users } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { AssessmentCard } from "@/components/assessment-card";
import { Skeleton } from "@/components/ui/skeleton";
import { Assessment } from "@/lib/types/assessment";

export default function EmployerAssessments() {
    const [selectedAssessment, setSelectedAssessment] = useState<Assessment | null>(null);
    const [activeDropdown, setActiveDropdown] = useState<string | null>(null);

    // Sample assessment data
    const assessments: Assessment[] = [
        {
            id: '1',
            role: 'Senior Software Engineer',
            employerName: 'Google',
            description: 'Full-stack development assessment focusing on system design and microservices architecture. Candidates will build a scalable web application with proper testing and documentation.',
            createdAt: new Date('2024-01-15'),
            updatedAt: new Date('2024-02-01'),
            assessmentName: 'Backend SWE Microservices Assessment',
            assessmentStatus: 'active',
            startDate: new Date('2024-02-01'),
            endDate: new Date('2024-02-15'),
            assessmentType: 'take-home',
            repoLink: 'https://github.com/company/backend-swe-assessment',
            metadata: {
                'Tech Stack': 'Node.js, React, PostgreSQL',
                'Duration': '7 days',
                'Difficulty': 'Senior Level',
                'Focus Areas': 'System Design, API Development'
            }
        },
        {
            id: '2',
            role: 'Data Scientist Intern',
            employerName: 'Meta',
            description: 'Machine learning assessment covering data analysis, model building, and statistical interpretation. Focus on real-world data processing and visualization.',
            createdAt: new Date('2024-01-20'),
            updatedAt: new Date('2024-01-25'),
            assessmentName: 'ML Data Analysis Challenge',
            assessmentStatus: 'inactive',
            startDate: new Date('2024-03-01'),
            endDate: new Date('2024-03-10'),
            assessmentType: 'take-home',
            repoLink: 'https://github.com/company/ml-data-assessment',
            metadata: {
                'Tech Stack': 'Python, Pandas, Scikit-learn',
                'Dataset Size': '10GB',
                'Expected Output': 'Jupyter Notebook + Report'
            }
        },
        {
            id: '3',
            role: 'Frontend Developer',
            employerName: 'Amazon',
            description: 'Live coding session focusing on React components, state management, and responsive design. Real-time problem solving with immediate feedback.',
            createdAt: new Date('2024-02-10'),
            updatedAt: new Date('2024-02-12'),
            assessmentName: 'React Frontend Live Coding',
            assessmentStatus: 'active',
            startDate: new Date('2024-02-20'),
            endDate: new Date('2024-02-20'),
            assessmentType: 'live-coding',
            repoLink: 'https://github.com/company/frontend-live-coding',
            metadata: {
                'Duration': '90 minutes',
                'Tools': 'CodeSandbox, VS Code Live Share',
                'Focus': 'Component Architecture, CSS Grid'
            }
        }
    ];

    const formatDateRange = (assessment: Assessment) => {
        if (assessment.assessmentType === 'live-coding') {
            const duration = assessment.duration || '60 minutes';
            return `Duration: ${duration}`;
        } else {
            const start = assessment?.startDate?.toLocaleDateString();
            const end = assessment?.endDate?.toLocaleDateString();
            return `${start} - ${end}`;
        }
    };

    const toggleAssessmentStatus = (assessmentId: string) => {
        // In a real app, this would make an API call
        console.log(`Toggling status for assessment ${assessmentId}`);
        setActiveDropdown(null);
    };

    const openRepository = (repoLink: string) => {
        window.open(repoLink, '_blank');
        setActiveDropdown(null);
    };

    const getRandomStats = () => ({
        candidatesStarted: Math.floor(Math.random() * 50) + 1,
        pullRequests: Math.floor(Math.random() * 25) + 1
    });

    if (selectedAssessment) {
        const stats = getRandomStats();
        return (
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-4xl mx-auto">
                    <button
                        onClick={() => setSelectedAssessment(null)}
                        className="flex items-center gap-2 text-blue-400 hover:text-blue-300 mb-6 transition-colors"
                    >
                        <ArrowLeft size={20} />
                        Back to Assessments
                    </button>

                    <div className="bg-gray-800 rounded-lg p-8 shadow-xl">
                        <div className="flex justify-between items-start mb-6">
                            <div>
                                <h1 className="text-3xl font-bold mb-2">{selectedAssessment.assessmentName}</h1>
                                <p className="text-gray-300 text-lg">{selectedAssessment.role} at {selectedAssessment.employerName}</p>
                            </div>
                            <div className="flex items-center gap-4">
                                <span className={`px-3 py-1 rounded-full text-sm font-medium ${selectedAssessment.assessmentStatus === 'active'
                                    ? 'bg-green-600 text-white'
                                    : 'bg-red-600 text-white'
                                    }`}>
                                    {selectedAssessment.assessmentStatus}
                                </span>
                                <button
                                    onClick={() => openRepository(selectedAssessment.repoLink)}
                                    className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg transition-colors"
                                >
                                    <ExternalLink size={16} />
                                    View Repository
                                </button>
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
                            <div className="bg-gray-700 rounded-lg p-6">
                                <div className="flex items-center gap-2 mb-4">
                                    <Calendar className="text-blue-400" size={20} />
                                    <h3 className="text-lg font-semibold">Timeline</h3>
                                </div>
                                <p className="text-gray-300 mb-2">
                                    <span className="font-medium">Assessment Period:</span> {formatDateRange(selectedAssessment)}
                                </p>
                                <p className="text-gray-300 mb-2">
                                    <span className="font-medium">Created:</span> {selectedAssessment.createdAt.toLocaleDateString()}
                                </p>
                                <p className="text-gray-300">
                                    <span className="font-medium">Type:</span> {selectedAssessment.assessmentType.replace('-', ' ')}
                                </p>
                            </div>

                            <div className="bg-gray-700 rounded-lg p-6">
                                <div className="flex items-center gap-2 mb-4">
                                    <Users className="text-green-400" size={20} />
                                    <h3 className="text-lg font-semibold">Statistics</h3>
                                </div>
                                <p className="text-gray-300 mb-2">
                                    <span className="font-medium">Candidates Started:</span> {stats.candidatesStarted}
                                </p>
                                <p className="text-gray-300">
                                    <span className="font-medium">Pull Requests:</span> {stats.pullRequests}
                                </p>
                            </div>
                        </div>

                        <div className="mb-8">
                            <h3 className="text-xl font-semibold mb-4">Description</h3>
                            <p className="text-gray-300 leading-relaxed">{selectedAssessment.description}</p>
                        </div>

                        <div>
                            <h3 className="text-xl font-semibold mb-4">Metadata</h3>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                {Object.entries(selectedAssessment.metadata).map(([key, value]) => (
                                    <div key={key} className="bg-gray-700 rounded-lg p-4">
                                        <p className="text-sm text-gray-400 mb-1">{key}</p>
                                        <p className="text-white font-medium">{value}</p>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <AppShell title="Assessments">
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-6xl mx-auto">
                    {/* <div className="mb-8">
                        <h1 className="text-3xl font-bold mb-2">Assessment Management</h1>
                        <p className="text-gray-400">Manage and monitor your technical assessments</p>
                    </div> */}

                    <div className="space-y-4">
                        {assessments.map((assessment) => (
                            <div
                                key={assessment.id}
                                className="bg-gray-800 rounded-lg p-6 hover:bg-gray-750 transition-colors cursor-pointer shadow-lg"
                                onClick={() => setSelectedAssessment(assessment)}
                            >
                                <div className="flex items-center justify-between">
                                    <div className="flex-1">
                                        <div className="flex items-center gap-4 mb-3">
                                            <h3 className="text-xl font-semibold text-white">{assessment.assessmentName}</h3>
                                            <span className={`px-3 py-1 rounded-full text-sm font-medium ${assessment.assessmentStatus === 'active'
                                                ? 'bg-green-600 text-white'
                                                : 'bg-red-600 text-white'
                                                }`}>
                                                {assessment.assessmentStatus}
                                            </span>
                                        </div>

                                        <p className="text-gray-300 mb-2">
                                            <span className="font-medium">{assessment.role}</span> at {assessment.employerName}
                                        </p>

                                        <div className="flex items-center gap-4 text-sm text-gray-400">
                                            <div className="flex items-center gap-1">
                                                {assessment.assessmentType === 'live-coding' ? (
                                                    <Clock size={16} />
                                                ) : (
                                                    <Calendar size={16} />
                                                )}
                                                <span>{formatDateRange(assessment)}</span>
                                            </div>
                                            <div className="flex items-center gap-1">
                                                <span className="w-2 h-2 bg-blue-400 rounded-full"></span>
                                                <span className="capitalize">{assessment.assessmentType.replace('-', ' ')}</span>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="relative">
                                        <button
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                setActiveDropdown(activeDropdown === assessment.id ? null : assessment.id);
                                            }}
                                            className="p-2 hover:bg-gray-700 rounded-lg transition-colors"
                                        >
                                            <MoreHorizontal size={20} />
                                        </button>

                                        {activeDropdown === assessment.id && (
                                            <div className="absolute right-0 top-12 bg-gray-700 rounded-lg shadow-xl py-2 w-48 z-10">
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        toggleAssessmentStatus(assessment.id);
                                                    }}
                                                    className="w-full px-4 py-2 text-left hover:bg-gray-600 flex items-center gap-2 text-sm"
                                                >
                                                    {assessment.assessmentStatus === 'active' ? (
                                                        <>
                                                            <Pause size={16} />
                                                            Deactivate Assessment
                                                        </>
                                                    ) : (
                                                        <>
                                                            <Play size={16} />
                                                            Activate Assessment
                                                        </>
                                                    )}
                                                </button>
                                                <button
                                                    onClick={(e) => {
                                                        e.stopPropagation();
                                                        openRepository(assessment.repoLink);
                                                    }}
                                                    className="w-full px-4 py-2 text-left hover:bg-gray-600 flex items-center gap-2 text-sm"
                                                >
                                                    <ExternalLink size={16} />
                                                    Open Repository
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Click outside to close dropdown */}
                    {activeDropdown && (
                        <div
                            className="fixed inset-0 z-5"
                            onClick={() => setActiveDropdown(null)}
                        />
                    )}
                </div>
            </div>
        </AppShell>
    );
}