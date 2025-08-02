export type Recording = {
    id: number;
    title: string;
    filename: string;
    fileSize: number;
    duration: number;
    format: string;
    createdAt: Date;
    hasAudio: boolean;
    thumbnailUrl: string;
}
    
export type ClientMetadata = {
    id: number;
    title: string;
    filename: string;
    duration: number;
    format: string;
    createdAt: Date;
    hasAudio: boolean;
    thumbnailUrl: string;
}

export type RecordingOptions = {
    screenSource: "entire" | "window" | "tab";
    includeMicrophone: boolean;
    includeSystemAudio: boolean;
    microphoneVolume: number;
    format: "mp4" | "webm";
}      