"use client";

import { useState, FormEvent, useEffect } from "react";
import { Paperclip, Mic, CornerDownLeft, Loader2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  ChatBubble,
  ChatBubbleAvatar,
  ChatBubbleMessage,
} from "@/components/ui/chat-bubble";
import { ChatMessageList } from "@/components/ui/chat-message-list";
import { ChatInput } from "@/components/ui/chat-input";
import { ChatMessage } from "@/lib/types/chat-message";
import { Select, SelectContent, SelectTrigger, SelectItem, SelectValue } from "@/components/ui/select";
import { getOpenRouterModels } from "@/lib/utils";

interface ChatMessagesProps {
  messages: ChatMessage[];
  onSendMessage: (message: string, model: string) => void;
  isLoading?: boolean;
  isHistoryLoading?: boolean;
}

export function ChatMessages({ messages, onSendMessage, isLoading = false, isHistoryLoading = false }: ChatMessagesProps) {
  const [input, setInput] = useState("");
  const [models, setModels] = useState<string[]>([]);
  const [model, setModel] = useState("anthropic/claude-sonnet-4");

  useEffect(() => {
    getOpenRouterModels(setModels);
  }, []);

  // User sends message
  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!input.trim()) return;

    // Call the parent's onSendMessage function
    onSendMessage(input, model);
    setInput("");
  };

  // User attaches file
  const handleAttachFile = () => {
    //
  };

  // User uses microphone
  const handleMicrophoneClick = () => {
    //
  };

  // display just the <DETAILS/> section from the initial user prompt
  const messageList: ChatMessage[] = [
    {
      id: messages[0]?.id, 
      text: messages[0]?.text.split("<DETAILS>")[1].split("</DETAILS>")[0], 
      messageType: "USER", 
      createdAt: messages[0]?.createdAt, 
      updatedAt: messages[0]?.updatedAt,
      model: messages[0]?.model
    }, 
  ...messages.slice(1).filter((message) => message.messageType !== "SYSTEM")
];

  return (
    <div className="h-full border border-slate-700 bg-background rounded-lg flex flex-col">
      <div className="flex-1 overflow-hidden bg-slate-800">
        <ChatMessageList>
          {isHistoryLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="flex items-center space-x-2">
                <Loader2 className="h-5 w-5 animate-spin text-gray-400" />
                <span className="text-gray-400">Loading chat history...</span>
              </div>
            </div>
          ) : (
            messageList.map((message) => (
              <ChatBubble
                key={message.id}
                variant={message.messageType === "USER" ? "sent" : "received"}
              >
                <ChatBubbleMessage
                  variant={message.messageType === "USER" ? "sent" : "received"}
                >
                  {message.text}
                </ChatBubbleMessage>
              </ChatBubble>
            ))
          )}

          {isLoading && (
            <ChatBubble variant="received">
              {/* <ChatBubbleAvatar
                className="h-8 w-8 shrink-0"
                src="https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=64&h=64&q=80&crop=faces&fit=crop"
                fallback="AI"
              /> */}
              <ChatBubbleMessage isLoading />
            </ChatBubble>
          )}
        </ChatMessageList>
      </div>

      <div className="p-4 border-t border-slate-700 bg-slate-800">
        <form
          onSubmit={handleSubmit}
          className="relative rounded-lg border border-slate-700 bg-slate-800 focus-within:ring-1 focus-within:ring-ring p-1 shadow-lg"
        >
          <ChatInput
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Type your message..."
            className="min-h-12 resize-none rounded-lg bg-slate-800 border-0 p-3 shadow-nond focus-visible:ring-0"
          />
          <div className="flex items-center p-3 pt-0 justify-between">
            <div className="flex">
              <Button
                variant="ghost"
                size="icon"
                type="button"
                onClick={handleAttachFile}
              >
                <Paperclip className="size-4" />
              </Button>

              <Button
                variant="ghost"
                size="icon"
                type="button"
                onClick={handleMicrophoneClick}
              >
                <Mic className="size-4" />
              </Button>
            </div>
            <Select value={model} onValueChange={(value) => setModel(value)}>
              <SelectTrigger className="bg-slate-800/60 border-slate-700/50 text-gray-100 placeholder:text-slate-500 focus:border-violet-500/50 focus:ring-violet-500/20 backdrop-blur-sm text-xs">
                <SelectValue placeholder="Select Model" />
              </SelectTrigger>
              <SelectContent className="bg-slate-800/60 border-slate-700/50 text-gray-100">
                {models.map((model) => (
                  <SelectItem key={model} value={model}>{model}</SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button type="submit" size="sm" className="ml-auto gap-1.5">
              
              <CornerDownLeft className="size-3.5" />
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
