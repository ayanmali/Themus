import { useState, useEffect, useRef, useCallback } from 'react';
import { ChatMessage } from '@/lib/types/chat-message';
import useApi from './use-api';

interface UseSseChatProps {
  assessmentId: number;
  onNewMessages: (messages: ChatMessage[]) => void;
}

export function useSseChat({ assessmentId, onNewMessages }: UseSseChatProps) {
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);
  const { apiCall } = useApi();

  const sendMessage = useCallback(async (message: string, model: string, url: string) => {
    if (!assessmentId) return;

    setIsLoading(true);
    
    try {
      // Send the message to create the job and get SSE connection
      const response = await apiCall(url, {
        method: 'POST',
        body: JSON.stringify({
          message,
          model,
          assessmentId
        }),
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (response && response.body) {
        // Handle the SSE response
        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        let eventName = '';
        let eventId = '';
        let buffer = '';

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;

          const chunk = decoder.decode(value);
          
          // Add chunk to buffer
          buffer += chunk;
          
          // Process complete lines from buffer
          const lines = buffer.split('\n');
          // Keep the last incomplete line in buffer
          buffer = lines.pop() || '';

          for (const line of lines) {
            if (line.startsWith('event: ')) {
              eventName = line.slice(7).trim();
            } else if (line.startsWith('id: ')) {
              eventId = line.slice(4).trim();
            } else if (line.startsWith('data: ')) {
              try {
                const dataString = line.slice(6);
                if (dataString.trim()) {
                  const data = JSON.parse(dataString);
                  handleSseEvent(eventName, data);
                }
              } catch (error) {
                console.error('Error parsing SSE data:', error);
              }
            } else if (line.trim() && !line.startsWith('event:') && !line.startsWith('id:') && !line.startsWith('data:')) {
              // This might be a data line without the "data: " prefix
              try {
                const data = JSON.parse(line);
                handleSseEvent(eventName, data);
              } catch (error) {
                // Not JSON, ignore
              }
            }
          }
        }
      }
    } catch (error) {
      console.error('Error with SSE chat:', error);
    } finally {
      setIsLoading(false);
    }
  }, [assessmentId, apiCall, onNewMessages]);

  const handleSseEvent = useCallback((eventName: string, data: any) => {
    console.log('📖 Handling SSE event:', eventName, data);
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
      case 'chat_completed':
        console.log('Chat completed:', data);
        if (data.messages) {
          onNewMessages(data.messages);
        }
        setIsConnected(false);
        break;
      case 'error':
        console.error('SSE error:', data);
        setIsConnected(false);
        break;
      default:
        console.log('Unknown SSE event:', eventName, data);
    }
  }, [onNewMessages]);

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

