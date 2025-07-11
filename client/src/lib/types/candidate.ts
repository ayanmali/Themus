export type Candidate = {
    id: number;
    name: string;
    email: string;
    // status: 'invited' | 'started' | 'submitted' | 'evaluated';
    appliedAt?: Date;
    // startedAt?: Date | null;
}