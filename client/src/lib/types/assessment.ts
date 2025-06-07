export type Assessment = {
    id: string;
    role: string; // ex. Software Engineering Intern, Senior Data Scientist, etc.
    employerId: string; // ex. "Google", "Meta", "Amazon", etc.
    description: string; // job and/or assessment description
    skills: string[]; // ex. ["React", "Node.js", "TypeScript", "Python", "SQL", "Docker", "Kubernetes"]
    createdAt: Date;
    updatedAt: Date;
    name: string; // ex. "Backend SWE Microservices Assessment"; used for employer's dashboard
    status: 'active' | 'inactive';
    startDate?: Date;
    endDate?: Date;
    duration?: number; // in minutes
    type: 'take-home' | 'live-coding'; // Take home assessments are completed by the candidate at their own pace, while live coding assessments are completed live in real-time
    repoLink: string; // link to the repository
    metadata: Record<string, string>; // additional metadata
}

export type Candidate = {
    id: string;
    name: string;
    email: string;
    status: 'invited' | 'started' | 'submitted' | 'evaluated';
    appliedAt: Date;
}