import { useState, useEffect, useRef, useCallback } from 'react';
import useApi from './use-api';
import { CreateAssessmentFormValues } from '@/pages/employer/new-assessment/create-assessment';

interface UseSseProps {
  onEventHandler?: (assessment: any) => void; // Handler for when an event is received from the SSE stream (e.g. assessment_created, chat_completion, etc)
  onError?: (error: string) => void;
}

export function useSse({ onEventHandler, onError }: UseSseProps) {
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const { apiCall } = useApi();

  const sendMessage = useCallback(async (data: any, url: string) => {
    //console.log('🚀 Starting assessment creation with data:', assessmentData);
    setIsLoading(true);
    
    try {
      // Send the assessment creation request and get SSE connection
      //console.log('📡 Making API call to /api/assessments/new');
      const response = await apiCall(url, {
        method: 'POST',
        body: JSON.stringify(data),
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      //console.log('📨 API response received:', response);
      //console.log('📨 Response type:', typeof response);
      //console.log('📨 Response has body:', !!response?.body);

      if (response && response.body) {
        //console.log('📖 Starting to read SSE stream');
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
            // else if (line.trim() && !line.startsWith('event:') && !line.startsWith('id:') && !line.startsWith('data:')) {
            //   // This might be a data line without the "data: " prefix
            // //   try {
            // //     const data = JSON.parse(line);
            // //     console.log('📖 Parsed SSE data (no prefix):', data);
            // //     handleSseEvent(eventName, data);
            // //   } catch (error) {
            // //     // Not JSON, ignore
            // //   }
            // }
          }
        }
      } else {
        console.log('❌ No response body found or response is null');
      }
    } catch (error) {
      console.error('Error with obtaining SSE stream:', error);
      if (onError) {
        onError('Failed to get SSE stream');
      }
    } finally {
      setIsLoading(false);
    }
  }, [apiCall, onEventHandler, onError]);

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
        if (onEventHandler) {
            onEventHandler(data);
        }
        setIsConnected(false);
        break;
      case 'message':
        // Single chat message event emitted during streaming
        // Normalize to array to match onEventHandler usage upstream
        if (onEventHandler && data) {
          onEventHandler(Array.isArray(data) ? data : [data]);
        }
        break;
       case 'chat_completion':
            console.log('Chat completed:', data);
            if (onEventHandler && data.messages) {
                onEventHandler(data.messages);
            }
            setIsConnected(false);
            break;
      case 'error':
        console.error('SSE error:', data);
        if (onError) {
          onError(data.error || 'SSE stream failed');
        }
        setIsConnected(false);
        break;
      default:
        console.log('Unknown SSE event:', eventName, data);
    }
  }, [onEventHandler, onError]);

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
    sendMessage,
    isConnected,
    isLoading,
    disconnect
  };
}
