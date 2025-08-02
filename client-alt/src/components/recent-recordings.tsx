import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Play, Download, Trash2 } from "lucide-react";
import type { Recording } from "@shared/schema";
import { apiRequest } from "@/lib/queryClient";
import { useToast } from "@/hooks/use-toast";

interface RecentRecordingsProps {
  onSelectRecording: (recording: Recording) => void;
}

export default function RecentRecordings({ onSelectRecording }: RecentRecordingsProps) {
  const { toast } = useToast();
  
  const { data: recordings = [], isLoading, refetch } = useQuery<Recording[]>({
    queryKey: ["/api/recordings"],
  });

  const recentRecordings = recordings.slice(0, 3);

  const handleDelete = async (id: number, event: React.MouseEvent) => {
    event.stopPropagation();
    try {
      await apiRequest("DELETE", `/api/recordings/${id}`);
      toast({
        title: "Recording deleted",
        description: "The recording has been successfully deleted.",
      });
      refetch();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete recording.",
        variant: "destructive",
      });
    }
  };

  const handleDownload = async (recording: Recording, event: React.MouseEvent) => {
    event.stopPropagation();
    try {
      const response = await fetch(`/api/recordings/${recording.id}/download`);
      if (!response.ok) throw new Error("Download failed");
      
      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${recording.title}.${recording.format}`;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to download recording.",
        variant: "destructive",
      });
    }
  };

  const formatDuration = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins} min ${secs.toString().padStart(2, "0")} sec`;
  };

  const formatFileSize = (bytes: number) => {
    const units = ['B', 'KB', 'MB', 'GB'];
    let size = bytes;
    let unitIndex = 0;
    
    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }
    
    return `${size.toFixed(1)} ${units[unitIndex]}`;
  };

  const formatDate = (date: Date) => {
    const now = new Date();
    const recordingDate = new Date(date);
    const timeDiff = now.getTime() - recordingDate.getTime();
    const daysDiff = Math.floor(timeDiff / (1000 * 3600 * 24));
    
    if (daysDiff === 0) {
      return `Today at ${recordingDate.toLocaleTimeString('en-US', { 
        hour: 'numeric', 
        minute: '2-digit', 
        hour12: true 
      })}`;
    } else if (daysDiff === 1) {
      return `Yesterday at ${recordingDate.toLocaleTimeString('en-US', { 
        hour: 'numeric', 
        minute: '2-digit', 
        hour12: true 
      })}`;
    } else {
      return recordingDate.toLocaleDateString('en-US', { 
        month: 'short', 
        day: 'numeric', 
        year: 'numeric',
        hour: 'numeric', 
        minute: '2-digit', 
        hour12: true 
      });
    }
  };

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Recent Recordings</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="flex items-center space-x-4 p-4 animate-pulse">
                <div className="w-16 h-12 bg-gray-200 rounded-lg"></div>
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                  <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle>Recent Recordings</CardTitle>
          <Button variant="ghost" size="sm">
            View All
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {recentRecordings.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            <Play className="h-12 w-12 mx-auto mb-4 opacity-50" />
            <p>No recordings yet</p>
            <p className="text-sm">Start recording to see your captures here</p>
          </div>
        ) : (
          <div className="space-y-4">
            {recentRecordings.map((recording) => (
              <div
                key={recording.id}
                className="flex items-center space-x-4 p-4 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer"
                onClick={() => onSelectRecording(recording)}
              >
                <div className="w-16 h-12 bg-gray-200 rounded-lg flex items-center justify-center">
                  <Play className="h-5 w-5 text-gray-500" />
                </div>
                <div className="flex-1">
                  <h4 className="font-medium text-gray-900">{recording.title}</h4>
                  <p className="text-sm text-gray-500">
                    {formatDate(recording.createdAt)} • {formatDuration(recording.duration)}
                  </p>
                </div>
                <div className="flex items-center space-x-2">
                  <span className="text-sm text-gray-500">{formatFileSize(recording.fileSize)}</span>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => handleDownload(recording, e)}
                  >
                    <Download className="h-4 w-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={(e) => handleDelete(recording.id, e)}
                    className="text-red-600 hover:text-red-700 hover:bg-red-50"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}
