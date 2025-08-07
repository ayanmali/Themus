export type Candidate = {
    id: number;
    name: string;
    email: string;
    // status: 'invited' | 'started' | 'submitted' | 'evaluated';
    appliedAt?: Date;
    // startedAt?: Date | null;
    // API response fields
    fullName?: string;
    firstName?: string;
    lastName?: string;
    createdDate?: string;
    updatedDate?: string;
    metadata?: Record<string, string>;
}