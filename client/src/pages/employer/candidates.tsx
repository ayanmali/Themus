import { useState } from "react";
import { AppShell } from "@/components/layout/app-shell";
import { Plus, Search, Loader2, Info } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { CandidateCard } from "@/components/candidate-card";
import { Candidate, AttemptStatus } from "@/lib/types/candidate";
import { useQuery } from "@tanstack/react-query";
import useApi from "@/hooks/use-api";
import { Button } from "@/components/ui/button";
import { Link } from "wouter";
import Pagination from "@/components/ui/pagination";
import { DateRangePicker } from "@/components/ui/date-range-picker";
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip";

interface PaginatedResponse {
  content: Candidate[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export default function EmployerCandidates() {
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [selectedStatuses, setSelectedStatuses] = useState<AttemptStatus[]>([]);
  const [dateFrom, setDateFrom] = useState<string>("");
  const [dateTo, setDateTo] = useState<string>("");
  const [appliedFilters, setAppliedFilters] = useState<{
    statuses: AttemptStatus[];
    dateFrom?: string;
    dateTo?: string;
  }>({ statuses: [] });
  const { apiCall } = useApi();

  // Fetch candidates using TanStack Query
  const { data: candidatesData, isLoading, error } = useQuery({
    queryKey: ['candidates', 'user', page, size, appliedFilters],
    queryFn: async (): Promise<PaginatedResponse> => {
      const params = new URLSearchParams({
        page: page.toString(),
        size: size.toString()
      });

      // Add status filters
      if (appliedFilters.statuses.length > 0) {
        appliedFilters.statuses.forEach(status => {
          params.append('attemptStatuses', status);
        });
      }

      // Add date filters
      if (appliedFilters.dateFrom) {
        params.append('createdAfter', appliedFilters.dateFrom);
      }
      if (appliedFilters.dateTo) {
        params.append('createdBefore', appliedFilters.dateTo);
      }

      const response = await apiCall(`/api/candidates/filter?${params.toString()}`, {
        method: 'GET',
      });

      if (!response) {
        throw new Error('Failed to fetch candidates');
      }

      return response;
    },
  });

  // Extract candidates and pagination data from the response
  const candidates = candidatesData?.content || [];
  const totalPages = candidatesData?.totalPages || 0;
  const currentPage = candidatesData?.page || 0;

  // Handle page change
  const handlePageChange = (newPage: number) => {
    setPage(newPage);
  };

  // Handle status selection
  const handleStatusChange = (status: AttemptStatus, checked: boolean) => {
    if (checked) {
      setSelectedStatuses(prev => [...prev, status]);
    } else {
      setSelectedStatuses(prev => prev.filter(s => s !== status));
    }
  };

  // Apply filters
  const applyFilters = () => {
    setAppliedFilters({
      statuses: selectedStatuses,
      dateFrom: dateFrom || undefined,
      dateTo: dateTo || undefined
    });
    setPage(0); // Reset to first page when applying filters
  };

  // Clear filters
  const clearFilters = () => {
    setSelectedStatuses([]);
    setDateFrom("");
    setDateTo("");
    setAppliedFilters({ statuses: [] });
    setPage(0);
  };

  // Check if filters have changed
  const hasFilterChanges = () => {
    const currentStatuses = appliedFilters.statuses.sort();
    const newStatuses = selectedStatuses.sort();

    if (currentStatuses.length !== newStatuses.length) return true;
    if (currentStatuses.some((s, i) => s !== newStatuses[i])) return true;
    if (appliedFilters.dateFrom !== dateFrom) return true;
    if (appliedFilters.dateTo !== dateTo) return true;

    return false;
  };

  // Filter candidates based on search query
  const filteredCandidates = candidates.filter((candidate: any) => {
    if (searchQuery === "") return true;

    return (
      candidate.fullName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      candidate.email?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  return (
    <AppShell>
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="serif-heading">Candidates</h1>
          <p className="text-gray-400 flex items-center space-x-2">
            <span>Add, view, and manage your candidates</span>
          </p>
        </div>
        <div className="flex space-x-3">
          <Link to="/candidates/new">
            <Button className="bg-slate-700 hover:bg-blue-700 text-gray-100 text-sm px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
              <Plus className="w-4 h-4" />
              <span>Add</span>
            </Button>
          </Link>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-slate-800 text-gray-100 shadow-md px-4 py-5 sm:rounded-lg sm:p-6 mb-6 border border-slate-700">
        <div className="md:flex md:items-center md:justify-between">
          <div className="flex-1 min-w-0">
            <h2 className="text-lg leading-6 font-medium">Filters</h2>
          </div>
          <div className="mt-4 flex md:mt-0 md:ml-4">
            <div className="flex items-center">
              <label htmlFor="candidate-search" className="sr-only">Search Candidates</label>
              <div className="relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  id="candidate-search"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10 text-gray-100 bg-slate-700 border-slate-600 focus:ring-slate-500 focus:border-slate-500"
                  placeholder="Search candidates..."
                />
              </div>
            </div>
          </div>
        </div>

        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
          {/* Status Filter - Multi-select */}
          <div>
            <div className="flex items-center gap-x-5">
              <Label htmlFor="status-filter">Assessment Status</Label>
              <TooltipProvider delayDuration={100}>
                <Tooltip>
                  <TooltipTrigger className="cursor-pointer">
                    <Info className="w-4 h-4" />
                  </TooltipTrigger>
                  <TooltipContent className="bg-slate-700 border-slate-600 text-gray-100">
                    <p>Filter for candidates who have an assessment with the selected status(es)</p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
            </div>
            <div className="mt-2 space-y-2">
              {(['INVITED', 'STARTED', 'COMPLETED', 'EVALUATED'] as AttemptStatus[]).map((status) => (
                <label key={status} className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    checked={selectedStatuses.includes(status)}
                    onChange={(e) => handleStatusChange(status, e.target.checked)}
                    className="rounded border-slate-600 bg-slate-700 text-blue-600 focus:ring-blue-500"
                  />
                  <span className="text-sm text-gray-200 capitalize">
                    {status.toLowerCase().replace('_', ' ')}
                  </span>
                </label>
              ))}
            </div>
          </div>

          {/* Date Range Filter */}
          <div>
            <div className="flex items-center gap-x-5">
              <Label htmlFor="date-filter">Date Added</Label>
            <TooltipProvider delayDuration={100}>
                <Tooltip>
                  <TooltipTrigger className="cursor-pointer">
                    <Info className="w-4 h-4" />
                  </TooltipTrigger>
                  <TooltipContent className="bg-slate-700 border-slate-600 text-gray-100">
                    <p>Filter for candidates who were added within the selected date range</p>
                  </TooltipContent>
                </Tooltip>
              </TooltipProvider>
            </div>
            <div className="mt-2 space-y-2">
                <DateRangePicker
                  onStartDateChange={(date) => setDateFrom(date?.toISOString() || "")}
                  onEndDateChange={(date) => setDateTo(date?.toISOString() || "")}
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
            <span className="text-gray-400">Loading candidates...</span>
          </div>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-md p-4">
          <div className="flex">
            <div className="ml-3">
              <h3 className="text-sm font-medium text-red-800">
                Error loading candidates
              </h3>
              <div className="mt-2 text-sm text-red-700">
                <p>{error.message}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Candidate List */}
      {!isLoading && !error && (
        <div className="space-y-4">
          {filteredCandidates.length === 0 ? (
            <div className="bg-slate-800 border border-slate-700 rounded-md p-8 text-center">
              <p className="text-gray-400">No candidates found matching your search</p>
            </div>
          ) : (
            filteredCandidates.map((candidate: any) => (
              <CandidateCard
                key={candidate.id}
                id={candidate.id}
                name={candidate.fullName}
                email={candidate.email}
              />
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
    </AppShell>
  );
}
