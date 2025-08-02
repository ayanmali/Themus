import { useRef, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Shield, Monitor, Mic } from "lucide-react";

interface PermissionDialogProps {
  onClose: () => void;
  onRetry: () => void;
}

export default function PermissionDialog({ onClose, onRetry }: PermissionDialogProps) {
  const modalRef = useRef<HTMLDivElement>(null);

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

  const handleAllow = () => {
    onRetry();
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div
        ref={modalRef}
        className="bg-white rounded-xl shadow-2xl max-w-md w-full"
      >
        <div className="p-6 text-center">
          <div className="w-16 h-16 bg-blue-50 rounded-full flex items-center justify-center mx-auto mb-4">
            <Shield className="text-blue-600 h-8 w-8" />
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">Screen Recording Permission</h3>
          <p className="text-gray-600 mb-6">
            Delphi needs permission to access your screen and microphone to record your sessions.
          </p>
          
          <div className="space-y-3 mb-6">
            <div className="flex items-center space-x-3 text-left">
              <Monitor className="text-blue-600 h-5 w-5" />
              <span className="text-gray-700">Screen capture access</span>
            </div>
            <div className="flex items-center space-x-3 text-left">
              <Mic className="text-blue-600 h-5 w-5" />
              <span className="text-gray-700">Microphone access (optional)</span>
            </div>
          </div>
          
          <div className="flex space-x-3">
            <Button variant="outline" onClick={onClose} className="flex-1">
              Cancel
            </Button>
            <Button onClick={handleAllow} className="flex-1">
              Allow Access
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
