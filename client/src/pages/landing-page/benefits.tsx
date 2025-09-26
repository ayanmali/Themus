import React, { useState } from 'react';
import { ChevronDown, Calendar, Clock, Globe, Video, User } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Link } from 'wouter';

/**
 * 
 * 1. Tailored for you - define the problems and skills you're looking for and select the best candidate for your team
 * 2. 
 */
const Benefits = () => {
    const [minNotice, setMinNotice] = useState('1 hour');
    const [bufferBefore, setBufferBefore] = useState('15 mins');
    const [bufferAfter, setBufferAfter] = useState('15 mins');
    const [timeSlot, setTimeSlot] = useState('5 mins');

    return (
        <div className="min-h-screen bg-slate-800 text-white relative flex max-w-6xl flex-col">
            {/* Navigation */}
            <nav className="flex justify-center items-center px-8">
                <div className="text-lg text-white/80">Benefits</div>
            </nav>

            {/* Hero Section */}
            <div className="text-center py-12">
                <h1 className="text-5xl font-lora md:text-6xl mb-8 leading-tight">
                    Why keep guessing?
                </h1>
                <p className="text-xl text-gray-400 mb-12 max-w-2xl mx-auto">
                    Cut through the noise and hire difference-makers who can ship features and fix bugs, not just solve LeetCode problems
                </p>
                <Link href="/signup" className="flex items-center mx-auto">
                    <Button className="bg-gray-800 hover:bg-gray-700 text-white px-8 py-3 rounded-full border border-gray-600 transition-colors flex items-center mx-auto">
                        <span>Get started</span>
                        <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                        </svg>
                    </Button>
                </Link>

            </div>

            {/* Features Grid */}
            <div className="py-8">
                <div className="grid md:grid-cols-2 gap-8 max-w-7xl mx-auto">
                    {/* Avoid meeting overload */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <h3 className="text-2xl mt-2 mb-4 font-lora">Tailored to your team's needs</h3>
                        <div className="text-gray-300 mb-8">
                            You have complete control over the skills you want to screen for and the challenges for candidates to work on. Specify the role you're hiring for, the technologies and skills you're looking for, and a brief description of what you want to assess. We take care of the rest.

                            {/* Assessment Customization Form */}
                            {/* <div className="space-y-4 mt-6"> */}
                            {/* Form Header */}
                            {/* <div className="flex items-center gap-2 mb-4">
                                    <div className="w-2 h-2 bg-green-400 rounded-full"></div>
                                    <div className="w-2 h-2 bg-yellow-400 rounded-full"></div>
                                    <div className="w-2 h-2 bg-red-400 rounded-full"></div>
                                    <span className="text-xs text-gray-400 ml-2">Customize Assessment</span>
                                </div> */}

                            {/* Role Field */}
                            {/* <div className="space-y-2">
                                    <label className="text-xs text-gray-400 block">Role</label>
                                    <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2">
                                        <div className="h-4 bg-gray-600/30 rounded animate-pulse"></div>
                                    </div>
                                </div> */}

                            {/* Skills Field */}
                            {/* <div className="space-y-2">
                                    <label className="text-xs text-gray-400 block">Required Skills</label>
                                    <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2">
                                        <div className="flex flex-wrap gap-1">
                                            <div className="h-4 w-16 bg-blue-500/20 rounded animate-pulse"></div>
                                            <div className="h-4 w-20 bg-green-500/20 rounded animate-pulse"></div>
                                            <div className="h-4 w-14 bg-purple-500/20 rounded animate-pulse"></div>
                                            <div className="h-4 w-18 bg-yellow-500/20 rounded animate-pulse"></div>
                                        </div>
                                    </div>
                                </div> */}

                            {/* Problem Description */}
                            {/* <div className="space-y-2">
                                    <label className="text-xs text-gray-400 block">Problem Description</label>
                                    <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-3">
                                        <div className="space-y-1">
                                            <div className="h-3 bg-gray-600/30 rounded animate-pulse"></div>
                                            <div className="h-3 bg-gray-600/30 rounded animate-pulse w-3/4"></div>
                                            <div className="h-3 bg-gray-600/30 rounded animate-pulse w-1/2"></div>
                                        </div>
                                    </div>
                                </div> */}

                            {/* Duration Field */}
                            {/* <div className="space-y-2">
                                    <label className="text-xs text-gray-400 block">Assessment Duration</label>
                                    <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2">
                                        <div className="h-4 bg-gray-600/30 rounded animate-pulse w-20"></div>
                                    </div>
                                </div> */}

                            {/* Difficulty Level */}
                            {/* <div className="space-y-2">
                                    <label className="text-xs text-gray-400 block">Difficulty Level</label>
                                    <div className="flex gap-2">
                                        <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2 flex-1">
                                            <div className="h-4 bg-gray-600/30 rounded animate-pulse"></div>
                                        </div>
                                        <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2 flex-1">
                                            <div className="h-4 bg-gray-600/30 rounded animate-pulse"></div>
                                        </div>
                                        <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2 flex-1">
                                            <div className="h-4 bg-gray-600/30 rounded animate-pulse"></div>
                                        </div>
                                    </div>
                                </div> */}

                            {/* Generate Button */}
                            {/* <div className="pt-2">
                                    <div className="bg-blue-600/20 border border-blue-500/30 rounded-md px-4 py-2 text-center">
                                        <span className="text-sm text-blue-300">🎯 Generate Assessment</span>
                                    </div>
                                </div> */}
                            {/* </div> */}
                        </div>
                    </div>

                    {/* Custom booking link */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <h3 className="text-2xl mt-2 mb-4 font-lora">See how candidates work with AI-generated code</h3>
                        <p className="text-gray-300 mb-8">
                            See how candidates improve half-implemented features and fix bugs introduced by LLMs, essential skills for fast-paced teams that ship reliable, maintainable code fast and often.
                        </p>

                        {/* Booking Link Preview */}

                    </div>
                </div>
            </div>
        </div>
    );
};

export default Benefits;