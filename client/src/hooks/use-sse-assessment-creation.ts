import { useState, useEffect, useRef, useCallback } from 'react';
import useApi from './use-api';
import { CreateAssessmentFormValues } from '@/pages/employer/new-assessment/create-assessment';

interface UseSseAssessmentCreationProps {
  onAssessmentCreated?: (assessment: {
    jobId: string;
    assessmentId: string;
    //status: string;
    //message: string;
  }) => void;
  onError?: (error: string) => void;
}

export function useSseAssessmentCreation({ onAssessmentCreated, onError }: UseSseAssessmentCreationProps) {
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const { apiCall } = useApi();

  const createAssessment = useCallback(async (assessmentData: CreateAssessmentFormValues) => {
    console.log('🚀 Starting assessment creation with data:', assessmentData);
    setIsLoading(true);
    
    try {
      // Send the assessment creation request and get SSE connection
      console.log('📡 Making API call to /api/assessments/new');
      const response = await apiCall('/api/assessments/new', {
        method: 'POST',
        body: JSON.stringify(assessmentData),
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      console.log('📨 API response received:', response);
      console.log('📨 Response type:', typeof response);
      console.log('📨 Response has body:', !!response?.body);

      if (response && response.body) {
        console.log('📖 Starting to read SSE stream');
        // Handle the SSE response
        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        let eventName = '';
        let eventId = '';
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) {
            console.log('📖 SSE stream completed');
            break;
          }

          const chunk = decoder.decode(value);
          console.log('📖 Received SSE chunk:', chunk);
          
          // Add chunk to buffer
          buffer += chunk;
          
          // Process complete lines from buffer
          const lines = buffer.split('\n');
          // Keep the last incomplete line in buffer
          buffer = lines.pop() || '';

          console.log('📖 **Processing SSE lines:', lines);
          for (const line of lines) {
            console.log('📖 Processing SSE line:', line); 
            if (line.startsWith('id:')) {
              eventId = line.slice(3).trim();
              console.log('📖 Found event ID:', eventId);
            } 
            else if (line.startsWith('data:')) {
              try {
                const dataString = line.slice(5);
                if (dataString.trim()) {
                  const data = JSON.parse(dataString);
                  console.log('📖 Parsed SSE data:', data);
                  handleSseEvent(eventName, data);
                }
              } catch (error) {
                console.error('❌ Error parsing SSE data:', error);
                console.error('❌ Raw data line:', line);
              }
            }
            else if (line.startsWith('event:')) {
                eventName = line.slice(6).trim();
                console.log('📖 Found event name:', eventName);
            } 
            else if (line.trim() && !line.startsWith('event:') && !line.startsWith('id:') && !line.startsWith('data:')) {
              // This might be a data line without the "data: " prefix
            //   try {
            //     const data = JSON.parse(line);
            //     console.log('📖 Parsed SSE data (no prefix):', data);
            //     handleSseEvent(eventName, data);
            //   } catch (error) {
            //     // Not JSON, ignore
            //   }
            }
          }
        }
      } else {
        console.log('❌ No response body found or response is null');
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

  const handleSseEvent = useCallback((eventName: string, data: any) => {
    switch (eventName) {
      case 'connected':
        console.log('SSE connected:', data);
        setIsConnected(true);
        break;
      case 'job_created':
        console.log('Job created:', data);
        break;
      case 'job_running':
        console.log('Job running:', data);
        break;
      case 'assessment_created':
        console.log('Assessment created event received:', data);
        console.log('Assessment ID from data:', data?.assessmentId);
        if (onAssessmentCreated) {
          onAssessmentCreated(data);
        }
        setIsConnected(false);
        break;
      case 'error':
        console.error('SSE error:', data);
        if (onError) {
          onError(data.error || 'Assessment creation failed');
        }
        setIsConnected(false);
        break;
      default:
        console.log('Unknown SSE event:', eventName, data);
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
