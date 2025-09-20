import { useEffect, useRef, useCallback, useTransition } from "react";
import { useState } from "react";
import { cn, getOpenRouterModels } from "@/lib/utils";
import {
  Paperclip,
  XIcon,
  TabletSmartphone,
  Command as CommandIcon,
  Database,
  Code,
  Server,
  ChevronsUpDown,
  Check
} from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import * as React from "react"
import { Button } from "@/components/ui/button";
import { Form, FormField, FormItem, FormLabel, FormMessage } from "@/components/ui/form";
import { FormControl } from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { useLocation } from "wouter";
import { useToast } from "@/hooks/use-toast";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { queryClient } from "@/lib/queryClient";
import { z } from "zod";  
import { DatePicker } from "@/components/ui/date-picker";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Command, CommandEmpty, CommandGroup, CommandInput, CommandItem, CommandList } from "@/components/ui/command";
import { TechChoices } from "./tech-choices";
import { Dialog, DialogHeader, DialogTitle, DialogContent, DialogTrigger, DialogDescription, DialogFooter } from "@/components/ui/dialog";
import useApi from "@/hooks/use-api";
import { Textarea } from "@/components/ui/textarea";
import { useSse } from "@/hooks/use-sse";;

// Create a schema for the assessment creation form
const createAssessmentSchema = z.object({
  model: z.string().min(1, "Model is required").max(255, "Model must be less than 255 characters"),
  name: z.string().min(1, "Title is required").max(255, "Title must be less than 255 characters"),
  role: z.string().min(1, "Role is required").max(255, "Role must be less than 255 characters"),
  skills: z.array(z.string()).min(1, "Skills are required"),
  details: z.string().min(1, "Details are required").max(10000, "Details must be less than 10000 characters"),
  //assessmentType: z.enum(["TAKE_HOME", "LIVE_CODING"]),
  duration: z.number().min(1, "Duration must be at least 1 minute").max(255, "Duration must be less than 255 minutes"),
  languageOptions: z.array(z.string()).max(5, "There can be no more than 5 technology choices."),
  // startDate: z.coerce.date().min(new Date(), { message: "Start date must be in the future" }),
  // endDate: z.coerce.date().min(new Date(), { message: "End date must be in the future" }),
});

export type CreateAssessmentFormValues = z.infer<typeof createAssessmentSchema>;

// interface UseAutoResizeTextareaProps {
//   minHeight: number;
//   maxHeight?: number;
// }

// function useAutoResizeTextarea({
//   minHeight,
//   maxHeight,
// }: UseAutoResizeTextareaProps) {
//   const textareaRef = useRef<HTMLTextAreaElement>(null);

//   const adjustHeight = useCallback(
//     (reset?: boolean) => {
//       const textarea = textareaRef.current;
//       if (!textarea) return;

//       if (reset) {
//         textarea.style.height = `${minHeight}px`;
//         return;
//       }

//       textarea.style.height = `${minHeight}px`;
//       const newHeight = Math.max(
//         minHeight,
//         Math.min(
//           textarea.scrollHeight,
//           maxHeight ?? Number.POSITIVE_INFINITY
//         )
//       );

//       textarea.style.height = `${newHeight}px`;
//     },
//     [minHeight, maxHeight]
//   );

//   useEffect(() => {
//     const textarea = textareaRef.current;
//     if (textarea) {
//       textarea.style.height = `${minHeight}px`;
//     }
//   }, [minHeight]);

//   useEffect(() => {
//     const handleResize = () => adjustHeight();
//     window.addEventListener("resize", handleResize);
//     return () => window.removeEventListener("resize", handleResize);
//   }, [adjustHeight]);

//   return { textareaRef, adjustHeight };
// }

interface CommandSuggestion {
  icon: React.ReactNode;
  label: string;
  description: string;

  prefix: string;
  role: string;
  skills: string[];
  duration: number;
  durationUnit: "minutes" | "hours"

}

export function CreateAssessmentForm() {
  const [open, setOpen] = React.useState(false)
  const [modelValue, setModelValue] = React.useState("")
  const [role, setRole] = useState("");
  const [skills, setSkills] = useState<string>("");
  const [duration, setDuration] = useState(0);
  const [durationUnit, setDurationUnit] = useState<"minutes" | "hours">("minutes");
  const [languageOptions, setLanguageOptions] = useState<string[]>([]);
  const [name, setName] = useState<string>("");
  const [formDetails, setFormDetails] = useState("");
  const [attachments, setAttachments] = useState<string[]>([]);
  //const [isTyping, setIsTyping] = useState(false);
  const [isPending, startTransition] = useTransition();
  //const [activeSuggestion, setActiveSuggestion] = useState<number>(-1);
  //const [showCommandPalette, setShowCommandPalette] = useState(false);
  //const [recentCommand, setRecentCommand] = useState<string | null>(null);
  //const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
  const [isCheckingGitHub, setIsCheckingGitHub] = useState(false);
  //const { textareaRef, adjustHeight } = useAutoResizeTextarea({
  //  minHeight: 60,
  //  maxHeight: 200,
  //});
  const [inputFocused, setInputFocused] = useState(false);
  const commandPaletteRef = useRef<HTMLDivElement>(null);
  const detailsRef = useRef<HTMLTextAreaElement | null>(null);

  const [models, setModels] = useState<string[]>([]);
  const defaultModel: string = "anthropic/claude-sonnet-4";

  // Setup form with updated default values (declare before any watchers)
  const form = useForm<CreateAssessmentFormValues>({
    resolver: zodResolver(createAssessmentSchema),
    defaultValues: {
      name: "",
      role: "",
      skills: [],
      details: "",
      languageOptions: [],
      duration: 0,
    },
  });

  const adjustDetailsHeight = useCallback(() => {
    const el = detailsRef.current;
    if (!el) return;
    el.style.height = "auto";
    el.style.height = `${el.scrollHeight}px`;
  }, []);

  useEffect(() => {
    adjustDetailsHeight();
  }, [adjustDetailsHeight]);

  const detailsValue = form.watch("details");
  useEffect(() => {
    adjustDetailsHeight();
  }, [detailsValue, adjustDetailsHeight]);

  const { toast } = useToast();
  const [, navigate] = useLocation();
  const { apiCall } = useApi();

  // form already initialized above

  // Ensure a default model is selected so validation passes if user doesn't pick one

  // if (!modelValue && Array.isArray(models) && models.length > 0) {
  //   setModelValue(defaultModel);
  //   form.setValue("model", defaultModel, { shouldValidate: true, shouldDirty: true });
  // }

  // SSE Assessment creation hook
  const { sendMessage: createAssessment, isLoading: isSseLoading, isConnected } = useSse({
    // Callback function to handle the assessment creation
    onEventHandler: async (data: {
      jobId: string;
      assessmentId: string;
      //status: string;
      //message: string;
    }) => {
      
      // Invalidate all assessment-related queries to ensure fresh data
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: ['assessments'] }),
        queryClient.invalidateQueries({ queryKey: ['availableCandidates'] }),
        queryClient.invalidateQueries({ queryKey: ["/api/assessments"] })
      ]);
      
      // Force refetch the assessments data to ensure it's fresh
      await queryClient.refetchQueries({ queryKey: ['assessments'] });
      
      const newAssessmentId = data?.assessmentId;
      //console.log('Extracted assessment ID:', newAssessmentId);
      
      toast({
        title: "Assessment created",
        description: "Your assessment has been created successfully",
      });
      
      if (newAssessmentId) {
        console.log('Redirecting to assessment view:', `/assessments/view/${newAssessmentId}`);
        navigate(`/assessments/view/${newAssessmentId}`);
      } else {
        console.log('No assessment ID found, redirecting to assessments list');
        navigate("/assessments");
      }
    },
    onError: (error: string) => {
      toast({
        title: "Failed to create assessment",
        description: error,
        variant: "destructive",
      });
    },
  });

  // Function to convert duration to minutes
  const convertDurationToMinutes = (duration: number, unit: "minutes" | "hours"): number => {
    switch (unit) {
      case "minutes":
        return duration;
      case "hours":
        return duration * 60;
      default:
        return duration;
    }
  };

  // Function to check if user has valid GitHub token
  const checkGitHubToken = async (): Promise<{ result: boolean, redirectUrl?: string, requiresRedirect?: boolean }> => {
    try {
      const response = await apiCall("/api/users/is-connected-github", {
        method: "GET",
      });
      return response;
    } catch (error) {
      console.error('Error checking GitHub token:', error);
      return { result: false };
    }
  };

  // Function to poll for GitHub token validation
  const pollForGitHubToken = async (): Promise<boolean> => {
    const maxAttempts = 60; // 5 minutes with 7-second intervals
    const pollInterval = 7000; // 7 seconds
    let attempts = 0;

    const poll = async (): Promise<boolean> => {
      if (attempts >= maxAttempts) {
        console.log('GitHub token polling timeout reached');
        return false;
      }

      try {
        const hasValidToken = await checkGitHubToken();
        console.log('Polling attempt', attempts + 1, ':', hasValidToken);

        if (hasValidToken?.result) {
          console.log('Valid GitHub token found');
          return true;
        }

        // Continue polling
        attempts++;
        await new Promise(resolve => setTimeout(resolve, pollInterval));
        return poll();
      } catch (error) {
        console.error('Polling error:', error);
        attempts++;
        await new Promise(resolve => setTimeout(resolve, pollInterval));
        return poll();
      }
    };

    return poll();
  };

  const onSubmit = async (data: CreateAssessmentFormValues) => {
    console.log('Form data with candidate choices:', data);
    
    // Convert duration to minutes before sending to API
    const durationInMinutes = convertDurationToMinutes(duration, durationUnit);
    // Skills are already converted to array in the form field onChange handler

    // Create the data object with converted duration
      const apiData = {
      ...data,
      duration: durationInMinutes,
    };
    
    console.log('API data with duration in minutes:', apiData);
    
    setIsCheckingGitHub(true);
    
    try {
      // Check if user has valid GitHub token
      const hasValidGitHubToken = await checkGitHubToken();
      if (!hasValidGitHubToken?.result) {
        // Generate the install URL on the server
        const githubInstallUrlResponse: { redirectUrl: string } = await apiCall("/api/users/github/generate-install-url", {
          method: "POST",
        });
        
        // Open GitHub app installation in new window

        console.log('GitHub install URL:', githubInstallUrlResponse.redirectUrl);
        window.open(githubInstallUrlResponse.redirectUrl, '_blank');
        
        // Show loading state
        toast({
          title: "Connecting GitHub",
          description: "Please complete the GitHub installation in the new tab. This tab will automatically proceed once connected.",
        });
        
        // Poll for GitHub token validation
        const tokenObtained = await pollForGitHubToken();
        
        if (!tokenObtained) {
          toast({
            title: "GitHub Connection Timeout",
            description: "GitHub connection timed out. Please try again.",
            variant: "destructive",
          });
          return;
        }
        
        toast({
          title: "GitHub Connected",
          description: "GitHub account successfully connected!",
        });
      }
      
      // Proceed with assessment creation using SSE
      //console.log('🎯 About to call createAssessment with data:', apiData);
      createAssessment(apiData, '/api/assessments/new');
    } catch (error) {
      console.error('Error in form submission:', error);
      toast({
        title: "Error",
        description: "An error occurred while processing your request. Please try again.",
        variant: "destructive",
      });
    } finally {
      setIsCheckingGitHub(false);
    }
  };

  useEffect(() => {
    getOpenRouterModels(setModels);
  }, []);

  const commandSuggestions: CommandSuggestion[] = [
    {
      icon: <Code className="w-4 h-4" />,
      label: "SWE Intern",
      description: "Software Engineering Intern",
      prefix:
        `Have candidates build a web application with Next, using a Postgres database. Have them Dockerize the app and include a docker-compose.yml file in their submission.`,
      role: "Software Engineering Intern",
      skills: ["Next.js", "Tailwind CSS", "TypeScript", "Docker", "Prisma"],
      duration: 60,
      durationUnit: "minutes"
    },
    {
      icon: <Server className="w-4 h-4" />,
      label: "Mid-Level Backend Engineer",
      description: "Backend Engineer",
      prefix:
        `This assessment is for a mid-level backend engineer with 3+ years of experience. Have candidates ship a REST API, with paginated endpoints, rate limiting, and caching.`,
      role: "Mid-Level Backend Engineer",
      skills: ["Go", "REST API Design", "ORM", "Redis", "Caching", "Rate limiting"],
      duration: 3,
      durationUnit: "hours"
    },
    {
      icon: <Database className="w-4 h-4" />,
      label: "Senior MLOps Engineer",
      description: "MLOps Engineer",
      prefix: `Create an assessment for a Senior MLOps Engineer with 5+ years of experience. Test applicants on their proficiency in MLFlow, ML Studio, and Azure ML.`,
      role: "Senior MLOps Engineer",
      skills: ["MLFlow", "ML Studio", "Azure ML"],
      duration: 2,
      durationUnit: "hours"
    },
    {
      icon: <TabletSmartphone className="w-4 h-4" />,
      label: "iOS Engineer",
      description: "iOS Engineer",
      prefix: `This assessment is for an iOS Engineer with 2+ years of experience. Have candidates build a mobile app with Swift and SwiftUI.`,
      role: "iOS Engineer",
      skills: ["Swift", "SwiftUI", "iOS"],
      duration: 2,
      durationUnit: "hours"
    },
  ];

  // useEffect(() => {
  //   if (formDesc.startsWith('/') && !formDesc.includes(' ')) {
  //     setShowCommandPalette(true);

  //     const matchingSuggestionIndex = commandSuggestions.findIndex(
  //       (cmd) => cmd.prefix.startsWith(formDesc)
  //     );

  //     if (matchingSuggestionIndex >= 0) {
  //       setActiveSuggestion(matchingSuggestionIndex);
  //     } else {
  //       setActiveSuggestion(-1);
  //     }
  //   } else {
  //     setShowCommandPalette(false);
  //   }
  // }, [formDesc]);

  // useEffect(() => {
  //   const handleMouseMove = (e: MouseEvent) => {
  //     setMousePosition({ x: e.clientX, y: e.clientY });
  //   };

  //   window.addEventListener('mousemove', handleMouseMove);
  //   return () => {
  //     window.removeEventListener('mousemove', handleMouseMove);
  //   };
  // }, []);

  // useEffect(() => {
  //   const handleClickOutside = (event: MouseEvent) => {
  //     const target = event.target as Node;
  //     const commandButton = document.querySelector('[data-command-button]');

  //     if (commandPaletteRef.current &&
  //       !commandPaletteRef.current.contains(target) &&
  //       !commandButton?.contains(target)) {
  //       setShowCommandPalette(false);
  //     }
  //   };

  //   document.addEventListener('mousedown', handleClickOutside);
  //   return () => {
  //     document.removeEventListener('mousedown', handleClickOutside);
  //   };
  // }, []);

  // const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
  //   if (showCommandPalette) {
  //     if (e.key === 'ArrowDown') {
  //       e.preventDefault();
  //       setActiveSuggestion(prev =>
  //         prev < commandSuggestions.length - 1 ? prev + 1 : 0
  //       );
  //     } else if (e.key === 'ArrowUp') {
  //       e.preventDefault();
  //       setActiveSuggestion(prev =>
  //         prev > 0 ? prev - 1 : commandSuggestions.length - 1
  //       );
  //     } else if (e.key === 'Tab' || e.key === 'Enter') {
  //       e.preventDefault();
  //       if (activeSuggestion >= 0) {
  //         const selectedCommand = commandSuggestions[activeSuggestion];
  //         setTitle(selectedCommand.label);

  //         setRole(selectedCommand.role);
  //         setSkills(selectedCommand.skills);
  //         setDuration(selectedCommand.duration);
  //         setDurationUnit(selectedCommand.durationUnit);
  //         setFormDesc(selectedCommand.prefix + ' ');
  //         // Sync to form state so validation sees values
  //         form.setValue("title", selectedCommand.label, { shouldValidate: true, shouldDirty: true });
  //         form.setValue("role", selectedCommand.role, { shouldValidate: true, shouldDirty: true });
  //         form.setValue("skills", selectedCommand.skills, { shouldValidate: true, shouldDirty: true });
  //         form.setValue("duration", selectedCommand.duration, { shouldValidate: true, shouldDirty: true });
  //         form.setValue("description", selectedCommand.prefix + ' ', { shouldValidate: true, shouldDirty: true });
  //         // Provide a reasonable default title if empty
  //         if (!form.getValues("title")) {
  //           form.setValue("title", selectedCommand.label, { shouldValidate: true, shouldDirty: true });
  //         }
  //         // Ensure model is set
  //         if (!form.getValues("model") && models.length > 0) {
  //           form.setValue("model", defaultModel, { shouldValidate: true, shouldDirty: true });
  //           setModelValue(models[0]);
  //         }
  //         setShowCommandPalette(false);

  //         setRecentCommand(selectedCommand.label);
  //         setTimeout(() => setRecentCommand(null), 3500);
  //       }
  //     } else if (e.key === 'Escape') {
  //       e.preventDefault();
  //       setShowCommandPalette(false);
  //     }
  //   }
  //   // else if (e.key === "Enter" && !e.shiftKey) {
  //   //     e.preventDefault();
  //   //     if (value.trim()) {
  //   //         handleSendMessage();
  //   //     }
  //   // }
  // };

  // const handleSendMessage = () => {
  //     if (value.trim()) {
  //         startTransition(() => {
  //             setIsTyping(true);
  //             setTimeout(() => {
  //                 setIsTyping(false);
  //                 setValue("");
  //                 adjustHeight(true);
  //             }, 3000);
  //         });
  //     }
  // };

  // const handleAttachFile = () => {
  //   const mockFileName = `file-${Math.floor(Math.random() * 1000)}.pdf`;
  //   setAttachments(prev => [...prev, mockFileName]);
  // };

  const removeAttachment = (index: number) => {
    setAttachments(prev => prev.filter((_, i) => i !== index));
  };

  const selectCommandSuggestion = (index: number) => {
    const selectedCommand = commandSuggestions[index];
    const skillsString = selectedCommand.skills.join(', ');
    
    setName(selectedCommand.label);
    setRole(selectedCommand.role);
    setSkills(skillsString);
    setDuration(selectedCommand.duration);
    setDurationUnit(selectedCommand.durationUnit);
    setFormDetails(selectedCommand.prefix + ' ');
    // setShowCommandPalette(false);

    // Sync to form state so validation sees values
    form.setValue("name", selectedCommand.label, { shouldValidate: true, shouldDirty: true });
    form.setValue("role", selectedCommand.role, { shouldValidate: true, shouldDirty: true });
    form.setValue("skills", selectedCommand.skills, { shouldValidate: true, shouldDirty: true }); // Already an array
    form.setValue("duration", selectedCommand.duration, { shouldValidate: true, shouldDirty: true });
    form.setValue("details", selectedCommand.prefix + ' ', { shouldValidate: true, shouldDirty: true });
    // if (!form.getValues("title")) {
    //   form.setValue("title", selectedCommand.label, { shouldValidate: true, shouldDirty: true });
    // }
    if (!form.getValues("model") && models.length > 0) {
      form.setValue("model", defaultModel, { shouldValidate: true, shouldDirty: true });
      setModelValue(defaultModel);
    }

    // setRecentCommand(selectedCommand.label);
    // setTimeout(() => setRecentCommand(null), 2000);
  };

  return (
    <div className="min-h-screen flex flex-col w-full items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white p-6 relative overflow-hidden">
      {/* Fixed header with buttons */}
      <div className="fixed top-0 left-0 w-full z-50 bg-slate-900/80 backdrop-blur border-b border-slate-800 shadow-lg flex justify-between px-8 py-4 gap-3">
        <Dialog>
          <DialogTrigger asChild>
            <Button
              type="button"
              variant="outline"
              className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
            >
              Cancel
            </Button>
          </DialogTrigger>
          <DialogContent className="bg-slate-800/60 border-slate-700/50 text-slate-300 backdrop-blur-sm">
            <DialogHeader>
              <DialogTitle>Cancel Assessment</DialogTitle>
              <DialogDescription>
                Are you sure you want to cancel this assessment?
              </DialogDescription>
            </DialogHeader>
            <DialogFooter>
              <Button variant="default" className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm" onClick={() => navigate("/assessments")}>Discard</Button>
              <Button type="submit" form="assessment-form" variant="default" className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
                onClick={() => navigate("/assessments")}>
                  Save as draft
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>

        <Button
          type="submit"
          form="assessment-form"
          disabled={isSseLoading || isCheckingGitHub}
          className="bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20 border-0"
        >
          {isCheckingGitHub ? "Checking GitHub..." : isSseLoading ? "Creating..." : "Create Assessment"}
        </Button>
        
        {/* SSE Connection Status Indicator */}
        {isSseLoading && (
          <div className="flex items-center gap-2 text-sm text-slate-400">
            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
            <span>Connected to AI - Planning your assessment...</span>
          </div>
        )}
      </div>

      {/* Add top padding to prevent content from being hidden behind the fixed header */}
      <div className="w-full max-w-2xl mx-auto relative pt-28">
        <motion.div
          className="relative z-10 space-y-6"
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: "easeOut" }}
        >
          <div className="text-center space-y-9">
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2, duration: 0.5 }}
              className="inline-block"
            >
              {/* <h1 className="text-3xl font-gfs-didot font-medium tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-300 pb-3"> */}
              <h1 className="serif-heading">
                Describe your assessment
              </h1>

              <motion.div
                className="h-px bg-gradient-to-r from-transparent via-slate-400/60 to-transparent"
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
              Create a new assessment by describing the role, experience level, skills and competencies you want to assess.
            </motion.p>

          </div>

          <div className="flex flex-wrap items-center justify-center gap-2">
            {commandSuggestions.map((suggestion, index) => (
              <motion.button
                key={suggestion.prefix}
                onClick={() => selectCommandSuggestion(index)}
                className="flex items-center gap-2 px-3 py-2 bg-slate-800/60 hover:bg-slate-700/80 rounded-lg text-sm text-slate-300 hover:text-white transition-all relative group border border-slate-700/50 backdrop-blur-sm"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
              >
                {suggestion.icon}
                <span>{suggestion.label}</span>
              </motion.button>
            ))}
          </div>

          <Form {...form}>
            <form
              id="assessment-form"
              onSubmit={form.handleSubmit(onSubmit)}
              className="space-y-6"
            >
              <FormField
                control={form.control}
                name="model"
                render={({ field }) => (
                  <FormItem>
                    {/* <FormLabel className="text-slate-300 font-medium">Model</FormLabel> */}
                    <FormControl>
                      <div className="flex justify-center items-center w-full pt-2 pb-8">
                        <Popover open={open} onOpenChange={setOpen}>
                          <PopoverTrigger asChild className="w-1/2">
                            <Button
                              variant="outline"
                              role="combobox"
                              aria-expanded={open}
                              className="w-1/2 justify-between bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
                            >
                              {modelValue
                                ? models.find((model) => model === modelValue)
                                : "Select model..."}
                              <ChevronsUpDown className="opacity-50" />
                            </Button>
                          </PopoverTrigger>
                          <PopoverContent className="w-[200px] p-0 bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm">
                            <Command className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm">
                              <CommandInput placeholder="Search model..." className="h-9" />
                              <CommandList>
                                <CommandEmpty>No model found.</CommandEmpty>
                                <CommandGroup className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm">
                                  {models.map((model) => (
                                    <CommandItem
                                      key={model}
                                      value={model}
                                      onSelect={(currentValue) => {
                                        const next = currentValue === modelValue ? "" : currentValue;
                                        setModelValue(next);
                                        form.setValue("model", next, { shouldValidate: true, shouldDirty: true });
                                        setOpen(false);
                                      }}
                                    >
                                      {model}
                                      <Check
                                        className={cn(
                                          "ml-auto",
                                          modelValue === model ? "opacity-100" : "opacity-0"
                                        )}
                                      />
                                    </CommandItem>
                                  ))}
                                </CommandGroup>
                              </CommandList>
                            </Command>
                          </PopoverContent>
                        </Popover>
                      </div>
                    </FormControl>
                  </FormItem>
                )}

              />
              <FormField
                control={form.control}
                name="name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-slate-300 font-medium">Assessment Name</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="e.g., Junior Full-Stack Dev - XXX Team, May 2025"
                        {...field}
                        value={name}
                        onChange={(e) => {
                          setName(e.target.value);
                          field.onChange(e.target.value);
                        }}      
                        className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="role"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-slate-300 font-medium">Role and Experience Level</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="e.g., Junior Full-Stack Developer"
                        {...field}
                        value={role}
                        onChange={(e) => {
                          setRole(e.target.value);
                          field.onChange(e.target.value);
                        }}
                        className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="skills"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-slate-300 font-medium">Skills, Technologies, and Competencies</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="e.g., Python, FastAPI, React, TypeScript, REST API Development, Authentication & Authorization, etc."
                        value={skills}
                        onChange={(e) => {
                          const skillsString = e.target.value;
                          setSkills(skillsString);
                          // Convert string to array and update form state
                          const skillsArray = skillsString.split(',').map(skill => skill.trim()).filter(skill => skill.length > 0);
                          field.onChange(skillsArray);
                        }}
                        className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              {/* <FormField
                control={form.control}
                name="assessmentType"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-slate-300 font-medium">Assessment Type</FormLabel>
                    <Select
                      onValueChange={field.onChange}
                      defaultValue="TAKE_HOME"

                    >
                      <SelectTrigger className="bg-slate-800/60 border-slate-700/50 text-gray-100 placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm">
                        <SelectValue placeholder="Select Assessment Type" />
                      </SelectTrigger>
                      <SelectContent className="bg-slate-800/60 border-slate-700/50 text-gray-100">
                        <SelectItem value="TAKE_HOME">Take Home Assignment</SelectItem>
                        <SelectItem value="LIVE_CODING">Live Coding</SelectItem>
                      </SelectContent>
                    </Select>
                    <FormMessage />
                  </FormItem>
                )}
              /> */}

              <FormField
                control={form.control}
                name="duration"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-slate-300 font-medium">Time Limit</FormLabel>
                    <div className="flex items-center gap-2">
                      <FormControl className="w-1/4">
                        <Input
                          type="number"
                          min={1}
                          max={999}
                          placeholder={duration.toString()}
                          {...field}
                          value={duration.toString()}
                          onChange={(e) => {
                            const next = Number(e.target.value);
                            setDuration(next);
                            field.onChange(next);
                          }}
                          className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                        />
                      </FormControl>

                      <FormControl>
                        <Select
                          value={durationUnit}
                          onValueChange={(value) => {
                            setDurationUnit(value as "minutes" | "hours");
                            field.onChange;
                          }}
                        >
                          <SelectTrigger className="bg-slate-800/60 border-slate-700/50 text-gray-100 placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm">
                            <SelectValue placeholder="Select Duration Unit" />
                          </SelectTrigger>
                          <SelectContent className="bg-slate-800/60 border-slate-700/50 text-gray-100">
                            <SelectItem value="minutes">Minutes</SelectItem>
                            <SelectItem value="hours">Hours</SelectItem>
                          </SelectContent>
                        </Select>
                      </FormControl>
                    </div>
                  </FormItem>
                )}
              />

              {/* <FormField
                                control={form.control}
                                name="description"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel className="text-slate-300 font-medium">Description</FormLabel>
                                        <FormControl>
                                            <Textarea
                                                placeholder="Describe what the candidate needs to do in this assessment"
                                                {...field}
                                                rows={5}
                                                value={field.value ?? ''}
                                                className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm resize-none"
                                                showRing={false}
                                            />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            /> */}

              {/* TODO: add date range picker to the assessments details page and enforce a valid date range before setting status to active*/}
              {/* <div>
                <FormLabel className="text-slate-300 font-medium">Date Range</FormLabel>

                <div className="flex items-center gap-2 mt-2 justify-center">
                  <FormControl>
                    <DatePicker />
                  </FormControl>
                  <FormControl>
                    <DatePicker />
                  </FormControl>
                </div>
              </div> */}

              <FormField
                control={form.control}
                name="languageOptions"
                render={({ field }) => (
                  <FormItem>
                    <FormControl>
                      <TechChoices value={field.value} onChange={field.onChange} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <FormField
                control={form.control}
                name="details"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel className="text-slate-300 font-medium">Additional Details</FormLabel>
                    <FormControl>
                      <motion.div
                        className="relative backdrop-blur-xl bg-slate-800/90 rounded-2xl border border-slate-700/50 shadow-2xl"
                        initial={{ scale: 0.98 }}
                        animate={{ scale: 1 }}
                        transition={{ delay: 0.1 }}
                      >

                        {/* <AnimatePresence>

                          {showCommandPalette && ( 
                            <motion.div
                              //ref={commandPaletteRef}
                              className="absolute left-4 right-4 bottom-full mb-2 backdrop-blur-xl bg-slate-900/95 rounded-lg z-50 shadow-lg border border-slate-700/50 overflow-hidden"
                              initial={{ opacity: 0, y: 5 }}
                              animate={{ opacity: 1, y: 0 }}
                              exit={{ opacity: 0, y: 5 }}
                              transition={{ duration: 0.15 }}
                            >
                              <div className="py-1 bg-slate-900/95">
                                {commandSuggestions.map((suggestion, index) => (
                                  <motion.div
                                    key={suggestion.prefix}
                                    className={cn(
                                      "flex items-center gap-2 px-3 py-2 text-xs transition-colors cursor-pointer",
                                      //activeSuggestion === index
                                        ? "bg-violet-600/20 text-white"
                                        : "text-slate-300 hover:bg-slate-800/50"
                                    )}
                                    onClick={() => selectCommandSuggestion(index)}
                                    initial={{ opacity: 0 }}
                                    animate={{ opacity: 1 }}
                                    transition={{ delay: index * 0.03 }}
                                  >
                                    <div className="w-5 h-5 flex items-center justify-center text-slate-400">
                                      {suggestion.icon}
                                    </div>
                                    <div className="font-medium">{suggestion.label}</div>
                                    <div className="text-slate-500 text-xs ml-1">
                                      {suggestion.prefix}
                                    </div>
                                  </motion.div>
                                ))}
                              </div>
                            </motion.div>
                          )}

                        </AnimatePresence> */}

                        <Textarea
                          placeholder="Describe what the candidate needs to do in this assessment"
                          value={field.value ?? ""}
                          onChange={(e) => {
                            setFormDetails(e.target.value);
                            field.onChange(e.target.value);
                            adjustDetailsHeight();
                          }}
                          ref={detailsRef}
                          rows={1}
                          className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm resize-none overflow-hidden"
                        />

                        <AnimatePresence>
                          {attachments.length > 0 && (
                            <motion.div
                              className="px-4 pb-3 flex gap-2 flex-wrap"
                              initial={{ opacity: 0, height: 0 }}
                              animate={{ opacity: 1, height: "auto" }}
                              exit={{ opacity: 0, height: 0 }}
                            >
                              {attachments.map((file, index) => (
                                <motion.div
                                  key={index}
                                  className="flex items-center gap-2 text-xs bg-slate-700/50 py-1.5 px-3 rounded-lg text-slate-300"
                                  initial={{ opacity: 0, scale: 0.9 }}
                                  animate={{ opacity: 1, scale: 1 }}
                                  exit={{ opacity: 0, scale: 0.9 }}
                                >
                                  <span>{file}</span>
                                  <button
                                    onClick={() => removeAttachment(index)}
                                    className="text-slate-500 hover:text-white transition-colors"
                                  >
                                    <XIcon className="w-3 h-3" />
                                  </button>
                                </motion.div>
                              ))}
                            </motion.div>
                          )}
                        </AnimatePresence>

                        {/* TODO: add support for file attachments */}
                        {/* <div className="p-4 border-t border-slate-700/50 flex items-center justify-between gap-4"> */}
                          {/* <div className="flex items-center gap-3">
                            <motion.button
                              type="button"
                              onClick={handleAttachFile}
                              whileTap={{ scale: 0.94 }}
                              className="p-2 text-slate-400 hover:text-white rounded-lg transition-colors relative group"
                            >
                              <Paperclip className="w-4 h-4" />
                              <motion.span
                                className="absolute inset-0 bg-slate-700/30 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
                                layoutId="button-highlight"
                              />
                            </motion.button>

                            <motion.button
                              type="button"
                              data-command-button
                              onClick={(e) => {
                                e.stopPropagation();
                                setShowCommandPalette(prev => !prev);
                              }}
                              whileTap={{ scale: 0.94 }}
                              className={cn(
                                "p-2 text-slate-400 hover:text-white rounded-lg transition-colors relative group",
                                showCommandPalette && "bg-violet-600/20 text-white"
                              )}
                            >
                              <CommandIcon className="w-4 h-4" />
                              <motion.span
                                className="absolute inset-0 bg-slate-700/30 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
                                layoutId="button-highlight"
                              />
                            </motion.button>
                          </div> */}

                          {/* <motion.button
                                        type="button"
                                        onClick={handleSendMessage}
                                        whileHover={{ scale: 1.01 }}
                                        whileTap={{ scale: 0.98 }}
                                        disabled={isTyping || !value.trim()}
                                        className={cn(
                                            "px-4 py-2 rounded-lg text-sm font-medium transition-all",
                                            "flex items-center gap-2",
                                            value.trim()
                                                ? "bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20"
                                                : "bg-slate-700/50 text-slate-500"
                                        )}
                                    >
                                        {isTyping ? (
                                            <LoaderIcon className="w-4 h-4 animate-[spin_2s_linear_infinite]" />
                                        ) : (
                                            <SendIcon className="w-4 h-4" />
                                        )}
                                        <span>Send</span>
                                    </motion.button> */}
                        {/* </div> */}
                      </motion.div>
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <div className="flex justify-end space-x-3">
                {/* <Button
                  type="button"
                  variant="outline"
                  onClick={() => navigate("/assessments")}
                  className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
                >
                  Cancel
                </Button> */}
                <Button
                  type="submit"
                  disabled={isSseLoading || isCheckingGitHub}
                  className="bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20 border-0"
                >
                  {isCheckingGitHub ? "Checking GitHub..." : isSseLoading ? "Creating..." : "Create Assessment"}
                </Button>
              </div>
              
              {/* Real-time Progress Indicator */}
              {isSseLoading && (
                <motion.div
                  className="mt-4 p-4 bg-slate-800/60 border border-slate-700/50 rounded-lg backdrop-blur-sm"
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.3 }}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
                    <div className="flex-1">
                      <div className="text-sm font-medium text-white">Creating your assessment</div>
                      <div className="text-xs text-slate-400 mt-1">
                        {isConnected ? "Connected to AI - Processing..." : "Connecting to AI..."}
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}
            </form>
          </Form>

        </motion.div>
      </div>

      {/* <AnimatePresence>
        {isTyping && (
          <motion.div
            className="fixed bottom-8 mx-auto transform -translate-x-1/2 backdrop-blur-xl bg-slate-800/90 rounded-full px-4 py-2 shadow-lg border border-slate-700/50"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-7 rounded-full bg-slate-700/50 flex items-center justify-center text-center">
                <span className="text-xs font-medium text-white mb-0.5">zap</span>
              </div>
              <div className="flex items-center gap-2 text-sm text-slate-300">
                <span>Thinking</span>
                <TypingDots />
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence> */}

      {/* {inputFocused && (
        <motion.div
          className="fixed w-[50rem] h-[50rem] rounded-full pointer-events-none z-0 opacity-[0.03] bg-gradient-to-r from-violet-500 via-fuchsia-500 to-indigo-500 blur-[96px]"
          animate={{
            x: mousePosition.x - 400,
            y: mousePosition.y - 400,
          }}
          transition={{
            type: "spring",
            damping: 25,
            stiffness: 150,
            mass: 0.5,
          }}
        />
      )} */}
    </div>
  );
}

// function TypingDots() {
//   return (
//     <div className="flex items-center ml-1">
//       {[1, 2, 3].map((dot) => (
//         <motion.div
//           key={dot}
//           className="w-1.5 h-1.5 bg-white rounded-full mx-0.5"
//           initial={{ opacity: 0.3 }}
//           animate={{
//             opacity: [0.3, 0.9, 0.3],
//             scale: [0.85, 1.1, 0.85]
//           }}
//           transition={{
//             duration: 1.2,
//             repeat: Infinity,
//             delay: dot * 0.15,
//             ease: "easeInOut",
//           }}
//           style={{
//             boxShadow: "0 0 4px rgba(255, 255, 255, 0.3)"
//           }}
//         />
//       ))}
//     </div>
//   );
// }

// interface ActionButtonProps {
//   icon: React.ReactNode;
//   label: string;
// }

// function ActionButton({ icon, label }: ActionButtonProps) {
//   const [isHovered, setIsHovered] = useState(false);

//   return (
//     <motion.button
//       type="button"
//       whileHover={{ scale: 1.05, y: -2 }}
//       whileTap={{ scale: 0.97 }}
//       onHoverStart={() => setIsHovered(true)}
//       onHoverEnd={() => setIsHovered(false)}
//       className="flex items-center gap-2 px-4 py-2 bg-neutral-900 hover:bg-neutral-800 rounded-full border border-neutral-800 text-neutral-400 hover:text-white transition-all relative overflow-hidden group"
//     >
//       <div className="relative z-10 flex items-center gap-2">
//         {icon}
//         <span className="text-xs relative z-10">{label}</span>
//       </div>

//       <AnimatePresence>
//         {isHovered && (
//           <motion.div
//             className="absolute inset-0 bg-gradient-to-r from-violet-500/10 to-indigo-500/10"
//             initial={{ opacity: 0 }}
//             animate={{ opacity: 1 }}
//             exit={{ opacity: 0 }}
//             transition={{ duration: 0.2 }}
//           />
//         )}
//       </AnimatePresence>

//       <motion.span
//         className="absolute bottom-0 left-0 w-full h-0.5 bg-gradient-to-r from-violet-500 to-indigo-500"
//         initial={{ width: 0 }}
//         whileHover={{ width: "100%" }}
//         transition={{ duration: 0.3 }}
//       />
//     </motion.button>
//   );
// }

const rippleKeyframes = `
@keyframes ripple {
  0% { transform: scale(0.5); opacity: 0.6; }
  100% { transform: scale(2); opacity: 0; }
}
`;

if (typeof document !== 'undefined') {
  const style = document.createElement('style');
  style.innerHTML = rippleKeyframes;
  document.head.appendChild(style);
}

export default function CreateAssessment() {
  return (
    <CreateAssessmentForm />
  );
}