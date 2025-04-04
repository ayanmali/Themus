import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "wouter";
import { AppShell } from "@/components/layout/app-shell";
import { Button } from "@/components/ui/button";
import { Plus, Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { AssessmentCard } from "@/components/assessment-card";
import { Skeleton } from "@/components/ui/skeleton";

interface Assessment {
  id: number;
  title: string;
  description: string;
  status: string;
  durationDays: number;
  candidateAssessments?: any[];
}

export default function EmployerAssessments() {
  const [statusFilter, setStatusFilter] = useState<string>("all");
  const [roleFilter, setRoleFilter] = useState<string>("all");
  const [searchQuery, setSearchQuery] = useState<string>("");

  const { data: assessments, isLoading, error } = useQuery<Assessment[]>({
    queryKey: ["/api/assessments"],
  });

  // Filter assessments based on selected filters
  const filteredAssessments = assessments?.filter(assessment => {
    // Apply status filter
    if (statusFilter !== "all" && assessment.status !== statusFilter) {
      return false;
    }

    // Apply search query
    if (searchQuery && !assessment.title.toLowerCase().includes(searchQuery.toLowerCase())) {
      return false;
    }

    return true;
  }) || [];

  return (
    <AppShell title="Assessments">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg leading-6 font-medium text-gray-900">All Assessments</h2>
        <Button asChild>
          <Link href="/employer/create-assessment">
            <Plus className="-ml-1 mr-2 h-5 w-5" />
            Create Assessment
          </Link>
        </Button>
      </div>
      
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
        <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <div>
            <Label htmlFor="status">Status</Label>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger id="status" className="mt-1">
                <SelectValue placeholder="All Statuses" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value="active">Active</SelectItem>
                <SelectItem value="completed">Completed</SelectItem>
                <SelectItem value="draft">Draft</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label htmlFor="job-role">Job Role</Label>
            <Select value={roleFilter} onValueChange={setRoleFilter}>
              <SelectTrigger id="job-role" className="mt-1">
                <SelectValue placeholder="All Roles" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Roles</SelectItem>
                <SelectItem value="frontend">Frontend Developer</SelectItem>
                <SelectItem value="backend">Backend Developer</SelectItem>
                <SelectItem value="devops">DevOps Engineer</SelectItem>
                <SelectItem value="fullstack">Full Stack Developer</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <div>
            <Label htmlFor="daterange">Date Range</Label>
            <Select defaultValue="all">
              <SelectTrigger id="daterange" className="mt-1">
                <SelectValue placeholder="All Time" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Time</SelectItem>
                <SelectItem value="7days">Last 7 Days</SelectItem>
                <SelectItem value="30days">Last 30 Days</SelectItem>
                <SelectItem value="90days">Last 90 Days</SelectItem>
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
      ) : filteredAssessments.length === 0 ? (
        <div className="bg-white shadow rounded-md p-8 text-center">
          <p className="text-gray-500">No assessments found matching your filters</p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredAssessments.map(assessment => (
            <AssessmentCard
              key={assessment.id}
              id={assessment.id}
              title={assessment.title}
              description={assessment.description || ""}
              status={assessment.status}
              durationDays={assessment.durationDays}
              candidateCount={assessment.candidateAssessments?.length || 0}
              completedCount={assessment.candidateAssessments?.filter(ca => ca.status === "completed").length || 0}
              inProgressCount={assessment.candidateAssessments?.filter(ca => ca.status === "in_progress").length || 0}
              isEmployerView={true}
            />
          ))}
        </div>
      )}
    </AppShell>
  );
}
