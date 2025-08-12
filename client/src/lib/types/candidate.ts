export type Candidate = {
    id: number;
    name: string;
    email: string;
    appliedAt?: Date;
    // API response fields
    fullName?: string;
    firstName?: string;
    lastName?: string;
    createdDate?: string;
    updatedDate?: string;
    metadata?: Record<string, string>;
}