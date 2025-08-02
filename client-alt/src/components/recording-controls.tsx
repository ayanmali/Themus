import { Button } from "@/components/ui/button";
import { Play, Square, Pause, Settings } from "lucide-react";

interface RecordingControlsProps {
  isRecording: boolean;
  isPaused: boolean;
  onStartRecording: () => void;
  onStopRecording: () => void;
  onPauseRecording: () => void;
  onResumeRecording: () => void;
}

export default function RecordingControls({
  isRecording,
  isPaused,
  onStartRecording,
  onStopRecording,
  onPauseRecording,
  onResumeRecording,
}: RecordingControlsProps) {
  return (
    <div className="flex items-center justify-center space-x-4 mb-6">
      {/* Start/Stop Recording Button */}
      <Button
        onClick={isRecording ? onStopRecording : onStartRecording}
        size="lg"
        className={`w-16 h-16 rounded-full shadow-lg transition-all duration-200 transform hover:scale-105 ${
          isRecording 
            ? "bg-red-500 hover:bg-red-600" 
            : "bg-red-500 hover:bg-red-600"
        }`}
      >
        {isRecording ? (
          <Square className="h-6 w-6 text-white" />
        ) : (
          <div className="w-6 h-6 bg-white rounded-full" />
        )}
      </Button>
      
      {/* Pause/Resume Button */}
      {isRecording && (
        <Button
          onClick={isPaused ? onResumeRecording : onPauseRecording}
          variant="outline"
          size="lg"
          className="w-12 h-12 rounded-full"
        >
          {isPaused ? (
            <Play className="h-5 w-5" />
          ) : (
            <Pause className="h-5 w-5" />
          )}
        </Button>
      )}
      
      {/* Settings Button */}
      <Button
        variant="outline"
        size="lg"
        className="w-12 h-12 rounded-full"
        disabled={isRecording}
      >
        <Settings className="h-5 w-5" />
      </Button>
    </div>
  );
}
