import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { AppShell } from "@/components/layout/app-shell";
import { AssessmentCard } from "@/components/assessment-card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Skeleton } from "@/components/ui/skeleton";
import { Search } from "lucide-react";

interface CandidateAssessment {
  id: number;
  title: string;
  description: string;
  status: string;
  durationDays: number;
  candidateAssessment: {
    id: number;
    startDate: string | null;
    dueDate: string | null;
    status: string;
    progress: number;
    pullRequestUrl: string | null;
  };
}

export default function CandidateAssessments() {
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [activeTab, setActiveTab] = useState<string>("all");

  // Fetch candidate's assessments
  const { data: assessments, isLoading, error } = useQuery<CandidateAssessment[]>({
    queryKey: ["/api/assessments"],
  });

  // Filter assessments based on search query and status filter
  const filteredAssessments = assessments?.filter(assessment => {
    // Apply search filter
    if (
      searchQuery && 
      !assessment.title.toLowerCase().includes(searchQuery.toLowerCase()) &&
      !assessment.description.toLowerCase().includes(searchQuery.toLowerCase())
    ) {
      return false;
    }

    // Apply status filter
    if (statusFilter !== "all" && assessment.candidateAssessment.status !== statusFilter) {
      return false;
    }

    // Apply tab filter
    if (activeTab === "in_progress" && assessment.candidateAssessment.status !== "in_progress") {
      return false;
    } else if (activeTab === "completed" && assessment.candidateAssessment.status !== "completed") {
      return false;
    } else if (activeTab === "not_started" && assessment.candidateAssessment.status !== "not_started") {
      return false;
    }

    return true;
  });

  return (
    <AppShell title="My Assessments">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg leading-6 font-medium text-gray-900">My Assessments</h2>
      </div>

      {/* Tabs for quick filtering */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="mb-6">
        <TabsList>
          <TabsTrigger value="all">All</TabsTrigger>
          <TabsTrigger value="in_progress">In Progress</TabsTrigger>
          <TabsTrigger value="not_started">Not Started</TabsTrigger>
          <TabsTrigger value="completed">Completed</TabsTrigger>
        </TabsList>
      </Tabs>

      {/* Filters */}
      <div className="bg-white shadow px-4 py-5 sm:rounded-lg sm:p-6 mb-6">
        <div className="md:flex md:items-center md:justify-between">
          <div className="flex-1 min-w-0">
            <h2 className="text-lg leading-6 font-medium text-gray-900">Filters</h2>
          </div>
          <div className="mt-4 flex md:mt-0 md:ml-4">
            <div className="flex items-center">
              <label htmlFor="search" className="sr-only">Search Assessments</label>
              <div className="relative rounded-md shadow-sm">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Search className="h-5 w-5 text-gray-400" />
                </div>
                <Input
                  type="search"
                  id="search"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-10"
                  placeholder="Search assessments..."
                />
              </div>
            </div>
          </div>
        </div>
        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
          <div>
            <Label htmlFor="status">Status</Label>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger id="status" className="mt-1">
                <SelectValue placeholder="All Statuses" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value="not_started">Not Started</SelectItem>
                <SelectItem value="in_progress">In Progress</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </div>
      </div>

      {/* Assessment List */}
      {isLoading ? (
        <div className="space-y-4">
          {[1, 2, 3].map(i => (
            <div key={i} className="bg-white shadow rounded-md p-6">
              <div className="flex justify-between items-start">
                <div className="space-y-2">
                  <Skeleton className="h-6 w-48" />
                  <Skeleton className="h-4 w-32" />
                </div>
                <Skeleton className="h-8 w-20 rounded-full" />
              </div>
              <div className="mt-4 space-y-2">
                <Skeleton className="h-4 w-full" />
                <Skeleton className="h-4 w-3/4" />
              </div>
            </div>
          ))}
        </div>
      ) : error ? (
        <div className="bg-red-50 p-4 rounded-md">
          <p className="text-red-800">Error loading assessments</p>
        </div>
      ) : filteredAssessments && filteredAssessments.length > 0 ? (
        <div className="space-y-4">
          {filteredAssessments.map(assessment => {
            const ca = assessment.candidateAssessment;
            
            // Calculate proper dates if they exist
            const assignedDate = ca.startDate ? new Date(ca.startDate) : undefined;
            const dueDate = ca.dueDate ? new Date(ca.dueDate) : undefined;
            
            return (
              <AssessmentCard
                key={assessment.id}
                id={assessment.id}
                title={assessment.title}
                description={assessment.description || ""}
                status={ca.status}
                durationDays={assessment.durationDays}
                progress={ca.progress || 0}
                pullRequestUrl={ca.pullRequestUrl}
                assignedDate={assignedDate}
                dueDate={dueDate}
                isEmployerView={false}
              />
            );
          })}
        </div>
      ) : (
        <div className="bg-white shadow rounded-md p-8 text-center">
          <p className="text-gray-500">No assessments found matching your criteria</p>
        </div>
      )}
    </AppShell>
  );
}
