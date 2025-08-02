import { useState, useRef, useCallback } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import type { RecordingOptions } from "@shared/schema";

export function useScreenRecording(options: RecordingOptions) {
  const [isRecording, setIsRecording] = useState(false);
  const [isPaused, setIsPaused] = useState(false);
  const [recordingDuration, setRecordingDuration] = useState(0);
  const [error, setError] = useState<string | null>(null);

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  const { toast } = useToast();
  const queryClient = useQueryClient();

  const saveRecording = useMutation({
    mutationFn: async ({ blob, title, duration }: { blob: Blob; title: string; duration: number }) => {
      const formData = new FormData();
      const filename = `${title.replace(/[^a-z0-9]/gi, '_').toLowerCase()}.${options.format}`;
      
      formData.append("video", blob, filename);
      formData.append("metadata", JSON.stringify({
        title,
        filename,
        fileSize: blob.size,
        duration,
        format: options.format,
        hasAudio: options.includeMicrophone || options.includeSystemAudio,
        thumbnailUrl: null,
      }));

      // Use apiRequest for proper URL handling
      const response = await apiRequest("POST", "/api/recordings", formData);
      return response.json();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["/api/recordings"] });
      queryClient.invalidateQueries({ queryKey: ["/api/recordings/storage/stats"] });
      toast({
        title: "Recording saved",
        description: "Your screen recording has been saved successfully.",
      });
    },
    onError: () => {
      toast({
        title: "Error",
        description: "Failed to save recording. Please try again.",
        variant: "destructive",
      });
    },
  });

  const startTimer = useCallback(() => {
    timerRef.current = setInterval(() => {
      setRecordingDuration(prev => prev + 1);
    }, 1000);
  }, []);

  const stopTimer = useCallback(() => {
    if (timerRef.current) {
      clearInterval(timerRef.current);
      timerRef.current = null;
    }
  }, []);

  const startRecording = useCallback(async () => {
    try {
      setError(null);

      // Get display media (screen capture)
      const displayStream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: options.includeSystemAudio,
      });

      let audioStream: MediaStream | null = null;

      // Get microphone audio if enabled
      if (options.includeMicrophone) {
        try {
          audioStream = await navigator.mediaDevices.getUserMedia({
            audio: {
              echoCancellation: true,
              noiseSuppression: true,
              sampleRate: 44100,
            },
          });
        } catch (audioError) {
          console.warn("Could not access microphone:", audioError);
          toast({
            title: "Microphone access denied",
            description: "Recording will continue without microphone audio.",
            variant: "destructive",
          });
        }
      }

      // Combine streams
      const tracks: MediaStreamTrack[] = [...displayStream.getVideoTracks()];
      
      if (options.includeSystemAudio) {
        tracks.push(...displayStream.getAudioTracks());
      }
      
      if (audioStream && options.includeMicrophone) {
        tracks.push(...audioStream.getAudioTracks());
      }

      const combinedStream = new MediaStream(tracks);
      streamRef.current = combinedStream;

      // Setup MediaRecorder
      const mimeType = options.format === "mp4" ? "video/mp4" : "video/webm";
      const mediaRecorder = new MediaRecorder(combinedStream, {
        mimeType: MediaRecorder.isTypeSupported(mimeType) ? mimeType : "video/webm",
      });

      mediaRecorderRef.current = mediaRecorder;
      chunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        if (event.data.size > 0) {
          chunksRef.current.push(event.data);
        }
      };

      mediaRecorder.onstop = async () => {
        const blob = new Blob(chunksRef.current, { 
          type: options.format === "mp4" ? "video/mp4" : "video/webm" 
        });
        
        const title = `Recording ${new Date().toLocaleDateString()} ${new Date().toLocaleTimeString()}`;
        
        await saveRecording.mutateAsync({
          blob,
          title,
          duration: recordingDuration,
        });

        // Clean up
        if (streamRef.current) {
          streamRef.current.getTracks().forEach(track => track.stop());
          streamRef.current = null;
        }
      };

      // Handle stream end (user stops sharing)
      displayStream.getVideoTracks()[0].addEventListener("ended", () => {
        stopRecording();
      });

      mediaRecorder.start(1000); // Collect data every second
      setIsRecording(true);
      setRecordingDuration(0);
      startTimer();

    } catch (err) {
      console.error("Error starting recording:", err);
      setError(err instanceof Error ? err.message : "Failed to start recording");
      throw err;
    }
  }, [options, recordingDuration, saveRecording, startTimer, toast]);

  const stopRecording = useCallback(() => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      setIsRecording(false);
      setIsPaused(false);
      stopTimer();
    }
  }, [isRecording, stopTimer]);

  const pauseRecording = useCallback(() => {
    if (mediaRecorderRef.current && isRecording && !isPaused) {
      mediaRecorderRef.current.pause();
      setIsPaused(true);
      stopTimer();
    }
  }, [isRecording, isPaused, stopTimer]);

  const resumeRecording = useCallback(() => {
    if (mediaRecorderRef.current && isRecording && isPaused) {
      mediaRecorderRef.current.resume();
      setIsPaused(false);
      startTimer();
    }
  }, [isRecording, isPaused, startTimer]);

  return {
    isRecording,
    isPaused,
    recordingDuration,
    error,
    startRecording,
    stopRecording,
    pauseRecording,
    resumeRecording,
  };
}
