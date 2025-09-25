import React, { useState } from 'react';
import { ChevronDown, Calendar, Clock, Globe, Video, User } from 'lucide-react';
import { Button } from '@/components/ui/button';

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
                <Button className="bg-gray-800 hover:bg-gray-700 text-white px-8 py-3 rounded-full border border-gray-600 transition-colors flex items-center mx-auto">
                    Get started
                    <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                </Button>
            </div>

            {/* Features Grid */}
            <div className="py-8">
                <div className="grid md:grid-cols-2 gap-8 max-w-7xl mx-auto">
                    {/* Avoid meeting overload */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <h3 className="text-2xl mt-2 mb-4 font-lora">Tailored to your team's needs</h3>
                        <div className="text-gray-300 mb-8">
                            You have complete control over the skills you want to screen for and the challenges for candidates to work on. Specify the role you're hiring for, the technologies and skills you're looking for, and a brief description of what you want to assess. We take care of the rest.
                            <p className="text-2xl font-bold mb-4">Notice and buffers</p>

                            <div className="space-y-6">
                                <div>
                                    <label className="block text-sm text-gray-400 mb-2">Minimum notice</label>
                                    <div className="relative">
                                        <select
                                            value={minNotice}
                                            onChange={(e) => setMinNotice(e.target.value)}
                                            className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-3 text-white appearance-none cursor-pointer focus:outline-none focus:border-gray-500"
                                        >
                                            <option>1 hour</option>
                                            <option>2 hours</option>
                                            <option>4 hours</option>
                                            <option>1 day</option>
                                        </select>
                                        <ChevronDown className="absolute right-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                                    </div>
                                </div>

                                <div className="grid grid-cols-2 gap-4">
                                    <div>
                                        <label className="block text-sm text-gray-400 mb-2">Buffer before event</label>
                                        <div className="relative">
                                            <select
                                                value={bufferBefore}
                                                onChange={(e) => setBufferBefore(e.target.value)}
                                                className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-3 text-white appearance-none cursor-pointer focus:outline-none focus:border-gray-500"
                                            >
                                                <option>15 mins</option>
                                                <option>30 mins</option>
                                                <option>1 hour</option>
                                            </select>
                                            <ChevronDown className="absolute right-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                                        </div>
                                    </div>

                                    <div>
                                        <label className="block text-sm text-gray-400 mb-2">Buffer after event</label>
                                        <div className="relative">
                                            <select
                                                value={bufferAfter}
                                                onChange={(e) => setBufferAfter(e.target.value)}
                                                className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-3 text-white appearance-none cursor-pointer focus:outline-none focus:border-gray-500"
                                            >
                                                <option>15 mins</option>
                                                <option>30 mins</option>
                                                <option>1 hour</option>
                                            </select>
                                            <ChevronDown className="absolute right-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                                        </div>
                                    </div>
                                </div>

                                <div>
                                    <label className="block text-sm text-gray-400 mb-2">Time-slot intervals</label>
                                    <div className="relative">
                                        <select
                                            value={timeSlot}
                                            onChange={(e) => setTimeSlot(e.target.value)}
                                            className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-3 text-white appearance-none cursor-pointer focus:outline-none focus:border-gray-500"
                                        >
                                            <option>5 mins</option>
                                            <option>10 mins</option>
                                            <option>15 mins</option>
                                            <option>30 mins</option>
                                        </select>
                                        <ChevronDown className="absolute right-4 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Custom booking link */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <h3 className="text-2xl mt-2 mb-4 font-lora">See how candidates work with AI-generated code</h3>
                        <p className="text-gray-300 mb-8">
                            See how candidates improve half-implemented features and fix bugs introduced by LLMs, essential skills for fast-paced teams that ship reliable, maintainable code fast and often.
                        </p>

                        {/* Booking Link Preview */}
                        <div className="bg-gray-900 p-6 rounded-2xl border border-gray-600">
                            <div className="text-center mb-4">
                                <div className="text-lg font-medium text-gray-300">cal.com/ewa</div>
                            </div>

                            <div className="flex items-center space-x-3 mb-4">
                                <div className="w-10 h-10 bg-gray-700 rounded-full flex items-center justify-center">
                                    <User className="w-5 h-5 text-gray-400" />
                                </div>
                                <div>
                                    <div className="text-sm text-gray-400">Ewa Michalak</div>
                                </div>
                            </div>

                            <div className="mb-4">
                                <h4 className="text-xl font-semibold mb-2">Marketing Strategy Session</h4>
                                <p className="text-sm text-gray-400 mb-4">
                                    Let's collaborate on campaigns, co-marketing opportunities, and learn how Cal.com is approaching growth and brand.
                                </p>
                            </div>

                            <div className="flex space-x-2 mb-4">
                                <span className="px-2 py-1 bg-gray-700 text-xs rounded">15m</span>
                                <span className="px-2 py-1 bg-gray-700 text-xs rounded">30m</span>
                                <span className="px-2 py-1 bg-gray-700 text-xs rounded">45m</span>
                                <span className="px-2 py-1 bg-gray-700 text-xs rounded">1h</span>
                            </div>

                            <div className="flex items-center space-x-2 mb-2">
                                <Video className="w-4 h-4 text-green-400" />
                                <span className="text-sm text-gray-300">Google Meet</span>
                            </div>

                            <div className="flex items-center space-x-2">
                                <Globe className="w-4 h-4 text-gray-400" />
                                <span className="text-sm text-gray-300">Europe/Warsaw</span>
                                <ChevronDown className="w-4 h-4 text-gray-400" />
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Benefits;