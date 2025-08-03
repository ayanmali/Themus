import React, { useState } from 'react';
import { ChevronDown, Calendar, Clock, Globe, Video, User } from 'lucide-react';
import { Button } from '@/components/ui/button';

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
                <h1 className="text-5xl font-gfs-didot md:text-6xl font-bold mb-8 leading-tight">
                    The new way to screen candidates
                </h1>
                <p className="text-xl text-gray-400 mb-12 max-w-2xl mx-auto">
                    Ditch the old hiring process and screen candidates easily, with confidence
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
                        <h3 className="text-2xl font-bold mb-4">Avoid meeting overload</h3>
                        <p className="text-gray-400 mb-8">
                            Only get booked when you want to. Set daily, weekly or monthly limits and add buffers around your events to allow you to focus or take a break.
                            <h3 className="text-2xl font-bold mb-4">Notice and buffers</h3>

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
                        </p>
                    </div>

                    {/* Custom booking link */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <h3 className="text-2xl font-bold mb-4">Stand out with a custom booking link</h3>
                        <p className="text-gray-400 mb-8">
                            Customize your booking link so it's short and easy to remember for your bookers. No more long, complicated links one can easily forget.
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

                    {/* Streamline bookers' experience */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <h3 className="text-2xl font-bold mb-4">Streamline your bookers' experience</h3>
                        <p className="text-gray-400 mb-8">
                            Let your bookers overlay their calendar, receive booking confirmations via text or email, get events added to their calendar, and allow them to reschedule with ease.
                        </p>

                        {/* Calendar Preview */}
                        <div className="bg-gray-900 p-6 rounded-2xl border border-gray-600">
                            <div className="flex justify-between items-center mb-4">
                                <h4 className="text-lg font-medium">Overlay my calendar</h4>
                                <div className="flex space-x-2">
                                    <button className="px-3 py-1 bg-gray-700 text-xs rounded">12h</button>
                                    <button className="px-3 py-1 bg-gray-600 text-xs rounded">24h</button>
                                </div>
                            </div>

                            <div className="grid grid-cols-5 gap-2">
                                {['Wed 06', 'Thu 07', 'Fri 08', 'Sat 09', 'Sun 10'].map((day, index) => (
                                    <div key={day} className="text-center">
                                        <div className="text-sm text-gray-400 mb-2">{day}</div>
                                        <div className="h-32 bg-gray-800 rounded-lg border border-gray-700 relative">
                                            {/* Sample calendar blocks */}
                                            {index === 1 && (
                                                <div className="absolute top-2 left-1 right-1 h-6 bg-blue-600 rounded opacity-60"></div>
                                            )}
                                            {index === 2 && (
                                                <>
                                                    <div className="absolute top-4 left-1 right-1 h-4 bg-green-600 rounded opacity-60"></div>
                                                    <div className="absolute bottom-4 left-1 right-1 h-8 bg-purple-600 rounded opacity-60"></div>
                                                </>
                                            )}
                                            {index === 4 && (
                                                <div className="absolute top-8 left-1 right-1 h-6 bg-red-600 rounded opacity-60"></div>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    {/* Reduce no-shows */}
                    <div className="bg-gray-800 p-8 rounded-3xl border border-gray-700">
                        <h3 className="text-2xl font-bold mb-4">Reduce no-shows with automated meeting reminders</h3>
                        <p className="text-gray-400 mb-8">
                            Easily send sms or meeting reminder emails about bookings, and send automated follow-ups to gather any relevant information before the meeting.
                        </p>

                        {/* Notification Preview */}
                        <div className="bg-gray-900 p-4 rounded-2xl border border-gray-600">
                            <div className="flex items-center space-x-3">
                                <div className="w-8 h-8 bg-white text-black rounded-lg flex items-center justify-center font-bold text-sm">
                                    Cal
                                </div>
                                <div className="flex-1">
                                    <div className="flex justify-between items-start">
                                        <div>
                                            <div className="text-sm font-medium">Booking rescheduled</div>
                                            <div className="text-xs text-gray-400">
                                                Joshua Smith has rescheduled the meeting to Wed, 30 Mar 15:00
                                            </div>
                                        </div>
                                        <div className="text-xs text-gray-500">16 mins</div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Benefits;