import { useState } from "react";
import { Calendar, Clock, MoreHorizontal, Plus, Eye, Loader2, Info } from "lucide-react";
import { Button } from "@/components/ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { AppShell } from "@/components/layout/app-shell";
import { Assessment } from "@/lib/types/assessment";
import { Link, useLocation } from "wouter";
import { useQuery } from "@tanstack/react-query";
import useApi from "@/hooks/use-api";
import { Label } from "@/components/ui/label";
import { DateRangePicker } from "@/components/ui/date-range-picker";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";
import Pagination from "@/components/ui/pagination";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { PaginatedResponse } from "@/lib/types/paginated-response";

export default function EmployerAssessments() {
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [selectedStatus, setSelectedStatus] = useState<string>("");
    const [creationDateFrom, setCreationDateFrom] = useState<string>("");
    const [creationDateTo, setCreationDateTo] = useState<string>("");
    const [assessmentDateFrom, setAssessmentDateFrom] = useState<string>("");
    const [assessmentDateTo, setAssessmentDateTo] = useState<string>("");
    const [appliedFilters, setAppliedFilters] = useState<{
        status?: string;
        creationDateFrom?: string;
        creationDateTo?: string;
        assessmentDateFrom?: string;
        assessmentDateTo?: string;
    }>({});
    const { apiCall } = useApi();
    const [, navigate] = useLocation();

    // Fetch assessments using TanStack Query
    const { data: assessmentsData, isLoading, error } = useQuery({
        queryKey: ['assessments', 'user', page, size, appliedFilters],
        queryFn: async (): Promise<PaginatedResponse<Assessment>> => {
            const params = new URLSearchParams({
                page: page.toString(),
                size: size.toString()
            });

            // Add status filter
            if (appliedFilters.status) {
                params.append('status', appliedFilters.status);
            }

            // Add creation date filters
            if (appliedFilters.creationDateFrom) {
                params.append('startDate', appliedFilters.creationDateFrom);
            }
            if (appliedFilters.creationDateTo) {
                params.append('endDate', appliedFilters.creationDateTo);
            }

            // Add assessment date filters
            if (appliedFilters.assessmentDateFrom) {
                params.append('assessmentStartDate', appliedFilters.assessmentDateFrom);
            }
            if (appliedFilters.assessmentDateTo) {
                params.append('assessmentEndDate', appliedFilters.assessmentDateTo);
            }

            const response = await apiCall(`/api/assessments/filter?${params.toString()}`, {
                method: 'GET',
            });

            if (!response) {
                throw new Error('Failed to fetch assessments');
            }

            return response;
        },
    });

    // Extract assessments and pagination data from the response
    const assessments = assessmentsData?.content || [];
    const totalPages = assessmentsData?.totalPages || 0;
    const currentPage = assessmentsData?.page || 0;

    // Handle page change
    const handlePageChange = (newPage: number) => {
        setPage(newPage);
    };

    // Apply filters
    const applyFilters = () => {
        setAppliedFilters({
            status: selectedStatus || undefined,
            creationDateFrom: creationDateFrom || undefined,
            creationDateTo: creationDateTo || undefined,
            assessmentDateFrom: assessmentDateFrom || undefined,
            assessmentDateTo: assessmentDateTo || undefined
        });
        setPage(0); // Reset to first page when applying filters
    };

    // Clear filters
    const clearFilters = () => {
        setSelectedStatus("");
        setCreationDateFrom("");
        setCreationDateTo("");
        setAssessmentDateFrom("");
        setAssessmentDateTo("");
        setAppliedFilters({});
        setPage(0);
    };

    // Check if filters have changed
    const hasFilterChanges = () => {
        if (appliedFilters.status !== selectedStatus) return true;
        if (appliedFilters.creationDateFrom !== creationDateFrom) return true;
        if (appliedFilters.creationDateTo !== creationDateTo) return true;
        if (appliedFilters.assessmentDateFrom !== assessmentDateFrom) return true;
        if (appliedFilters.assessmentDateTo !== assessmentDateTo) return true;
        
        return false;
    };

    // TODO: add date range for active assessments
    const formatDateRange = (assessment: any) => {
        const start = assessment?.startDate ? new Date(assessment.startDate).toLocaleDateString() : '';
        const end = assessment?.endDate ? new Date(assessment.endDate).toLocaleDateString() : '';
        return `${start} - ${end}`;
    };

    const handleAssessmentSelect = (assessment: Assessment) => {
        navigate(`/assessments/view/${assessment.id}`);
    };

    return (
        <AppShell>
            <div className="max-w-6xl mx-auto text-white">
                <div className="flex items-center justify-between mb-8">
                    <div>
                        <h1 className="serif-heading">Assessments</h1>
                        <p className="text-gray-400 flex items-center space-x-2">
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

                {/* Filters Section */}
                <div className="bg-slate-800 text-gray-100 shadow-md px-4 py-5 sm:rounded-lg sm:p-6 mb-6 border border-slate-700">
                    <div className="md:flex md:items-center md:justify-between">
                        <div className="flex-1 min-w-0">
                            <h2 className="text-lg leading-6 font-medium">Filters</h2>
                        </div>
                    </div>
                    
                    <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                        {/* Status Filter */}
                        <div className="space-y-4">
                            <div className="flex items-center gap-x-2">
                                <Label htmlFor="status-filter">Assessment Status</Label>
                                <TooltipProvider delayDuration={100}>
                                    <Tooltip>
                                        <TooltipTrigger className="cursor-pointer">
                                            <Info className="w-4 h-4" />
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-slate-700 border-slate-600 text-gray-100">
                                            <p>Filter by assessment status</p>
                                        </TooltipContent>
                                    </Tooltip>
                                </TooltipProvider>
                            </div>
                            <Select
                                value={selectedStatus}
                                onValueChange={(value) => setSelectedStatus(value)}
                            >
                                <SelectTrigger className="bg-slate-700 border-slate-600 text-gray-100">
                                    <SelectValue placeholder="All Statuses" />
                                </SelectTrigger>
                                <SelectContent className="bg-slate-700 border-slate-600 text-gray-100">
                                    <SelectItem value="ALL">All Statuses</SelectItem>
                                    <SelectItem value="DRAFT">Draft</SelectItem>
                                    <SelectItem value="ACTIVE">Active</SelectItem>
                                    <SelectItem value="INACTIVE">Inactive</SelectItem>
                                    <SelectItem value="EXPIRED">Expired</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        {/* Creation Date Range Filter */}
                        <div className="space-y-4">
                            <div className="flex items-center gap-x-2">
                                <Label htmlFor="creation-date-filter">Created Date Range</Label>
                                <TooltipProvider delayDuration={100}>
                                    <Tooltip>
                                        <TooltipTrigger className="cursor-pointer">
                                            <Info className="w-4 h-4" />
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-slate-700 border-slate-600 text-gray-100">
                                            <p>Filter by when the assessment was created</p>
                                        </TooltipContent>
                                    </Tooltip>
                                </TooltipProvider>
                            </div>
                            <div className="mt-1">
                                <DateRangePicker
                                    onStartDateChange={(date) => setCreationDateFrom(date?.toISOString() || "")}
                                    onEndDateChange={(date) => setCreationDateTo(date?.toISOString() || "")}
                                />
                            </div>
                        </div>

                        {/* Assessment Date Range Filter */}
                        <div className="space-y-4">
                            <div className="flex items-center gap-x-2">
                                <Label htmlFor="assessment-date-filter">Assessment Date Range</Label>
                                <TooltipProvider delayDuration={100}>
                                    <Tooltip>
                                        <TooltipTrigger className="cursor-pointer">
                                            <Info className="w-4 h-4" />
                                        </TooltipTrigger>
                                        <TooltipContent className="bg-slate-700 border-slate-600 text-gray-100">
                                            <p>Filter by assessment start/end dates</p>
                                        </TooltipContent>
                                    </Tooltip>
                                </TooltipProvider>
                            </div>
                            <div className="mt-1">
                                <DateRangePicker
                                    onStartDateChange={(date) => setAssessmentDateFrom(date?.toISOString() || "")}
                                    onEndDateChange={(date) => setAssessmentDateTo(date?.toISOString() || "")}
                                />
                            </div>
                        </div>
                    </div>
                    
                    {/* Filter Actions */}
                    <div className="flex justify-end mt-6 gap-x-4">
                        <Button 
                            className="bg-slate-700 hover:bg-red-600 text-gray-100 text-sm px-4 py-2 rounded-lg font-medium transition-colors"
                            onClick={clearFilters}
                        >
                            Clear Filters
                        </Button>
                        <Button 
                            className="bg-green-700 hover:bg-green-600 text-gray-100 text-sm px-4 py-2 rounded-lg font-medium transition-colors"
                            onClick={applyFilters}
                            disabled={!hasFilterChanges()}
                        >
                            Apply Filters
                        </Button>
                    </div>
                </div>

                {/* Loading State */}
                {isLoading && (
                    <div className="flex items-center justify-center py-12">
                        <div className="flex items-center space-x-2">
                            <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
                            <span className="text-gray-400">Loading assessments...</span>
                        </div>
                    </div>
                )}

                {/* Error State */}
                {error && (
                    <div className="bg-red-50 border border-red-200 rounded-md p-4">
                        <div className="flex">
                            <div className="ml-3">
                                <h3 className="text-sm font-medium text-red-800">
                                    Error loading assessments
                                </h3>
                                <div className="mt-2 text-sm text-red-700">
                                    <p>{error.message}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}

                {/* Assessments List */}
                {!isLoading && !error && (
                    <div className="space-y-4">
                        {assessments.length === 0 ? (
                            <div className="bg-slate-800 border border-slate-700 rounded-md p-8 text-center">
                                <p className="text-gray-400">No assessments found</p>
                            </div>
                        ) : (
                            assessments.map((assessment: Assessment) => (
                                <div
                                    key={assessment.id}
                                    className="bg-gray-800 border border-slate-700 rounded-lg p-6 hover:bg-gray-750 transition-colors cursor-pointer shadow-lg"
                                >
                                    <div className="flex items-center justify-between">
                                        <div className="flex-1" onClick={() => handleAssessmentSelect(assessment)}>
                                            <div className="mb-3">
                                                <h3 className="text-xl font-semibold text-white mb-2">{assessment.name}</h3>
                                                <div className="flex items-center gap-3 mb-2">
                                                    <span className={`px-3 py-1 rounded-full text-sm font-medium capitalize ${assessment.status === 'ACTIVE'
                                                        ? 'bg-green-600 text-white'
                                                        : 'bg-red-600 text-white'
                                                        }`}>
                                                        {assessment.status?.toLowerCase()}
                                                    </span>
                                                </div>
                                            </div>

                                            <p className="text-gray-300 mb-3">
                                                <span className="font-medium">{assessment.role}</span>
                                            </p>

                                            <div className="flex items-center gap-6 text-sm text-gray-400">
                                                <div className="flex items-center gap-2">
                                                    <Calendar size={16} />
                                                    <span>Created {assessment.createdDate.toString()}</span>
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
                                                    <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white" onClick={() => navigator.clipboard.writeText(`${import.meta.env.VITE_APP_URL}/assessments/preview/${assessment.id}`)}>
                                                        Copy link
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                                                        {assessment.status === 'ACTIVE' ? 'Deactivate Assessment' : 'Activate Assessment'}
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white" onClick={() => window.open(assessment.githubRepositoryLink, '_blank')}>
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
                            ))
                        )}
                    </div>
                )}

                {/* Pagination Controls */}
                {!isLoading && !error && totalPages > 1 && (
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        onPageChange={handlePageChange}
                        isLoading={isLoading}
                    />
                )}
            </div>
        </AppShell>
    );
}