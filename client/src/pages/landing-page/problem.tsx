import React, { useState } from 'react';
import { Calendar, Clock, Video, Phone, MapPin, Copy, Code, X, Brain } from 'lucide-react';
import { Button } from '@/components/ui/button';
import FeatureGrid from '@/components/layout/features';
import { Link } from 'wouter';

/*
1. the best talent isn't doing leetcode problems anymore. They're building products, shipping code, and making real contributions
2. traditional data structures and algorithms problems don't reflect the skills that technical professionals need to have to be successful in their role
3. easy to cheat on due to AI - leaving you catfished once they show up for the job
*/
const Problem = () => {
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
                <div className="text-lg text-white/80">The Cost of Guessing</div>
            </nav>

            {/* Hero Section */}
            <div className="text-center py-12">
                <h1 className="text-5xl font-lora md:text-6xl mb-8 leading-tight">
                    Why hiring great tech talent is harder than ever
                </h1>
                <p className="text-xl text-gray-400 mb-12 max-w-2xl mx-auto">
                    In a constantly evolving industry, technical hiring hasn't changed in 20 years
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
            <FeatureGrid features={[
                {
                    icon: <Code size={24} />,
                    title: "The top candidates aren't doing LeetCode problems",
                    description: "They're building products, shipping code, and making real contributions instead of memorizing algorithms."
                },
                {
                    icon: <Brain size={24} />,
                    title: "Throwing darts in the dark",
                    description: "Data structures and algorithms code screens leave you with candidates who lack the practical skills that impact your team's success."
                },
                {
                    icon: <X size={24} />,
                    title: "Easy to cheat on",
                    description: "AI tools make it easier than ever to cheat on assessments, clogging up your pipeline with candidates who can't deliver."
                }
            ]} />
        </div>
    );
};

export default Problem;