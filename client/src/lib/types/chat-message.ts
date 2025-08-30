import { Assessment } from "./assessment";

export type ChatMessage = {
    id: string;
    text: string;
    model: string;
    messageType: 'USER' | 'ASSISTANT' | 'TOOL' | 'SYSTEM';
    createdAt: Date;
    updatedAt: Date;
    assessment?: Assessment;
    toolCalls?: ToolCall[];
    toolResponses?: ToolResponse[];
}

export type ToolCall = {
    id: string;
    name: string;
    arguments: string;
}

export type ToolResponse = {
    id: string;
    name: string;
    responseData: string;
}