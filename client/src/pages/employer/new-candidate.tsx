import { useLocation } from "wouter";
import { useToast } from "@/hooks/use-toast";
import useApi from "@/hooks/use-api";
import { motion } from "framer-motion";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { queryClient } from "@/lib/queryClient";
import { z } from "zod";
import { 
  User, 
  Mail, 
  UserPlus, 
  Building2, 
  Phone, 
  MapPin,
  Globe,
  GraduationCap,
  Briefcase,
  Calendar
} from "lucide-react";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { Button } from "@/components/ui/button";

// Create candidate schema
const createCandidateSchema = z.object({
  firstName: z.string().min(1, "First name is required").max(50, "First name must be less than 50 characters"),
  lastName: z.string().min(1, "Last name is required").max(50, "Last name must be less than 50 characters"),
  email: z.string().email("Please enter a valid email address"),
  phone: z.string().optional(),
  company: z.string().optional(),
  position: z.string().optional(),
  location: z.string().optional(),
  linkedin: z.string().url("Please enter a valid LinkedIn URL").optional().or(z.literal("")),
  portfolio: z.string().url("Please enter a valid portfolio URL").optional().or(z.literal("")),
  experience: z.string().optional(),
  education: z.string().optional(),
  notes: z.string().optional(),
});

type CreateCandidateFormValues = z.infer<typeof createCandidateSchema>;

const FormMessage = ({ children }: any) => (
  children ? <p className="text-red-400 text-sm mt-1">{children}</p> : null
);

interface QuickFillSuggestion {
  icon: React.ReactNode;
  label: string;
  description: string;
  data: Partial<CreateCandidateFormValues>;
}

export default function AddCandidate() {
  const [, navigate] = useLocation();
  const { toast } = useToast();
  const { apiCall } = useApi();

  const form = useForm<CreateCandidateFormValues>({
    resolver: zodResolver(createCandidateSchema),
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      phone: "",
      company: "",
      position: "",
      location: "",
      linkedin: "",
      portfolio: "",
      experience: "",
      education: "",
      notes: "",
    },
  });

  // Quick fill suggestions for common candidate types
  // const quickFillSuggestions: QuickFillSuggestion[] = [
  //   {
  //     icon: <GraduationCap className="w-4 h-4" />,
  //     label: "Recent Graduate",
  //     description: "New graduate with internship experience",
  //     data: {
  //       experience: "0-1 years",
  //       education: "Bachelor's Degree in Computer Science",
  //     }
  //   },
  //   {
  //     icon: <Briefcase className="w-4 h-4" />,
  //     label: "Mid-Level Developer",
  //     description: "Experienced professional developer",
  //     data: {
  //       experience: "3-5 years",
  //       position: "Software Developer",
  //     }
  //   },
  //   {
  //     icon: <Building2 className="w-4 h-4" />,
  //     label: "Senior Engineer",
  //     description: "Senior level technical professional",
  //     data: {
  //       experience: "5+ years",
  //       position: "Senior Software Engineer",
  //     }
  //   },
  // ];

  // Create candidate mutation
  const createCandidateMutation = useMutation({
    mutationFn: async (data: CreateCandidateFormValues) => {
      const candidateData = {
        name: `${data.firstName} ${data.lastName}`,
        fullName: `${data.firstName} ${data.lastName}`,
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        appliedAt: new Date(),
        metadata: {
          phone: data.phone || "",
          company: data.company || "",
          position: data.position || "",
          location: data.location || "",
          linkedin: data.linkedin || "",
          portfolio: data.portfolio || "",
          experience: data.experience || "",
          education: data.education || "",
          notes: data.notes || "",
        }
      };

      const res = await apiCall("/api/candidates/new", {
        method: "POST",
        body: JSON.stringify(candidateData),
      });
      return res;
    },
    onSuccess: async (data: any) => {
      // Invalidate all candidate-related queries to ensure fresh data
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['candidates'] }),
        queryClient.invalidateQueries({ queryKey: ['availableCandidates'] })
      ]);
      
      // Force refetch the candidates data to ensure it's fresh
      await queryClient.refetchQueries({ queryKey: ['candidates'] });
      
      toast({
        title: "Candidate added",
        description: "The candidate has been added successfully",
      });
      navigate("/candidates");
    },
    onError: (error: Error) => {
      toast({
        title: "Failed to add candidate",
        description: error.message,
        variant: "destructive",
      });
    },
  });

  const onSubmit = async (data: CreateCandidateFormValues) => {
    createCandidateMutation.mutate(data);
  };

  const applyQuickFill = (suggestion: QuickFillSuggestion) => {
    Object.entries(suggestion.data).forEach(([key, value]) => {
      form.setValue(key as keyof CreateCandidateFormValues, value as string, {
        shouldValidate: true,
        shouldDirty: true,
      });
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    form.handleSubmit(onSubmit)(e);
  };

  return (
    <div className="min-h-screen flex flex-col w-full items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white p-6 relative overflow-hidden">
      {/* Fixed header with buttons */}
      <div className="fixed top-0 left-0 w-full z-50 bg-slate-900/80 backdrop-blur border-b border-slate-800 shadow-lg flex justify-between px-8 py-4 gap-3">
        <Button
          type="button"
          variant="outline"
          onClick={() => navigate("/candidates")}
          className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
        >
          Cancel
        </Button>

        <Button
          type="button"
          onClick={form.handleSubmit(onSubmit)}
          disabled={createCandidateMutation.isPending}
          className="bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20 border-0"
        >
          {createCandidateMutation.isPending ? "Adding..." : "Add Candidate"}
        </Button>
      </div>

      {/* Main content with top padding */}
      <div className="w-full max-w-2xl mx-auto relative pt-28">
        <motion.div
          className="relative z-10 space-y-6"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: "easeOut" }}
        >
          {/* Header */}
          <div className="text-center space-y-4">
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, duration: 0.5 }}
              className="inline-block"
            >
              <h1 className="serif-heading">
                Add New Candidate
              </h1>
              <motion.div
                className="h-px bg-gradient-to-r from-transparent via-slate-400/60 to-transparent mt-3"
                initial={{ width: 0, opacity: 0 }}
                animate={{ width: "100%", opacity: 1 }}
                transition={{ delay: 0.5, duration: 0.8 }}
              />
            </motion.div>

            <motion.p
              className="text-sm text-slate-400"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 0.3 }}
            >
              Add a new candidate to your talent pipeline
            </motion.p>
          </div>

          {/* Quick fill suggestions */}
          {/* <div className="flex flex-wrap items-center justify-center gap-2">
            {quickFillSuggestions.map((suggestion, index) => (
              <motion.button
                key={suggestion.label}
                onClick={() => applyQuickFill(suggestion)}
                className="flex items-center gap-2 px-3 py-2 bg-slate-800/60 hover:bg-slate-700/80 rounded-lg text-sm text-slate-300 hover:text-white transition-all relative group border border-slate-700/50 backdrop-blur-sm"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                type="button"
              >
                {suggestion.icon}
                <span>{suggestion.label}</span>
              </motion.button>
            ))}
          </div> */}

          {/* Form */}
          <div className="space-y-6" onSubmit={handleSubmit}>
            {/* Personal Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <User className="w-4 h-4" />
                  First Name
                </Label>
                <Input
                  {...form.register("firstName")}
                  placeholder="Enter first name"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.firstName?.message}</FormMessage>
              </div>

              <div>
                <Label className="text-slate-300 font-medium">
                  Last Name
                </Label>
                <Input
                  {...form.register("lastName")}
                  placeholder="Enter last name"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.lastName?.message}</FormMessage>
              </div>
            </div>

            {/* Contact Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <Mail className="w-4 h-4" />
                  Email
                </Label>
                <Input
                  {...form.register("email")}
                  type="email"
                  placeholder="candidate@example.com"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.email?.message}</FormMessage>
              </div>

              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <Phone className="w-4 h-4" />
                  Phone (Optional)
                </Label>
                <Input
                  {...form.register("phone")}
                  placeholder="+1 (555) 123-4567"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.phone?.message}</FormMessage>
              </div>
            </div>

            {/* Professional Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <Building2 className="w-4 h-4" />
                  Current Company (Optional)
                </Label>
                <Input
                  {...form.register("company")}
                  placeholder="Current employer"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.company?.message}</FormMessage>
              </div>

              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <Briefcase className="w-4 h-4" />
                  Position (Optional)
                </Label>
                <Input
                  {...form.register("position")}
                  placeholder="Current job title"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.position?.message}</FormMessage>
              </div>
            </div>

            {/* Location and Links */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <MapPin className="w-4 h-4" />
                  Location (Optional)
                </Label>
                <Input
                  {...form.register("location")}
                  placeholder="City, State/Country"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.location?.message}</FormMessage>
              </div>

              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <Calendar className="w-4 h-4" />
                  Experience Level (Optional)
                </Label>
                <Input
                  {...form.register("experience")}
                  placeholder="e.g., 3-5 years"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.experience?.message}</FormMessage>
              </div>
            </div>

            {/* URLs */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  LinkedIn URL (Optional)
                </Label>
                <Input
                  {...form.register("linkedin")}
                  placeholder="https://linkedin.com/in/username"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.linkedin?.message}</FormMessage>
              </div>

              <div>
                <Label className="text-slate-300 font-medium flex items-center gap-2">
                  <Globe className="w-4 h-4" />
                  Portfolio URL (Optional)
                </Label>
                <Input
                  {...form.register("portfolio")}
                  placeholder="https://portfolio.com"
                  className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                />
                <FormMessage>{form.formState.errors.portfolio?.message}</FormMessage>
              </div>
            </div>

            {/* Education */}
            <div>
              <Label className="text-slate-300 font-medium flex items-center gap-2">
                <GraduationCap className="w-4 h-4" />
                Education (Optional)
              </Label>
              <Input
                {...form.register("education")}
                placeholder="e.g., Bachelor's in Computer Science, Stanford University"
                className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
              />
              <FormMessage>{form.formState.errors.education?.message}</FormMessage>
            </div>

            {/* Notes */}
            <div>
              <Label className="text-slate-300 font-medium">
                Notes (Optional)
              </Label>
              <Textarea
                {...form.register("notes")}
                placeholder="Additional notes about the candidate..."
                rows={4}
                className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
              />
              <FormMessage>{form.formState.errors.notes?.message}</FormMessage>
            </div>

            {/* Submit button - duplicated for mobile convenience */}
            <div className="flex justify-end space-x-3 md:hidden">
              <Button
                type="button"
                variant="outline"
                onClick={() => navigate("/candidates")}
                className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
              >
                Cancel
              </Button>
              <Button
                type="button"
                onClick={form.handleSubmit(onSubmit)}
                disabled={createCandidateMutation.isPending}
                className="bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20 border-0"
              >
                {createCandidateMutation.isPending ? "Adding..." : "Add Candidate"}
              </Button>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}