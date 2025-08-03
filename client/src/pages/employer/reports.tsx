import { AppShell } from "@/components/layout/app-shell";
import { Card, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FileDown, FileText, Users, CheckCircle, Clock } from "lucide-react";
import { useAuth } from "@/contexts/AuthContext";
import { navigate } from "wouter/use-browser-location";

export default function EmployerReports() {
  const { isAuthenticated } = useAuth();

  return (
    <AppShell title="Reports">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-lg leading-6 font-medium text-gray-900">Assessment Reports</h2>
        <div className="flex space-x-2">
          <Button variant="outline">
            <FileDown className="-ml-1 mr-2 h-5 w-5 text-gray-500" />
            Export Data
          </Button>
        </div>
      </div>
      
      {/* Summary Cards */}
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4 mb-6">
        <Card>
          <CardContent className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-primary rounded-md p-3">
                <FileText className="h-6 w-6 text-white" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Assessments</dt>
                  <dd>
                    <div className="text-lg font-medium text-gray-900">24</div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-green-600 rounded-md p-3">
                <Users className="h-6 w-6 text-white" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Total Candidates</dt>
                  <dd>
                    <div className="text-lg font-medium text-gray-900">86</div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-green-600 rounded-md p-3">
                <CheckCircle className="h-6 w-6 text-white" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Completion Rate</dt>
                  <dd>
                    <div className="text-lg font-medium text-gray-900">78%</div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-5">
            <div className="flex items-center">
              <div className="flex-shrink-0 bg-orange-500 rounded-md p-3">
                <Clock className="h-6 w-6 text-white" />
              </div>
              <div className="ml-5 w-0 flex-1">
                <dl>
                  <dt className="text-sm font-medium text-gray-500 truncate">Avg. Completion Time</dt>
                  <dd>
                    <div className="text-lg font-medium text-gray-900">2.4 days</div>
                  </dd>
                </dl>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
      
      {/* Charts Placeholder */}
      <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
        <Card>
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg font-medium leading-6 text-gray-900">Completion Rates by Assessment</h3>
            <div className="mt-2 h-64 bg-gray-100 rounded-md flex items-center justify-center">
              <p className="text-gray-500 text-sm">Chart visualization would appear here</p>
            </div>
          </div>
        </Card>
        
        <Card>
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg font-medium leading-6 text-gray-900">Assessment Difficulty Comparison</h3>
            <div className="mt-2 h-64 bg-gray-100 rounded-md flex items-center justify-center">
              <p className="text-gray-500 text-sm">Chart visualization would appear here</p>
            </div>
          </div>
        </Card>
        
        <Card className="lg:col-span-2">
          <div className="px-4 py-5 sm:p-6">
            <h3 className="text-lg font-medium leading-6 text-gray-900">Assessment Performance Trends</h3>
            <div className="mt-2 h-64 bg-gray-100 rounded-md flex items-center justify-center">
              <p className="text-gray-500 text-sm">Chart visualization would appear here</p>
            </div>
          </div>
        </Card>
      </div>
    </AppShell>
  );
}
