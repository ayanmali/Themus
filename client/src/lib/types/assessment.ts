export type Assessment = {
    id: string;
    role: string; // ex. Software Engineering Intern, Senior Data Scientist, etc.
    employerName: string; // ex. "Google", "Meta", "Amazon", etc.
    description: string; // job and/or assessment description
    createdAt: Date;
    updatedAt: Date;
    assessmentName: string; // ex. "Backend SWE Microservices Assessment"; used for employer's dashboard
    assessmentStatus: 'active' | 'inactive';
    startDate?: Date;
    endDate?: Date;
    duration?: number; // in minutes
    assessmentType: 'take-home' | 'live-coding'; // Take home assessments are completed by the candidate at their own pace, while live coding assessments are completed live in real-time
    repoLink: string; // link to the repository
    metadata: Record<string, string>; // additional metadata
}