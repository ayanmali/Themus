import { useState } from "react";
import { Sidebar } from "./sidebar";
import { Header } from "./header";
import { Button } from "@/components/ui/button";
import { Plus } from "lucide-react";
import { Link } from "wouter";

interface AppShellProps {
  children: React.ReactNode;
  title: string;
}

export function AppShell({ children, title }: AppShellProps) {
  const [sidebarOpen, setSidebarOpen] = useState(true);

  return (
    <div className="h-screen flex overflow-hidden bg-slate-700">
      {/* Sidebar for larger screens and mobile when open */}
      <Sidebar open={sidebarOpen} setOpen={setSidebarOpen} />

      {/* Main content */}
      <div className="flex flex-col w-0 flex-1 overflow-hidden">
        <Header setSidebarOpen={setSidebarOpen} />

        <main className="flex-1 relative overflow-y-auto focus:outline-none">
          <div className="mt-4">
            {/* Title and Create button container */}
            <div className="flex items-center justify-between ml-8 mr-8 mb-4">
              <div>
                <h1 className="text-2xl font-medium text-gray-100 md:block hidden">
                  {title}
                </h1>

                <h1 className="text-xl font-semibold text-gray-100 md:hidden">
                  {title}
                </h1>
              </div>
              {title === "Assessments" && (
                <Link to="/assessments/new">
                <Button className="bg-blue-600 hover:bg-blue-700 text-white">
                    <Plus size={16} className="mr-2" />
                    New 
                </Button>
                </Link>
              )}
          </div>

          <div className="max-w-7xl mx-auto px-4 sm:px-6 md:px-8">
            {children}
          </div>
      </div>
    </main>
      </div >
    </div >
  );
}
