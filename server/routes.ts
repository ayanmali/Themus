import type { Express } from "express";
import { createServer, type Server } from "http";
import { storage } from "./storage";
import { setupAuth } from "./auth";
import { z } from "zod";
import { 
  insertRepositorySchema, 
  insertAssessmentSchema, 
  insertCandidateAssessmentSchema, 
  updateCandidateAssessmentSchema 
} from "@shared/schema";

export async function registerRoutes(app: Express): Promise<Server> {
  // Setup authentication routes
  setupAuth(app);

  // Middleware to ensure user is authenticated
  const ensureAuthenticated = (req: any, res: any, next: any) => {
    if (req.isAuthenticated()) {
      return next();
    }
    res.status(401).json({ message: "Unauthorized" });
  };

  // Repository routes
  app.get("/api/repositories", ensureAuthenticated, async (req, res) => {
    try {
      const employerId = req.user?.role === "employer" ? req.user.id : undefined;
      
      if (!employerId) {
        return res.status(403).json({ message: "Forbidden: Only employers can access repositories" });
      }
      
      const repositories = await storage.getRepositoriesByEmployer(employerId);
      res.json(repositories);
    } catch (error) {
      res.status(500).json({ message: "Failed to fetch repositories" });
    }
  });

  app.get("/api/repositories/:id", ensureAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const repository = await storage.getRepository(id);
      
      if (!repository) {
        return res.status(404).json({ message: "Repository not found" });
      }
      
      // Check if user has access to this repository
      if (req.user?.role === "employer" && repository.employerId !== req.user.id) {
        return res.status(403).json({ message: "Forbidden: You don't have access to this repository" });
      }
      
      res.json(repository);
    } catch (error) {
      res.status(500).json({ message: "Failed to fetch repository" });
    }
  });

  app.post("/api/repositories", ensureAuthenticated, async (req, res) => {
    try {
      if (req.user?.role !== "employer") {
        return res.status(403).json({ message: "Forbidden: Only employers can create repositories" });
      }
      
      const validationResult = insertRepositorySchema.safeParse({
        ...req.body,
        employerId: req.user.id
      });
      
      if (!validationResult.success) {
        return res.status(400).json({ message: "Invalid repository data", errors: validationResult.error.errors });
      }
      
      const repository = await storage.createRepository(validationResult.data);
      res.status(201).json(repository);
    } catch (error) {
      res.status(500).json({ message: "Failed to create repository" });
    }
  });

  // Assessment routes
  app.get("/api/assessments", ensureAuthenticated, async (req, res) => {
    try {
      const userId = req.user?.id;
      
      if (req.user?.role === "employer") {
        const employerAssessments = await storage.getAssessmentsByEmployer(userId);
        return res.json(employerAssessments);
      } else {
        // For candidates, get their assigned assessments
        const candidateAssessments = await storage.getCandidateAssessmentsByCandidate(userId);
        
        // Get the full assessment details for each candidate assessment
        const assessmentDetails = await Promise.all(
          candidateAssessments.map(async (ca) => {
            const assessment = await storage.getAssessment(ca.assessmentId);
            if (assessment) {
              return {
                ...assessment,
                candidateAssessment: ca
              };
            }
            return null;
          })
        );
        
        return res.json(assessmentDetails.filter(a => a !== null));
      }
    } catch (error) {
      res.status(500).json({ message: "Failed to fetch assessments" });
    }
  });

  app.get("/api/assessments/:id", ensureAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const assessment = await storage.getAssessment(id);
      
      if (!assessment) {
        return res.status(404).json({ message: "Assessment not found" });
      }
      
      // Check if user has access to this assessment
      if (req.user?.role === "employer" && assessment.employerId !== req.user.id) {
        return res.status(403).json({ message: "Forbidden: You don't have access to this assessment" });
      }
      
      if (req.user?.role === "candidate") {
        // Check if the candidate is assigned to this assessment
        const candidateAssessments = await storage.getCandidateAssessmentsByCandidate(req.user.id);
        const isAssigned = candidateAssessments.some(ca => ca.assessmentId === id);
        
        if (!isAssigned) {
          return res.status(403).json({ message: "Forbidden: You don't have access to this assessment" });
        }
      }
      
      res.json(assessment);
    } catch (error) {
      res.status(500).json({ message: "Failed to fetch assessment" });
    }
  });

  app.post("/api/assessments", ensureAuthenticated, async (req, res) => {
    try {
      if (req.user?.role !== "employer") {
        return res.status(403).json({ message: "Forbidden: Only employers can create assessments" });
      }
      
      const validationResult = insertAssessmentSchema.safeParse({
        ...req.body,
        employerId: req.user.id
      });
      
      if (!validationResult.success) {
        return res.status(400).json({ message: "Invalid assessment data", errors: validationResult.error.errors });
      }
      
      const assessment = await storage.createAssessment(validationResult.data);
      res.status(201).json(assessment);
    } catch (error) {
      res.status(500).json({ message: "Failed to create assessment" });
    }
  });

  app.patch("/api/assessments/:id/status", ensureAuthenticated, async (req, res) => {
    try {
      if (req.user?.role !== "employer") {
        return res.status(403).json({ message: "Forbidden: Only employers can update assessment status" });
      }
      
      const id = parseInt(req.params.id);
      const assessment = await storage.getAssessment(id);
      
      if (!assessment) {
        return res.status(404).json({ message: "Assessment not found" });
      }
      
      if (assessment.employerId !== req.user.id) {
        return res.status(403).json({ message: "Forbidden: You don't have access to this assessment" });
      }
      
      const statusSchema = z.object({
        status: z.enum(["draft", "active", "completed"])
      });
      
      const validationResult = statusSchema.safeParse(req.body);
      
      if (!validationResult.success) {
        return res.status(400).json({ message: "Invalid status", errors: validationResult.error.errors });
      }
      
      const updatedAssessment = await storage.updateAssessmentStatus(id, validationResult.data.status);
      res.json(updatedAssessment);
    } catch (error) {
      res.status(500).json({ message: "Failed to update assessment status" });
    }
  });

  // Candidate Assessment routes
  app.get("/api/candidate-assessments", ensureAuthenticated, async (req, res) => {
    try {
      const assessmentId = req.query.assessmentId ? parseInt(req.query.assessmentId as string) : undefined;
      const candidateId = req.query.candidateId ? parseInt(req.query.candidateId as string) : undefined;
      
      if (assessmentId) {
        // Check if the user has access to this assessment
        const assessment = await storage.getAssessment(assessmentId);
        
        if (!assessment) {
          return res.status(404).json({ message: "Assessment not found" });
        }
        
        if (req.user?.role === "employer" && assessment.employerId !== req.user.id) {
          return res.status(403).json({ message: "Forbidden: You don't have access to this assessment" });
        }
        
        const candidateAssessments = await storage.getCandidateAssessmentsByAssessment(assessmentId);
        return res.json(candidateAssessments);
      }
      
      if (candidateId) {
        // Check if the user is the candidate or an employer with access
        if (req.user?.role === "candidate" && req.user.id !== candidateId) {
          return res.status(403).json({ message: "Forbidden: You don't have access to this candidate's assessments" });
        }
        
        const candidateAssessments = await storage.getCandidateAssessmentsByCandidate(candidateId);
        
        if (req.user?.role === "employer") {
          // Filter out assessments that don't belong to this employer
          const employerAssessments = await storage.getAssessmentsByEmployer(req.user.id);
          const employerAssessmentIds = employerAssessments.map(a => a.id);
          
          const filteredAssessments = candidateAssessments.filter(ca => 
            employerAssessmentIds.includes(ca.assessmentId)
          );
          
          return res.json(filteredAssessments);
        }
        
        return res.json(candidateAssessments);
      }
      
      // If no specific filters, return based on role
      if (req.user?.role === "candidate") {
        const candidateAssessments = await storage.getCandidateAssessmentsByCandidate(req.user.id);
        return res.json(candidateAssessments);
      } else if (req.user?.role === "employer") {
        // For employers, get all candidate assessments for their assessments
        const employerAssessments = await storage.getAssessmentsByEmployer(req.user.id);
        const employerAssessmentIds = employerAssessments.map(a => a.id);
        
        const allCandidateAssessments: any[] = [];
        
        for (const assessmentId of employerAssessmentIds) {
          const candidateAssessments = await storage.getCandidateAssessmentsByAssessment(assessmentId);
          allCandidateAssessments.push(...candidateAssessments);
        }
        
        return res.json(allCandidateAssessments);
      }
      
    } catch (error) {
      res.status(500).json({ message: "Failed to fetch candidate assessments" });
    }
  });

  app.post("/api/candidate-assessments", ensureAuthenticated, async (req, res) => {
    try {
      if (req.user?.role !== "employer") {
        return res.status(403).json({ message: "Forbidden: Only employers can assign assessments" });
      }
      
      const validationResult = insertCandidateAssessmentSchema.safeParse(req.body);
      
      if (!validationResult.success) {
        return res.status(400).json({ message: "Invalid assignment data", errors: validationResult.error.errors });
      }
      
      // Check if the employer has access to this assessment
      const assessment = await storage.getAssessment(validationResult.data.assessmentId);
      
      if (!assessment) {
        return res.status(404).json({ message: "Assessment not found" });
      }
      
      if (assessment.employerId !== req.user.id) {
        return res.status(403).json({ message: "Forbidden: You don't have access to this assessment" });
      }
      
      const candidateAssessment = await storage.createCandidateAssessment(validationResult.data);
      res.status(201).json(candidateAssessment);
    } catch (error) {
      res.status(500).json({ message: "Failed to assign assessment" });
    }
  });

  app.patch("/api/candidate-assessments/:id", ensureAuthenticated, async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const candidateAssessment = await storage.getCandidateAssessment(id);
      
      if (!candidateAssessment) {
        return res.status(404).json({ message: "Candidate assessment not found" });
      }
      
      // Check if the user has access to update this candidate assessment
      if (req.user?.role === "candidate" && candidateAssessment.candidateId !== req.user.id) {
        return res.status(403).json({ message: "Forbidden: You don't have access to update this assessment" });
      }
      
      if (req.user?.role === "employer") {
        // Check if the employer owns the assessment
        const assessment = await storage.getAssessment(candidateAssessment.assessmentId);
        
        if (!assessment || assessment.employerId !== req.user.id) {
          return res.status(403).json({ message: "Forbidden: You don't have access to update this assessment" });
        }
      }
      
      const validationResult = updateCandidateAssessmentSchema.safeParse(req.body);
      
      if (!validationResult.success) {
        return res.status(400).json({ message: "Invalid update data", errors: validationResult.error.errors });
      }
      
      const updatedCandidateAssessment = await storage.updateCandidateAssessment(id, validationResult.data);
      res.json(updatedCandidateAssessment);
    } catch (error) {
      res.status(500).json({ message: "Failed to update candidate assessment" });
    }
  });

  const httpServer = createServer(app);

  return httpServer;
}
