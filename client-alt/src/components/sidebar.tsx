import { useLocation } from "wouter";
import { useQuery } from "@tanstack/react-query";
import { Link } from "wouter";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Video, Folder, Settings, HelpCircle, RadioIcon as Record } from "lucide-react";
import { cn } from "@/lib/utils";
import type { Recording } from "@shared/schema";

interface StorageStats {
  totalSize: number;
  totalRecordings: number;
  formats: {
    mp4: number;
    webm: number;
  };
  withAudio: number;
  withoutAudio: number;
}

const MAX_STORAGE_SIZE = 1024 * 1024 * 1024 * 10; // 10GB

export default function Sidebar() {
  const [location] = useLocation();
  
  const { data: recordings = [] } = useQuery<Recording[]>({
    queryKey: ["/api/recordings"],
  });

  const { data: storageStats } = useQuery<StorageStats>({
    queryKey: ["/api/recordings/storage/stats"],
  });

  const formatStorageSize = (bytes: number) => {
    const units = ['B', 'KB', 'MB', 'GB'];
    let size = bytes;
    let unitIndex = 0;
    
    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }
    
    return `${size.toFixed(2)} ${units[unitIndex]}`;
  };

  const storagePercentage = storageStats 
    ? Math.round((storageStats.totalSize / MAX_STORAGE_SIZE) * 100)
    : 0;

  const navigationItems = [
    {
      href: "/",
      icon: Record,
      label: "Record",
      active: location === "/",
    },
    {
      href: "/recordings",
      icon: Folder,
      label: "My Recordings",
      active: location === "/recordings",
      badge: recordings.length,
    },
    {
      href: "/settings",
      icon: Settings,
      label: "Settings",
      active: location === "/settings",
    },
    {
      href: "/help",
      icon: HelpCircle,
      label: "Help",
      active: location === "/help",
    },
  ];

  return (
    <div className="w-64 bg-white shadow-sm border-r border-gray-200 flex flex-col">
      {/* Logo/Header */}
      <div className="p-6 border-b border-gray-100">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 bg-blue-600 rounded-lg flex items-center justify-center">
            <Video className="text-white h-5 w-5" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-gray-900">Delphi</h1>
            <p className="text-xs text-gray-500">v2.1.0</p>
          </div>
        </div>
      </div>
      
      {/* Navigation */}
      <nav className="flex-1 px-4 py-6 space-y-2">
        {navigationItems.map((item) => (
          <Link key={item.href} href={item.href}>
            <div
              className={cn(
                "flex items-center space-x-3 px-3 py-2 rounded-lg font-medium transition-colors cursor-pointer",
                item.active
                  ? "bg-blue-50 text-blue-600"
                  : "text-gray-600 hover:bg-gray-50"
              )}
            >
              <item.icon className="h-5 w-5" />
              <span>{item.label}</span>
              {item.badge !== undefined && (
                <Badge 
                  variant="secondary" 
                  className="ml-auto text-xs"
                >
                  {item.badge}
                </Badge>
              )}
            </div>
          </Link>
        ))}
      </nav>
      
      {/* Storage Info */}
      <div className="p-4 border-t border-gray-100">
        <Card>
          <CardContent className="p-3">
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-700">Storage Used</span>
              <span className="text-sm text-gray-500">
                {storageStats ? formatStorageSize(storageStats.totalSize) : "0 B"}
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-blue-600 h-2 rounded-full transition-all"
                style={{ width: `${Math.min(storagePercentage, 100)}%` }}
              />
            </div>
            <p className="text-xs text-gray-500 mt-1">
              {storageStats 
                ? `${formatStorageSize(MAX_STORAGE_SIZE - storageStats.totalSize)} remaining`
                : "Loading..."
              }
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
