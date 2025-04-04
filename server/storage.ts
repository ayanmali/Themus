import {
  users,
  repositories,
  assessments,
  candidateAssessments,
  type User,
  type InsertUser,
  type Repository,
  type InsertRepository,
  type Assessment,
  type InsertAssessment,
  type CandidateAssessment,
  type InsertCandidateAssessment,
  type UpdateCandidateAssessment
} from "@shared/schema";
import session from "express-session";
import createMemoryStore from "memorystore";

const MemoryStore = createMemoryStore(session);

// modify the interface with any CRUD methods
// you might need
export interface IStorage {
  // User methods
  getUser(id: number): Promise<User | undefined>;
  getUserByUsername(username: string): Promise<User | undefined>;
  createUser(user: InsertUser): Promise<User>;
  
  // Repository methods
  getRepository(id: number): Promise<Repository | undefined>;
  getRepositoriesByEmployer(employerId: number): Promise<Repository[]>;
  createRepository(repository: InsertRepository): Promise<Repository>;
  
  // Assessment methods
  getAssessment(id: number): Promise<Assessment | undefined>;
  getAssessmentsByEmployer(employerId: number): Promise<Assessment[]>;
  createAssessment(assessment: InsertAssessment): Promise<Assessment>;
  updateAssessmentStatus(id: number, status: string): Promise<Assessment | undefined>;
  
  // Candidate Assessment methods
  getCandidateAssessment(id: number): Promise<CandidateAssessment | undefined>;
  getCandidateAssessmentsByCandidate(candidateId: number): Promise<CandidateAssessment[]>;
  getCandidateAssessmentsByAssessment(assessmentId: number): Promise<CandidateAssessment[]>;
  createCandidateAssessment(candidateAssessment: InsertCandidateAssessment): Promise<CandidateAssessment>;
  updateCandidateAssessment(
    id: number, 
    updates: UpdateCandidateAssessment
  ): Promise<CandidateAssessment | undefined>;
  
  // Session store
  sessionStore: session.SessionStore;
}

export class MemStorage implements IStorage {
  private usersMap: Map<number, User>;
  private repositoriesMap: Map<number, Repository>;
  private assessmentsMap: Map<number, Assessment>;
  private candidateAssessmentsMap: Map<number, CandidateAssessment>;
  
  userCurrentId: number;
  repositoryCurrentId: number;
  assessmentCurrentId: number;
  candidateAssessmentCurrentId: number;
  sessionStore: session.SessionStore;

  constructor() {
    this.usersMap = new Map();
    this.repositoriesMap = new Map();
    this.assessmentsMap = new Map();
    this.candidateAssessmentsMap = new Map();
    
    this.userCurrentId = 1;
    this.repositoryCurrentId = 1;
    this.assessmentCurrentId = 1;
    this.candidateAssessmentCurrentId = 1;
    
    this.sessionStore = new MemoryStore({
      checkPeriod: 86400000,
    });
  }

  // User methods
  async getUser(id: number): Promise<User | undefined> {
    return this.usersMap.get(id);
  }

  async getUserByUsername(username: string): Promise<User | undefined> {
    return Array.from(this.usersMap.values()).find(
      (user) => user.username === username,
    );
  }

  async createUser(insertUser: InsertUser): Promise<User> {
    const id = this.userCurrentId++;
    const now = new Date();
    const user: User = { ...insertUser, id, createdAt: now };
    this.usersMap.set(id, user);
    return user;
  }
  
  // Repository methods
  async getRepository(id: number): Promise<Repository | undefined> {
    return this.repositoriesMap.get(id);
  }
  
  async getRepositoriesByEmployer(employerId: number): Promise<Repository[]> {
    return Array.from(this.repositoriesMap.values()).filter(
      (repo) => repo.employerId === employerId
    );
  }
  
  async createRepository(insertRepository: InsertRepository): Promise<Repository> {
    const id = this.repositoryCurrentId++;
    const now = new Date();
    const repository: Repository = { ...insertRepository, id, createdAt: now };
    this.repositoriesMap.set(id, repository);
    return repository;
  }
  
  // Assessment methods
  async getAssessment(id: number): Promise<Assessment | undefined> {
    return this.assessmentsMap.get(id);
  }
  
  async getAssessmentsByEmployer(employerId: number): Promise<Assessment[]> {
    return Array.from(this.assessmentsMap.values()).filter(
      (assessment) => assessment.employerId === employerId
    );
  }
  
  async createAssessment(insertAssessment: InsertAssessment): Promise<Assessment> {
    const id = this.assessmentCurrentId++;
    const now = new Date();
    const assessment: Assessment = { ...insertAssessment, id, createdAt: now };
    this.assessmentsMap.set(id, assessment);
    return assessment;
  }
  
  async updateAssessmentStatus(id: number, status: string): Promise<Assessment | undefined> {
    const assessment = this.assessmentsMap.get(id);
    if (!assessment) return undefined;
    
    const updatedAssessment = { ...assessment, status };
    this.assessmentsMap.set(id, updatedAssessment);
    return updatedAssessment;
  }
  
  // Candidate Assessment methods
  async getCandidateAssessment(id: number): Promise<CandidateAssessment | undefined> {
    return this.candidateAssessmentsMap.get(id);
  }
  
  async getCandidateAssessmentsByCandidate(candidateId: number): Promise<CandidateAssessment[]> {
    return Array.from(this.candidateAssessmentsMap.values()).filter(
      (candidateAssessment) => candidateAssessment.candidateId === candidateId
    );
  }
  
  async getCandidateAssessmentsByAssessment(assessmentId: number): Promise<CandidateAssessment[]> {
    return Array.from(this.candidateAssessmentsMap.values()).filter(
      (candidateAssessment) => candidateAssessment.assessmentId === assessmentId
    );
  }
  
  async createCandidateAssessment(insertCandidateAssessment: InsertCandidateAssessment): Promise<CandidateAssessment> {
    const id = this.candidateAssessmentCurrentId++;
    const now = new Date();
    const candidateAssessment: CandidateAssessment = { 
      ...insertCandidateAssessment, 
      id, 
      progress: 0,
      pullRequestUrl: null,
      completedDate: null,
      createdAt: now 
    };
    this.candidateAssessmentsMap.set(id, candidateAssessment);
    return candidateAssessment;
  }
  
  async updateCandidateAssessment(
    id: number, 
    updates: UpdateCandidateAssessment
  ): Promise<CandidateAssessment | undefined> {
    const candidateAssessment = this.candidateAssessmentsMap.get(id);
    if (!candidateAssessment) return undefined;
    
    const updatedCandidateAssessment: CandidateAssessment = { 
      ...candidateAssessment,
      ...updates
    };
    this.candidateAssessmentsMap.set(id, updatedCandidateAssessment);
    return updatedCandidateAssessment;
  }
}

export const storage = new MemStorage();
