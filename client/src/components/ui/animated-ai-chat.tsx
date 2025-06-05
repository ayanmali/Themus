"use client";

import { useEffect, useRef, useCallback, useTransition } from "react";
import { useState } from "react";
import { cn } from "@/lib/utils";
import {
    Paperclip,
    XIcon,
    TabletSmartphone,
    Command,
    Database,
    Code,
    Server
} from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";
import * as React from "react"
import { Button } from "./button";
import { Form, FormField, FormItem, FormLabel, FormMessage } from "./form";
import { FormControl } from "./form";
import { Input } from "./input";
import { useLocation } from "wouter";
import { useToast } from "@/hooks/use-toast";
import { useForm } from "react-hook-form";
import { insertAssessmentSchema } from "@shared/schema";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation } from "@tanstack/react-query";
import { apiRequest, queryClient } from "@/lib/queryClient";
import { z } from "zod";
import { DatePicker } from "./date-picker";

// Create a schema for the assessment creation form
const createAssessmentSchema = insertAssessmentSchema.pick({
    title: true,
    role: true,
    skills: true,
    description: true,
    repositoryLink: true,
    startDate: true,
    endDate: true,
}).extend({
    startDate: z.coerce.date().min(new Date(), { message: "Start date must be in the future" }),
    endDate: z.coerce.date().min(new Date(), { message: "End date must be in the future" }),
});

type CreateAssessmentFormValues = z.infer<typeof createAssessmentSchema>;

interface UseAutoResizeTextareaProps {
    minHeight: number;
    maxHeight?: number;
}

function useAutoResizeTextarea({
    minHeight,
    maxHeight,
}: UseAutoResizeTextareaProps) {
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    const adjustHeight = useCallback(
        (reset?: boolean) => {
            const textarea = textareaRef.current;
            if (!textarea) return;

            if (reset) {
                textarea.style.height = `${minHeight}px`;
                return;
            }

            textarea.style.height = `${minHeight}px`;
            const newHeight = Math.max(
                minHeight,
                Math.min(
                    textarea.scrollHeight,
                    maxHeight ?? Number.POSITIVE_INFINITY
                )
            );

            textarea.style.height = `${newHeight}px`;
        },
        [minHeight, maxHeight]
    );

    useEffect(() => {
        const textarea = textareaRef.current;
        if (textarea) {
            textarea.style.height = `${minHeight}px`;
        }
    }, [minHeight]);

    useEffect(() => {
        const handleResize = () => adjustHeight();
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, [adjustHeight]);

    return { textareaRef, adjustHeight };
}

interface CommandSuggestion {
    icon: React.ReactNode;
    label: string;
    description: string;
    prefix: string;
}

interface TextareaProps
    extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
    containerClassName?: string;
    showRing?: boolean;
}

const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
    ({ className, containerClassName, showRing = true, ...props }, ref) => {
        const [isFocused, setIsFocused] = React.useState(false);

        return (
            <div className={cn(
                "relative",
                containerClassName
            )}>
                <textarea
                    className={cn(
                        "flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm",
                        "transition-all duration-200 ease-in-out",
                        "placeholder:text-muted-foreground",
                        "disabled:cursor-not-allowed disabled:opacity-50",
                        showRing ? "focus-visible:outline-none focus-visible:ring-0 focus-visible:ring-offset-0" : "",
                        className
                    )}
                    ref={ref}
                    onFocus={() => setIsFocused(true)}
                    onBlur={() => setIsFocused(false)}
                    {...props}
                />

                {showRing && isFocused && (
                    <motion.span
                        className="absolute inset-0 rounded-md pointer-events-none ring-2 ring-offset-0 ring-violet-500/30"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        transition={{ duration: 0.2 }}
                    />
                )}

                {props.onChange && (
                    <div
                        className="absolute bottom-2 right-2 opacity-0 w-2 h-2 bg-violet-500 rounded-full"
                        style={{
                            animation: 'none',
                        }}
                        id="textarea-ripple"
                    />
                )}
            </div>
        )
    }
)
Textarea.displayName = "Textarea"

export function AnimatedAIChat() {
    const [value, setValue] = useState("");
    const [attachments, setAttachments] = useState<string[]>([]);
    const [isTyping, setIsTyping] = useState(false);
    const [isPending, startTransition] = useTransition();
    const [activeSuggestion, setActiveSuggestion] = useState<number>(-1);
    const [showCommandPalette, setShowCommandPalette] = useState(false);
    const [recentCommand, setRecentCommand] = useState<string | null>(null);
    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });
    const { textareaRef, adjustHeight } = useAutoResizeTextarea({
        minHeight: 60,
        maxHeight: 200,
    });
    const [inputFocused, setInputFocused] = useState(false);
    const commandPaletteRef = useRef<HTMLDivElement>(null);

    const { toast } = useToast();
    const [, navigate] = useLocation();

    // Setup form
    const form = useForm<CreateAssessmentFormValues>({
        resolver: zodResolver(createAssessmentSchema),
        defaultValues: {
            title: "",
            role: "",
            skills: "",
            description: "",
            repositoryLink: "",
        },
    });

    // Create assessment mutation
    const createAssessmentMutation = useMutation({
        mutationFn: async (data: CreateAssessmentFormValues) => {
            const res = await apiRequest("POST", "/api/assessments", data);
            return await res.json();
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ["/api/assessments"] });
            toast({
                title: "Assessment created",
                description: "Your assessment has been created successfully",
            });
            navigate("/employer/assessments");
        },
        onError: (error: Error) => {
            toast({
                title: "Failed to create assessment",
                description: error.message,
                variant: "destructive",
            });
        },
    });

    const onSubmit = (data: CreateAssessmentFormValues) => {
        createAssessmentMutation.mutate(data);
    };

    const commandSuggestions: CommandSuggestion[] = [
        {
            icon: <Code className="w-4 h-4" />,
            label: "SWE Intern",
            description: "Software Engineering Intern",
            prefix: 
            `Create an assessment for a Software Engineering Intern specializing in frontend development. Test applicants on their proficiency in Next.js, Tailwind CSS, and TypeScript.`
        },
        {
            icon: <Server className="w-4 h-4" />,
            label: "Mid-Level Backend Engineer",
            description: "Backend Engineer",
            prefix: 
            `Create an assessment for a mid-level backend engineer with 3+ years of experience. Test applicants on their proficiency in the Go language, along with skills on performant REST API development, ORMs, creating data models, and caching with Redis.`
        },
        {
            icon: <Database className="w-4 h-4" />,
            label: "Senior MLOps Engineer",
            description: "MLOps Engineer",
            prefix: `Create an assessment for a Senior MLOps Engineer with 5+ years of experience. Test applicants on their proficiency in MLFlow, ML Studio, and Azure ML.`
        },
        {
            icon: <TabletSmartphone className="w-4 h-4" />,
            label: "iOS Engineer",
            description: "iOS Engineer",
            prefix: `Create an assessment for a iOS Engineer with 2+ years of experience. Test applicants on their proficiency in Swift, SwiftUI, and iOS development.`
        },
    ];

    useEffect(() => {
        if (value.startsWith('/') && !value.includes(' ')) {
            setShowCommandPalette(true);

            const matchingSuggestionIndex = commandSuggestions.findIndex(
                (cmd) => cmd.prefix.startsWith(value)
            );

            if (matchingSuggestionIndex >= 0) {
                setActiveSuggestion(matchingSuggestionIndex);
            } else {
                setActiveSuggestion(-1);
            }
        } else {
            setShowCommandPalette(false);
        }
    }, [value]);

    useEffect(() => {
        const handleMouseMove = (e: MouseEvent) => {
            setMousePosition({ x: e.clientX, y: e.clientY });
        };

        window.addEventListener('mousemove', handleMouseMove);
        return () => {
            window.removeEventListener('mousemove', handleMouseMove);
        };
    }, []);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            const target = event.target as Node;
            const commandButton = document.querySelector('[data-command-button]');

            if (commandPaletteRef.current &&
                !commandPaletteRef.current.contains(target) &&
                !commandButton?.contains(target)) {
                setShowCommandPalette(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
        if (showCommandPalette) {
            if (e.key === 'ArrowDown') {
                e.preventDefault();
                setActiveSuggestion(prev =>
                    prev < commandSuggestions.length - 1 ? prev + 1 : 0
                );
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                setActiveSuggestion(prev =>
                    prev > 0 ? prev - 1 : commandSuggestions.length - 1
                );
            } else if (e.key === 'Tab' || e.key === 'Enter') {
                e.preventDefault();
                if (activeSuggestion >= 0) {
                    const selectedCommand = commandSuggestions[activeSuggestion];
                    setValue(selectedCommand.prefix + ' ');
                    setShowCommandPalette(false);

                    setRecentCommand(selectedCommand.label);
                    setTimeout(() => setRecentCommand(null), 3500);
                }
            } else if (e.key === 'Escape') {
                e.preventDefault();
                setShowCommandPalette(false);
            }
        } 
        // else if (e.key === "Enter" && !e.shiftKey) {
        //     e.preventDefault();
        //     if (value.trim()) {
        //         handleSendMessage();
        //     }
        // }
    };

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

    const handleAttachFile = () => {
        const mockFileName = `file-${Math.floor(Math.random() * 1000)}.pdf`;
        setAttachments(prev => [...prev, mockFileName]);
    };

    const removeAttachment = (index: number) => {
        setAttachments(prev => prev.filter((_, i) => i !== index));
    };

    const selectCommandSuggestion = (index: number) => {
        const selectedCommand = commandSuggestions[index];
        setValue(selectedCommand.prefix + ' ');
        setShowCommandPalette(false);

        setRecentCommand(selectedCommand.label);
        setTimeout(() => setRecentCommand(null), 2000);
    };

    return (
        <div className="min-h-screen flex flex-col w-full items-center justify-center bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 text-white p-6 relative overflow-hidden">
            {/* Fixed header with buttons */}
            <div className="fixed top-0 left-0 w-full z-50 bg-slate-900/80 backdrop-blur border-b border-slate-800 shadow-lg flex justify-between px-8 py-4 gap-3">
                <Button
                    type="button"
                    variant="outline"
                    onClick={() => navigate("/assessments")}
                    className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
                >
                    Cancel
                </Button>
                <Button
                    type="submit"
                    form="assessment-form"
                    disabled={createAssessmentMutation.isPending}
                    className="bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20 border-0"
                >
                    {createAssessmentMutation.isPending ? "Creating..." : "Create Assessment"}
                </Button>
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
                            <h1 className="text-3xl font-medium tracking-tight bg-clip-text text-transparent bg-gradient-to-r from-white to-slate-300 pb-3">
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
                                name="title"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel className="text-slate-300 font-medium">Assessment Title</FormLabel>
                                        <FormControl>
                                            <Input
                                                placeholder="e.g., Junior Full-Stack Dev - XXX Team, May 2025"
                                                {...field}
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
                                                {...field}
                                                className="bg-slate-800/60 border-slate-700/50 text-white placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm"
                                            />
                                        </FormControl>
                                        <FormMessage />
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

                            <div>
                                <FormLabel className="text-slate-300 font-medium">Date Range</FormLabel>

                                <div className="flex items-center gap-2 mt-2 justify-center">
                                    <FormControl>
                                        <DatePicker />
                                    </FormControl>
                                    <FormControl>
                                        <DatePicker />
                                    </FormControl>
                                </div>
                            </div>

                            <FormField
                                control={form.control}
                                name="description"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel className="text-slate-300 font-medium">Description</FormLabel>
                                        <FormControl>
                                        <motion.div
                                className="relative backdrop-blur-xl bg-slate-800/90 rounded-2xl border border-slate-700/50 shadow-2xl"
                                initial={{ scale: 0.98 }}
                                animate={{ scale: 1 }}
                                transition={{ delay: 0.1 }}
                            >

                                <AnimatePresence>

                                    {showCommandPalette && (
                                        <motion.div
                                            ref={commandPaletteRef}
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
                                                            activeSuggestion === index
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

                                </AnimatePresence>

                                <div className="p-4">

                                    <Textarea
                                        ref={textareaRef}
                                        value={value}
                                        onChange={(e) => {
                                            setValue(e.target.value);
                                            adjustHeight();
                                        }}
                                        onKeyDown={handleKeyDown}
                                        onFocus={() => setInputFocused(true)}
                                        onBlur={() => setInputFocused(false)}
                                        placeholder={`Enter any other important information about the assessment, role, or company, such as:
                                        - the job description
                                        - the team this role applies to
                                        - technical constraints to follow
                                        - specific problems, bugs, or tasks for candidates to solve, or features to implement`}
                                        containerClassName="w-full"
                                        className={cn(
                                            "w-full px-4 py-3",
                                            "resize-none",
                                            "bg-transparent",
                                            "border-none",
                                            "text-white text-sm",
                                            "focus:outline-none",
                                            "placeholder:text-slate-500",
                                            "min-h-[60px]",
                                            "justify-start",
                                            "text-left"
                                        )}
                                        style={{
                                            overflow: "hidden",
                                        }}
                                        showRing={false}
                                    />
                                </div>

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

                                <div className="p-4 border-t border-slate-700/50 flex items-center justify-between gap-4">
                                    <div className="flex items-center gap-3">
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
                                            <Command className="w-4 h-4" />
                                            <motion.span
                                                className="absolute inset-0 bg-slate-700/30 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity"
                                                layoutId="button-highlight"
                                            />
                                        </motion.button>
                                    </div>

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
                                </div>
                            </motion.div>
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />

                            <div className="flex justify-end space-x-3">
                                <Button
                                    type="button"
                                    variant="outline"
                                    onClick={() => navigate("/assessments")}
                                    className="bg-slate-800/60 border-slate-700/50 text-slate-300 hover:bg-slate-700/80 hover:text-white hover:border-slate-600/50 backdrop-blur-sm"
                                >
                                    Cancel
                                </Button>
                                <Button
                                    type="submit"
                                    disabled={createAssessmentMutation.isPending}
                                    className="bg-violet-600 hover:bg-violet-700 text-white shadow-lg shadow-violet-600/20 border-0"
                                >
                                    {createAssessmentMutation.isPending ? "Creating..." : "Create Assessment"}
                                </Button>
                            </div>
                        </form>
                    </Form>

                </motion.div>
            </div>

            <AnimatePresence>
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
            </AnimatePresence>

            {inputFocused && (
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
            )}
        </div>
    );
}

function TypingDots() {
    return (
        <div className="flex items-center ml-1">
            {[1, 2, 3].map((dot) => (
                <motion.div
                    key={dot}
                    className="w-1.5 h-1.5 bg-white rounded-full mx-0.5"
                    initial={{ opacity: 0.3 }}
                    animate={{
                        opacity: [0.3, 0.9, 0.3],
                        scale: [0.85, 1.1, 0.85]
                    }}
                    transition={{
                        duration: 1.2,
                        repeat: Infinity,
                        delay: dot * 0.15,
                        ease: "easeInOut",
                    }}
                    style={{
                        boxShadow: "0 0 4px rgba(255, 255, 255, 0.3)"
                    }}
                />
            ))}
        </div>
    );
}

interface ActionButtonProps {
    icon: React.ReactNode;
    label: string;
}

function ActionButton({ icon, label }: ActionButtonProps) {
    const [isHovered, setIsHovered] = useState(false);

    return (
        <motion.button
            type="button"
            whileHover={{ scale: 1.05, y: -2 }}
            whileTap={{ scale: 0.97 }}
            onHoverStart={() => setIsHovered(true)}
            onHoverEnd={() => setIsHovered(false)}
            className="flex items-center gap-2 px-4 py-2 bg-neutral-900 hover:bg-neutral-800 rounded-full border border-neutral-800 text-neutral-400 hover:text-white transition-all relative overflow-hidden group"
        >
            <div className="relative z-10 flex items-center gap-2">
                {icon}
                <span className="text-xs relative z-10">{label}</span>
            </div>

            <AnimatePresence>
                {isHovered && (
                    <motion.div
                        className="absolute inset-0 bg-gradient-to-r from-violet-500/10 to-indigo-500/10"
                        initial={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        exit={{ opacity: 0 }}
                        transition={{ duration: 0.2 }}
                    />
                )}
            </AnimatePresence>

            <motion.span
                className="absolute bottom-0 left-0 w-full h-0.5 bg-gradient-to-r from-violet-500 to-indigo-500"
                initial={{ width: 0 }}
                whileHover={{ width: "100%" }}
                transition={{ duration: 0.3 }}
            />
        </motion.button>
    );
}

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


