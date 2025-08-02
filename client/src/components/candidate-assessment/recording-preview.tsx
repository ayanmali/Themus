import { Monitor } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

interface RecordingPreviewProps {
  isRecording: boolean;
  recordingDuration: number;
  includeMicrophone: boolean;
}

export default function RecordingPreview({
  isRecording,
  recordingDuration,
  includeMicrophone,
}: RecordingPreviewProps) {
  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  return (
    <Card className="mb-6">
      <CardContent className="p-6">
        <div className="bg-gray-900 rounded-lg aspect-video mb-4 relative overflow-hidden">
          {/* Screen Preview */}
          <div className="absolute inset-0 bg-gradient-to-br from-gray-800 to-gray-900 flex items-center justify-center">
            <div className="text-center text-gray-400">
              <Monitor className="h-16 w-16 mb-4 mx-auto opacity-50" />
              <p className="text-lg font-medium">Screen Preview</p>
              <p className="text-sm opacity-75">Your screen will appear here when recording starts</p>
            </div>
          </div>
          
          {/* Recording Indicator */}
          {isRecording && (
            <div className="absolute top-4 left-4 flex items-center space-x-2 bg-black/50 backdrop-blur-sm rounded-lg px-3 py-2">
              <div className="w-3 h-3 bg-red-500 rounded-full animate-pulse"></div>
              <span className="text-white text-sm font-medium">REC</span>
              <span className="text-white text-sm font-mono">{formatTime(recordingDuration)}</span>
            </div>
          )}
          
          {/* Audio Level Indicator */}
          {isRecording && includeMicrophone && (
            <div className="absolute bottom-4 left-4 flex items-center space-x-2 bg-black/50 backdrop-blur-sm rounded-lg px-3 py-2">
              <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M7 4a3 3 0 016 0v4a3 3 0 11-6 0V4zm4 10.93A7.001 7.001 0 0017 8a1 1 0 10-2 0A5 5 0 015 8a1 1 0 00-2 0 7.001 7.001 0 006 6.93V17H6a1 1 0 100 2h8a1 1 0 100-2h-3v-2.07z" clipRule="evenodd" />
              </svg>
              <div className="flex space-x-1">
                {[...Array(5)].map((_, i) => (
                  <div
                    key={i}
                    className={`w-1 h-4 rounded-full ${
                      i < 3 ? "bg-green-500" : "bg-gray-600"
                    }`}
                  />
                ))}
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
