// import { useState } from "react";
// import { useForm } from "react-hook-form";
// import { zodResolver } from "@hookform/resolvers/zod";
// import { z } from "zod";
// import { insertAssessmentSchema } from "@shared/schema";
// import { useQuery, useMutation } from "@tanstack/react-query";
// import { apiRequest, queryClient } from "@/lib/queryClient";
// import { useToast } from "@/hooks/use-toast";
// import { useLocation } from "wouter";

import { AppShell } from "@/components/layout/app-shell";
import { AnimatedAIChat } from "@/components/new-assessment/animated-ai-chat";
import { useAuth } from "@/hooks/use-auth";
import { navigate } from "wouter/use-browser-location";

// import { AppShell } from "@/components/layout/app-shell";
// import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
// import { Input } from "@/components/ui/input";
// import { Textarea } from "@/components/ui/textarea";
// import { Button } from "@/components/ui/button";
// import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
// import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
// import { Loader2 } from "lucide-react";

// // Create a schema for the assessment creation form
// const createAssessmentSchema = insertAssessmentSchema.pick({
//   title: true,
//   description: true,
//   repositoryId: true,
//   durationDays: true,
//   status: true,
// }).extend({
//   durationDays: z.coerce.number().min(1, { message: "Duration must be at least 1 day" }),
// });

// type CreateAssessmentFormValues = z.infer<typeof createAssessmentSchema>;

// export default function CreateAssessment() {
//   const { toast } = useToast();
//   const [, navigate] = useLocation();

//   // Fetch repositories for the select input
//   const { data: repositories, isLoading: isLoadingRepositories } = useQuery({
//     queryKey: ["/api/repositories"],
//   });

//   // Setup form
//   const form = useForm<CreateAssessmentFormValues>({
//     resolver: zodResolver(createAssessmentSchema),
//     defaultValues: {
//       title: "",
//       description: "",
//       durationDays: 5,
//       status: "draft",
//     },
//   });

//   // Create assessment mutation
//   const createAssessmentMutation = useMutation({
//     mutationFn: async (data: CreateAssessmentFormValues) => {
//       const res = await apiRequest("POST", "/api/assessments", data);
//       return await res.json();
//     },
//     onSuccess: () => {
//       queryClient.invalidateQueries({ queryKey: ["/api/assessments"] });
//       toast({
//         title: "Assessment created",
//         description: "Your assessment has been created successfully",
//       });
//       navigate("/employer/assessments");
//     },
//     onError: (error: Error) => {
//       toast({
//         title: "Failed to create assessment",
//         description: error.message,
//         variant: "destructive",
//       });
//     },
//   });

//   const onSubmit = (data: CreateAssessmentFormValues) => {
//     createAssessmentMutation.mutate(data);
//   };

//   return (
//     <AppShell title="Create Assessment">
//       <div className="max-w-3xl mx-auto">
//         <Card>
//           <CardHeader>
//             <CardTitle className="text-2xl">Create New Assessment</CardTitle>
//             <CardDescription>
//               Define the details of the coding assessment that will be assigned to candidates.
//             </CardDescription>
//           </CardHeader>
//           <CardContent>
//             <Form {...form}>
//               <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6">
//                 <FormField
//                   control={form.control}
//                   name="title"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Assessment Title</FormLabel>
//                       <FormControl>
//                         <Input placeholder="e.g., Frontend Developer Assessment" {...field} />
//                       </FormControl>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />

//                 <FormField
//                   control={form.control}
//                   name="description"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Description</FormLabel>
//                       <FormControl>
//                         <Textarea 
//                           placeholder="Describe what the candidate needs to do in this assessment"
//                           {...field}
//                           rows={5}
//                           value={field.value ?? ''}
//                         />
//                       </FormControl>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />

//                 <FormField
//                   control={form.control}
//                   name="repositoryId"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Repository</FormLabel>
//                       <Select
//                         onValueChange={(value) => field.onChange(parseInt(value))}
//                         value={field.value?.toString()}
//                       >
//                         <FormControl>
//                           <SelectTrigger>
//                             <SelectValue placeholder="Select a repository for this assessment" />
//                           </SelectTrigger>
//                         </FormControl>
//                         <SelectContent>
//                           {isLoadingRepositories ? (
//                             <div className="flex items-center justify-center p-4">
//                               <Loader2 className="h-5 w-5 animate-spin text-primary" />
//                             </div>
//                           ) : repositories && repositories.length > 0 ? (
//                             repositories.map((repo: any) => (
//                               <SelectItem key={repo.id} value={repo.id.toString()}>
//                                 {repo.name}
//                               </SelectItem>
//                             ))
//                           ) : (
//                             <div className="p-2 text-center text-sm text-gray-500">
//                               No repositories found. Please add a repository first.
//                             </div>
//                           )}
//                         </SelectContent>
//                       </Select>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />

//                 <FormField
//                   control={form.control}
//                   name="durationDays"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Duration (days)</FormLabel>
//                       <FormControl>
//                         <Input type="number" min={1} {...field} />
//                       </FormControl>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />

//                 <FormField
//                   control={form.control}
//                   name="status"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Status</FormLabel>
//                       <Select
//                         onValueChange={field.onChange}
//                         defaultValue={field.value}
//                       >
//                         <FormControl>
//                           <SelectTrigger>
//                             <SelectValue placeholder="Select assessment status" />
//                           </SelectTrigger>
//                         </FormControl>
//                         <SelectContent>
//                           <SelectItem value="draft">Draft</SelectItem>
//                           <SelectItem value="active">Active</SelectItem>
//                         </SelectContent>
//                       </Select>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />

//                 <div className="flex justify-end space-x-3">
//                   <Button
//                     type="button"
//                     variant="outline"
//                     onClick={() => navigate("/employer/assessments")}
//                   >
//                     Cancel
//                   </Button>
//                   <Button 
//                     type="submit" 
//                     disabled={createAssessmentMutation.isPending || isLoadingRepositories || !repositories?.length}
//                   >
//                     {createAssessmentMutation.isPending ? "Creating..." : "Create Assessment"}
//                   </Button>
//                 </div>
//               </form>
//             </Form>
//           </CardContent>
//         </Card>
//       </div>
//     </AppShell>
//   );
// }

export default function CreateAssessment() {
  const { isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    navigate("/login");
  }
  
  return (
    <AnimatedAIChat />
  );
}