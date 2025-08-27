import { Assessment } from "@/lib/types/assessment";
import { ArrowLeft, Calendar, Check, ChevronLeft, ChevronRight, Clock, Edit3, ExternalLink, Link2, MoreHorizontal, Plus, Trash2, X, Loader2, Info } from "lucide-react"
import { useState } from "react";
import { CandidateAttempt } from "@/lib/types/candidate-attempt";
import { Candidate } from "@/lib/types/candidate";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger, DialogClose } from "@/components/ui/dialog";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { ChatMessageListExample } from "@/pages/employer/assessment-details/chat-msg-list";
import { useAuth } from "@/contexts/AuthContext";
import { useRoute, useLocation } from "wouter";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import useApi from "@/hooks/use-api";
import { useToast } from "@/hooks/use-toast";
import { DateRangePicker } from "@/components/ui/date-range-picker";
import { Tooltip, TooltipProvider, TooltipTrigger, TooltipContent } from "@/components/ui/tooltip";

export default function AssessmentDetails() {
    const { apiCall } = useApi();
    const { toast } = useToast();
    const queryClient = useQueryClient();
    const [, navigate] = useLocation();
    const [, params] = useRoute("/assessments/view/:assessmentId");
    const assessmentId = params?.assessmentId;

    // State for editing
    const [isEditingDescription, setIsEditingDescription] = useState(false);
    const [isEditingName, setIsEditingName] = useState(false);
    const [isEditingRole, setIsEditingRole] = useState(false);
    const [isEditingDates, setIsEditingDates] = useState(false);
    const [tempDescription, setTempDescription] = useState('');
    const [tempName, setTempName] = useState('');
    const [tempRole, setTempRole] = useState('');
    const [tempStartDate, setTempStartDate] = useState<Date | undefined>(undefined);
    const [tempEndDate, setTempEndDate] = useState<Date | undefined>(undefined);

    // State for tracking changes
    const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
    const [pendingChanges, setPendingChanges] = useState<Partial<Assessment>>({});

    // State for activation dialog
    const [showActivationDialog, setShowActivationDialog] = useState(false);

    // State for metadata editing
    const [newMetadataKey, setNewMetadataKey] = useState('');
    const [newMetadataValue, setNewMetadataValue] = useState('');

    // Candidates pagination and filtering
    const [currentCandidatePage, setCurrentCandidatePage] = useState(1);
    const [candidateSearchTerm, setCandidateSearchTerm] = useState('');
    const [candidateStatusFilter, setCandidateStatusFilter] = useState<string>('all');
    const candidatesPerPage = 5;

    // Command dialog state
    const [selectedCandidateIds, setSelectedCandidateIds] = useState<string[]>([]);
    const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);

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
            clearPendingChanges();
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

    // Add candidates to assessment mutation
    const addCandidatesMutation = useMutation({
        mutationFn: async (candidateIds: string[]) => {
            const promises = candidateIds.map(candidateId =>
                apiCall(`/api/attempts/invite`, {
                    method: 'POST',
                    body: JSON.stringify({
                        candidateId: candidateId,
                        assessmentId: assessmentId,
                    }),
                })
            );
            
            const results = await Promise.all(promises);
            return results;
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['candidateAttempts', 'assessment', assessmentId] });
            queryClient.invalidateQueries({ queryKey: ['availableCandidates', 'assessment', assessmentId] });
            setSelectedCandidateIds([]);
            toast({
                title: "Success",
                description: "Candidates added to assessment successfully",
            });
        },
        onError: (error: any) => {
            toast({
                title: "Error",
                description: error.message || "Failed to add candidates to assessment",
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

    // Fetch available candidates for this assessment (candidates NOT in the assessment)
    const { data: availableCandidatesData, isLoading: availableCandidatesLoading, error: availableCandidatesError } = useQuery({
        queryKey: ['availableCandidates', 'assessment', assessmentId],
        queryFn: async () => {
            const response = await apiCall(`/api/candidates/available-for-assessment?assessmentId=${assessmentId}&page=0&size=100`, {
                method: 'GET',
            });

            if (!response) {
                throw new Error('Failed to fetch available candidates');
            }

            return response;
        },
        enabled: !!assessmentId && isAddDialogOpen,
    });

    // Extract attempts from the response
    const candidateAttempts: CandidateAttempt[] = attemptsData?.content || [];
    const totalAttempts: number = attemptsData?.totalElements || 0;
    const totalAttemptsPages: number = attemptsData?.totalPages || 0;

    // Extract available candidates from the response
    const availableCandidates: Candidate[] = availableCandidatesData?.content || [];

    // Filter candidate attempts based on search term
    const filteredAttempts: CandidateAttempt[] = candidateAttempts.filter((attempt: any) => {
        const candidateName = attempt.candidate?.fullName || `${attempt.candidate?.firstName || ''} ${attempt.candidate?.lastName || ''}`.trim();
        const candidateEmail = attempt.candidate?.email;
        const matchesSearch = candidateSearchTerm === '' ||
            candidateName?.toLowerCase().includes(candidateSearchTerm.toLowerCase()) ||
            candidateEmail?.toLowerCase().includes(candidateSearchTerm.toLowerCase());
        return matchesSearch;
    });

    // Filter available candidates based on search term
    const filteredAvailableCandidates = availableCandidates.filter((candidate: Candidate) => {
        const candidateName = candidate.fullName || `${candidate.firstName || ''} ${candidate.lastName || ''}`.trim();
        const candidateEmail = candidate.email;
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

    // Helper function to track changes
    const addPendingChange = (field: keyof Assessment, value: any) => {
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
            updateAssessmentMutation.mutate(pendingChanges);
            clearPendingChanges();
        }
    };

    // Helper function to get current value (including pending changes)
    const getCurrentValue = (field: keyof Assessment) => {
        if (pendingChanges[field] !== undefined) {
            return pendingChanges[field];
        }
        return assessment?.[field];
    };

    // Helper function to get current status
    const getCurrentStatus = (): 'DRAFT' | 'ACTIVE' | 'INACTIVE' => {
        if (pendingChanges.status !== undefined) {
            return pendingChanges.status as 'DRAFT' | 'ACTIVE' | 'INACTIVE';
        }
        return assessment?.status || 'DRAFT';
    };



    // Helper functions for specific field types
    const getCurrentName = (): string => {
        if (pendingChanges.name !== undefined) {
            return pendingChanges.name as string;
        }
        return assessment?.name || '';
    };

    const getCurrentRole = (): string => {
        if (pendingChanges.role !== undefined) {
            return pendingChanges.role as string;
        }
        return assessment?.role || '';
    };

    const getCurrentDescription = (): string => {
        if (pendingChanges.description !== undefined) {
            return pendingChanges.description as string;
        }
        return assessment?.description || '';
    };

    const getCurrentStartDate = (): Date | undefined => {
        if (pendingChanges.startDate !== undefined) {
            return pendingChanges.startDate as Date;
        }
        return assessment?.startDate ? new Date(assessment.startDate) : undefined;
    };

    const getCurrentEndDate = (): Date | undefined => {
        if (pendingChanges.endDate !== undefined) {
            return pendingChanges.endDate as Date;
        }
        return assessment?.endDate ? new Date(assessment.endDate) : undefined;
    };

    const getCurrentMetadata = (): Record<string, string> => {
        if (pendingChanges.metadata !== undefined) {
            return pendingChanges.metadata as Record<string, string>;
        }
        return assessment?.metadata || {};
    };

    const getCurrentLanguageOptions = (): string[] => {
        if (pendingChanges.languageOptions !== undefined) {
            return pendingChanges.languageOptions as string[];
        }
        return assessment?.languageOptions || [];
    };

    // Helper function to check if date range is valid (not entirely in the past)
    // const isDateRangeValid = (startDate: Date | undefined, endDate: Date | undefined): boolean => {
    //     if (!startDate || !endDate) return false;
    //     if (endDate <= startDate) return false;

    //     const now = new Date();
    //     const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()); // Start of today

    //     // Check if the entire date range is in the past
    //     return endDate >= today;
    // };

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

            addPendingChange('metadata', updatedMetadata);

            setNewMetadataKey('');
            setNewMetadataValue('');
        }
    };

    const deleteMetadataField = (key: string) => {
        if (assessment) {
            const newMetadata = { ...assessment.metadata };
            delete newMetadata[key];

            addPendingChange('metadata', newMetadata);
        }
    };

    const updateMetadataField = (oldKey: string, newKey: string, newValue: string) => {
        if (assessment) {
            const newMetadata = { ...assessment.metadata };
            if (oldKey !== newKey) {
                delete newMetadata[oldKey];
            }
            newMetadata[newKey] = newValue;

            addPendingChange('metadata', newMetadata);
        }
    };

    // Editing functions
    const startEditingDescription = () => {
        setIsEditingDescription(true);
        setTempDescription(assessment?.description || '');
    };

    const saveDescription = () => {
        if (assessment) {
            addPendingChange('description', tempDescription);
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
            addPendingChange('name', tempName);
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
            addPendingChange('role', tempRole);
        }
        setIsEditingRole(false);
    };

    const cancelRoleEdit = () => {
        setIsEditingRole(false);
        setTempRole('');
    };

    const startEditingDates = () => {
        setIsEditingDates(true);
        setTempStartDate(assessment?.startDate ? new Date(assessment.startDate) : undefined);
        setTempEndDate(assessment?.endDate ? new Date(assessment.endDate) : undefined);
    };

    const saveDates = () => {
        if (assessment && tempStartDate && tempEndDate) {
            addPendingChange('startDate', tempStartDate);
            addPendingChange('endDate', tempEndDate);
        }
        setIsEditingDates(false);
    };

    const cancelDateEdit = () => {
        setIsEditingDates(false);
        setTempStartDate(undefined);
        setTempEndDate(undefined);
    };

    const handleStatusChange = (newStatus: 'DRAFT' | 'ACTIVE' | 'INACTIVE') => {
        if (newStatus === 'ACTIVE') {
            // Validate dates before allowing activation (use current values including pending changes)
            const currentStartDate = getCurrentStartDate();
            const currentEndDate = getCurrentEndDate();

            if (!currentStartDate || !currentEndDate) {
                toast({
                    title: "Missing Date Range",
                    description: "Please set a start and end date before activating the assessment.",
                    variant: "destructive",
                });
                return;
            }

            const now = new Date();

            // if (currentStartDate <= now) {
            //     toast({
            //         title: "Invalid Start Date",
            //         description: "Start date must be in the future.",
            //         variant: "destructive",
            //     });
            //     return;
            // }

            if (currentEndDate <= currentStartDate) {
                toast({
                    title: "Invalid End Date",
                    description: "End date must be after start date.",
                    variant: "destructive",
                });
                return;
            }

            // Show activation confirmation dialog
            setShowActivationDialog(true);
        } else {
            // For non-active status changes, add to pending changes
            addPendingChange('status', newStatus);
        }
    };

    const confirmActivation = () => {
        addPendingChange('status', 'ACTIVE');
        setShowActivationDialog(false);
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
                    <span className="text-red-500" hidden={!hasUnsavedChanges}>You have unsaved changes</span>
                    <button
                        className="flex items-center gap-2 text-gray-200 hover:text-gray-100 mb-6 transition-colors bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg"
                    >
                        Switch to Candidate View
                    </button>
                </div>

                <div className="text-sm grid grid-cols-1 lg:grid-cols-3 gap-6 h-[calc(100vh-8rem)]">
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
                                                <p className="text-gray-300 text-lg">{getCurrentRole()}</p>
                                                <button
                                                    onClick={startEditingRole}
                                                    className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                                >
                                                    <Edit3 size={14} />
                                                </button>
                                            </div>
                                        )}
                                        <button
                                            onClick={() => openRepository(assessment.githubRepositoryLink)}
                                            className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg transition-colors text-sm"
                                        >
                                            <ExternalLink size={16} />
                                            View Template Repository
                                        </button>

                                        <button
                                            onClick={() => alert('Coming soon')}
                                            className="flex items-center gap-2 bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg transition-colors text-sm"
                                        >
                                            <ExternalLink size={16} />
                                            View analytics
                                        </button>
                                    </div>

                                    <div className="flex items-center gap-3 mb-4">
                                        <div className="flex items-center gap-2">
                                            <span className={`px-3 py-1 rounded-full text-sm font-medium capitalize ${getCurrentStatus() === 'ACTIVE'
                                                ? 'bg-green-600 text-white'
                                                : getCurrentStatus() === 'DRAFT'
                                                    ? 'bg-yellow-600 text-white'
                                                    : 'bg-red-600 text-white'
                                                }`}>
                                                {getCurrentStatus() === 'ACTIVE' ? 'Active' :
                                                    getCurrentStatus() === 'DRAFT' ? 'Draft' : 'Inactive'}
                                            </span>
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button variant="ghost" className="p-1 h-auto text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors">
                                                        <Edit3 size={14} />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent className="w-48 bg-slate-800 text-white border-slate-500" align="start">
                                                    <DropdownMenuLabel>Change Status</DropdownMenuLabel>
                                                    <DropdownMenuGroup>
                                                        {getCurrentStatus() !== 'DRAFT' && (
                                                            <DropdownMenuItem
                                                                className="hover:bg-slate-700 transition-colors hover:text-white"
                                                                onClick={() => handleStatusChange('DRAFT')}
                                                            >
                                                                Draft
                                                            </DropdownMenuItem>
                                                        )}
                                                        {getCurrentStatus() !== 'ACTIVE' && (
                                                            <DropdownMenuItem
                                                                className="hover:bg-slate-700 transition-colors hover:text-white"
                                                                onClick={() => handleStatusChange('ACTIVE')}
                                                            >
                                                                Active
                                                            </DropdownMenuItem>
                                                        )}
                                                        {getCurrentStatus() !== 'INACTIVE' && (
                                                            <DropdownMenuItem
                                                                className="hover:bg-slate-700 transition-colors hover:text-white"
                                                                onClick={() => handleStatusChange('INACTIVE')}
                                                            >
                                                                Inactive
                                                            </DropdownMenuItem>
                                                        )}
                                                    </DropdownMenuGroup>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-6 text-sm text-gray-400">
                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} />
                                            <span>Created: {assessment.createdDate.toString()}</span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Calendar size={16} />
                                            <span>{formatDateRange(assessment)}</span>
                                        </div>
                                    </div>

                                    <div className="flex items-center gap-2 my-5">
                                        <Button variant="link" className="flex items-center" onClick={() => {
                                            navigator.clipboard.writeText(import.meta.env.VITE_APP_URL + '/assessments/preview/' + assessment.id);
                                            toast({
                                                title: 'Public link copied to clipboard',
                                                description: 'You can now share the link with your candidates',
                                                variant: 'default',
                                            });
                                        }}>
                                            <Link2 size={20} className="text-blue-400" />
                                            <span className="text-blue-400">Copy public assessment link</span>
                                        </Button>
                                    </div>

                                    <div className="mt-6">
                                        <div className="flex items-center justify-between mb-2">
                                            <div className="flex items-center gap-5">
                                                <h3 className="text-lg font-semibold">Date Range</h3>
                                                <TooltipProvider delayDuration={100}>
                                                    <Tooltip>
                                                        <TooltipTrigger asChild>
                                                            <Info size={16} className="text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors" />
                                                        </TooltipTrigger>
                                                        <TooltipContent className="bg-gray-800 text-white border-gray-500">
                                                            <p>The date range in which candidates can take the assessment.</p>
                                                        </TooltipContent>
                                                    </Tooltip>
                                                </TooltipProvider>
                                            </div>
                                            {!isEditingDates && (
                                                <button
                                                    onClick={startEditingDates}
                                                    className="p-1 text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors"
                                                >
                                                    <Edit3 size={16} />
                                                </button>
                                            )}
                                        </div>
                                        {isEditingDates ? (
                                            <div className="space-y-2">
                                                <DateRangePicker
                                                    startDate={tempStartDate}
                                                    endDate={tempEndDate}
                                                    onStartDateChange={setTempStartDate}
                                                    onEndDateChange={setTempEndDate}
                                                />
                                                <div className="flex items-center gap-2">
                                                    <button
                                                        onClick={() => {
                                                            if (!tempStartDate || !tempEndDate) {
                                                                toast({
                                                                    title: "Missing Dates",
                                                                    description: "Please select both start and end dates.",
                                                                    variant: "destructive",
                                                                });
                                                                return;
                                                            }

                                                            if (tempEndDate <= tempStartDate) {
                                                                toast({
                                                                    title: "Invalid Date Range",
                                                                    description: "End date must be after start date.",
                                                                    variant: "destructive",
                                                                });
                                                                return;
                                                            }

                                                            const now = new Date();
                                                            const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

                                                            if (tempEndDate < today) {
                                                                toast({
                                                                    title: "Invalid Date Range",
                                                                    description: "The selected date range is entirely in the past. Please select dates in the future.",
                                                                    variant: "destructive",
                                                                });
                                                                return;
                                                            }

                                                            saveDates();
                                                        }}
                                                        disabled={!tempStartDate || !tempEndDate}
                                                        className="px-3 py-1 text-green-400 hover:text-green-300 hover:bg-gray-700 rounded transition-colors text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                                                    >
                                                        Save
                                                    </button>
                                                    <button
                                                        onClick={cancelDateEdit}
                                                        className="px-3 py-1 text-red-400 hover:text-red-300 hover:bg-gray-700 rounded transition-colors text-sm"
                                                    >
                                                        Cancel
                                                    </button>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="bg-gray-700 rounded-lg p-4">
                                                <div className="flex items-center gap-4 text-gray-300">
                                                    <div className="flex items-center gap-2">
                                                        <Calendar size={16} />
                                                        <span>Start: {getCurrentStartDate() ? getCurrentStartDate()!.toLocaleDateString() : 'Not set'}</span>
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        <Calendar size={16} />
                                                        <span>End: {getCurrentEndDate() ? getCurrentEndDate()!.toLocaleDateString() : 'Not set'}</span>
                                                    </div>
                                                </div>
                                            </div>
                                        )}
                                    </div>

                                    <div className="mt-6">
                                        <div className="flex items-center justify-between mb-2">
                                            <div className="flex items-center gap-5">
                                                <h3 className="text-lg font-semibold">Description</h3>
                                                <TooltipProvider delayDuration={100}>
                                                    <Tooltip>
                                                        <TooltipTrigger asChild>
                                                            <Info size={16} className="text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors" />
                                                        </TooltipTrigger>
                                                        <TooltipContent className="bg-gray-800 text-white border-gray-500">
                                                            <p>A brief description of the role and/or the assessment that will be shown to candidates before they take the assessment.</p>
                                                        </TooltipContent>
                                                    </Tooltip>
                                                </TooltipProvider>
                                            </div>
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
                                            <div className="space-y-2">
                                                <textarea
                                                    value={tempDescription}
                                                    onChange={(e) => setTempDescription(e.target.value)}
                                                    className="w-full bg-gray-700 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-400 focus:outline-none min-h-[100px] resize-none"
                                                    placeholder="Enter assessment description..."
                                                    autoFocus
                                                />
                                                <div className="flex items-center gap-2">
                                                    <button
                                                        onClick={saveDescription}
                                                        className="px-3 py-1 text-green-400 hover:text-green-300 hover:bg-gray-700 rounded transition-colors text-sm"
                                                    >
                                                        Save
                                                    </button>
                                                    <button
                                                        onClick={cancelDescriptionEdit}
                                                        className="px-3 py-1 text-red-400 hover:text-red-300 hover:bg-gray-700 rounded transition-colors text-sm"
                                                    >
                                                        Cancel
                                                    </button>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="bg-gray-700 rounded-lg p-4">
                                                <p className="text-gray-300 leading-relaxed whitespace-pre-wrap">
                                                    {getCurrentDescription() || 'No description provided'}
                                                </p>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            </div>

                            <div className="mt-6">
                                <div className="flex items-center justify-between mb-2">
                                    <h3 className="text-lg font-semibold">Skills, Technologies, and Focus Areas</h3>
                                </div>
                                <div className="flex flex-wrap gap-2 p-4">
                                    {assessment?.skills?.map((language, index) => (
                                        <span
                                            key={index}
                                            className="bg-slate-800/60 border border-slate-700/50 rounded-lg px-3 py-2 text-sm text-slate-300 backdrop-blur-sm"
                                        >
                                            {language}
                                        </span>
                                    ))}
                                </div>

                            </div>

                            {getCurrentLanguageOptions().length > 0 && (
                                <div className="mt-6">
                                    <div className="flex items-center gap-5 mb-2">
                                        <h3 className="text-lg font-semibold">Language Options</h3>
                                        <TooltipProvider delayDuration={100}>
                                            <Tooltip>
                                                <TooltipTrigger asChild>
                                                    <Info size={16} className="text-gray-400 hover:text-gray-300 hover:bg-gray-700 rounded transition-colors" />
                                                </TooltipTrigger>
                                                <TooltipContent className="bg-gray-800 text-white border-gray-500">
                                                    <p>Technologies that candidates can choose from to complete the assessment.</p>
                                                </TooltipContent>
                                            </Tooltip>
                                        </TooltipProvider>
                                    </div>
                                    <div className="rounded-lg p-4">

                                        <div className="flex flex-wrap gap-2">
                                            {getCurrentLanguageOptions().map((language, index) => (
                                                <span
                                                    key={index}
                                                    className="bg-slate-800/60 border border-slate-700/50 rounded-lg px-3 py-2 text-sm text-slate-300 backdrop-blur-sm"
                                                >
                                                    {language}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}

                            <div className="mt-6">
                                <div className="flex items-center justify-between mb-2">
                                    <h3 className="text-lg font-semibold">Candidates</h3>
                                    <Dialog open={isAddDialogOpen} onOpenChange={(open) => { setIsAddDialogOpen(open); if (!open) { setSelectedCandidateIds([]); } }}>
                                        <DialogTrigger asChild>
                                            <Button
                                                variant="default"
                                                className="w-fit p-2 px-4 hover:bg-slate-700 hover:text-white rounded-lg border border-slate-700 transition-colors"
                                                onClick={() => {
                                                    // Reset selected candidates when opening dialog
                                                    setSelectedCandidateIds([]);

                                                }}
                                                disabled={availableCandidatesLoading || !!availableCandidatesError}
                                            >
                                                <Plus size={16} />
                                                <span>Add</span>
                                            </Button>
                                        </DialogTrigger>
                                        <DialogContent className="sm:max-w-[500px] p-0 bg-slate-800 text-white border-slate-500">
                                            <DialogHeader className="px-6 pt-6">
                                                <DialogTitle>Add candidates to assessment</DialogTitle>
                                                <DialogDescription>
                                                    Search and select a candidate to add to the assessment.
                                                </DialogDescription>
                                            </DialogHeader>

                                            {/* Candidate Attempts Section */}
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
                                                        {availableCandidatesLoading ? (
                                                            <div className="flex items-center justify-center p-4">
                                                                <Loader2 className="h-4 w-4 animate-spin mr-2" />
                                                                Loading candidates...
                                                            </div>
                                                        ) : (
                                                            <>
                                                                <CommandEmpty>No candidates found.</CommandEmpty>
                                                                <CommandGroup heading="Available Candidates">
                                                                    {filteredAvailableCandidates.map((candidate: Candidate) => (
                                                                <CommandItem
                                                                    key={candidate.id}
                                                                    className="flex items-center gap-3 p-3 cursor-pointer hover:bg-slate-700"
                                                                    onSelect={() => {
                                                                        setSelectedCandidateIds(prev =>
                                                                            prev.includes(candidate.id.toString())
                                                                                ? prev.filter(id => id !== candidate.id.toString())
                                                                                : [...prev, candidate.id.toString()]
                                                                        );
                                                                    }}
                                                                >
                                                                    <div className="flex items-center justify-center w-4 h-4 border border-gray-400 rounded">
                                                                        {selectedCandidateIds.includes(candidate.id.toString()) && (
                                                                            <Check size={12} className="text-blue-400" />
                                                                        )}
                                                                    </div>
                                                                    <div className="flex flex-col flex-1">
                                                                        <span className="font-medium text-white">
                                                                            {candidate.fullName || `${candidate.firstName || ''} ${candidate.lastName || ''}`.trim()}
                                                                        </span>
                                                                        <span className="text-sm text-gray-400">{candidate.email}</span>
                                                                    </div>
                                                                </CommandItem>
                                                            ))}
                                                                </CommandGroup>
                                                            </>
                                                        )}
                                                    </CommandList>
                                                </Command>
                                            </div>

                                            <DialogFooter className="px-6 pb-6">
                                                <div className="flex items-center justify-between w-full">
                                                    <span className="text-sm text-gray-400">
                                                        {selectedCandidateIds.length} candidate{selectedCandidateIds.length !== 1 ? 's' : ''} selected
                                                    </span>
                                                    <div className="flex gap-2">
                                                        <DialogClose asChild>
                                                            <Button
                                                                variant="secondary"
                                                                onClick={() => {

                                                                    setSelectedCandidateIds([]);
                                                                }}
                                                            >
                                                                Cancel
                                                            </Button>
                                                        </DialogClose>

                                                        <Button
                                                            type="submit"
                                                            disabled={selectedCandidateIds.length === 0 || addCandidatesMutation.isPending}
                                                            onClick={() => {
                                                                console.log('Adding candidates:', selectedCandidateIds);
                                                                addCandidatesMutation.mutate(selectedCandidateIds);
                                                            }}
                                                        >
                                                            {addCandidatesMutation.isPending ? (
                                                                <>
                                                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                                                    Adding...
                                                                </>
                                                            ) : (
                                                                `Add ${selectedCandidateIds.length} Candidate${selectedCandidateIds.length !== 1 ? 's' : ''}`
                                                            )}
                                                        </Button>


                                                    </div>
                                                </div>
                                            </DialogFooter>
                                        </DialogContent>
                                    </Dialog>

                                </div>
                                <div className="bg-slate-700 rounded-lg p-4">

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
                                                    Showing {filteredAttempts.length} of {totalAttempts} candidates
                                                </p>
                                            </div>

                                            <div className="space-y-3">
                                                {filteredAttempts.length > 0 ? (
                                                    filteredAttempts.map((attempt: any) => (
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
                            </div>

                            <div className="mt-6">
                                <div className="flex items-center justify-between mb-2">
                                    <h3 className="text-lg font-semibold">Metadata</h3>
                                </div>
                                <div className="bg-gray-700 rounded-lg p-4">

                                    <div className="space-y-4 mb-6">
                                        {assessment && Object.entries(getCurrentMetadata()).map(([key, value]) => (
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
                                    {updateAssessmentMutation.isPending && (
                                        <div className="flex items-center space-x-2 text-gray-400">
                                            <Loader2 className="h-4 w-4 animate-spin" />
                                            <span>Saving changes...</span>
                                        </div>
                                    )}
                                    <Button
                                        variant="outline"
                                        onClick={clearPendingChanges}
                                        disabled={!hasUnsavedChanges || updateAssessmentMutation.isPending}
                                        className="text-gray-400 hover:text-gray-300 border-gray-600 hover:border-gray-500"
                                    >
                                        Cancel
                                    </Button>
                                    <Button
                                        onClick={saveAllChanges}
                                        disabled={!hasUnsavedChanges || updateAssessmentMutation.isPending}
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
                            <ChatMessageListExample />
                        </div>
                    </div>
                </div>
            </div>

            {/* Activation Confirmation Dialog */}
            <Dialog open={showActivationDialog} onOpenChange={setShowActivationDialog}>
                <DialogContent className="sm:max-w-[500px] bg-slate-800 text-white border-slate-500">
                    <DialogHeader>
                        <DialogTitle>Activate Assessment</DialogTitle>
                        <DialogDescription className="text-gray-300">
                            Are you sure you want to activate this assessment? Once activated, the public link will become available and candidates can start taking the assessment.
                        </DialogDescription>
                    </DialogHeader>
                    <DialogFooter>
                        <Button
                            variant="secondary"
                            onClick={() => setShowActivationDialog(false)}
                        >
                            Cancel
                        </Button>
                        <Button
                            onClick={confirmActivation}
                            className="bg-green-600 hover:bg-green-700 text-white"
                        >
                            Confirm
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    )
}