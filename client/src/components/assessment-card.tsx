import { Link } from "wouter";
import { cn } from "@/lib/utils";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Clock, Users, GitPullRequest, CheckCircle, AlertCircle } from "lucide-react";

interface AssessmentCardProps {
  id: number;
  title: string;
  description: string;
  status: string;
  durationDays: number;
  candidateCount?: number;
  completedCount?: number;
  inProgressCount?: number;
  assignedDate?: Date;
  dueDate?: Date;
  progress?: number;
  pullRequestUrl?: string | null;
  isEmployerView?: boolean;
  className?: string;
}

export function AssessmentCard({
  id,
  title,
  description,
  status,
  durationDays,
  candidateCount,
  completedCount,
  inProgressCount,
  assignedDate,
  dueDate,
  progress = 0,
  pullRequestUrl,
  isEmployerView = true,
  className,
}: AssessmentCardProps) {
  // Status badge color
  const getBadgeVariant = (status: string) => {
    switch (status.toLowerCase()) {
      case "active":
      case "in_progress":
        return "warning";
      case "completed":
        return "success";
      case "draft":
        return "secondary";
      case "not_started":
        return "outline";
      default:
        return "default";
    }
  };

  const formattedStatus = 
    status === "in_progress" 
      ? "In Progress" 
      : status === "not_started"
        ? "Not Started"
        : status.charAt(0).toUpperCase() + status.slice(1);

  // Calculate days remaining if assigned date and due date are provided
  const daysRemaining = dueDate && assignedDate
    ? Math.ceil((dueDate.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24))
    : null;

  return (
    <Card className={cn("overflow-hidden", className)}>
      <CardContent className="p-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-primary truncate">{title}</h3>
            {!isEmployerView && <p className="mt-1 text-sm text-gray-500">ABC Technologies</p>}
          </div>
          <Badge variant={getBadgeVariant(status)}>
            {formattedStatus}
          </Badge>
        </div>

        {/* Details for assessment */}
        <div className="mt-4 sm:flex sm:justify-between">
          <div className="sm:flex items-center">
            <p className="flex items-center text-sm text-gray-500 mr-6">
              <Clock className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
              <span>{durationDays} days duration</span>
            </p>
            
            {isEmployerView && candidateCount !== undefined && (
              <p className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
                <Users className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
                <span>
                  {candidateCount} candidate{candidateCount !== 1 ? 's' : ''} assigned
                </span>
              </p>
            )}
            
            {!isEmployerView && daysRemaining !== null && (
              <p className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
                {daysRemaining > 0 ? (
                  <>
                    <Clock className="flex-shrink-0 mr-1.5 h-5 w-5 text-accent" />
                    <span>{daysRemaining} days remaining</span>
                  </>
                ) : (
                  <>
                    <AlertCircle className="flex-shrink-0 mr-1.5 h-5 w-5 text-destructive" />
                    <span>Deadline passed</span>
                  </>
                )}
              </p>
            )}
          </div>
          
          {isEmployerView && completedCount !== undefined && inProgressCount !== undefined && (
            <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
              <p>
                <span className="font-medium text-gray-900">{completedCount}</span> completed, 
                <span className="font-medium text-gray-900 ml-1">{inProgressCount}</span> in progress
              </p>
            </div>
          )}
          
          {!isEmployerView && pullRequestUrl && (
            <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
              <GitPullRequest className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
              <span>PR submitted</span>
            </div>
          )}
        </div>
        
        {/* Description */}
        <div className="mt-2">
          <p className="text-sm text-gray-600 line-clamp-2">{description}</p>
        </div>
        
        {/* Progress bar for candidate view */}
        {!isEmployerView && (
          <div className="mt-4">
            <div className="w-full bg-gray-200 rounded-full h-2.5">
              <div 
                className={cn("h-2.5 rounded-full", 
                  status === "completed" ? "bg-green-600" : "bg-primary"
                )} 
                style={{ width: `${progress}%` }}
              ></div>
            </div>
            <p className="mt-1 text-xs text-gray-500">Progress: {progress}%</p>
          </div>
        )}
      </CardContent>
      
      <CardFooter className="px-4 py-3 bg-gray-50 flex justify-between">
        {isEmployerView ? (
          <div className="flex space-x-2">
            <Button size="sm" asChild>
              <Link href={`/employer/assessments/${id}`}>
                Edit
              </Link>
            </Button>
            <Button size="sm" variant="outline" asChild>
              <Link href={`/employer/assessments/${id}/results`}>
                View Results
              </Link>
            </Button>
          </div>
        ) : (
          <div>
            {status === "not_started" ? (
              <Button size="sm">
                Start Assessment
              </Button>
            ) : status === "in_progress" ? (
              <Button size="sm">
                Continue Assessment
              </Button>
            ) : (
              <Button size="sm" variant="outline">
                View Submission
              </Button>
            )}
          </div>
        )}
      </CardFooter>
    </Card>
  );
}
