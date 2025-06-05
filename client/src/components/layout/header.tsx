import { UserSwitch } from "@/components/user-switch";
import { Bell, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";

interface HeaderProps {
  title: string;
  setSidebarOpen: (open: boolean) => void;
}

export function Header({ title, setSidebarOpen }: HeaderProps) {
  return (
    <div className="relative z-10 flex-shrink-0 flex h-16 bg-white shadow">
      <div className="flex-1 px-4 flex justify-between">
        <div className="flex-1 flex md:ml-0">
          {/* Mobile menu button */}
          <div className="md:hidden flex items-center">
            <button
              type="button"
              className="inline-flex items-center justify-center p-2 rounded-md text-gray-700 hover:text-white hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
              onClick={() => setSidebarOpen(true)}
            >
              <span className="sr-only">Open sidebar</span>
              <svg
                className="h-6 w-6"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M4 6h16M4 12h16M4 18h16"
                />
              </svg>
            </button>
          </div>
          
          {/* Page title */}
          <div className="w-full flex items-center ml-4">
            <h1 className="text-2xl font-medium text-gray-800 md:block hidden">
              {title}
            </h1>
            <h1 className="text-xl font-semibold text-gray-800 md:hidden">
              {title}
            </h1>
          </div>
        </div>
        
        <div className="ml-4 flex items-center md:ml-6">
          {/* Notifications */}
          <Button variant="ghost" size="icon">
            <Bell className="h-5 w-5" />
            <span className="sr-only">View notifications</span>
          </Button>

          <Button variant="ghost" size="icon">
            <Plus className="h-5 w-5" />
            <span className="sr-only">Create</span>
          </Button>
          
          {/* User switcher (for demo) */}
          <div className="ml-3 relative">
            <UserSwitch />
          </div>
        </div>
      </div>
    </div>
  );
}
