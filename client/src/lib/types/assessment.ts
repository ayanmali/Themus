import { CandidateAttempt } from "./candidate-attempt";

export type Assessment = {
    id: number;
    role: string; // ex. Software Engineering Intern, Senior Data Scientist, etc.
    employerId: string; // ex. "Google", "Meta", "Amazon", etc.
    employerName?: string; // ex. "Google", "Meta", "Amazon", etc.
    description: string; // job and/or assessment description
    skills: string[]; // ex. ["React", "Node.js", "TypeScript", "Python", "SQL", "Docker", "Kubernetes"]
    createdDate: Date;
    updatedDate: Date;
    name: string; // ex. "Backend SWE Microservices Assessment"; used for employer's dashboard
    status: 'DRAFT' | 'ACTIVE' | 'INACTIVE';
    candidateAttempts: CandidateAttempt[];
    startDate?: Date;
    endDate?: Date;
    duration?: number; // in minutes - for take home assessments, this is the estimated duration of the assessment in minutes. For live coding assessments, this is the duration of the assessment in minutes.
    languageOptions?: string[]; // ex. ["React", "Vue", "Angular"]
    rules?: string;
    instructions?: string;
    details?: string; // additional details about the assessment
    //type: 'take-home' | 'live-coding'; // Take home assessments are completed by the candidate at their own pace, while live coding assessments are completed live in real-time
    githubRepositoryLink: string; // link to the repository
    metadata?: Record<string, string>; // additional metadata
}