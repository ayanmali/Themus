export type Assessment = {
    id: string;
    role: string; // ex. Software Engineering Intern, Senior Data Scientist, etc.
    employerName: string;
    description: string; // job description
    createdAt: string;
    updatedAt: string;
    assessmentName: string; // ex. "Backend SWE Microservices Assessment"; used for employer's dashboard
    assessmentStatus: 'active' | 'inactive';
    startDate: Date;
    endDate: Date;
    assessmentType: 'take-home' | 'live-coding';
    repoLink: string; // link to the repository
}