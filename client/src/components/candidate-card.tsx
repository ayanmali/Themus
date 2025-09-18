import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Link } from "wouter";
import { MoreHorizontal } from "lucide-react";
import { DropdownMenu, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem, DropdownMenuLabel, DropdownMenuSeparator, DropdownMenuTrigger } from "./ui/dropdown-menu";

interface CandidateCardProps {
  id: number;
  name: string;
  email: string;
  // profileImage?: string;
  // currentAssessment?: string;
  // assessmentStatus?: string;
  // completionDate?: string;
  // daysRemaining?: number;
  // skills?: string[];
}

export function CandidateCard({
  id,
  name,
  email,
  // profileImage,
  // currentAssessment,
  // assessmentStatus,
  // completionDate,
  // daysRemaining,
  // skills = [],
}: CandidateCardProps) {
  const getStatusColor = (status?: string) => {
    if (!status) return "bg-gray-100 text-gray-800";

    switch (status.toLowerCase()) {
      case "completed":
        return "bg-green-100 text-green-800";
      case "in progress":
      case "in_progress":
        return "bg-yellow-100 text-yellow-800";
      case "not started":
      case "not_started":
      case "available":
        return "bg-gray-100 text-gray-800";
      default:
        return "bg-gray-100 text-gray-800";
    }
  };

  // const formattedStatus = assessmentStatus === "in_progress" 
  //   ? "In Progress" 
  //   : assessmentStatus === "not_started" 
  //     ? "Not Started" 
  //     : assessmentStatus;

  return (
    <div className="px-4 py-4 sm:px-6 bg-slate-700 shadow rounded-lg border border-slate-600">
      <Link to={`/candidates/${id}`}>
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <img
              className="h-10 w-10 rounded-full"
              src={`https://ui-avatars.com/api/?name=${name}&background=random`}
              alt={name}
            />
            <div className="ml-4">
              <div className="text-lg font-medium text-gray-100">{name}</div>
              <div className="text-sm text-gray-100">{email}</div>
            </div>
          </div>
          <div className="flex space-x-2">
            <Button size="sm">
              Assign
            </Button>
          </div>
          <div>

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
                    Item 1
                  </DropdownMenuItem>
                  <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white">
                    Item 2
                  </DropdownMenuItem>
                  <DropdownMenuItem className="hover:bg-slate-700 transition-colors hover:text-white" onClick={() => window.open(assessment.githubRepositoryLink, '_blank')}>
                    Item 3
                  </DropdownMenuItem>
                  <DropdownMenuSeparator className="bg-slate-700" />
                  <DropdownMenuItem className="hover:bg-slate-700 text-red-400 transition-colors hover:text-white">
                    Delete Candidate
                  </DropdownMenuItem>
                </DropdownMenuGroup>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        </div>


        {/* <div className="mt-5">
        {currentAssessment ? (
          <div className="text-sm text-gray-100">
            <span className="font-medium">Current Assessment:</span> {currentAssessment}
          </div>
        ) : (
          <div className="text-sm text-gray-100">
            <span className="font-medium">No current assessment</span>
          </div>
        )}
        
        <div className="mt-1 flex items-center">
          <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(assessmentStatus)}`}>
            {formattedStatus}
          </span>
          
          {assessmentStatus === "completed" && completionDate && (
            <span className="ml-2 text-sm text-gray-100">Completed on {completionDate}</span>
          )}
          
          {assessmentStatus === "in progress" && daysRemaining !== undefined && (
            <span className="ml-2 text-sm text-gray-100">{daysRemaining} days remaining</span>
          )}
          
          {assessmentStatus === "not started" && (
            <span className="ml-2 text-sm text-gray-100">Not yet started</span>
          )}
          
          {assessmentStatus === "available" && (
            <span className="ml-2 text-sm text-gray-100">Ready for assignment</span>
          )}
        </div>
      </div> */}

        {/* {skills.length > 0 && (
        <div className="mt-2">
          {skills.map((skill, index) => (
            <Badge 
              key={index} 
              variant="secondary" 
              className="mr-2 mb-1"
            >
              {skill}
            </Badge>
          ))}
        </div>
      )} */}
      </Link>
    </div>
  );
}
