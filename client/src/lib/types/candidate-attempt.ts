import { Assessment } from './assessment';
import { Candidate } from './candidate';

export type CandidateAttempt = {
    id: number;
    //candidateId: number;
    //assessmentId: number;
    status: 'invited' | 'started' | 'completed' | 'evaluated' | 'expired';
    startedAt?: Date | null;
    //submittedAt?: Date | null;
    //evaluatedAt?: Date | null;
    // API response fields
    githubRepositoryLink?: string;
    languageChoice?: string;
    createdDate?: Date;
    updatedDate?: Date;
    startedDate?: Date;
    completedDate?: Date;
    evaluatedDate?: Date;
    //evaluationId?: number;
    assessment?: Assessment;
    candidate?: Candidate;
    //evaluation?: Evaluation;
}