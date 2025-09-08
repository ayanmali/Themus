import { useState } from "react";
import Sidebar from "@/components/candidate-assessment/assessment-preview-sidebar";
import RecordingPreview from "@/components/candidate-assessment/recording-preview";
import RecordingControls from "@/components/candidate-assessment/recording-controls";
import RecordingOptionsComponent from "@/components/candidate-assessment/recording-options";
import RecentRecordings from "@/components/candidate-assessment/candidate-instructions";
import VideoPreviewModal from "@/components/candidate-assessment/video-preview-modal";
import PermissionDialog from "@/components/candidate-assessment/permission-dialog";
import { useScreenRecording } from "@/hooks/use-screen-recording";
import type { RecordingOptions, Recording } from "@/lib/types/recording";
import CandidateInstructions from "@/components/candidate-assessment/candidate-instructions";

/**
 * When candidate starts recording,
 * their github repository is created for them and it opens in another tab
 * @returns 
 */
export default function Record() {
  const [selectedRecording, setSelectedRecording] = useState<Recording | null>(null);
  const [showPermissionDialog, setShowPermissionDialog] = useState(false);
  const [recordingOptions, setRecordingOptions] = useState<RecordingOptions>({
    screenSource: "entire",
    includeMicrophone: true,
    includeSystemAudio: false,
    microphoneVolume: 75,
    format: "mp4",
  });

  const {
    isRecording,
    isPaused,
    recordingDuration,
    startRecording,
    stopRecording,
    pauseRecording,
    resumeRecording,
    error: recordingError,
  } = useScreenRecording(recordingOptions);

  // TODO:
  const createCandidateRepo = async () => {
    try {

    }
    catch (error) {

    }
  }

  const handleStartRecording = async () => {
    try {
      const candidateRepoUrl: string = await createCandidateRepo();
      await startRecording();
    } catch (error) {
      if (error instanceof Error && error.name === "NotAllowedError") {
        setShowPermissionDialog(true);
      }
    }
  };

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, "0")}:${secs.toString().padStart(2, "0")}`;
  };

  return (
    <div className="min-h-screen flex bg-gray-50">
      {/* <Sidebar /> */}
      
      <div className="flex-1 flex flex-col">
        {/* Top Bar */}
        <header className="bg-white border-b border-gray-200 px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold text-gray-900">ASSESSMENT NAME HERE</h2>
              <p className="text-gray-600">COMPANY NAME HERE</p>
            </div>
            <div className="flex items-center space-x-4">
              {isRecording && (
                <div className="flex items-center space-x-2 text-sm text-gray-500">
                  <div className="w-2 h-2 bg-red-500 rounded-full animate-pulse"></div>
                  <span>Recording</span>
                  <span className="font-mono font-medium">{formatTime(recordingDuration)}</span>
                </div>
              )}
              <span>CANDIDATE NAME HERE</span>
              <div className="w-8 h-8 bg-gray-300 rounded-full"></div>
            </div>
          </div>
        </header>

        {/* Main Content */}
        <main className="flex-1 p-6">
          
          <div className="max-w-4xl mx-auto">
            {/* Instructions*/}
            <CandidateInstructions onSelectRecording={setSelectedRecording} />

            {/* Recording Preview */}
            <RecordingPreview 
              isRecording={isRecording} 
              recordingDuration={recordingDuration}
              includeMicrophone={recordingOptions.includeMicrophone}
            />

            {/* Recording Controls */}
            <RecordingControls
              isRecording={isRecording}
              isPaused={isPaused}
              onStartRecording={handleStartRecording}
              onStopRecording={stopRecording}
              onPauseRecording={pauseRecording}
              onResumeRecording={resumeRecording}
            />

            {/* Recording Options */}
            <RecordingOptionsComponent
              options={recordingOptions}
              onOptionsChange={setRecordingOptions}
              disabled={isRecording}
            />

          </div>
        </main>
      </div>

      {/* Video Preview Modal */}
      {selectedRecording && (
        <VideoPreviewModal
          recording={selectedRecording}
          onClose={() => setSelectedRecording(null)}
        />
      )}

      {/* Permission Dialog */}
      {showPermissionDialog && (
        <PermissionDialog
          onClose={() => setShowPermissionDialog(false)}
          onRetry={handleStartRecording}
        />
      )}
    </div>
  );
}
