import { useState } from "react";
import { AppShell } from "@/components/layout/app-shell";
import { Button } from "@/components/ui/button";
import { Calendar, Eye, Plus, Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { CandidateCard } from "@/components/candidate-card";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuPortal, DropdownMenuShortcut, DropdownMenuSub, DropdownMenuSubContent, DropdownMenuSubTrigger, DropdownMenuTrigger } from "@/components/ui/dropdown-menu";
import { Candidate } from "@/lib/types/candidate";
import { navigate } from "wouter/use-browser-location";
import { useAuth } from "@/hooks/use-auth";

export default function EmployerCandidates() {
  const [searchQuery, setSearchQuery] = useState("");

  // Mock candidates for demonstration
  const candidates: Candidate[] = [
    {
      id: 1,
      name: "John Doe",
      email: "john.doe@example.com",
      //profileImage: "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80",
      // currentAssessment: "Frontend Developer Assessment",
      // assessmentStatus: "completed",
      // completionDate: "Jan 15, 2023",
      // skills: ["React", "JavaScript", "Frontend"]
    },
    {
      id: 2,
      name: "Alice Johnson",
      email: "alice.johnson@example.com",
      //profileImage: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80",
      // currentAssessment: "Backend Developer Assessment",
      // assessmentStatus: "in_progress",
      // daysRemaining: 2,
      // skills: ["Node.js", "Express", "Backend"]
    },
    {
      id: 3,
      name: "Michael Smith",
      email: "michael.smith@example.com",
      //profileImage: "https://images.unsplash.com/photo-1519244703995-f4e0f30006d5?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80",
      // assessmentStatus: "available",
      // skills: ["DevOps", "Docker", "CI/CD"]
    }
  ];

  // Filter candidates based on search query
  const filteredCandidates = candidates.filter(candidate => {
    if (searchQuery === "") return true;

    return (
      candidate.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      candidate.email.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  return (
    <AppShell title="Candidates">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Candidates</h1>
          <p className="text-gray-400 flex items-center space-x-2">
            {/* <Calendar className="w-4 h-4" /> */}
            <span>Add, view, and manage your candidates</span>
          </p>
        </div>
        <div className="flex space-x-3">
              <button className="bg-slate-700 hover:bg-blue-700 text-gray-100 text-sm px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                <Plus className="w-4 h-4" />
                <span>Add</span>
              </button>
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

      {/* Candidate List */}
      <div className="space-y-4">
        {filteredCandidates.length === 0 ? (
          <div className="bg-white shadow rounded-md p-8 text-center">
            <p className="text-gray-500">No candidates found matching your search</p>
          </div>
        ) : (
          filteredCandidates.map(candidate => (
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
    </AppShell>
  );
}
