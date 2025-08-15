import { useAuth } from "@/contexts/AuthContext";
import { Link, useLocation } from "wouter";
import { Code, LayoutDashboard, FileText, Database, Users, ChartBarStacked, User as LucideUser } from "lucide-react";
import { cn } from "@/lib/utils";
import { User } from "@/lib/types/user";

interface SidebarProps {
  open: boolean;
  setOpen: (open: boolean) => void;
}

export function Sidebar({ open, setOpen }: SidebarProps) {
  const {user, isAuthenticated, isLoading } = useAuth();

  const [location] = useLocation();

  const closeSidebar = () => setOpen(false);

  const isActive = (path: string) => {
    return location === path;
  };

  if (isLoading) {
    // Show a sidebar skeleton while loading
    return <div className="w-64 bg-gray-800">Loading...</div>;
  }
  
  if (!isAuthenticated) {
    console.log("Not authenticated, sidebar componentreturning null")
    return null;
  }

  // TODO: replace with actual user data
  // const user: User = {
  //   id: "1",
  //   email: "john.doe@example.com",
  //   password: "password",
  //   name: "Nefarious Joaquin",
  //   role: "employer" as const,
  //   orgName: "Delphi",
  //   createdAt: new Date(),
  // };

  const employerNavItems = [
    {
      name: "Dashboard",
      href: "/dashboard",
      icon: LayoutDashboard,
      active: isActive("/dashboard"),
    },
    {
      name: "Assessments",
      href: "/assessments",
      icon: FileText,
      active: isActive("/assessments"),
    },
    // {
    //   name: "Repositories",
    //   href: "/repositories",
    //   icon: Database,
    //   active: isActive("/repositories"),
    // },
    {
      name: "Candidates",
      href: "/candidates",
      icon: Users,
      active: isActive("/candidates"),
    },
    {
      name: "Reports",
      href: "/reports",
      icon: ChartBarStacked,
      active: isActive("/reports"),
    },
  ];

  const candidateNavItems = [
    {
      name: "My Assessments",
      href: "/candidate/assessments",
      icon: FileText,
      active: isActive("/candidate/assessments"),
    },
    {
      name: "Profile",
      href: "/candidate/profile",
      icon: LucideUser,
      active: isActive("/candidate/profile"),
    },
  ];

  // const navItems = user.role === "employer" ? employerNavItems : candidateNavItems;
  const navItems = employerNavItems;


  // Mobile overlay
  const overlay = open ? (
    <div className="md:hidden fixed inset-0 bg-gray-600 bg-opacity-75 z-20" onClick={closeSidebar} />
  ) : null;

  return (
    <>
      {overlay}
      
      {/* Sidebar for desktop */}
      <div className={cn(
        "md:flex md:flex-shrink-0 transition-all",
        { "hidden": !open, "fixed inset-0 z-30 md:static md:inset-auto": open }
      )}>
        <div className="flex flex-col w-64">
          <div className="flex flex-col h-0 flex-1 bg-gray-800">
            {/* Close button for mobile */}
            {open && (
              <div className="absolute top-0 right-0 -mr-12 pt-2 md:hidden">
                <button
                  onClick={closeSidebar}
                  className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                >
                  <span className="sr-only">Close sidebar</span>
                  <svg className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            )}
            
            {/* Logo */}
            <div className="flex items-center h-16 flex-shrink-0 px-4 bg-slate-800">
              <div className="flex items-center">
                {/* <Code className="h-8 w-8 text-primary" />
                <span className="ml-2 text-white text-xl font-semibold">Themus</span> */}
              </div>
            </div>
            
            {/* Navigation */}
            <div className="flex-1 flex flex-col overflow-y-auto">
              <nav className="flex-1 px-2 py-4 space-y-1">
                {navItems.map((item) => (
                  <Link
                    key={item.name}
                    href={item.href}
                    onClick={closeSidebar}
                    className={cn(
                      "group flex items-center px-2 py-2 text-sm font-medium rounded-md cursor-pointer",
                      item.active
                        ? "bg-gray-900 text-white"
                        : "text-gray-300 hover:bg-gray-700 hover:text-white"
                    )}
                  >
                    <item.icon className="mr-3 h-6 w-6" />
                    {item.name}
                  </Link>
                ))}
              </nav>
            </div>
            
            {/* User profile */}
            <div className="flex-shrink-0 flex border-t border-gray-700 p-4">
              <div className="flex-shrink-0 w-full group block">
                <div className="flex items-center">
                  <div>
                    <img
                      className="inline-block h-9 w-9 rounded-full"
                      src={`https://ui-avatars.com/api/?name=${user?.name}&background=random`}
                      alt={user?.name}
                    />
                  </div>
                  <div className="ml-3">
                    <p className="text-sm font-medium text-white">{user?.name}</p>
                    <p className="text-xs font-medium text-gray-300 capitalize">{user?.organizationName ? user?.organizationName : "Org Name Here"}</p>
                  </div>
                  {/* <button
                    onClick={() => logout()}
                    className="ml-auto text-gray-300 hover:text-white"
                  >
                    <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth="2"
                        d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                      />
                    </svg>
                  </button> */}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}
