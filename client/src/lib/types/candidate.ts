export type AttemptStatus = 'INVITED' | 'STARTED' | 'COMPLETED' | 'EVALUATED';

export type Candidate = {
    id: number;
    email: string;
    appliedAt?: Date;
    // API response fields
    fullName?: string;
    firstName?: string;
    lastName?: string;
    createdDate?: string;
    updatedDate?: string;
    metadata?: Record<string, string>;
    attemptStatuses?: Record<AttemptStatus, number[]>;
}