// import { useState } from "react";
// import { useQuery } from "@tanstack/react-query";
// import { AppShell } from "@/components/layout/app-shell";
// import { Button } from "@/components/ui/button";
// import { Plus } from "lucide-react";
// import { Input } from "@/components/ui/input";
// import { Skeleton } from "@/components/ui/skeleton";
// import { RepositoryCard } from "@/components/repository-card";
// import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog";
// import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
// import { useForm } from "react-hook-form";
// import { z } from "zod";
// import { zodResolver } from "@hookform/resolvers/zod";
// import { useMutation } from "@tanstack/react-query";
// import { apiRequest, queryClient } from "@/lib/queryClient";
// import { useToast } from "@/hooks/use-toast";
// import { insertRepositorySchema } from "@shared/schema";
// import { Textarea } from "@/components/ui/textarea";

// const repositorySchema = insertRepositorySchema.pick({
//   name: true,
//   url: true,
//   description: true,
//   tags: true,
// }).extend({
//   tags: z.string().optional().transform(val => 
//     val ? val.split(',').map(tag => tag.trim()) : []
//   ),
// });

// type CreateRepositoryInput = z.infer<typeof repositorySchema>;

// interface Repository {
//   id: number;
//   name: string;
//   url: string;
//   description: string | null;
//   tags: string[];
//   employerId: number;
//   createdAt: string;
// }

// export default function EmployerRepositories() {
//   const [isDialogOpen, setIsDialogOpen] = useState(false);
//   const { toast } = useToast();

//   const { data: repositories, isLoading, error } = useQuery<Repository[]>({
//     queryKey: ["/api/repositories"],
//   });

//   const form = useForm<CreateRepositoryInput>({
//     resolver: zodResolver(repositorySchema),
//     defaultValues: {
//       name: "",
//       url: "",
//       description: "",
//       tags: "",
//     },
//   });

//   const createMutation = useMutation({
//     mutationFn: async (data: CreateRepositoryInput) => {
//       const res = await apiRequest("POST", "/api/repositories", data);
//       return await res.json();
//     },
//     onSuccess: () => {
//       queryClient.invalidateQueries({ queryKey: ["/api/repositories"] });
//       toast({
//         title: "Repository created",
//         description: "Your repository has been added successfully",
//       });
//       form.reset();
//       setIsDialogOpen(false);
//     },
//     onError: (error) => {
//       toast({
//         title: "Failed to create repository",
//         description: error.message,
//         variant: "destructive",
//       });
//     },
//   });

//   const onSubmit = (data: CreateRepositoryInput) => {
//     createMutation.mutate(data);
//   };

//   return (
//     <AppShell title="Repositories">
//       <div className="flex justify-between items-center mb-6">
//         <h2 className="text-lg leading-6 font-medium text-gray-900">Assessment Repositories</h2>
//         <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
//           <DialogTrigger asChild>
//             <Button>
//               <Plus className="-ml-1 mr-2 h-5 w-5" />
//               Add Repository
//             </Button>
//           </DialogTrigger>
//           <DialogContent>
//             <DialogHeader>
//               <DialogTitle>Add new repository</DialogTitle>
//               <DialogDescription>
//                 Add a Git repository to use for your coding assessments.
//               </DialogDescription>
//             </DialogHeader>
//             <Form {...form}>
//               <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
//                 <FormField
//                   control={form.control}
//                   name="name"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Repository Name</FormLabel>
//                       <FormControl>
//                         <Input placeholder="e.g., react-shopping-cart" {...field} />
//                       </FormControl>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />
//                 <FormField
//                   control={form.control}
//                   name="url"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Repository URL</FormLabel>
//                       <FormControl>
//                         <Input placeholder="https://github.com/username/repo" {...field} />
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
//                         <Textarea placeholder="Brief description of the repository" {...field} />
//                       </FormControl>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />
//                 <FormField
//                   control={form.control}
//                   name="tags"
//                   render={({ field }) => (
//                     <FormItem>
//                       <FormLabel>Tags (comma separated)</FormLabel>
//                       <FormControl>
//                         <Input placeholder="e.g., React, Frontend, API" {...field} />
//                       </FormControl>
//                       <FormMessage />
//                     </FormItem>
//                   )}
//                 />
//                 <DialogFooter>
//                   <Button type="submit" disabled={createMutation.isPending}>
//                     {createMutation.isPending ? "Adding..." : "Add Repository"}
//                   </Button>
//                 </DialogFooter>
//               </form>
//             </Form>
//           </DialogContent>
//         </Dialog>
//       </div>
      
//       {/* Repository List */}
//       {isLoading ? (
//         <div className="space-y-4">
//           {[1, 2, 3].map(i => (
//             <div key={i} className="bg-white shadow rounded-md p-6">
//               <div className="flex justify-between items-start">
//                 <div className="space-y-2">
//                   <Skeleton className="h-6 w-48" />
//                   <Skeleton className="h-4 w-64" />
//                 </div>
//                 <div className="flex space-x-2">
//                   <Skeleton className="h-8 w-24" />
//                   <Skeleton className="h-8 w-16" />
//                 </div>
//               </div>
//               <div className="mt-4 space-y-2">
//                 <Skeleton className="h-4 w-full" />
//                 <div className="flex mt-2 space-x-2">
//                   <Skeleton className="h-6 w-16 rounded-full" />
//                   <Skeleton className="h-6 w-20 rounded-full" />
//                   <Skeleton className="h-6 w-16 rounded-full" />
//                 </div>
//               </div>
//             </div>
//           ))}
//         </div>
//       ) : error ? (
//         <div className="bg-red-50 p-4 rounded-md">
//           <p className="text-red-800">Error loading repositories</p>
//         </div>
//       ) : repositories?.length === 0 ? (
//         <div className="bg-white shadow rounded-md p-8 text-center">
//           <p className="text-gray-500">No repositories found. Add your first repository to get started.</p>
//         </div>
//       ) : (
//         <div className="space-y-4">
//           {repositories?.map(repo => (
//             <RepositoryCard
//               key={repo.id}
//               id={repo.id}
//               name={repo.name}
//               url={repo.url}
//               description={repo.description}
//               tags={repo.tags || []}
//               lastUsedIn="Frontend Developer Assessment"
//             />
//           ))}
//         </div>
//       )}
//     </AppShell>
//   );
// }
