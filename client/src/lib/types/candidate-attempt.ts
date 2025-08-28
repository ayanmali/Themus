import { Assessment } from './assessment';
import { Candidate } from './candidate';

export type CandidateAttempt = {
    id: number;
    candidateId: number;
    assessmentId: number;
    status: 'invited' | 'started' | 'completed' | 'evaluated' | 'expired';
    startedAt?: Date | null;
    submittedAt?: Date | null;
    evaluatedAt?: Date | null;
    // API response fields
    githubRepositoryLink?: string;
    languageChoice?: string;
    createdDate?: string;
    updatedDate?: string;
    startedDate?: string;
    completedDate?: string;
    evaluatedDate?: string;
    evaluationId?: number;
    assessment?: Assessment;
    candidate?: Candidate;
}