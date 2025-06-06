import { useState } from "react";
import { ArrowLeft, Calendar, Clock, ExternalLink, MoreHorizontal, Pause, Play, Plus, Trash2, X } from "lucide-react";
import { AppShell } from "@/components/layout/app-shell";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuPortal, DropdownMenuSeparator, DropdownMenuShortcut, DropdownMenuSub, DropdownMenuSubContent, DropdownMenuSubTrigger, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { toast } from "sonner"
import AssesmentPagination from "@/components/ui/AssessmentPagination";
import AssessmentPagination from "@/components/ui/AssessmentPagination";

interface Assessment {
    id: string;
    role: string;
    employerName: string;
    description: string;
    skills: string[];
    createdAt: Date;
    updatedAt: Date;
    name: string;
    status: 'active' | 'inactive';
    startDate?: Date;
    endDate?: Date;
    duration?: number;
    type: 'take-home' | 'live-coding';
    repoLink: string;
    metadata: Record<string, string>;
}

export default function EmployerAssessments() {
    const [selectedAssessment, setSelectedAssessment] = useState<Assessment | null>(null);
    const [activeDropdown, setActiveDropdown] = useState<string | null>(null);
    const [editedAssessment, setEditedAssessment] = useState<Assessment | null>(null);
    const [newMetadataKey, setNewMetadataKey] = useState('');
    const [newMetadataValue, setNewMetadataValue] = useState('');

    // Sample assessment data
    const assessments: Assessment[] = [
        {
            id: '1',
            role: 'Senior Software Engineer',
            employerName: 'Google',
            description: 'Full-stack development assessment focusing on system design and microservices architecture. Candidates will build a scalable web application with proper testing and documentation.',
            skills: ['React', 'Node.js', 'TypeScript', 'Python', 'SQL', 'Docker', 'Kubernetes'],
            createdAt: new Date('2024-01-15'),
            updatedAt: new Date('2024-02-01'),
            name: 'Backend SWE Microservices Assessment',
            status: 'active',
            startDate: new Date('2024-02-01'),
            endDate: new Date('2024-02-15'),
            type: 'take-home',
            repoLink: 'https://github.com/company/backend-swe-assessment',
            metadata: {
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
            skills: ['Python', 'Pandas', 'Scikit-learn', 'PyTorch', 'NumPy', 'Matplotlib', 'Seaborn'],
            createdAt: new Date('2024-01-20'),
            updatedAt: new Date('2024-01-25'),
            name: 'ML Data Analysis Challenge',
            status: 'inactive',
            startDate: new Date('2024-03-01'),
            endDate: new Date('2024-03-10'),
            type: 'take-home',
            repoLink: 'https://github.com/company/ml-data-assessment',
            metadata: {
                'Dataset Size': '10GB',
                'Expected Output': 'Jupyter Notebook + Report'
            }
        },
        {
            id: '3',
            role: 'Frontend Developer',
            employerName: 'Amazon',
            description: 'Live coding session focusing on React components, state management, and responsive design. Real-time problem solving with immediate feedback.',
            skills: ['React', 'JavaScript', 'HTML', 'CSS', 'TypeScript', 'Component Architecture', 'CSS Grid', 'Responsive Design', 'Real-time Problem Solving'],
            createdAt: new Date('2024-02-10'),
            updatedAt: new Date('2024-02-12'),
            name: 'React Frontend Live Coding',
            status: 'active',
            duration: 90,
            type: 'live-coding',
            repoLink: 'https://github.com/company/frontend-live-coding',
            metadata: {
                'Tools': 'CodeSandbox, VS Code Live Share',
                'Focus': 'Component Architecture, CSS Grid'
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

    const toggleAssessmentStatus = (assessmentId: string) => {
        console.log(`Toggling status for assessment ${assessmentId}`);
        setActiveDropdown(null);
    };

    const openRepository = (repoLink: string) => {
        window.open(repoLink, '_blank');
        setActiveDropdown(null);
    };

    const handleAssessmentSelect = (assessment: Assessment) => {
        setSelectedAssessment(assessment);
        setEditedAssessment({ ...assessment });
    };

    const addMetadataField = () => {
        if (newMetadataKey && newMetadataValue && editedAssessment) {
            setEditedAssessment({
                ...editedAssessment,
                metadata: {
                    ...editedAssessment.metadata,
                    [newMetadataKey]: newMetadataValue
                }
            });
            setNewMetadataKey('');
            setNewMetadataValue('');
        }
    };

    const deleteMetadataField = (key: string) => {
        if (editedAssessment) {
            const newMetadata = { ...editedAssessment.metadata };
            delete newMetadata[key];
            setEditedAssessment({
                ...editedAssessment,
                metadata: newMetadata
            });
        }
    };

    const updateMetadataField = (oldKey: string, newKey: string, newValue: string) => {
        if (editedAssessment) {
            const newMetadata = { ...editedAssessment.metadata };
            if (oldKey !== newKey) {
                delete newMetadata[oldKey];
            }
            newMetadata[newKey] = newValue;
            setEditedAssessment({
                ...editedAssessment,
                metadata: newMetadata
            });
        }
    };

    const saveChanges = () => {
        if (editedAssessment) {
            // In a real app, this would make an API call to save changes
            console.log('Saving changes:', editedAssessment);
            setSelectedAssessment(editedAssessment);
            toast("Assessment has been updated", {
                description: "Assessment has been updated",
                action: {
                  label: "Undo",
                  onClick: () => console.log("Undo"),
                },
              })
        }
    };

    if (selectedAssessment) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-4xl mx-auto">
                    <div className="flex justify-between items-center mb-6">
                        <button
                            onClick={() => setSelectedAssessment(null)}
                            className="flex items-center gap-2 text-blue-400 hover:text-blue-300 mb-6 transition-colors"
                        >
                            <ArrowLeft size={20} />
                            Back to Assessments
                        </button>
                        <button
                            className="flex items-center gap-2 text-gray-200 hover:text-gray-100 mb-6 transition-colors bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg"
                        >
                            Switch to Candidate View
                        </button>
                    </div>

                    <div className="bg-gray-800 rounded-lg p-8 shadow-xl">
                        <div className="flex justify-between items-start mb-6">
                            <div className="flex-1">
                                <h1 className="text-3xl font-bold mb-4">{selectedAssessment.name}</h1>
                                <p className="text-gray-300 text-lg mb-4">{selectedAssessment.role}</p>

                                {/* Status and Type labels */}
                                <div className="flex items-center gap-3 mb-4">
                                    <span className={`px-3 py-1 rounded-full text-sm font-medium capitalize ${selectedAssessment.status === 'active'
                                        ? 'bg-green-600 text-white'
                                        : 'bg-red-600 text-white'
                                        }`}>
                                        {selectedAssessment.status}
                                    </span>
                                    <span className="px-3 py-1 rounded-full text-sm font-medium capitalize bg-blue-600 text-white">
                                        {selectedAssessment.type.replace('-', ' ')}
                                    </span>
                                </div>

                                {/* Created date and duration/date range */}
                                <div className="flex items-center gap-6 text-sm text-gray-400">
                                    <div className="flex items-center gap-2">
                                        <Calendar size={16} />
                                        <span>Created: {selectedAssessment.createdAt.toLocaleDateString()}</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        {selectedAssessment.type === 'live-coding' ? (
                                            <Clock size={16} />
                                        ) : (
                                            <Calendar size={16} />
                                        )}
                                        <span>{formatDateRange(selectedAssessment)}</span>
                                    </div>
                                </div>
                            </div>

                            <button
                                onClick={() => openRepository(selectedAssessment.repoLink)}
                                className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg transition-colors"
                            >
                                <ExternalLink size={16} />
                                View Repository
                            </button>
                        </div>

                        <div className="mb-8">
                            <h3 className="text-xl font-semibold mb-4">Description</h3>
                            <p className="text-gray-300 leading-relaxed">{selectedAssessment.description}</p>
                        </div>

                        <div className="mb-8">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-xl font-semibold">Metadata</h3>
                            </div>

                            <div className="space-y-4 mb-6">
                                {editedAssessment && Object.entries(editedAssessment.metadata).map(([key, value]) => (
                                    <div key={key} className="bg-gray-700 rounded-lg p-4 flex items-center gap-4">
                                        <div className="flex-1 grid grid-cols-2 gap-4">
                                            <input
                                                type="text"
                                                value={key}
                                                onChange={(e) => updateMetadataField(key, e.target.value, value)}
                                                className="bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                                                placeholder="Field name"
                                            />
                                            <input
                                                type="text"
                                                value={value}
                                                onChange={(e) => updateMetadataField(key, key, e.target.value)}
                                                className="bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
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

                            {/* Add new metadata field */}
                            <div className="bg-gray-700 rounded-lg p-4 flex items-center gap-4">
                                <div className="flex-1 grid grid-cols-2 gap-4">
                                    <input
                                        type="text"
                                        value={newMetadataKey}
                                        onChange={(e) => setNewMetadataKey(e.target.value)}
                                        className="bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
                                        placeholder="New field name"
                                    />
                                    <input
                                        type="text"
                                        value={newMetadataValue}
                                        onChange={(e) => setNewMetadataValue(e.target.value)}
                                        className="bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none"
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

                        {/* Save button */}
                        <div className="flex justify-end">
                            <button
                                onClick={saveChanges}
                                className="bg-green-600 hover:bg-green-700 text-white px-6 py-2 rounded-lg transition-colors font-medium"
                            >
                                Save Changes
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <AppShell title="Assessments">
            <div className="min-h-screen bg-slate-700 text-white p-6">
                <div className="max-w-6xl mx-auto">
                    <div className="space-y-4">
                        {assessments.map((assessment) => (
                            <div
                                key={assessment.id}
                                className="bg-gray-800 rounded-lg p-6 hover:bg-gray-750 transition-colors cursor-pointer shadow-lg"
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
                                        <DropdownMenuTrigger asChild className="p-2 hover:bg-slate-700 hover:text-white rounded-lg transition-colors">
                                            <Button variant="ghost"><MoreHorizontal size={20} /></Button>
                                        </DropdownMenuTrigger>
                                        <DropdownMenuContent className="w-56 bg-slate-800 text-white border-slate-500" align="start">
                                            <DropdownMenuLabel>More Actions</DropdownMenuLabel>
                                            <DropdownMenuGroup className="hover:bg-slate-700 transition-colors hover:text-white">
                                                <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                {assessment.status === 'active' ? (
                                                        <>
                                                            Deactivate Assessment
                                                        </>
                                                    ) : (
                                                        <>
                                                            Activate Assessment
                                                        </>
                                                    )}
                                                </DropdownMenuItem>
                                                <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                    View Repository on GitHub
                                                </DropdownMenuItem>
                                                
                                            </DropdownMenuGroup>
                                            
                                        </DropdownMenuContent>
                                    </DropdownMenu>
                                    {/* <div className="relative">
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
                                                    {assessment.status === 'active' ? (
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
                                    </div> */}
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
                <AssessmentPagination/>
            </div>
        </AppShell>
    );
}