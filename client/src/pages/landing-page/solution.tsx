import React, { useState } from 'react';
import { Calendar, Clock, Video, Phone, MapPin, Copy } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Typewriter } from '@/components/ui/typewriter';

/**
 * 1. repository-based assessments that reflect real-world bugs and challenges they would face on the job
 * 2. Candidates are assessed on their ability to parse and build upon AI-generated code
 * 3. Coming soon - Complete analysis of their style of AI-assisted programming
 */
const Solution = () => {
    const [selectedDay, setSelectedDay] = useState('Mon');

    const availabilityData = [
        { day: 'Mon', start: '8:30 am', end: '5:00 pm' },
        { day: 'Tue', start: '9:00 am', end: '6:30 pm' },
        { day: 'Wed', start: '10:00 am', end: '7:00 pm' }
    ];

    return (
        <div className="min-h-screen bg-slate-800 text-white relative flex max-w-6xl flex-col">
            {/* Navigation */}
            <nav className="flex justify-center items-center px-8">
                <div className="text-lg text-white/80">The Solution</div>
            </nav>

            {/* Hero Section */}
            <div className="text-center py-12">
                <h1 className="text-5xl font-lora md:text-6xl mb-8 leading-tight">
                    <p className="whitespace-pre-wrap">
                        <span>{"Find your next superstar "}</span>
                        <Typewriter
                            text={[
                                "engineer",
                                "researcher",
                                "developer",
                                "scientist",
                                "analyst"
                            ]}
                            speed={70}
                            className="text-purple-300"
                            waitTime={1500}
                            deleteSpeed={40}
                            cursorChar={"_"}
                        />
                    </p>
                </h1>
                <p className="text-xl text-gray-400 mb-12 max-w-2xl mx-auto">
                    Make the hire that's right for your team
                </p>
                {/* <p className="text-xl text-gray-400 mb-12 max-w-2xl mx-auto">
                    Traditional data structures and algorithms problems don't reflect the skills that technical professionals need to have to be successful in their role.
                </p> */}
                <Button className="bg-gray-800 hover:bg-gray-700 text-white px-8 py-3 rounded-full border border-gray-600 transition-colors flex items-center mx-auto">
                    Get started
                    <svg className="w-4 h-4 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                </Button>
            </div>

            {/* Features Grid */}
            <div className="py-8">
                <div className="grid md:grid-cols-3 gap-8 max-w-7xl mx-auto">
                    {/* Feature 1 - Calendar Connection */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700 relative overflow-hidden">
                        <div className="mb-6">
                            <span className="text-gray-500 text-sm">01</span>
                            <h3 className="text-2xl mt-2 mb-4 font-lora">Describe the role</h3>
                            <p className="text-gray-300">
                                Fill out a short form to describe the role you're hiring for.
                            </p>
                        </div>

                        {/* Calendar Visualization */}
                        <div className="relative mt-12">
                            <div className="w-40 h-40 mx-auto border-2 border-gray-600 rounded-full flex items-center justify-center bg-gray-900">
                                <div className="text-center">
                                    <Calendar className="w-8 h-8 mx-auto mb-2 text-blue-400" />
                                    <span className="text-sm font-medium">Cal.com</span>
                                </div>
                            </div>
                            {/* Connected Apps */}
                            <div className="absolute top-4 left-8 w-8 h-8 bg-blue-500 rounded-lg flex items-center justify-center">
                                <Calendar className="w-4 h-4" />
                            </div>
                            <div className="absolute top-16 right-8 w-8 h-8 bg-red-500 rounded-lg flex items-center justify-center">
                                <Calendar className="w-4 h-4" />
                            </div>
                            <div className="absolute bottom-8 left-4 w-8 h-8 bg-purple-500 rounded-lg flex items-center justify-center">
                                <Calendar className="w-4 h-4" />
                            </div>
                        </div>
                    </div>

                    {/* Feature 2 - Availability */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <div className="mb-6">
                            <span className="text-gray-500 text-sm">02</span>
                            <h3 className="text-2xl mt-2 mb-4 font-lora">Repository creation</h3>
                            <p className="text-gray-300">
                                We'll design the assessment and store the files in a private GitHub repository.
                            </p>
                        </div>

                        {/* Availability Schedule */}
                        <div className="space-y-3">
                            {availabilityData.map((item, index) => (
                                <div
                                    key={item.day}
                                    className={`flex items-center justify-between p-3 rounded-lg border transition-colors cursor-pointer ${selectedDay === item.day
                                        ? 'bg-gray-700 border-gray-500'
                                        : 'bg-gray-900 border-gray-600 hover:bg-gray-700'
                                        }`}
                                    onClick={() => setSelectedDay(item.day)}
                                >
                                    <span className="text-gray-300 w-12">{item.day}</span>
                                    <span className="text-sm text-gray-400">{item.start}</span>
                                    <span className="text-sm text-gray-400">{item.end}</span>
                                    <Copy className="w-4 h-4 text-gray-500" />
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Feature 3 - Meeting Options */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <div className="mb-6">
                            <span className="text-gray-500 text-sm">03</span>
                            <h3 className="text-2xl mt-2 mb-4 font-lora">Invite candidates</h3>
                            <p className="text-gray-300">
                                Just enter their name and email and we'll take care of inviting and notifying them.
                            </p>
                        </div>

                        {/* Meeting Options */}
                        <div className="flex justify-center items-end space-x-2 mt-12">
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Video className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Phone className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-blue-600 hover:bg-blue-500 rounded-lg transition-colors">
                                <MapPin className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Clock className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Calendar className="w-6 h-6" />
                            </button>
                        </div>
                    </div>
                </div>
                <div className="py-4"></div>
                <div className="grid md:grid-cols-2 gap-8 max-w-7xl mx-auto">
                    {/* Feature 4 - Completing the assessment */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <div className="mb-6">
                            <span className="text-gray-500 text-sm">04</span>
                            <h3 className="text-2xl mt-2 mb-4 font-lora">Completing the assessment</h3>
                            <p className="text-gray-300">
                                Candidates will receive a secure link to complete the assessment. They'll have their own private GitHub repository to work on, and they'll submit a pull request with their changes for review when they're finished.
                            </p>
                        </div>

                        {/* Meeting Options */}
                        <div className="flex justify-center items-end space-x-2 mt-12">
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Video className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Phone className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-blue-600 hover:bg-blue-500 rounded-lg transition-colors">
                                <MapPin className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Clock className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Calendar className="w-6 h-6" />
                            </button>
                        </div>
                    </div>

                    {/* Feature 5 - Evaluating the assessment */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <div className="mb-6">
                            <span className="text-gray-500 text-sm">05</span>
                            <h3 className="text-2xl mt-2 mb-4 font-lora">Evaluations</h3>
                            <p className="text-gray-300">
                                Easily keep track of all pull requests and contact candidates directly through your dashboard.
                            </p>
                        </div>

                        {/* Meeting Options */}
                        <div className="flex justify-center items-end space-x-2 mt-12">
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Video className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Phone className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-blue-600 hover:bg-blue-500 rounded-lg transition-colors">
                                <MapPin className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Clock className="w-6 h-6" />
                            </button>
                            <button className="p-3 bg-gray-700 hover:bg-gray-600 rounded-lg transition-colors">
                                <Calendar className="w-6 h-6" />
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Solution;