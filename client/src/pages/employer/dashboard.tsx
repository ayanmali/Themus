// import { AppShell } from "@/components/layout/app-shell";
// import { Card, CardContent } from "@/components/ui/card";
// import { 
//   FileText, Users, CheckCircle, 
//   File, User, Clock, Calendar 
// } from "lucide-react";

// export default function EmployerDashboard() {
//   return (
//     <AppShell title="Dashboard">
//       {/* Summary Cards */}
//       <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
//         <Card>
//           <CardContent className="p-5">
//             <div className="flex items-center">
//               <div className="flex-shrink-0 bg-primary rounded-md p-3">
//                 <FileText className="h-6 w-6 text-white" />
//               </div>
//               <div className="ml-5 w-0 flex-1">
//                 <dl>
//                   <dt className="text-sm font-medium text-gray-500 truncate">Active Assessments</dt>
//                   <dd>
//                     <div className="text-lg font-medium text-gray-900">12</div>
//                   </dd>
//                 </dl>
//               </div>
//             </div>
//           </CardContent>
//           <div className="bg-gray-50 px-5 py-3">
//             <div className="text-sm">
//               <a href="/employer/assessments" className="font-medium text-primary hover:text-primary/90">View all</a>
//             </div>
//           </div>
//         </Card>

//         <Card>
//           <CardContent className="p-5">
//             <div className="flex items-center">
//               <div className="flex-shrink-0 bg-green-600 rounded-md p-3">
//                 <Users className="h-6 w-6 text-white" />
//               </div>
//               <div className="ml-5 w-0 flex-1">
//                 <dl>
//                   <dt className="text-sm font-medium text-gray-500 truncate">Active Candidates</dt>
//                   <dd>
//                     <div className="text-lg font-medium text-gray-900">48</div>
//                   </dd>
//                 </dl>
//               </div>
//             </div>
//           </CardContent>
//           <div className="bg-gray-50 px-5 py-3">
//             <div className="text-sm">
//               <a href="/employer/candidates" className="font-medium text-primary hover:text-primary/90">View all</a>
//             </div>
//           </div>
//         </Card>

//         <Card>
//           <CardContent className="p-5">
//             <div className="flex items-center">
//               <div className="flex-shrink-0 bg-orange-500 rounded-md p-3">
//                 <CheckCircle className="h-6 w-6 text-white" />
//               </div>
//               <div className="ml-5 w-0 flex-1">
//                 <dl>
//                   <dt className="text-sm font-medium text-gray-500 truncate">Completed Assessments</dt>
//                   <dd>
//                     <div className="text-lg font-medium text-gray-900">32</div>
//                   </dd>
//                 </dl>
//               </div>
//             </div>
//           </CardContent>
//           <div className="bg-gray-50 px-5 py-3">
//             <div className="text-sm">
//               <a href="/employer/assessments" className="font-medium text-primary hover:text-primary/90">View all</a>
//             </div>
//           </div>
//         </Card>
//       </div>

//       {/* Recent Activity */}
//       <div className="mt-8">
//         <h2 className="text-lg leading-6 font-medium text-gray-900">Recent Activity</h2>
//         <div className="mt-2 bg-white shadow overflow-hidden sm:rounded-md">
//           <ul className="divide-y divide-gray-200">
//             <li>
//               <a href="#" className="block hover:bg-gray-50">
//                 <div className="px-4 py-4 sm:px-6">
//                   <div className="flex items-center justify-between">
//                     <p className="text-sm font-medium text-primary truncate">John Doe completed Front-end Developer Assessment</p>
//                     <div className="ml-2 flex-shrink-0 flex">
//                       <p className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">Completed</p>
//                     </div>
//                   </div>
//                   <div className="mt-2 sm:flex sm:justify-between">
//                     <div className="sm:flex">
//                       <p className="flex items-center text-sm text-gray-500">
//                         <File className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
//                         Pull Request #142
//                       </p>
//                     </div>
//                     <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
//                       <Calendar className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
//                       <p>Completed <time dateTime="2023-01-15">Jan 15, 2023</time></p>
//                     </div>
//                   </div>
//                 </div>
//               </a>
//             </li>
//             <li>
//               <a href="#" className="block hover:bg-gray-50">
//                 <div className="px-4 py-4 sm:px-6">
//                   <div className="flex items-center justify-between">
//                     <p className="text-sm font-medium text-primary truncate">Alice Johnson started Backend Developer Assessment</p>
//                     <div className="ml-2 flex-shrink-0 flex">
//                       <p className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">In Progress</p>
//                     </div>
//                   </div>
//                   <div className="mt-2 sm:flex sm:justify-between">
//                     <div className="sm:flex">
//                       <p className="flex items-center text-sm text-gray-500">
//                         <Clock className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
//                         2 days remaining
//                       </p>
//                     </div>
//                     <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
//                       <Calendar className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
//                       <p>Started <time dateTime="2023-01-12">Jan 12, 2023</time></p>
//                     </div>
//                   </div>
//                 </div>
//               </a>
//             </li>
//             <li>
//               <a href="#" className="block hover:bg-gray-50">
//                 <div className="px-4 py-4 sm:px-6">
//                   <div className="flex items-center justify-between">
//                     <p className="text-sm font-medium text-primary truncate">New Assessment Created: DevOps Engineer</p>
//                     <div className="ml-2 flex-shrink-0 flex">
//                       <p className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">New</p>
//                     </div>
//                   </div>
//                   <div className="mt-2 sm:flex sm:justify-between">
//                     <div className="sm:flex">
//                       <p className="flex items-center text-sm text-gray-500">
//                         <User className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
//                         5 Candidates assigned
//                       </p>
//                     </div>
//                     <div className="mt-2 flex items-center text-sm text-gray-500 sm:mt-0">
//                       <Calendar className="flex-shrink-0 mr-1.5 h-5 w-5 text-gray-400" />
//                       <p>Created <time dateTime="2023-01-10">Jan 10, 2023</time></p>
//                     </div>
//                   </div>
//                 </div>
//               </a>
//             </li>
//           </ul>
//         </div>
//       </div>
//     </AppShell>
//   );
// }

import React from 'react';
import {
  Users,
  FileText,
  GitPullRequest,
  Clock,
  Plus,
  Eye,
  TrendingUp,
  CheckCircle,
  AlertCircle,
  Calendar,
  Filter
} from 'lucide-react';
import { AppShell } from '@/components/layout/app-shell';
import { API_URL } from '@/lib/utils';
import { navigate } from 'wouter/use-browser-location';
import { useAuth } from '@/hooks/use-auth';
import useApi from '@/hooks/useapi';

const EmployerDashboard = () => {
  const { isAuthenticated } = useAuth();
  const { apiCall } = useApi();

  if (!isAuthenticated) {
    navigate("/login");
  }

  const getData = async () => {
    const [stats, recentActivity] = await Promise.all(
      [getStats(), getRecentActivity()]
    );
    return { stats, recentActivity };
  }

  const getStats = async () => {
    const response = await apiCall("api/assessments/stats", {
      method: 'GET',
    });
    return response.json();
  }
  
  const getRecentActivity = async () => {
    const response = await apiCall("api/assessments/recent-activity", {
      method: 'GET',
    });
    return response.json();
  }

  // Mock data - replace with actual API calls
  const stats = {
    activeAssessments: 12,
    totalInvited: 284,
    ongoingAttempts: 47,
    pendingReviews: 23
  };

  const recentActivity = [
    { id: 1, type: 'submission', candidate: 'Sarah Chen', assessment: 'Full Stack Developer', time: '2 hours ago', status: 'completed' },
    { id: 2, type: 'start', candidate: 'Mike Johnson', assessment: 'React Developer', time: '4 hours ago', status: 'in-progress' },
    { id: 3, type: 'review', candidate: 'Alex Kumar', assessment: 'Backend Engineer', time: '6 hours ago', status: 'under-review' },
    { id: 4, type: 'invitation', candidate: 'Lisa Wang', assessment: 'DevOps Engineer', time: '8 hours ago', status: 'invited' }
  ];

  const activeAssessments = [
    { id: 1, title: 'Full Stack Developer Assessment', invited: 45, started: 12, completed: 8, deadline: '2025-06-28' },
    { id: 2, title: 'React Developer Challenge', invited: 32, started: 18, completed: 15, deadline: '2025-06-25' },
    { id: 3, title: 'Backend API Development', invited: 28, started: 9, completed: 4, deadline: '2025-07-02' },
    { id: 4, title: 'DevOps Engineer Assessment', invited: 22, started: 8, completed: 6, deadline: '2025-06-30' }
  ];

  const pendingPullRequests = [
    { id: 1, candidate: 'John Doe', assessment: 'Full Stack Dev', repo: 'ecommerce-app', time: '3 hours ago' },
    { id: 2, candidate: 'Emma Wilson', assessment: 'React Challenge', repo: 'todo-app', time: '5 hours ago' },
    { id: 3, candidate: 'David Park', assessment: 'Backend API', repo: 'user-service', time: '1 day ago' },
    { id: 4, candidate: 'Maria Garcia', assessment: 'DevOps', repo: 'deployment-config', time: '2 days ago' }
  ];

  const StatCard = ({ icon, title, value, subtitle, trend }: { icon: React.ReactNode, title: string, value: number, subtitle: string, trend: string }) => (
    <div className="bg-gray-800/50 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6 hover:bg-gray-800/70 transition-all duration-200">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="p-2 bg-blue-600/20 rounded-lg">
            {icon}
          </div>
          <div>
            <p className="text-gray-400 text-sm font-medium">{title}</p>
            <p className="text-2xl font-bold text-white">{value}</p>
          </div>
        </div>
        {trend && (
          <div className="flex items-center space-x-1 text-green-400">
            <TrendingUp className="w-4 h-4" />
            <span className="text-sm font-medium">{trend}</span>
          </div>
        )}
      </div>
      {subtitle && <p className="text-gray-500 text-xs mt-2">{subtitle}</p>}
    </div>
  );

  const ActivityItem = ({ activity }: { activity: any }) => {
    const getStatusColor = (status: string) => {
      switch (status) {
        case 'completed': return 'text-green-400 bg-green-400/20';
        case 'in-progress': return 'text-blue-400 bg-blue-400/20';
        case 'under-review': return 'text-yellow-400 bg-yellow-400/20';
        case 'invited': return 'text-purple-400 bg-purple-400/20';
        default: return 'text-gray-400 bg-gray-400/20';
      }
    };

    const getActivityIcon = (type: string) => {
      switch (type) {
        case 'submission': return <CheckCircle className="w-4 h-4" />;
        case 'start': return <Clock className="w-4 h-4" />;
        case 'review': return <Eye className="w-4 h-4" />;
        case 'invitation': return <Users className="w-4 h-4" />;
        default: return <FileText className="w-4 h-4" />;
      }
    };

    return (
      <div className="flex items-center space-x-4 p-4 hover:bg-gray-800/30 rounded-lg transition-colors">
        <div className="p-2 bg-gray-700/50 rounded-lg">
          {getActivityIcon(activity.type)}
        </div>
        <div className="flex-1">
          <p className="text-white font-medium">{activity.candidate}</p>
          <p className="text-gray-400 text-sm">{activity.assessment}</p>
        </div>
        <div className="text-right">
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(activity.status)}`}>
            {activity.status.replace('-', ' ')}
          </span>
          <p className="text-gray-500 text-xs mt-1">{activity.time}</p>
        </div>
      </div>
    );
  };

  return (

    // <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900 text-white">
    //   <div className="container mx-auto px-6 py-8">
      <AppShell>

        {/* Header */}
          <div className="flex items-center justify-between mb-8">
            <div>
              <h1 className="text-3xl font-bold text-white mb-2">Welcome Back, John</h1>
              <p className="text-gray-400 flex items-center space-x-2">
                <Calendar className="w-4 h-4" />
                <span>Saturday, June 21, 2025</span>
              </p>
            </div>
            {/* <div className="flex space-x-3">
              <button className="bg-blue-600 hover:bg-blue-700 px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                <Plus className="w-4 h-4" />
                <span>Create Assessment</span>
              </button>
              <button className="bg-gray-700 hover:bg-gray-600 px-4 py-2 rounded-lg font-medium transition-colors flex items-center space-x-2">
                <Eye className="w-4 h-4" />
                <span>View All</span>
              </button>
            </div> */}
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
            <StatCard
              icon={<FileText className="w-5 h-5 text-blue-400" />}
              title="Active Assessments"
              value={stats.activeAssessments}
              subtitle="Currently running"
              trend="+2 this week"
            />
            <StatCard
              icon={<Users className="w-5 h-5 text-green-400" />}
              title="Total Invited"
              value={stats.totalInvited}
              subtitle="Candidates invited"
              trend="+18 today"
            />
            <StatCard
              icon={<Clock className="w-5 h-5 text-yellow-400" />}
              title="Ongoing Attempts"
              value={stats.ongoingAttempts}
              subtitle="Currently taking assessments"
              trend="+1 today"
            />
            <StatCard
              icon={<GitPullRequest className="w-5 h-5 text-purple-400" />}
              title="Pending Reviews"
              value={stats.pendingReviews}
              subtitle="Pull requests to review"
              trend="5 urgent"
            />
          </div>

          {/* Main Content Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left Column - Recent Activity & Active Assessments */}
            <div className="lg:col-span-2 space-y-8">
              {/* Recent Activity */}
              <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-semibold text-white">Recent Activity</h2>
                  <button className="text-blue-400 hover:text-blue-300 flex items-center space-x-1">
                    <Filter className="w-4 h-4" />
                    <span className="text-sm">Filter</span>
                  </button>
                </div>
                <div className="space-y-2">
                  {recentActivity.map(activity => (
                    <ActivityItem key={activity.id} activity={activity} />
                  ))}
                </div>
              </div>

              {/* Active Assessments */}
              <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6">
                <h2 className="text-xl font-semibold text-white mb-6">Active Assessments</h2>
                <div className="space-y-4">
                  {activeAssessments.map(assessment => (
                    <div key={assessment.id} className="p-4 bg-gray-700/30 rounded-lg hover:bg-gray-700/50 transition-colors">
                      <div className="flex items-center justify-between mb-3">
                        <h3 className="font-medium text-white">{assessment.title}</h3>
                        <span className="text-sm text-gray-400">Due: {assessment.deadline}</span>
                      </div>
                      <div className="grid grid-cols-3 gap-4 text-sm">
                        <div>
                          <p className="text-gray-400">Invited</p>
                          <p className="text-white font-medium">{assessment.invited}</p>
                        </div>
                        <div>
                          <p className="text-gray-400">Started</p>
                          <p className="text-blue-400 font-medium">{assessment.started}</p>
                        </div>
                        <div>
                          <p className="text-gray-400">Completed</p>
                          <p className="text-green-400 font-medium">{assessment.completed}</p>
                        </div>
                      </div>
                      <div className="mt-3">
                        <div className="w-full bg-gray-600 rounded-full h-2">
                          <div
                            className="bg-blue-500 h-2 rounded-full transition-all duration-300"
                            style={{ width: `${(assessment.completed / assessment.invited) * 100}%` }}
                          ></div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            {/* Right Column - Quick Stats & Pending PRs */}
            <div className="space-y-8">
              {/* Quick Overview */}
              <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6">
                <h2 className="text-xl font-semibold text-white mb-6">Quick Overview</h2>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Completion Rate</span>
                    <span className="text-green-400 font-medium">68%</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Avg. Time to Complete</span>
                    <span className="text-white font-medium">2.5 days</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Top Performing Assessment</span>
                    <span className="text-blue-400 font-medium">React Challenge</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-gray-400">Reviews This Week</span>
                    <span className="text-purple-400 font-medium">34</span>
                  </div>
                </div>
              </div>

              {/* Pending Pull Requests */}
              <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6">
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-semibold text-white">Pending Reviews</h2>
                  <span className="bg-red-600/20 text-red-400 px-2 py-1 rounded-full text-xs font-medium">
                    {pendingPullRequests.length} urgent
                  </span>
                </div>
                <div className="space-y-3">
                  {pendingPullRequests.map(pr => (
                    <div key={pr.id} className="p-3 bg-gray-700/30 rounded-lg hover:bg-gray-700/50 transition-colors cursor-pointer">
                      <div className="flex items-center space-x-3">
                        <GitPullRequest className="w-4 h-4 text-purple-400 flex-shrink-0" />
                        <div className="flex-1 min-w-0">
                          <p className="text-white font-medium truncate">{pr.candidate}</p>
                          <p className="text-gray-400 text-xs">{pr.assessment}</p>
                          <p className="text-gray-500 text-xs font-mono">{pr.repo}</p>
                        </div>
                      </div>
                      <p className="text-gray-500 text-xs mt-2">{pr.time}</p>
                    </div>
                  ))}
                </div>
              </div>

              {/* System Alerts */}
              <div className="bg-gray-800/30 backdrop-blur-sm border border-gray-700/50 rounded-xl p-6">
                <h2 className="text-xl font-semibold text-white mb-4">System Alerts</h2>
                <div className="space-y-3">
                  <div className="flex items-start space-x-3 p-3 bg-yellow-600/10 border border-yellow-600/20 rounded-lg">
                    <AlertCircle className="w-4 h-4 text-yellow-400 mt-0.5 flex-shrink-0" />
                    <div>
                      <p className="text-yellow-400 text-sm font-medium">Assessment Expiring Soon</p>
                      <p className="text-gray-400 text-xs">React Developer Challenge expires in 4 days</p>
                    </div>
                  </div>
                  <div className="flex items-start space-x-3 p-3 bg-green-600/10 border border-green-600/20 rounded-lg">
                    <CheckCircle className="w-4 h-4 text-green-400 mt-0.5 flex-shrink-0" />
                    <div>
                      <p className="text-green-400 text-sm font-medium">All Systems Operational</p>
                      <p className="text-gray-400 text-xs">Repository integration working normally</p>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
      </AppShell>
    // </div>
  );
};

export default EmployerDashboard;