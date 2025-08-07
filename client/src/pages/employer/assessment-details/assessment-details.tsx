import { Assessment } from "@/lib/types/assessment";
import { ArrowLeft, Calendar, Check, ChevronLeft, ChevronRight, Clock, Command, Edit3, ExternalLink, Link2, MoreHorizontal, Plus, Trash2, X, Loader2 } from "lucide-react"
import { useState } from "react";
import { CandidateAttempt } from "@/lib/types/candidate-attempt";
import { Candidate } from "@/lib/types/candidate";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { ChatMessageListExample } from "@/pages/employer/assessment-details/chat-msg-list";
import { useAuth } from "@/contexts/AuthContext";
import { useRoute, useLocation } from "wouter";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import useApi from "@/hooks/use-api";
import { useToast } from "@/hooks/use-toast";

export default function AssessmentDetails() {
    const { apiCall } = useApi();
    const { toast } = useToast();
    const queryClient = useQueryClient();
    const [, navigate] = useLocation();
    const [, params] = useRoute("/assessments/:assessmentId");
    const assessmentId = params?.assessmentId;

    // State for editing
    const [isEditingDescription, setIsEditingDescription] = useState(false);
    const [isEditingName, setIsEditingName] = useState(false);
    const [isEditingRole, setIsEditingRole] = useState(false);
    const [tempDescription, setTempDescription] = useState('');
    const [tempName, setTempName] = useState('');
    const [tempRole, setTempRole] = useState('');

    // State for metadata editing
    const [newMetadataKey, setNewMetadataKey] = useState('');
    const [newMetadataValue, setNewMetadataValue] = useState('');

    // Candidates pagination and filtering
    const [currentCandidatePage, setCurrentCandidatePage] = useState(1);
    const [candidateSearchTerm, setCandidateSearchTerm] = useState('');
    const [candidateStatusFilter, setCandidateStatusFilter] = useState<string>('all');
    const candidatesPerPage = 5;

    // Command dialog state
    const [isCommandOpen, setIsCommandOpen] = useState(false);
    const [selectedCandidateIds, setSelectedCandidateIds] = useState<string[]>([]);

    // Fetch assessment data
    const { data: assessment, isLoading: assessmentLoading, error: assessmentError } = useQuery({
        queryKey: ['assessment', assessmentId],
        queryFn: async (): Promise<Assessment> => {
            const response = await apiCall(`/api/assessments/${assessmentId}`, {
                method: 'GET',
            });
            
            if (!response) {
                throw new Error('Failed to fetch assessment');
            }
            
            return response;
        },
        enabled: !!assessmentId,
    });

    // Update assessment mutation
    const updateAssessmentMutation = useMutation({
        mutationFn: async (updatedAssessment: Partial<Assessment>) => {
            const response = await apiCall(`/api/assessments/${assessmentId}`, {
                method: 'PUT',
                body: JSON.stringify(updatedAssessment),
            });
            
            if (!response) {
                throw new Error('Failed to update assessment');
            }
            
            return response;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['assessment', assessmentId] });
            queryClient.invalidateQueries({ queryKey: ['assessments', 'user'] });
            toast({
                title: "Success",
                description: "Assessment updated successfully",
            });
        },
        onError: (error: any) => {
            toast({
                title: "Error",
                description: error.message || "Failed to update assessment",
                variant: "destructive",
            });
        },
    });

    // Fetch candidate attempts for this assessment
    const { data: attemptsData, isLoading: attemptsLoading, error: attemptsError } = useQuery({
        queryKey: ['candidateAttempts', 'assessment', assessmentId, currentCandidatePage, candidateStatusFilter],
        queryFn: async () => {
            const statusParam = candidateStatusFilter !== 'all' ? `&status=${candidateStatusFilter.toUpperCase()}` : '';
            const response = await apiCall(`/api/attempts/filter?assessmentId=${assessmentId}&page=${currentCandidatePage - 1}&size=${candidatesPerPage}${statusParam}`, {
                method: 'GET',
            });
            
            if (!response) {
                throw new Error('Failed to fetch candidate attempts');
            }
            
            return response;
        },
        enabled: !!assessmentId,
    });

    // Extract attempts from the response
    const candidateAttempts = attemptsData?.content || [];
    const totalAttempts = attemptsData?.totalElements || 0;
    const totalAttemptsPages = attemptsData?.totalPages || 0;

    // Filter candidate attempts based on search term
    const filteredAttempts = candidateAttempts.filter((attempt: any) => {
        const candidateName = attempt.candidate?.fullName || `${attempt.candidate?.firstName || ''} ${attempt.candidate?.lastName || ''}`.trim();
        const candidateEmail = attempt.candidate?.email;
        const matchesSearch = candidateSearchTerm === '' || 
            candidateName?.toLowerCase().includes(candidateSearchTerm.toLowerCase()) ||
            candidateEmail?.toLowerCase().includes(candidateSearchTerm.toLowerCase());
        return matchesSearch;
    });

    const formatDateRange = (assessment: Assessment) => {
        if (assessment.duration) {
            return `Duration: ${assessment.duration} minutes`;
        } else {
            const start = assessment?.startDate?.toLocaleDateString();
            const end = assessment?.endDate?.toLocaleDateString();
            return `${start} - ${end}`;
        }
    };

    const openRepository = (repoLink: string) => {
        window.open(repoLink, '_blank');
    };

    // Metadata functions
    const addMetadataField = () => {
        if (newMetadataKey && newMetadataValue && assessment) {
            const updatedMetadata = {
                ...assessment.metadata,
                [newMetadataKey]: newMetadataValue
            };
            
            updateAssessmentMutation.mutate({
                metadata: updatedMetadata
            });
            
            setNewMetadataKey('');
            setNewMetadataValue('');
        }
    };

    const deleteMetadataField = (key: string) => {
        if (assessment) {
            const newMetadata = { ...assessment.metadata };
            delete newMetadata[key];
            
            updateAssessmentMutation.mutate({
                metadata: newMetadata
            });
        }
    };

    const updateMetadataField = (oldKey: string, newKey: string, newValue: string) => {
        if (assessment) {
            const newMetadata = { ...assessment.metadata };
            if (oldKey !== newKey) {
                delete newMetadata[oldKey];
            }
            newMetadata[newKey] = newValue;
            
            updateAssessmentMutation.mutate({
                metadata: newMetadata
            });
        }
    };

    // Editing functions
    const startEditingDescription = () => {
        setIsEditingDescription(true);
        setTempDescription(assessment?.description || '');
    };

    const saveDescription = () => {
        if (assessment) {
            updateAssessmentMutation.mutate({
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
        setTempName(assessment?.name || '');
    };

    const saveName = () => {
        if (assessment) {
            updateAssessmentMutation.mutate({
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
        setTempRole(assessment?.role || '');
    };

    const saveRole = () => {
        if (assessment) {
            updateAssessmentMutation.mutate({
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

    // Loading state
    if (assessmentLoading) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-7xl mx-auto">
                    <div className="flex items-center justify-center py-12">
                        <div className="flex items-center space-x-2">
                            <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
                            <span className="text-gray-400">Loading assessment...</span>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // Error state
    if (assessmentError || !assessment) {
        return (
            <div className="min-h-screen bg-gray-900 text-white p-6">
                <div className="max-w-7xl mx-auto">
                    <div className="bg-red-50 border border-red-200 rounded-md p-4">
                        <div className="flex">
                            <div className="ml-3">
                                <h3 className="text-sm font-medium text-red-800">
                                    Error loading assessment
                                </h3>
                                <div className="mt-2 text-sm text-red-700">
                                    <p>{assessmentError?.message || "Assessment not found"}</p>
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
                        onClick={() => navigate("/assessments")}
                        className="flex items-center gap-2 text-blue-400 hover:text-blue-300 mb-6 transition-colors"
                    >
                        <ArrowLeft size={20} />
                        Back to Assessments
                    </button>
                    <button
                        className="flex items-center gap-2 text-gray-200 hover:text-gray-100 mb-6 transition-colors bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg"
                    >
                        Switch to Candidate View
                    </button>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[calc(100vh-8rem)]">
                    <div className="lg:col-span-2 overflow-y-auto">
                        <div className="bg-gray-800 rounded-lg p-8 shadow-xl">
                            <div className="flex justify-between items-start mb-6">
                                <div className="flex-1">
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
                                                    <X size={16} />
                                                </button>
                                            </div>
                                        ) : (
                                            <div className="flex items-center gap-2">
                                                <h1 className="text-3xl font-bold">{assessment.name}</h1>
                                                <button
                                                    onClick={startEditingName}
                                                    className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                                >
                                                    <Edit3 size={16} />
                                                </button>
                                            </div>
                                        )}
                                    </div>

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
                                                <p className="text-gray-300 text-lg">{assessment.role}</p>
                                                <button
                                                    onClick={startEditingRole}
                                                    className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                                >
                                                    <Edit3 size={14} />
                                                </button>
                                            </div>
                                        )}
                                        <button
                                            onClick={() => openRepository(assessment.repoLink)}
                                            className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg transition-colors text-sm"
                                        >
                                            <ExternalLink size={16} />
                                            View Template Repository
                                        </button>

                                        <button
                                            onClick={() => openRepository(assessment.repoLink)}
                                            className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg transition-colors text-sm"
                                        >
                                            <ExternalLink size={16} />
                                            View analytics
                                        </button>
                                    </div>

                                    <div className="flex items-center gap-3 mb-4">
                                        <span className={`px-3 py-1 rounded-full text-sm font-medium capitalize ${assessment.status === 'active'
                                            ? 'bg-green-600 text-white'
                                            : 'bg-red-600 text-white'
                                            }`}>
                                            {assessment.status}
                                        </span>
                                    </div>

                                    <div className="flex items-center gap-6 text-sm text-gray-400">
                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} />
                                            <span>Created: {assessment.createdAt.toLocaleDateString()}</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} />
                                            <span>{formatDateRange(assessment)}</span>
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

                            <div className="mb-8 bg-slate-700 rounded-lg p-4">
                                <h3 className="text-xl font-semibold mb-4">Skills, Technologies, and Focus Areas</h3>
                                <p className="text-gray-300 leading-relaxed">{assessment.skills.join(', ')}</p>
                            </div>

                            <div className="mb-8 bg-slate-700 rounded-lg p-4">
                                <div className="flex items-center justify-between mb-8">
                                    <h3 className="text-xl font-semibold">Candidates</h3>
                                    <Button variant="default" className="w-fit p-2 px-4 hover:bg-slate-700 hover:text-white rounded-lg border border-slate-700 transition-colors"
                                        onClick={() => setIsCommandOpen(true)}>
                                        <Plus size={16} />
                                        <span>Add</span>
                                    </Button>
                                </div>

                                <div className="flex flex-col sm:flex-row gap-4 mb-6">
                                    <div className="flex-1">
                                        <input
                                            type="text"
                                            placeholder="Search candidates by name or email..."
                                            value={candidateSearchTerm}
                                            onChange={(e) => setCandidateSearchTerm(e.target.value)}
                                            className="w-full bg-gray-800 text-white px-4 py-2 rounded-lg border border-gray-600 focus:border-blue-400 focus:outline-none placeholder-gray-400"
                                        />
                                    </div>

                                    <div className="sm:w-48">
                                        <select
                                            value={candidateStatusFilter}
                                            onChange={(e) => setCandidateStatusFilter(e.target.value)}
                                            className="w-full bg-gray-800 text-white px-4 py-2 rounded-lg border border-gray-600 focus:border-blue-400 focus:outline-none"
                                        >
                                            <option value="all">All Statuses</option>
                                            <option value="invited">Invited</option>
                                            <option value="started">Started</option>
                                            <option value="submitted">Submitted</option>
                                            <option value="evaluated">Evaluated</option>
                                        </select>
                                    </div>
                                </div>

                                {/* Loading State for Attempts */}
                                {attemptsLoading && (
                                    <div className="flex items-center justify-center py-8">
                                        <div className="flex items-center space-x-2">
                                            <Loader2 className="h-5 w-5 animate-spin text-gray-400" />
                                            <span className="text-gray-400">Loading candidates...</span>
                                        </div>
                                    </div>
                                )}

                                {/* Error State for Attempts */}
                                {attemptsError && (
                                    <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
                                        <div className="flex">
                                            <div className="ml-3">
                                                <h3 className="text-sm font-medium text-red-800">
                                                    Error loading candidates
                                                </h3>
                                                <div className="mt-2 text-sm text-red-700">
                                                    <p>{attemptsError.message}</p>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                )}

                                {/* Candidates List */}
                                {!attemptsLoading && !attemptsError && (
                                    <>
                                        <div className="mb-4">
                                            <p className="text-sm text-gray-400">
                                                Showing {candidateAttempts.length} of {totalAttempts} candidates
                                            </p>
                                        </div>

                                        <div className="space-y-3">
                                            {candidateAttempts.length > 0 ? (
                                                candidateAttempts.map((attempt: any) => (
                                                    <div
                                                        key={attempt.id}
                                                        className="bg-gray-800 rounded-lg p-4 flex items-center justify-between hover:bg-gray-650 transition-colors"
                                                    >
                                                        <div className="flex-1">
                                                            <div className="flex items-center gap-4">
                                                                <div>
                                                                    <h4 className="font-medium text-white">
                                                                        {attempt.candidate?.fullName || `${attempt.candidate?.firstName || ''} ${attempt.candidate?.lastName || ''}`.trim()}
                                                                    </h4>
                                                                    <p className="text-sm text-gray-400">{attempt.candidate?.email}</p>
                                                                    <p className="text-sm text-gray-400">
                                                                        {attempt.startedDate ? 'Started at: ' + new Date(attempt.startedDate).toLocaleString() : ''}
                                                                    </p>
                                                                    <p className="text-sm text-gray-400">
                                                                        {attempt.completedDate ? 'Submitted at: ' + new Date(attempt.completedDate).toLocaleString() : ''}
                                                                    </p>
                                                                    <p className="text-sm text-gray-400">
                                                                        {attempt.evaluatedDate ? 'Evaluated at: ' + new Date(attempt.evaluatedDate).toLocaleString() : ''}
                                                                    </p>
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
                                                                        <MoreHorizontal size={20} />
                                                                    </Button>
                                                                </DropdownMenuTrigger>
                                                                <DropdownMenuContent className="w-56 bg-slate-800 text-white border-slate-500" align="start">
                                                                    <DropdownMenuLabel>More Actions</DropdownMenuLabel>
                                                                    <DropdownMenuGroup>
                                                                        <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                                            Send email
                                                                        </DropdownMenuItem>
                                                                        {(attempt.status?.toLowerCase() === "completed" || attempt.status?.toLowerCase() === "evaluated") ? (
                                                                            <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                                                View Pull Request on GitHub
                                                                            </DropdownMenuItem>
                                                                        ) : attempt.status?.toLowerCase() === "started" ? (
                                                                            <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                                                View Repository on GitHub
                                                                            </DropdownMenuItem>
                                                                        ) : (
                                                                            <></>
                                                                        )}
                                                                        <DropdownMenuSeparator className="bg-slate-700" />
                                                                    </DropdownMenuGroup>
                                                                </DropdownMenuContent>
                                                            </DropdownMenu>
                                                        </div>
                                                    </div>
                                                ))
                                            ) : (
                                                <div className="bg-gray-700 rounded-lg p-8 text-center">
                                                    <p className="text-gray-400">No candidates found for this assessment.</p>
                                                </div>
                                            )}
                                        </div>

                                        {totalAttemptsPages > 1 && (
                                            <div className="flex items-center justify-between mt-6">
                                                <p className="text-sm text-gray-400">
                                                    Showing {((currentCandidatePage - 1) * candidatesPerPage) + 1} to{' '}
                                                    {Math.min(currentCandidatePage * candidatesPerPage, totalAttempts)} of{' '}
                                                    {totalAttempts} candidates
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
                                                        {currentCandidatePage} of {totalAttemptsPages}
                                                    </span>
                                                    <button
                                                        onClick={() => setCurrentCandidatePage(prev => Math.min(prev + 1, totalAttemptsPages))}
                                                        disabled={currentCandidatePage === totalAttemptsPages}
                                                        className="p-2 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                                    >
                                                        <ChevronRight size={16} />
                                                    </button>
                                                </div>
                                            </div>
                                        )}
                                    </>
                                )}
                            </div>

                            <Dialog open={isCommandOpen} onOpenChange={setIsCommandOpen}>
                                <DialogContent className="sm:max-w-[500px] p-0 bg-slate-800 text-white border-slate-500">
                                    <DialogHeader className="px-6 pt-6">
                                        <DialogTitle>Add candidates to assessment</DialogTitle>
                                        <DialogDescription>
                                            Search and select a candidate to add to the assessment.
                                        </DialogDescription>
                                    </DialogHeader>

                                    <div className="px-6">
                                        <Command className="rounded-lg border border-gray-600 bg-slate-800 text-white">
                                            <CommandInput
                                                placeholder="Search candidates by name or email..."
                                                className="border-none focus:ring-0"
                                            />
                                            <Button variant="ghost" className="w-full p-2 pt-5 pb-5 justify-start font-light">
                                                <Plus size={16} />
                                                <span>Add new candidate</span>
                                            </Button>

                                            <CommandList className="max-h-[300px] overflow-y-auto">
                                                <CommandEmpty>No candidates found.</CommandEmpty>
                                                                                            <CommandGroup heading="Available Candidates">
                                                {candidateAttempts.map((attempt: any) => (
                                                    <CommandItem
                                                        key={attempt.id}
                                                        className="flex items-center gap-3 p-3 cursor-pointer hover:bg-slate-700"
                                                        onSelect={() => {
                                                            setSelectedCandidateIds(prev =>
                                                                prev.includes(attempt.id.toString())
                                                                    ? prev.filter(id => id !== attempt.id.toString())
                                                                    : [...prev, attempt.id.toString()]
                                                            );
                                                        }}
                                                    >
                                                        <div className="flex items-center justify-center w-4 h-4 border border-gray-400 rounded">
                                                            {selectedCandidateIds.includes(attempt.id.toString()) && (
                                                                <Check size={12} className="text-blue-400" />
                                                            )}
                                                        </div>
                                                        <div className="flex flex-col flex-1">
                                                            <span className="font-medium text-white">
                                                                {attempt.candidate?.fullName || `${attempt.candidate?.firstName || ''} ${attempt.candidate?.lastName || ''}`.trim()}
                                                            </span>
                                                            <span className="text-sm text-gray-400">{attempt.candidate?.email}</span>
                                                        </div>
                                                    </CommandItem>
                                                ))}
                                            </CommandGroup>
                                            </CommandList>
                                        </Command>
                                    </div>

                                    <DialogFooter className="px-6 pb-6">
                                        <div className="flex items-center justify-between w-full">
                                            <span className="text-sm text-gray-400">
                                                {selectedCandidateIds.length} candidate{selectedCandidateIds.length !== 1 ? 's' : ''} selected
                                            </span>
                                            <div className="flex gap-2">
                                                <Button
                                                    variant="secondary"
                                                    onClick={() => {
                                                        setIsCommandOpen(false);
                                                        setSelectedCandidateIds([]);
                                                    }}
                                                >
                                                    Cancel
                                                </Button>
                                                <Button
                                                    type="submit"
                                                    disabled={selectedCandidateIds.length === 0}
                                                    onClick={() => {
                                                        console.log('Adding candidates:', selectedCandidateIds);
                                                        // Handle adding selected candidates here
                                                        setIsCommandOpen(false);
                                                        setSelectedCandidateIds([]);
                                                    }}
                                                >
                                                    Add {selectedCandidateIds.length} Candidate{selectedCandidateIds.length !== 1 ? 's' : ''}
                                                </Button>
                                            </div>
                                        </div>
                                    </DialogFooter>
                                </DialogContent>
                            </Dialog>

                            <div className="mb-8 bg-gray-700 rounded-lg p-4">
                                <div className="flex items-center justify-between mb-4">
                                    <h3 className="text-xl font-semibold">Metadata</h3>
                                </div>

                                <div className="space-y-4 mb-6">
                                    {assessment && Object.entries(assessment?.metadata || {}).map(([key, value]) => (
                                        <div key={key} className="bg-gray-800 rounded-lg p-4 flex items-center gap-4">
                                            <div className="flex-1 grid grid-cols-2 gap-4">
                                                <input
                                                    type="text"
                                                    value={key}
                                                    onChange={(e) => updateMetadataField(key, e.target.value, value)}
                                                    className="bg-gray-700 text-white px-3 py-2 border rounded-md border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
                                                    placeholder="Field name"
                                                />
                                                <input
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

                                <div className="bg-gray-700 rounded-lg p-4 flex items-center gap-4">
                                    <div className="flex-1 grid grid-cols-2 gap-4">
                                        <input
                                            type="text"
                                            value={newMetadataKey}
                                            onChange={(e) => setNewMetadataKey(e.target.value)}
                                            className="bg-gray-700 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none text-sm"
                                            placeholder="New field name"
                                        />
                                        <input
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

                            {updateAssessmentMutation.isPending && (
                                <div className="flex justify-end">
                                    <div className="flex items-center space-x-2">
                                        <Loader2 className="h-4 w-4 animate-spin" />
                                        <span>Saving changes...</span>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>

                    <div className="lg:col-span-1">
                        <div className="h-full">
                            <ChatMessageListExample />
                        </div>
                    </div>
                </div>
            </div>
        </div>
    )
}