import { useState } from "react";
import { Calendar, Clock, MoreHorizontal, Plus, Eye } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { AppShell } from "@/components/layout/app-shell";
import { Assessment } from "@/lib/types/assessment";
import AssessmentPagination from "@/components/ui/AssessmentPagination";
import AssessmentDetails from "./assessment-details/assessment-details";
import { Link } from "wouter";

export default function EmployerAssessments() {
    const [selectedAssessment, setSelectedAssessment] = useState<Assessment | null>(null);
    const [editedAssessment, setEditedAssessment] = useState<Assessment | null>(null);

    // Sample assessment data
    const assessments: Assessment[] = [
        {
            id: 1,
            role: 'Senior Software Engineer',
            employerId: '123',
            description: 'Full-stack development assessment focusing on system design and microservices architecture. Candidates will build a scalable web application with proper testing and documentation.',
            skills: ['React', 'Node.js', 'TypeScript', 'Python', 'SQL', 'Docker', 'Kubernetes'],
            createdAt: new Date('2024-01-15'),
            updatedAt: new Date('2024-02-01'),
            name: 'Backend SWE Microservices Assessment',
            status: 'active',
            startDate: new Date('2025-06-01'),
            endDate: new Date('2025-06-15'),
            type: 'take-home',
            repoLink: 'https://github.com/company/backend-swe-assessment',
            metadata: {
                'Duration': '7 days',
                'Difficulty': 'Senior Level',
                'Focus Areas': 'System Design, API Development'
            }
        },
        {
            id: 2,
            role: 'Data Scientist Intern',
            employerId: '456',
            description: 'Machine learning assessment covering data analysis, model building, and statistical interpretation. Focus on real-world data processing and visualization.',
            skills: ['Python', 'Pandas', 'Scikit-learn', 'PyTorch', 'NumPy', 'Matplotlib', 'Seaborn'],
            createdAt: new Date('2024-01-20'),
            updatedAt: new Date('2024-01-25'),
            name: 'ML Data Analysis Challenge',
            status: 'inactive',
            startDate: new Date('2025-06-07'),
            endDate: new Date('2025-06-14'),
            type: 'take-home',
            repoLink: 'https://github.com/company/ml-data-assessment',
            metadata: {
                'Dataset Size': '10GB',
                'Expected Output': 'Jupyter Notebook + Report'
            }
        },
        {
            id: 3,
            role: 'Quantitative Development Intern',
            employerId: '789',
            description: 'Quantitative Development Intern assessment focusing on performant backtesting engine design. Candidates will build a backtesting engine in C++ that can backtest a trading strategy on a given dataset.',
            skills: ['C++', 'Backtesting', 'Performance Optimization', 'Algorithmic Trading', 'Data Structures', 'Object-Oriented Programming', 'Testing', 'Documentation'],
            createdAt: new Date('2024-01-25'),
            updatedAt: new Date('2024-01-25'),
            name: 'Quant Development Intern Assessment',
            status: 'active',
            startDate: new Date('2025-06-14'),
            endDate: new Date('2025-06-21'),
            type: 'take-home',
            repoLink: 'https://github.com/company/quant-development-intern-assessment',
            metadata: {
                'Duration': '7 days',
                'Difficulty': 'Junior Level',
                'Focus Areas': 'System Design, API Development'
            }
        },
        {
            id: 4,
            role: 'Backend SWE Intern',
            employerId: '789',
            description: 'Backend development Intern assessment focusing on proficiency in the Go programming language and concurrent programming.',
            skills: ['Go', 'Mutexes', 'Goroutines', 'Concurrency', 'Channels', 'Multithreading'],
            createdAt: new Date('2024-01-25'),
            updatedAt: new Date('2024-01-25'),
            name: 'Backend SWE Intern Assessment',
            status: 'active',
            startDate: new Date('2025-06-14'),
            endDate: new Date('2025-06-21'),
            type: 'take-home',
            repoLink: 'https://github.com/company/backend-swe-intern-assessment',
            metadata: {
                'Duration': '7 days',
                'Difficulty': 'Junior Level',
                'Focus Areas': 'System Design, API Development'
            }
        }
    ];

    const formatDateRange = (assessment: Assessment) => {
        if (assessment.type === 'live-coding') {
            const duration = assessment.duration || 60;
            return `Duration: ${duration} minutes`;
        } else {
            const start = assessment?.startDate?.toLocaleDateString();
            const end = assessment?.endDate?.toLocaleDateString();
            return `${start} - ${end}`;
        }
    };

    const handleAssessmentSelect = (assessment: Assessment) => {
        setSelectedAssessment(assessment);
        setEditedAssessment({ ...assessment });
    };

    // const formatTimeSpent = (startedAt: Date | null | undefined) => {
    //     if (!startedAt) return '';
    //     const now = new Date();
    //     const timeDiff = now.getTime() - startedAt.getTime();
    //     const minutes = Math.floor(timeDiff / (1000 * 60));
    //     return `Time spent: ${minutes} minutes`;
    // };

    if (selectedAssessment) {
        return (
            <AssessmentDetails assessment={selectedAssessment} setSelectedAssessment={setSelectedAssessment} editedAssessment={editedAssessment} setEditedAssessment={setEditedAssessment} />
        );
    }

    return (
        <AppShell>
            <div className="max-w-6xl mx-auto text-white">
                {/* <div className="flex justify-between items-center mb-6">
                        <h1 className="text-2xl font-medium text-gray-100">Assessments</h1>
                        <Link to="/assessments/new">
                            <Button className="flex items-center gap-2">
                                <Plus size={16} />
                                New Assessment
                            </Button>
                        </Link>
                    </div> */}
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="serif-heading">Assessments</h1>
                        <p className="text-gray-400 flex items-center space-x-2">
                            {/* <Calendar className="w-4 h-4" /> */}
                            <span>View and manage your assessments</span>
                        </p>
                    </div>
                    <div className="flex space-x-3">
                        <Link href="/assessments/new">
                            <button className="bg-slate-700 hover:bg-blue-700 text-gray-100 text-sm px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                                <Plus className="w-4 h-4" />
                                <span>Create Assessment</span>
                            </button>
                        </Link>
                        <button className="bg-slate-700 hover:bg-blue-700 text-gray-100 text-sm px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                            <Eye className="w-4 h-4" />
                            <span>View All</span>
                        </button>
                    </div>
                </div>

                <div className="space-y-4">
                    {assessments.map((assessment) => (
                        <div
                            key={assessment.id}
                            className="bg-gray-800 border border-slate-700 rounded-lg p-6 hover:bg-gray-750 transition-colors cursor-pointer shadow-lg"
                            onClick={() => handleAssessmentSelect(assessment)}
                        >
                            <div className="flex items-center justify-between">
                                <div className="flex-1">
                                    <div className="mb-3">
                                        <h3 className="text-xl font-semibold text-white mb-2">{assessment.name}</h3>
                                        <div className="flex items-center gap-3 mb-2">
                                            <span className={`px-3 py-1 rounded-full text-sm font-medium capitalize ${assessment.status === 'active'
                                                ? 'bg-green-600 text-white'
                                                : 'bg-red-600 text-white'
                                                }`}>
                                                {assessment.status}
                                            </span>
                                            <span className="px-3 py-1 rounded-full text-sm font-medium capitalize bg-blue-600 text-white">
                                                {assessment.type.replace('-', ' ')}
                                            </span>
                                        </div>
                                    </div>

                                    <p className="text-gray-300 mb-3">
                                        <span className="font-medium">{assessment.role}</span>
                                    </p>

                                    <div className="flex items-center gap-6 text-sm text-gray-400">
                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} />
                                            <span>Created: {assessment.createdAt.toLocaleDateString()}</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            {assessment.type === 'live-coding' ? (
                                                <Clock size={16} />
                                            ) : (
                                                <Calendar size={16} />
                                            )}
                                            <span>{formatDateRange(assessment)}</span>
                                        </div>
                                    </div>
                                </div>

                                <DropdownMenu>
                                    <DropdownMenuTrigger asChild>
                                        <Button variant="ghost" className="p-2 hover:bg-slate-700 hover:text-white rounded-lg transition-colors">
                                            <MoreHorizontal size={20} />
                                        </Button>
                                    </DropdownMenuTrigger>
                                    <DropdownMenuContent className="w-56 bg-slate-800 text-white border-slate-500" align="start">
                                        <DropdownMenuLabel>More Actions</DropdownMenuLabel>
                                        <DropdownMenuGroup>
                                            <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                Copy link
                                            </DropdownMenuItem>
                                            <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                {assessment.status === 'active' ? 'Deactivate Assessment' : 'Activate Assessment'}
                                            </DropdownMenuItem>
                                            <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                View Repository on GitHub
                                            </DropdownMenuItem>
                                            <DropdownMenuSeparator className="bg-slate-700" />
                                            <DropdownMenuItem className="hover:bg-slate-700 text-red-400 transition-colors hover:text-white">
                                                Delete Assessment
                                            </DropdownMenuItem>
                                        </DropdownMenuGroup>
                                    </DropdownMenuContent>
                                </DropdownMenu>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            <AssessmentPagination />
        </AppShell>
    );
}