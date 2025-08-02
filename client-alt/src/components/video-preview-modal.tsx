import { useRef, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { X, Download, Share, Trash2 } from "lucide-react";
import type { Recording } from "@shared/schema";
import { apiRequest } from "@/lib/queryClient";
import { useToast } from "@/hooks/use-toast";

interface VideoPreviewModalProps {
  recording: Recording;
  onClose: () => void;
}

export default function VideoPreviewModal({ recording, onClose }: VideoPreviewModalProps) {
  const modalRef = useRef<HTMLDivElement>(null);
  const { toast } = useToast();

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape") {
        onClose();
      }
    };

    const handleClickOutside = (e: MouseEvent) => {
      if (modalRef.current && !modalRef.current.contains(e.target as Node)) {
        onClose();
      }
    };

    document.addEventListener("keydown", handleEscape);
    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("keydown", handleEscape);
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [onClose]);

  const handleDownload = async () => {
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

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: recording.title,
          text: `Check out this screen recording: ${recording.title}`,
          url: window.location.href,
        });
      } catch (error) {
        // Candidate cancelled sharing
      }
    } else {
      // Fallback: copy URL to clipboard
      await navigator.clipboard.writeText(window.location.href);
      toast({
        title: "Link copied",
        description: "Recording link has been copied to clipboard.",
      });
    }
  };

  const handleDelete = async () => {
    try {
      await apiRequest("DELETE", `/api/recordings/${recording.id}`);
      toast({
        title: "Recording deleted",
        description: "The recording has been successfully deleted.",
      });
      onClose();
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to delete recording.",
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

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div
        ref={modalRef}
        className="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden"
      >
        {/* Modal Header */}
        <div className="p-6 border-b border-gray-100 flex items-center justify-between">
          <div>
            <h3 className="text-xl font-semibold text-gray-900">{recording.title}</h3>
            <p className="text-gray-500">
              {formatDuration(recording.duration)} • {formatFileSize(recording.fileSize)} • {recording.format.toUpperCase()}
            </p>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose}>
            <X className="h-5 w-5" />
          </Button>
        </div>
        
        {/* Video Player */}
        <div className="p-6">
          <div className="bg-black rounded-lg aspect-video mb-4 relative">
            <video
              className="w-full h-full object-contain"
              controls
              preload="metadata"
            >
              <source src={`/api/recordings/${recording.id}/stream`} type={`video/${recording.format}`} />
              Your browser does not support the video tag.
            </video>
          </div>
          
          {/* Video Actions */}
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <Button onClick={handleDownload}>
                <Download className="h-4 w-4 mr-2" />
                Download
              </Button>
              <Button variant="outline" onClick={handleShare}>
                <Share className="h-4 w-4 mr-2" />
                Share
              </Button>
            </div>
            <Button
              variant="outline"
              onClick={handleDelete}
              className="text-red-600 hover:text-red-700 hover:bg-red-50 border-red-200"
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
