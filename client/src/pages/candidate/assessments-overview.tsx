import React, { useState } from 'react';
import {
    Clock,
    CheckCircle,
    AlertCircle,
    FileText,
    Calendar,
    Filter,
    Search,
    Eye,
    Download,
    ExternalLink,
    GitBranch,
    Code,
    Trophy,
    TrendingUp,
    Bell
} from 'lucide-react';

const CandidateDashboard = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [statusFilter, setStatusFilter] = useState('all');

    // Mock candidate data
    const candidateInfo = {
        name: "Alex Johnson",
        email: "alex.johnson@email.com",
        totalAssessments: 8,
        completed: 6,
        ongoing: 1,
        recentEvaluations: 2
    };

    const assessments = [
        {
            id: 1,
            assessmentName: "Full Stack E-commerce Platform",
            roleName: "Senior Full Stack Developer",
            status: "evaluated",
            language: "React/Node.js",
            dateStarted: "2025-06-15",
            dateCompleted: "2025-06-18",
            dateEvaluated: "2025-06-20",
            score: 85,
            feedback: true
        },
        {
            id: 2,
            assessmentName: "Microservices API Gateway",
            roleName: "Backend Engineer",
            status: "submitted",
            language: "Go",
            dateStarted: "2025-06-10",
            dateCompleted: "2025-06-12",
            dateEvaluated: null,
            score: null,
            feedback: false
        },
        {
            id: 3,
            assessmentName: "React Component Library",
            roleName: "Frontend Developer",
            status: "started",
            language: "React/TypeScript",
            dateStarted: "2025-06-22",
            dateCompleted: null,
            dateEvaluated: null,
            score: null,
            feedback: false
        },
        {
            id: 4,
            assessmentName: "Data Pipeline Optimization",
            roleName: "Data Engineer",
            status: "evaluated",
            language: "Python/Django",
            dateStarted: "2025-06-01",
            dateCompleted: "2025-06-05",
            dateEvaluated: "2025-06-08",
            score: 92,
            feedback: true
        },
        {
            id: 5,
            assessmentName: "Mobile Banking App",
            roleName: "Mobile Developer",
            status: "expired",
            language: "React Native",
            dateStarted: "2025-05-20",
            dateCompleted: null,
            dateEvaluated: null,
            score: null,
            feedback: false
        },
        {
            id: 6,
            assessmentName: "DevOps Infrastructure",
            roleName: "DevOps Engineer",
            status: "evaluated",
            language: "Terraform/Docker",
            dateStarted: "2025-05-15",
            dateCompleted: "2025-05-18",
            dateEvaluated: "2025-05-22",
            score: 78,
            feedback: true
        }
    ];

    const recentActivity = [
        { id: 1, type: 'evaluation', message: 'Full Stack E-commerce Platform has been evaluated', time: '2 hours ago', status: 'positive' },
        { id: 2, type: 'feedback', message: 'Feedback available for Data Pipeline Optimization', time: '1 day ago', status: 'info' },
        { id: 3, type: 'reminder', message: 'React Component Library assessment expires in 5 days', time: '2 days ago', status: 'warning' }
    ];

    const StatCard = ({ icon, title, value, subtitle, color = "blue" }: { icon: React.ReactNode, title: string, value: string, subtitle: string, color: string }) => {
        const colorClasses = {
            blue: "bg-blue-600/20 text-blue-400",
            green: "bg-green-600/20 text-green-400",
            yellow: "bg-yellow-600/20 text-yellow-400",
            purple: "bg-purple-600/20 text-purple-400"
        };

        return (
            <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6 hover:bg-gray-800/70 transition-all duration-200">
                <div className="flex items-center space-x-4">
                    <div className={`p-3 rounded-lg ${colorClasses[color as keyof typeof colorClasses]}`}>
                        {icon}
                    </div>
                    <div>
                        <p className="text-2xl font-bold text-white">{value}</p>
                        <p className="text-gray-400 text-sm font-medium">{title}</p>
                        {subtitle && <p className="text-gray-500 text-xs mt-1">{subtitle}</p>}
                    </div>
                </div>
            </div>
        );
    };

    const getStatusBadge = (status: string) => {
        const statusConfig = {
            started: { color: 'bg-blue-600/20 text-blue-400 border-blue-600/30', label: 'In Progress' },
            submitted: { color: 'bg-yellow-600/20 text-yellow-400 border-yellow-600/30', label: 'Under Review' },
            evaluated: { color: 'bg-green-600/20 text-green-400 border-green-600/30', label: 'Completed' },
            expired: { color: 'bg-red-600/20 text-red-400 border-red-600/30', label: 'Expired' }
        };

        const config = statusConfig[status as keyof typeof statusConfig] || statusConfig.started;
        return (
            <span className={`px-3 py-1 rounded-full text-xs font-medium border ${config.color}`}>
                {config.label}
            </span>
        );
    };

    const getScoreBadge = (score: number) => {
        if (!score) return null;

        let colorClass = 'bg-gray-600/20 text-gray-400 border-gray-600/30';
        if (score >= 90) colorClass = 'bg-green-600/20 text-green-400 border-green-600/30';
        else if (score >= 80) colorClass = 'bg-blue-600/20 text-blue-400 border-blue-600/30';
        else if (score >= 70) colorClass = 'bg-yellow-600/20 text-yellow-400 border-yellow-600/30';
        else colorClass = 'bg-red-600/20 text-red-400 border-red-600/30';

        return (
            <span className={`px-2 py-1 rounded-md text-xs font-medium border ${colorClass}`}>
                {score}%
            </span>
        );
    };

    const filteredAssessments = assessments.filter(assessment => {
        const matchesSearch = assessment.assessmentName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            assessment.roleName.toLowerCase().includes(searchTerm.toLowerCase()) ||
            assessment.language.toLowerCase().includes(searchTerm.toLowerCase());
        const matchesStatus = statusFilter === 'all' || assessment.status === statusFilter;
        return matchesSearch && matchesStatus;
    });

    const formatDate = (dateString: string) => {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 text-white">
            <div className="container mx-auto px-6 py-8">
                {/* Header */}
                <div className="flex flex-col lg:flex-row lg:items-center justify-between mb-8">
                    <div>
                        <h1 className="text-3xl font-bold text-white mb-2">
                            Welcome back, {candidateInfo.name}
                        </h1>
                        <p className="text-gray-400 flex items-center space-x-2">
                            <Calendar className="w-4 h-4" />
                            <span>Saturday, June 24, 2025</span>
                        </p>
                    </div>
                    <div className="flex space-x-3 mt-4 lg:mt-0">
                        <button className="bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                            <FileText className="w-4 h-4" />
                            <span>View Available Assessments</span>
                        </button>
                        <button className="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                            <Bell className="w-4 h-4" />
                            <span>Notifications</span>
                        </button>
                    </div>
                </div>

                {/* Stats Cards */}
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
                    <StatCard
                        icon={<Clock className="w-6 h-6" />}
                        title="Ongoing Assessments"
                        value={candidateInfo.ongoing.toString()}
                        subtitle="Currently in progress"
                        color="blue"
                    />
                    <StatCard
                        icon={<CheckCircle className="w-6 h-6" />}
                        title="Total Completed"
                        value={candidateInfo.completed.toString()}
                        subtitle="Successfully submitted"
                        color="green"
                    />
                    <StatCard
                        icon={<Trophy className="w-6 h-6" />}
                        title="Recent Evaluations"
                        value={candidateInfo.recentEvaluations.toString()}
                        subtitle="New feedback available"
                        color="purple"
                    />
                    <StatCard
                        icon={<TrendingUp className="w-6 h-6" />}
                        title="Average Score"
                        value="85%"
                        subtitle="Across all assessments"
                        color="yellow"
                    />
                </div>

                {/* Main Content Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
                    {/* Assessment Table - Takes up 3/4 width */}
                    <div className="lg:col-span-3">
                        <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6">
                            <div className="flex flex-col lg:flex-row lg:items-center justify-between mb-6">
                                <h2 className="text-xl font-semibold text-white mb-4 lg:mb-0">Assessment History</h2>

                                {/* Search and Filter */}
                                <div className="flex flex-col sm:flex-row space-y-3 sm:space-y-0 sm:space-x-3">
                                    <div className="relative">
                                        <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                                        <input
                                            type="text"
                                            placeholder="Search assessments..."
                                            className="bg-gray-700/50 border border-gray-600/50 rounded-lg pl-10 pr-4 py-2 text-white placeholder-gray-400 focus:outline-none focus:border-blue-500 w-full sm:w-64"
                                            value={searchTerm}
                                            onChange={(e) => setSearchTerm(e.target.value)}
                                        />
                                    </div>
                                    <select
                                        className="bg-gray-700/50 border border-gray-600/50 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-blue-500"
                                        value={statusFilter}
                                        onChange={(e) => setStatusFilter(e.target.value)}
                                    >
                                        <option value="all">All Status</option>
                                        <option value="started">In Progress</option>
                                        <option value="submitted">Under Review</option>
                                        <option value="evaluated">Completed</option>
                                        <option value="expired">Expired</option>
                                    </select>
                                </div>
                            </div>

                            {/* Table */}
                            <div className="overflow-x-auto">
                                <table className="w-full">
                                    <thead>
                                        <tr className="border-b border-gray-700/50">
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Assessment</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Role</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Status</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Language</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Started</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Completed</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Evaluated</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Score</th>
                                            <th className="text-left py-3 px-4 text-gray-400 font-medium">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {filteredAssessments.map((assessment) => (
                                            <tr key={assessment.id} className="border-b border-gray-700/30 hover:bg-gray-800/20 transition-colors">
                                                <td className="py-4 px-4">
                                                    <div className="flex items-center space-x-3">
                                                        <div className="p-2 bg-gray-700/50 rounded-lg">
                                                            <FileText className="w-4 h-4 text-blue-400" />
                                                        </div>
                                                        <div>
                                                            <p className="text-white font-medium">{assessment.assessmentName}</p>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td className="py-4 px-4 text-gray-300">{assessment.roleName}</td>
                                                <td className="py-4 px-4">{getStatusBadge(assessment.status)}</td>
                                                <td className="py-4 px-4">
                                                    <div className="flex items-center space-x-2">
                                                        <Code className="w-4 h-4 text-purple-400" />
                                                        <span className="text-gray-300">{assessment.language}</span>
                                                    </div>
                                                </td>
                                                <td className="py-4 px-4 text-gray-400">{formatDate(assessment.dateStarted)}</td>
                                                <td className="py-4 px-4 text-gray-400">{formatDate(assessment.dateCompleted || '')}</td>
                                                <td className="py-4 px-4 text-gray-400">{formatDate(assessment.dateEvaluated || '')}</td>
                                                <td className="py-4 px-4">{getScoreBadge(assessment.score || 0)}</td>
                                                <td className="py-4 px-4">
                                                    <div className="flex items-center space-x-2">
                                                        {assessment.status === 'started' && (
                                                            <button className="p-2 bg-blue-600/20 hover:bg-blue-600/30 rounded-lg transition-colors" title="Continue Assessment">
                                                                <ExternalLink className="w-4 h-4 text-blue-400" />
                                                            </button>
                                                        )}
                                                        {assessment.feedback && (
                                                            <button className="p-2 bg-green-600/20 hover:bg-green-600/30 rounded-lg transition-colors" title="View Feedback">
                                                                <Eye className="w-4 h-4 text-green-400" />
                                                            </button>
                                                        )}
                                                        {assessment.status === 'evaluated' && (
                                                            <button className="p-2 bg-purple-600/20 hover:bg-purple-600/30 rounded-lg transition-colors" title="Download Report">
                                                                <Download className="w-4 h-4 text-purple-400" />
                                                            </button>
                                                        )}
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    {/* Right Sidebar - Recent Activity */}
                    <div className="lg:col-span-1">
                        <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6">
                            <h2 className="text-xl font-semibold text-white mb-6">Recent Activity</h2>
                            <div className="space-y-4">
                                {recentActivity.map((activity) => {
                                    const getActivityColor = (status: string) => {
                                        switch (status) {
                                            case 'positive': return 'border-l-green-400 bg-green-400/10';
                                            case 'warning': return 'border-l-yellow-400 bg-yellow-400/10';
                                            case 'info': return 'border-l-blue-400 bg-blue-400/10';
                                            default: return 'border-l-gray-400 bg-gray-400/10';
                                        }
                                    };

                                    return (
                                        <div key={activity.id} className={`p-4 rounded-lg border-l-4 ${getActivityColor(activity.status)}`}>
                                            <p className="text-white text-sm font-medium mb-1">{activity.message}</p>
                                            <p className="text-gray-400 text-xs">{activity.time}</p>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>

                        {/* Quick Actions */}
                        <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6 mt-8">
                            <h2 className="text-xl font-semibold text-white mb-4">Quick Actions</h2>
                            <div className="space-y-3">
                                <button className="w-full bg-blue-600/20 hover:bg-blue-600/30 border border-blue-600/30 rounded-lg p-3 text-blue-400 font-medium transition-colors flex items-center space-x-2">
                                    <GitBranch className="w-4 h-4" />
                                    <span>View Repository</span>
                                </button>
                                <button className="w-full bg-green-600/20 hover:bg-green-600/30 border border-green-600/30 rounded-lg p-3 text-green-400 font-medium transition-colors flex items-center space-x-2">
                                    <Download className="w-4 h-4" />
                                    <span>Download Reports</span>
                                </button>
                                <button className="w-full bg-purple-600/20 hover:bg-purple-600/30 border border-purple-600/30 rounded-lg p-3 text-purple-400 font-medium transition-colors flex items-center space-x-2">
                                    <Eye className="w-4 h-4" />
                                    <span>View All Feedback</span>
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CandidateDashboard;