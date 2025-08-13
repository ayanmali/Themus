import { useState } from "react";
import { AppShell } from "@/components/layout/app-shell";
import { Plus, Search, Loader2 } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { CandidateCard } from "@/components/candidate-card";
import { Candidate } from "@/lib/types/candidate";
import { useQuery } from "@tanstack/react-query";
import useApi from "@/hooks/use-api";
import { Button } from "@/components/ui/button";
import { Link } from "wouter";

export default function EmployerCandidates() {
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const { apiCall } = useApi();

  // Fetch candidates using TanStack Query
  const { data: candidatesData, isLoading, error } = useQuery({
    queryKey: ['candidates', 'user', page, size],
    queryFn: async () => {
      const response = await apiCall(`/api/candidates/filter?page=${page}&size=${size}`, {
        method: 'GET',
      });
      
      if (!response) {
        throw new Error('Failed to fetch candidates');
      }
      
      return response;
    },
  });

  // Extract candidates from the response
  const candidates = candidatesData?.content || [];
  const totalElements = candidatesData?.totalElements || 0;
  const totalPages = candidatesData?.totalPages || 0;

  // Filter candidates based on search query
  const filteredCandidates = candidates.filter((candidate: any) => {
    if (searchQuery === "") return true;

    return (
      candidate.name?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      candidate.email?.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  return (
    <AppShell>
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="serif-heading">Candidates</h1>
          <p className="text-gray-400 flex items-center space-x-2">
            {/* <Calendar className="w-4 h-4" /> */}
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
              {/* <button className="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                <Eye className="w-4 h-4" />
                <span>View All</span>
              </button> */}
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
        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div>
            <Label htmlFor="assessment-filter">Assessment</Label>
            <Select defaultValue="all">
              <SelectTrigger id="assessment-filter" className="mt-1 bg-slate-700 border-slate-600 focus:ring-slate-500 focus:border-slate-500">
                <SelectValue placeholder="All Assessments" />
              </SelectTrigger>
              <SelectContent className="bg-slate-700 border-slate-600 text-gray-100">
                <SelectItem value="all">All Assessments</SelectItem>
                <SelectItem value="frontend">Frontend Developer Assessment</SelectItem>
                <SelectItem value="backend">Backend Developer Assessment</SelectItem>
                <SelectItem value="devops">DevOps Engineer Assessment</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label htmlFor="status-filter">Status</Label>
            <Select defaultValue="all">
              <SelectTrigger id="status-filter" className="mt-1 bg-slate-700 border-slate-600 focus:ring-slate-500 focus:border-slate-500">
                <SelectValue placeholder="All Statuses" />
              </SelectTrigger>
              <SelectContent className="bg-slate-700 border-slate-600 text-gray-100">
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value="in_progress">In Progress</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="not_started">Not Started</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label htmlFor="date-filter">Date Added</Label>
            <Select defaultValue="all">
              <SelectTrigger id="date-filter" className="mt-1 bg-slate-700 border-slate-600 focus:ring-slate-500 focus:border-slate-500">
                <SelectValue placeholder="All Time" />
              </SelectTrigger>
              <SelectContent className="bg-slate-700 border-slate-600 text-gray-100">
                <SelectItem value="all">All Time</SelectItem>
                <SelectItem value="7days">Last 7 Days</SelectItem>
                <SelectItem value="30days">Last 30 Days</SelectItem>
                <SelectItem value="90days">Last 90 Days</SelectItem>
              </SelectContent>
            </Select>
          </div>
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
                name={candidate.name}
                email={candidate.email}
              // currentAssessment={candidate.currentAssessment}
              // assessmentStatus={candidate.assessmentStatus}
              // completionDate={candidate.completionDate}
              // daysRemaining={candidate.daysRemaining}
              // skills={candidate.skills}
              />
            ))
          )}
        </div>
      )}
    </AppShell>
  );
}
