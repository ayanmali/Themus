import { useState, useEffect, useRef, useCallback } from 'react';
import useApi from './use-api';

interface UseSseAssessmentCreationProps {
  onAssessmentCreated?: (assessment: any) => void;
  onError?: (error: string) => void;
}

export function useSseAssessmentCreation({ onAssessmentCreated, onError }: UseSseAssessmentCreationProps) {
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const { apiCall } = useApi();

  const createAssessment = useCallback(async (assessmentData: any) => {
    setIsLoading(true);
    
    try {
      // Send the assessment creation request and get SSE connection
      const response = await apiCall('/api/assessments/new', {
        method: 'POST',
        body: JSON.stringify(assessmentData),
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (response && response.body) {
        // Handle the SSE response
        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value);
          const lines = chunk.split('\n');

          for (const line of lines) {
            if (line.startsWith('data: ')) {
              try {
                const data = JSON.parse(line.slice(6));
                handleSseEvent(data);
              } catch (error) {
                console.error('Error parsing SSE data:', error);
              }
            }
          }
        }
      }
    } catch (error) {
      console.error('Error with SSE assessment creation:', error);
      if (onError) {
        onError('Failed to create assessment');
      }
    } finally {
      setIsLoading(false);
    }
  }, [apiCall, onAssessmentCreated, onError]);

  const handleSseEvent = useCallback((data: any) => {
    switch (data.event) {
      case 'connected':
        console.log('SSE connected:', data.data);
        setIsConnected(true);
        break;
      case 'job_created':
        console.log('Job created:', data.data);
        break;
      case 'job_running':
        console.log('Job running:', data.data);
        break;
      case 'assessment_created':
        console.log('Assessment created:', data.data);
        if (onAssessmentCreated) {
          onAssessmentCreated(data.data);
        }
        setIsConnected(false);
        break;
      case 'error':
        console.error('SSE error:', data.data);
        if (onError) {
          onError(data.data.error || 'Assessment creation failed');
        }
        setIsConnected(false);
        break;
      default:
        console.log('Unknown SSE event:', data);
    }
  }, [onAssessmentCreated, onError]);

  const disconnect = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
    setIsConnected(false);
  }, []);

  useEffect(() => {
    return () => {
      disconnect();
    };
  }, [disconnect]);

  return {
    createAssessment,
    isConnected,
    isLoading,
    disconnect
  };
}
