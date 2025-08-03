import { useState } from "react";
import { useAuth } from "@/contexts/AuthContext";
import { AppShell } from "@/components/layout/app-shell";
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useToast } from "@/hooks/use-toast";
import { useQuery } from "@tanstack/react-query";

// Mock data for skills and assessment history
// In a real implementation, these would come from API endpoints
const candidateSkills = [
  "JavaScript", "React", "Node.js", "TypeScript", "Express", "Git"
];

interface AssessmentHistoryItem {
  id: number;
  title: string;
  company: string;
  status: string;
  completedDate?: string;
  daysRemaining?: number;
}

const assessmentHistory: AssessmentHistoryItem[] = [
  {
    id: 1,
    title: "Backend Developer Assessment",
    company: "XYZ Solutions",
    status: "completed",
    completedDate: "Jan 5, 2023"
  },
  {
    id: 2,
    title: "Frontend Developer Assessment",
    company: "ABC Technologies",
    status: "in_progress",
    daysRemaining: 2
  },
  {
    id: 3,
    title: "Full Stack Developer Assessment",
    company: "TechStart Inc.",
    status: "not_started"
  }
];

// Profile update schema
const profileUpdateSchema = z.object({
  name: z.string().min(1, "Full name is required"),
  email: z.string().email("Invalid email address"),
  phone: z.string().optional(),
  location: z.string().optional(),
  githubProfile: z.string().url("Must be a valid URL").optional().or(z.literal("")),
});

type ProfileUpdateFormValues = z.infer<typeof profileUpdateSchema>;

export default function CandidateProfile() {
  const { user } = useAuth();
  const { toast } = useToast();
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  // Fetch candidate's assessments for history
  const { data: assessments } = useQuery({
    queryKey: ["/api/assessments"],
  });

  // Setup profile update form
  const form = useForm<ProfileUpdateFormValues>({
    resolver: zodResolver(profileUpdateSchema),
    defaultValues: {
      name: user?.name || "",
      email: user?.email || "",
      phone: "",
      location: "",
      githubProfile: "",
    },
  });

  const onSubmit = (data: ProfileUpdateFormValues) => {
    // In a real implementation, this would be a mutation to update the user profile
    console.log("Profile update data:", data);
    toast({
      title: "Profile updated",
      description: "Your profile has been updated successfully",
    });
    setIsDialogOpen(false);
  };

  return (
    <AppShell title="Profile">
      <div className="max-w-4xl mx-auto">
        {/* Profile Card */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-2xl">Candidate Profile</CardTitle>
              <CardDescription>Personal details and skills</CardDescription>
            </div>
            <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
              <DialogTrigger asChild>
                <Button>Edit Profile</Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>Edit Profile</DialogTitle>
                  <DialogDescription>
                    Update your profile information
                  </DialogDescription>
                </DialogHeader>
                <Form {...form}>
                  <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
                    <FormField
                      control={form.control}
                      name="name"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Full Name</FormLabel>
                          <FormControl>
                            <Input {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="email"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Email</FormLabel>
                          <FormControl>
                            <Input type="email" {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="phone"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Phone</FormLabel>
                          <FormControl>
                            <Input {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="location"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>Location</FormLabel>
                          <FormControl>
                            <Input {...field} />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <FormField
                      control={form.control}
                      name="githubProfile"
                      render={({ field }) => (
                        <FormItem>
                          <FormLabel>GitHub Profile</FormLabel>
                          <FormControl>
                            <Input {...field} placeholder="https://github.com/username" />
                          </FormControl>
                          <FormMessage />
                        </FormItem>
                      )}
                    />
                    <DialogFooter>
                      <Button type="submit">Save Changes</Button>
                    </DialogFooter>
                  </form>
                </Form>
              </DialogContent>
            </Dialog>
          </CardHeader>
          <CardContent>
            <dl>
              <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6 rounded-t-md">
                <dt className="text-sm font-medium text-gray-500">Full name</dt>
                <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{user?.name}</dd>
              </div>
              <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                <dt className="text-sm font-medium text-gray-500">Email address</dt>
                <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">{user?.email}</dd>
              </div>
              <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                <dt className="text-sm font-medium text-gray-500">Phone</dt>
                <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">(555) 123-4567</dd>
              </div>
              <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                <dt className="text-sm font-medium text-gray-500">Location</dt>
                <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">San Francisco, CA</dd>
              </div>
              <div className="bg-gray-50 px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
                <dt className="text-sm font-medium text-gray-500">GitHub Profile</dt>
                <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                  <a href="#" className="text-primary hover:underline">github.com/johndoe</a>
                </dd>
              </div>
              <div className="bg-white px-4 py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6 rounded-b-md">
                <dt className="text-sm font-medium text-gray-500">Skills</dt>
                <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                  <div className="flex flex-wrap gap-2">
                    {candidateSkills.map((skill, index) => (
                      <Badge key={index} variant="secondary">
                        {skill}
                      </Badge>
                    ))}
                  </div>
                </dd>
              </div>
            </dl>
          </CardContent>
        </Card>

        {/* Assessment History */}
        <Card className="mt-8">
          <CardHeader>
            <CardTitle>Assessment History</CardTitle>
            <CardDescription>Your past and current assessments</CardDescription>
          </CardHeader>
          <CardContent>
            <ul className="divide-y divide-gray-200">
              {assessmentHistory.map((item) => (
                <li key={item.id} className="px-4 py-4">
                  <div className="flex justify-between">
                    <div>
                      <h4 className="text-md font-medium text-primary">{item.title}</h4>
                      <p className="text-sm text-gray-500">{item.company}</p>
                    </div>
                    <Badge
                      variant={
                        item.status === "completed"
                          ? "success"
                          : item.status === "in_progress"
                          ? "warning"
                          : "outline"
                      }
                    >
                      {item.status === "in_progress"
                        ? "In Progress"
                        : item.status === "not_started"
                        ? "Not Started"
                        : "Completed"}
                    </Badge>
                  </div>
                  <div className="mt-2 text-sm text-gray-500">
                    {item.status === "completed" && (
                      <p>Completed on {item.completedDate}</p>
                    )}
                    {item.status === "in_progress" && (
                      <p>{item.daysRemaining} days remaining</p>
                    )}
                    {item.status === "not_started" && (
                      <p>Available to start</p>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          </CardContent>
        </Card>
      </div>
    </AppShell>
  );
}
