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
      console.error('Error with SSE chat:', error);
    } finally {
      setIsLoading(false);
    }
  }, [assessmentId, apiCall, onNewMessages]);

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
      case 'chat_completed':
        console.log('Chat completed:', data.data);
        if (data.data.messages) {
          onNewMessages(data.data.messages);
        }
        setIsConnected(false);
        break;
      case 'error':
        console.error('SSE error:', data.data);
        setIsConnected(false);
        break;
      default:
        console.log('Unknown SSE event:', data);
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
