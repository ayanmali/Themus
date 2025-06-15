import { useState } from "react";
import { ArrowLeft, Calendar, Clock, ExternalLink, Link2, MoreHorizontal, Pause, Play, Plus, Trash2, X, Edit3, Check, ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { AppShell } from "@/components/layout/app-shell";
import { Assessment, Candidate } from "@/lib/types/assessment";
import { Link } from "wouter";
import AssessmentPagination from "@/components/ui/AssessmentPagination";

export default function EmployerAssessments() {
    const [selectedAssessment, setSelectedAssessment] = useState<Assessment | null>(null);
    const [activeDropdown, setActiveDropdown] = useState<string | null>(null);
    const [editedAssessment, setEditedAssessment] = useState<Assessment | null>(null);
    const [newMetadataKey, setNewMetadataKey] = useState('');
    const [newMetadataValue, setNewMetadataValue] = useState('');

    // Editing states
    const [isEditingDescription, setIsEditingDescription] = useState(false);
    const [isEditingName, setIsEditingName] = useState(false);
    const [isEditingRole, setIsEditingRole] = useState(false);
    const [tempDescription, setTempDescription] = useState('');
    const [tempName, setTempName] = useState('');
    const [tempRole, setTempRole] = useState('');

    // Candidates pagination
    const [currentCandidatePage, setCurrentCandidatePage] = useState(1);
    const candidatesPerPage = 5;

    // Add these state variables after your existing useState declarations
    const [candidateSearchTerm, setCandidateSearchTerm] = useState('');
    const [candidateStatusFilter, setCandidateStatusFilter] = useState<string>('all');

    // Sample assessment data
    const assessments: Assessment[] = [
        {
            id: '1',
            role: 'Senior Software Engineer',
            employerId: '123',
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
            employerId: '456',
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
            role: 'Quantitative Development Intern',
            employerId: '789',
            description: 'Quantitative Development Intern assessment focusing on performant backtesting engine design. Candidates will build a backtesting engine in C++ that can backtest a trading strategy on a given dataset.',
            skills: ['C++', 'Backtesting', 'Performance Optimization', 'Algorithmic Trading', 'Data Structures', 'Object-Oriented Programming', 'Testing', 'Documentation'],
            createdAt: new Date('2024-01-25'),
            updatedAt: new Date('2024-01-25'),
            name: 'Quant Development Intern Assessment',
            status: 'active',
            startDate: new Date('2024-03-01'),
            endDate: new Date('2024-03-10'),
            type: 'take-home',
            repoLink: 'https://github.com/company/quant-development-intern-assessment',
            metadata: {
                'Duration': '7 days',
                'Difficulty': 'Junior Level',
                'Focus Areas': 'System Design, API Development'
            }
        }
    ];

    // Sample candidates data
    const candidates: Candidate[] = [
        { id: '1', name: 'Alice Johnson', email: 'alice.johnson@email.com', status: 'evaluated', appliedAt: new Date('2024-02-01') },
        { id: '2', name: 'Bob Smith', email: 'bob.smith@email.com', status: 'submitted', appliedAt: new Date('2024-02-02') },
        { id: '3', name: 'Carol Davis', email: 'carol.davis@email.com', status: 'started', appliedAt: new Date('2024-02-03') },
        { id: '4', name: 'David Wilson', email: 'david.wilson@email.com', status: 'invited', appliedAt: new Date('2024-02-04') },
        { id: '5', name: 'Eva Brown', email: 'eva.brown@email.com', status: 'submitted', appliedAt: new Date('2024-02-05') },
        { id: '6', name: 'Frank Miller', email: 'frank.miller@email.com', status: 'started', appliedAt: new Date('2024-02-06') },
        { id: '7', name: 'Grace Lee', email: 'grace.lee@email.com', status: 'invited', appliedAt: new Date('2024-02-07') },
        { id: '8', name: 'Henry Taylor', email: 'henry.taylor@email.com', status: 'evaluated', appliedAt: new Date('2024-02-08') },
    ];

    // Add this filtering logic before the pagination logic
    const filteredCandidates = candidates.filter(candidate => {
        const matchesSearch = candidate.name.toLowerCase().includes(candidateSearchTerm.toLowerCase()) ||
            candidate.email.toLowerCase().includes(candidateSearchTerm.toLowerCase());
        const matchesStatus = candidateStatusFilter === 'all' || candidate.status === candidateStatusFilter;
        return matchesSearch && matchesStatus;
    });

    // Update the pagination to use filtered candidates
    const totalCandidatePages = Math.ceil(filteredCandidates.length / candidatesPerPage);
    const paginatedCandidates = filteredCandidates.slice(
        (currentCandidatePage - 1) * candidatesPerPage,
        currentCandidatePage * candidatesPerPage
    );

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
            console.log('Saving changes:', editedAssessment);
            setSelectedAssessment(editedAssessment);
        }
    };

    // Editing functions
    const startEditingDescription = () => {
        setIsEditingDescription(true);
        setTempDescription(editedAssessment?.description || '');
    };

    const saveDescription = () => {
        if (editedAssessment) {
            setEditedAssessment({
                ...editedAssessment,
                description: tempDescription
            });
        }
        setIsEditingDescription(false);
    };

    const cancelDescriptionEdit = () => {
        setIsEditingDescription(false);
        setTempDescription('');
    };

    const startEditingName = () => {
        setIsEditingName(true);
        setTempName(editedAssessment?.name || '');
    };

    const saveName = () => {
        if (editedAssessment) {
            setEditedAssessment({
                ...editedAssessment,
                name: tempName
            });
        }
        setIsEditingName(false);
    };

    const cancelNameEdit = () => {
        setIsEditingName(false);
        setTempName('');
    };

    const startEditingRole = () => {
        setIsEditingRole(true);
        setTempRole(editedAssessment?.role || '');
    };

    const saveRole = () => {
        if (editedAssessment) {
            setEditedAssessment({
                ...editedAssessment,
                role: tempRole
            });
        }
        setIsEditingRole(false);
    };

    const cancelRoleEdit = () => {
        setIsEditingRole(false);
        setTempRole('');
    };

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'invited': return 'bg-blue-600';
            case 'started': return 'bg-yellow-600';
            case 'submitted': return 'bg-green-600';
            case 'evaluated': return 'bg-purple-600';
            default: return 'bg-gray-600';
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
                                {/* Editable Assessment Name */}
                                <div className="mb-4">
                                    {isEditingName ? (
                                        <div className="flex items-center gap-2">
                                            <input
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
                                                <X size={20} />
                                            </button>
                                        </div>
                                    ) : (
                                        <div className="flex items-center gap-2">
                                            <h1 className="text-3xl font-bold">{editedAssessment?.name}</h1>
                                            <button
                                                onClick={startEditingName}
                                                className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                            >
                                                <Edit3 size={16} />
                                            </button>
                                        </div>
                                    )}
                                </div>

                                {/* Editable Role */}
                                <div className="flex items-center justify-between mb-4">
                                    {isEditingRole ? (
                                        <div className="flex items-center gap-2">
                                            <input
                                                type="text"
                                                value={tempRole}
                                                onChange={(e) => setTempRole(e.target.value)}
                                                className="text-lg bg-gray-700 text-gray-300 px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none flex-1"
                                                autoFocus
                                            />
                                            <button
                                                onClick={saveRole}
                                                className="p-2 text-green-400 hover:text-green-300 hover:bg-gray-700 rounded transition-colors"
                                            >
                                                <Check size={16} />
                                            </button>
                                            <button
                                                onClick={cancelRoleEdit}
                                                className="p-2 text-red-400 hover:text-red-300 hover:bg-gray-700 rounded transition-colors"
                                            >
                                                <X size={16} />
                                            </button>
                                        </div>
                                    ) : (
                                        <div className="flex items-center gap-2">
                                            <p className="text-gray-300 text-lg">{editedAssessment?.role}</p>
                                            <button
                                                onClick={startEditingRole}
                                                className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                            >
                                                <Edit3 size={14} />
                                            </button>
                                        </div>
                                    )}
                                    <button
                                        onClick={() => openRepository(selectedAssessment.repoLink)}
                                        className="flex items-center gap-2 bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg transition-colors text-sm"
                                    >
                                        <ExternalLink size={16} />
                                        View Template Repository
                                    </button>
                                </div>

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

                                <div className="flex items-center gap-2 my-5">
                                    <Button variant="link" className="flex items-center">
                                        <Link2 size={20} className="text-blue-400" />
                                        <span className="text-blue-400">usedelphi.dev/invite/id</span>
                                    </Button>
                                </div>
                            </div>


                        </div>

                        <div className="mb-8">
                            <h3 className="text-xl font-semibold mb-4">Skills, Technologies, and Focus Areas</h3>
                            <p className="text-gray-300 leading-relaxed">{selectedAssessment.skills.join(', ')}</p>
                        </div>

                        {/* Editable Description */}
                        <div className="mb-8">
                            <div className="flex items-center gap-2 mb-4">
                                <h3 className="text-xl font-semibold">Description</h3>
                                {!isEditingDescription && (
                                    <button
                                        onClick={startEditingDescription}
                                        className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                    >
                                        <Edit3 size={16} />
                                    </button>
                                )}
                            </div>

                            {isEditingDescription ? (
                                <div className="space-y-4">
                                    <textarea
                                        value={tempDescription}
                                        onChange={(e) => setTempDescription(e.target.value)}
                                        className="w-full bg-gray-700 text-white px-4 py-3 rounded border border-gray-500 focus:border-blue-400 focus:outline-none resize-vertical min-h-[100px]"
                                        autoFocus
                                    />
                                    <div className="flex items-center gap-2">
                                        <button
                                            onClick={saveDescription}
                                            className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded-lg transition-colors font-medium"
                                        >
                                            Save
                                        </button>
                                        <button
                                            onClick={cancelDescriptionEdit}
                                            className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-lg transition-colors font-medium"
                                        >
                                            Cancel
                                        </button>
                                    </div>
                                </div>
                            ) : (
                                <p className="text-gray-300 leading-relaxed">{editedAssessment?.description}</p>
                            )}
                        </div>

                        {/* Candidates Section */}
                        <div className="mb-8">
                            <h3 className="text-xl font-semibold mb-6">Candidates</h3>

                            {/* Search Bar and Filters */}
                            <div className="flex flex-col sm:flex-row gap-4 mb-6">
                                {/* Search Bar */}
                                <div className="flex-1">
                                    <input
                                        type="text"
                                        placeholder="Search candidates by name or email..."
                                        value={candidateSearchTerm}
                                        onChange={(e) => setCandidateSearchTerm(e.target.value)}
                                        className="w-full bg-gray-700 text-white px-4 py-2 rounded-lg border border-gray-600 focus:border-blue-400 focus:outline-none placeholder-gray-400"
                                    />
                                </div>

                                {/* Status Filter */}
                                <div className="sm:w-48">
                                    <select
                                        value={candidateStatusFilter}
                                        onChange={(e) => setCandidateStatusFilter(e.target.value)}
                                        className="w-full bg-gray-700 text-white px-4 py-2 rounded-lg border border-gray-600 focus:border-blue-400 focus:outline-none"
                                    >
                                        <option value="all">All Statuses</option>
                                        <option value="invited">Invited</option>
                                        <option value="started">Started</option>
                                        <option value="submitted">Submitted</option>
                                        <option value="evaluated">Evaluated</option>
                                    </select>
                                </div>
                            </div>

                            {/* Results Summary */}
                            <div className="mb-4">
                                <p className="text-sm text-gray-400">
                                    Showing {filteredCandidates.length} of {candidates.length} candidates
                                </p>
                            </div>

                            {/* Candidates List */}
                            <div className="space-y-3">
                                {paginatedCandidates.length > 0 ? (
                                    paginatedCandidates.map((candidate) => (
                                        <div
                                            key={candidate.id}
                                            className="bg-gray-700 rounded-lg p-4 flex items-center justify-between hover:bg-gray-650 transition-colors"
                                        >
                                            <div className="flex-1">
                                                <div className="flex items-center gap-4">
                                                    <div>
                                                        <h4 className="font-medium text-white">{candidate.name}</h4>
                                                        <p className="text-sm text-gray-400">{candidate.email}</p>
                                                    </div>
                                                </div>
                                            </div>
                                            <div className="flex items-center gap-4">
                                                {/* <span className="text-sm text-gray-400">
                                                    Applied: {candidate.appliedAt.toLocaleDateString()}
                                                </span> */}
                                                <span className={`px-3 py-1 rounded-full text-sm font-medium capitalize text-white ${getStatusColor(candidate.status)}`}>
                                                    {candidate.status}
                                                </span>
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
                                                                Send email
                                                            </DropdownMenuItem>
                                                            {(candidate.status.toLowerCase() === "submitted" || candidate.status.toLowerCase() === "evaluated") && (
                                                                <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                                    View Pull Request on GitHub
                                                                </DropdownMenuItem>
                                                            )}
                                                            {candidate.status.toLowerCase() === "started" && (
                                                                <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                                    View Repository on GitHub
                                                                </DropdownMenuItem>
                                                            )}
                                                            <DropdownMenuSeparator className="bg-slate-700" />
                                                            {/* <DropdownMenuItem className="hover:bg-slate-700 text-red-400 transition-colors hover:text-white">
                                                    Delete Assessment
                                                </DropdownMenuItem> */}
                                                        </DropdownMenuGroup>
                                                    </DropdownMenuContent>
                                                </DropdownMenu>
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="bg-gray-700 rounded-lg p-8 text-center">
                                        <p className="text-gray-400">No candidates found matching your search criteria.</p>
                                    </div>
                                )}
                            </div>

                            {/* Candidates Pagination */}
                            {totalCandidatePages > 1 && (
                                <div className="flex items-center justify-between mt-6">
                                    <p className="text-sm text-gray-400">
                                        Showing {((currentCandidatePage - 1) * candidatesPerPage) + 1} to{' '}
                                        {Math.min(currentCandidatePage * candidatesPerPage, filteredCandidates.length)} of{' '}
                                        {filteredCandidates.length} filtered candidates
                                    </p>
                                    <div className="flex items-center gap-2">
                                        <button
                                            onClick={() => setCurrentCandidatePage(prev => Math.max(prev - 1, 1))}
                                            disabled={currentCandidatePage === 1}
                                            className="p-2 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            <ChevronLeft size={16} />
                                        </button>
                                        <span className="text-sm text-gray-300">
                                            {currentCandidatePage} of {totalCandidatePages}
                                        </span>
                                        <button
                                            onClick={() => setCurrentCandidatePage(prev => Math.min(prev + 1, totalCandidatePages))}
                                            disabled={currentCandidatePage === totalCandidatePages}
                                            className="p-2 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            <ChevronRight size={16} />
                                        </button>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className="mb-8">
                            <div className="flex items-center justify-between mb-4">
                                <h3 className="text-xl font-semibold">Metadata</h3>
                            </div>

                            <div className="space-y-4 mb-6">
                                {editedAssessment && Object.entries(editedAssessment?.metadata || {}).map(([key, value]) => (
                                    <div key={key} className="bg-gray-700 rounded-lg p-4 flex items-center gap-4">
                                        <div className="flex-1 grid grid-cols-2 gap-4">
                                            <input
                                                type="text"
                                                value={key}
                                                onChange={(e) => updateMetadataField(key, e.target.value, value)}
                                                className="bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
                                                placeholder="Field name"
                                            />
                                            <input
                                                type="text"
                                                value={value}
                                                onChange={(e) => updateMetadataField(key, key, e.target.value)}
                                                className="bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
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

                        {/* Save button - only show when description is being edited or other changes need saving */}
                        {(isEditingDescription || JSON.stringify(selectedAssessment) !== JSON.stringify(editedAssessment)) && (
                            <div className="flex justify-end">
                                <button
                                    onClick={saveChanges}
                                    className="bg-green-600 hover:bg-green-700 text-white px-6 py-2 rounded-lg transition-colors font-medium"
                                >
                                    Save Changes
                                </button>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        );
    }

    return (
        <AppShell title="Assessments">
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-6xl mx-auto">
                    <div className="flex justify-between items-center mb-6">
                        <h1 className="text-2xl font-medium text-gray-100">Assessments</h1>
                        <Link to="/assessments/new">
                            <Button className="flex items-center gap-2">
                                <Plus size={16} />
                                New Assessment
                            </Button>
                        </Link>
                    </div>

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

            </div>
        </AppShell>
    );
}