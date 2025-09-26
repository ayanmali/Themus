import React, { useState } from 'react';
import { Calendar, Clock, Video, Phone, MapPin, Copy } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Typewriter } from '@/components/ui/typewriter';
import { Link } from 'wouter';

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
                            speed={40}
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
                        <div className="bg-slate-900/70 border border-white/15 rounded-xl p-6 shadow-lg shadow-black/20">
                                <div className="space-y-4">
                                    {/* Form Header */}
                                    <div className="flex items-center gap-2 mb-4">
                                        <div className="w-2 h-2 bg-green-400 rounded-full"></div>
                                        <div className="w-2 h-2 bg-yellow-400 rounded-full"></div>
                                        <div className="w-2 h-2 bg-red-400 rounded-full"></div>
                                        {/* <span className="text-xs text-gray-400 ml-2">Create Assessment</span> */}
                                    </div>

                                    {/* Role Field */}
                                    <div className="space-y-2">
                                        <label className="text-xs text-gray-400 block">Role</label>
                                        <div className="bg-slate-800/50 border border-white/10 rounded-md px-3 py-2">
                                            <div className="h-4 bg-gray-600/30 rounded animate-pulse"></div>
                                        </div>
                                    </div>

                                    {/* Skills Field */}
                                    <div className="space-y-2">
                                        <label className="text-xs text-gray-400 block">Skills</label>
                                        <div className="bg-slate-800/50 border border-white/10 rounded-md px-3 py-2">
                                            <div className="flex flex-wrap gap-1">
                                                <div className="h-4 w-16 bg-blue-500/20 rounded animate-pulse"></div>
                                                <div className="h-4 w-20 bg-green-500/20 rounded animate-pulse"></div>
                                                <div className="h-4 w-14 bg-purple-500/20 rounded animate-pulse"></div>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Description Field */}
                                    <div className="space-y-2">
                                        <label className="text-xs text-gray-400 block">Description</label>
                                        <div className="bg-slate-800/50 border border-white/10 rounded-md px-3 py-3">
                                            <div className="space-y-1">
                                                <div className="h-3 bg-gray-600/30 rounded animate-pulse"></div>
                                                <div className="h-3 bg-gray-600/30 rounded animate-pulse w-3/4"></div>
                                                <div className="h-3 bg-gray-600/30 rounded animate-pulse w-1/2"></div>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Duration Field */}
                                    <div className="space-y-2">
                                        <label className="text-xs text-gray-400 block">Duration</label>
                                        <div className="bg-slate-800/50 border border-white/10 rounded-md px-3 py-2">
                                            <div className="h-4 bg-gray-600/30 rounded animate-pulse w-20"></div>
                                        </div>
                                    </div>

                                    {/* Create Button */}
                                    <div className="pt-2">
                                        <div className="bg-blue-600/20 border border-blue-500/30 rounded-md px-4 py-2 text-center">
                                            <span className="text-sm text-blue-300">Create Assessment</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                    </div>

                    {/* Feature 2 - Repository creation */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <div className="mb-6">
                            <span className="text-gray-500 text-sm">02</span>
                            <h3 className="text-2xl mt-2 mb-4 font-lora">Repository creation</h3>
                            <p className="text-gray-300">
                                We'll design the assessment and store the files in a private GitHub repository.
                            </p>
                        </div>

                        {/* GitHub Repository Visual */}
                        <div className="space-y-3">
                            {/* Repository Header */}
                            <div className="flex items-center gap-2 mb-4">
                                <div className="w-4 h-4 bg-gray-600 rounded"></div>
                                <span className="text-sm text-gray-300 font-mono">acme-corp/swe-intern-assessment</span>
                                {/* <span className="text-xs bg-green-600/20 text-green-400 px-2 py-1 rounded">Public</span> */}
                            </div>

                            {/* File Tree */}
                            <div className="bg-gray-900/50 rounded-lg p-3 border border-gray-700">
                                <div className="space-y-1 text-sm font-mono">
                                    <div className="flex items-center gap-2 text-gray-400">
                                        <span>📁</span>
                                        <span>src/</span>
                                    </div>
                                    <div className="ml-4 space-y-1">
                                        <div className="flex items-center gap-2 text-gray-300">
                                            <span>📄</span>
                                            <span>main.py</span>
                                        </div>
                                        <div className="flex items-center gap-2 text-gray-300">
                                            <span>📄</span>
                                            <span>requirements.txt</span>
                                        </div>
                                        <div className="flex items-center gap-2 text-gray-300">
                                            <span>📄</span>
                                            <span>README.md</span>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-2 text-gray-400">
                                        <span>📁</span>
                                        <span>tests/</span>
                                    </div>
                                    <div className="ml-4">
                                        <div className="flex items-center gap-2 text-gray-300">
                                            <span>📄</span>
                                            <span>test_main.py</span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Recent Commits */}
                            <div className="space-y-2">
                                <h4 className="text-sm text-gray-400">Recent commits</h4>
                                <div className="space-y-2">
                                    <div className="flex items-center gap-2 text-xs">
                                        <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                                        <span className="text-gray-300">Initial assessment setup</span>
                                        <span className="text-gray-500">2 hours ago</span>
                                    </div>
                                    <div className="flex items-center gap-2 text-xs">
                                        <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                                        <span className="text-gray-300">Add test cases</span>
                                        <span className="text-gray-500">1 hour ago</span>
                                    </div>
                                </div>
                            </div>

                            {/* Repository Stats */}
                            {/* <div className="flex items-center gap-4 text-xs text-gray-400">
                                <div className="flex items-center gap-1">
                                    <span>⭐</span>
                                    <span>12</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <span>🍴</span>
                                    <span>3</span>
                                </div>
                                <div className="flex items-center gap-1">
                                    <span>👁️</span>
                                    <span>45</span>
                                </div>
                            </div> */}
                        </div>
                    </div>

                    {/* Feature 3 - Invite candidates */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <div className="mb-6">
                            <span className="text-gray-500 text-sm">03</span>
                            <h3 className="text-2xl mt-2 mb-4 font-lora">Invite candidates</h3>
                            <p className="text-gray-300">
                                Just enter their name and email and we'll take care of inviting and notifying them.
                            </p>
                        </div>

                        {/* Invite candidates form */}
                        <div className="space-y-4">
                            {/* Form Header */}
                            <div className="flex items-center gap-2 mb-4">
                                <div className="w-2 h-2 bg-green-400 rounded-full"></div>
                                <div className="w-2 h-2 bg-yellow-400 rounded-full"></div>
                                <div className="w-2 h-2 bg-red-400 rounded-full"></div>
                                <span className="text-xs text-gray-400 ml-2">Add Candidates</span>
                            </div>

                            {/* Candidate Form */}
                            <div className="bg-gray-900/50 rounded-lg p-4 border border-gray-700">
                                <div className="space-y-3">
                                    {/* Name Field */}
                                    <div className="space-y-1">
                                        <label className="text-xs text-gray-400 block">Full Name</label>
                                        <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2">
                                            <div className="h-4 bg-gray-600/30 rounded animate-pulse"></div>
                                        </div>
                                    </div>

                                    {/* Email Field */}
                                    <div className="space-y-1">
                                        <label className="text-xs text-gray-400 block">Email Address</label>
                                        <div className="bg-gray-800/50 border border-gray-600 rounded-md px-3 py-2">
                                            <div className="h-4 bg-gray-600/30 rounded animate-pulse"></div>
                                        </div>
                                    </div>

                                    {/* Add Button */}
                                    <div className="pt-2">
                                        <div className="bg-blue-600/20 border border-blue-500/30 rounded-md px-4 py-2 text-center">
                                            <span className="text-sm text-blue-300">+ Add Candidate</span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* Candidate List Preview */}
                            <div className="space-y-2">
                                <h4 className="text-sm text-gray-400">Invited candidates</h4>
                                <div className="space-y-2">
                                    <div className="flex items-center justify-between bg-gray-900/30 rounded-md px-3 py-2">
                                        <div className="flex items-center gap-2">
                                            <div className="w-6 h-6 bg-blue-500/20 rounded-full flex items-center justify-center">
                                                <span className="text-xs text-blue-300">JS</span>
                                            </div>
                                            <span className="text-sm text-gray-300">John Smith</span>
                                        </div>
                                        <span className="text-xs text-gray-500">john@example.com</span>
                                    </div>
                                    <div className="flex items-center justify-between bg-gray-900/30 rounded-md px-3 py-2">
                                        <div className="flex items-center gap-2">
                                            <div className="w-6 h-6 bg-green-500/20 rounded-full flex items-center justify-center">
                                                <span className="text-xs text-green-300">AS</span>
                                            </div>
                                            <span className="text-sm text-gray-300">Alice Johnson</span>
                                        </div>
                                        <span className="text-xs text-gray-500">alice@example.com</span>
                                    </div>
                                </div>
                            </div>

                            {/* Send Invites Button */}
                            <div className="pt-2">
                                <div className="bg-green-600/20 border border-green-500/30 rounded-md px-4 py-2 text-center">
                                    <span className="text-sm text-green-300">📧 Send Invitations</span>
                                </div>
                            </div>
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

                        {/* Candidate Assessment Flow */}
                        <div className="space-y-4">
                            {/* Candidate Access */}
                            <div className="bg-gray-900/50 rounded-lg p-4 border border-gray-700">
                                <div className="flex items-center gap-2 mb-3">
                                    <div className="w-6 h-6 bg-blue-500/20 rounded-full flex items-center justify-center">
                                        <span className="text-xs text-blue-300">👤</span>
                                    </div>
                                    <span className="text-sm text-gray-300">john.smith@example.com</span>
                                    <span className="text-xs bg-green-600/20 text-green-400 px-2 py-1 rounded">Active</span>
                                </div>
                                
                                <div className="space-y-2">
                                    <div className="flex items-center gap-2 text-xs text-gray-400">
                                        <span>🔗</span>
                                        <span>Private repository: acme-corp/swe-intern-assessment-john</span>
                                    </div>
                                    <div className="flex items-center gap-2 text-xs text-gray-400">
                                        <span>⏰</span>
                                        <span>Time remaining: 2h 15m</span>
                                    </div>
                                </div>
                            </div>

                            {/* Code Changes */}
                            <div className="bg-gray-900/50 rounded-lg p-4 border border-gray-700">
                                <div className="flex items-center gap-2 mb-3">
                                    <span className="text-sm text-gray-400">Recent changes</span>
                                    <div className="flex gap-1">
                                        <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                                        <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                                        <div className="w-2 h-2 bg-yellow-500 rounded-full"></div>
                                    </div>
                                </div>
                                
                                <div className="space-y-1 text-xs font-mono">
                                    <div className="flex items-center gap-2">
                                        <span className="text-green-400">+</span>
                                        <span className="text-gray-300">src/api/endpoints.py</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <span className="text-blue-400">~</span>
                                        <span className="text-gray-300">src/models/user.py</span>
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <span className="text-yellow-400">+</span>
                                        <span className="text-gray-300">tests/test_api.py</span>
                                    </div>
                                </div>
                            </div>

                            {/* Pull Request Status */}
                            <div className="bg-gray-900/50 rounded-lg p-4 border border-gray-700">
                                <div className="flex items-center justify-between mb-2">
                                    <span className="text-sm text-gray-300">Pull Request</span>
                                    <span className="text-xs bg-yellow-600/20 text-yellow-400 px-2 py-1 rounded">Draft</span>
                                </div>
                                
                                <div className="space-y-2">
                                    <div className="text-xs text-gray-400">
                                        <span className="font-semibold">feat:</span> Implement user authentication and API endpoints
                                    </div>
                                    <div className="flex items-center gap-4 text-xs text-gray-500">
                                        <span>📝 3 commits</span>
                                        <span>📁 5 files changed</span>
                                        <span>➕ 127 additions</span>
                                    </div>
                                </div>
                            </div>

                            {/* Submit Button */}
                            <div className="pt-2">
                                <div className="bg-green-600/20 border border-green-500/30 rounded-md px-4 py-2 text-center">
                                    <span className="text-sm text-green-300">🚀 Submit for Review</span>
                                </div>
                            </div>
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

                        {/* Assessment Dashboard */}
                        <div className="space-y-4">
                            {/* Dashboard Header */}
                            <div className="flex items-center gap-2 mb-4">
                                <div className="w-2 h-2 bg-green-400 rounded-full"></div>
                                <div className="w-2 h-2 bg-yellow-400 rounded-full"></div>
                                <div className="w-2 h-2 bg-red-400 rounded-full"></div>
                                {/* <span className="text-xs text-gray-400 ml-2">Assessment Dashboard</span> */}
                            </div>

                            {/* Assessment Stats */}
                            <div className="grid grid-cols-3 gap-3 mb-4">
                                <div className="bg-gray-900/50 rounded-lg p-3 text-center">
                                    <div className="text-lg font-semibold text-white">12</div>
                                    <div className="text-xs text-gray-400">Total</div>
                                </div>
                                <div className="bg-gray-900/50 rounded-lg p-3 text-center">
                                    <div className="text-lg font-semibold text-green-400">8</div>
                                    <div className="text-xs text-gray-400">Completed</div>
                                </div>
                                <div className="bg-gray-900/50 rounded-lg p-3 text-center">
                                    <div className="text-lg font-semibold text-yellow-400">4</div>
                                    <div className="text-xs text-gray-400">Pending</div>
                                </div>
                            </div>

                            {/* Candidate Attempts List */}
                            <div className="space-y-2">
                                <h4 className="text-sm text-gray-400">Recent submissions</h4>
                                
                                {/* Attempt 1 */}
                                <div className="bg-gray-900/50 rounded-lg p-3 border border-gray-700">
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center gap-2">
                                            <div className="w-6 h-6 bg-blue-500/20 rounded-full flex items-center justify-center">
                                                <span className="text-xs text-blue-300">JS</span>
                                            </div>
                                            <span className="text-sm text-gray-300">John Smith</span>
                                        </div>
                                        <span className="text-xs bg-green-600/20 text-green-400 px-2 py-1 rounded">Completed</span>
                                    </div>
                                    <div className="text-xs text-gray-500">Submitted 2 hours ago</div>
                                </div>

                                {/* Attempt 2 */}
                                <div className="bg-gray-900/50 rounded-lg p-3 border border-gray-700">
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center gap-2">
                                            <div className="w-6 h-6 bg-green-500/20 rounded-full flex items-center justify-center">
                                                <span className="text-xs text-green-300">AJ</span>
                                            </div>
                                            <span className="text-sm text-gray-300">Alice Johnson</span>
                                        </div>
                                        <span className="text-xs bg-yellow-600/20 text-yellow-400 px-2 py-1 rounded">In Progress</span>
                                    </div>
                                    <div className="text-xs text-gray-500">Started 1 day ago</div>
                                </div>

                                {/* Attempt 3 */}
                                <div className="bg-gray-900/50 rounded-lg p-3 border border-gray-700">
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center gap-2">
                                            <div className="w-6 h-6 bg-purple-500/20 rounded-full flex items-center justify-center">
                                                <span className="text-xs text-purple-300">MB</span>
                                            </div>
                                            <span className="text-sm text-gray-300">Mike Brown</span>
                                        </div>
                                        <span className="text-xs bg-green-600/20 text-green-400 px-2 py-1 rounded">Completed</span>
                                    </div>
                                    <div className="text-xs text-gray-500">Submitted 3 days ago</div>
                                </div>
                            </div>

                            {/* View All Button */}
                            <div className="pt-2">
                                <div className="bg-blue-600/20 border border-blue-500/30 rounded-md px-4 py-2 text-center">
                                    <span className="text-sm text-blue-300">📊 View All Attempts</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Solution;