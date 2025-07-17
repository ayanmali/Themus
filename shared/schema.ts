import { pgTable, text, serial, integer, boolean, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { array, z } from "zod";

export const users = pgTable("users", {
  id: serial("id").primaryKey(),
  username: text("username").notNull().unique(),
  password: text("password").notNull(),
  email: text("email").notNull(),
  name: text("name").notNull(),
  role: text("role").notNull().default("candidate"), // "employer" or "candidate"
  createdAt: timestamp("created_at").defaultNow().notNull(),
});

export const repositories = pgTable("repositories", {
  id: serial("id").primaryKey(),
  name: text("name").notNull(),
  url: text("url").notNull(),
  description: text("description"),
  tags: text("tags").array(),
  employerId: integer("employer_id").notNull(),
  createdAt: timestamp("created_at").defaultNow().notNull(),
});

export const assessments = pgTable("assessments", {
  model: text("model").notNull(),
  id: serial("id").primaryKey(),
  title: text("title").notNull(),
  assessmentType: text("assessment_type").notNull(),
  role: text("role").notNull(),
  skills: text("skills").notNull(),
  description: text("description"),
  repositoryLink: text("repository_link"),
  duration: integer("duration").notNull(),
  durationUnit: text("duration_unit").notNull(),
  candidateChoices: text("candidate_choices").array(),
  //repositoryId: integer("repository_id").notNull(),
  userId: integer("user_id").notNull(),
  startDate: timestamp("start_date"),
  endDate: timestamp("end_date"),
  createdAt: timestamp("created_at").defaultNow().notNull(),
});

export const candidateAssessments = pgTable("candidate_assessments", {
  id: serial("id").primaryKey(),
  assessmentId: integer("assessment_id").notNull(),
  candidateId: integer("candidate_id").notNull(),
  startDate: timestamp("start_date"),
  dueDate: timestamp("due_date"),
  completedDate: timestamp("completed_date"),
  pullRequestUrl: text("pull_request_url"),
  status: text("status").notNull().default("not_started"), // "not_started", "in_progress", "completed"
  progress: integer("progress").default(0),
  createdAt: timestamp("created_at").defaultNow().notNull(),
});

// Insert schemas
export const insertUserSchema = createInsertSchema(users).pick({
  username: true,
  password: true,
  email: true,
  name: true,
  role: true,
});

export const insertRepositorySchema = createInsertSchema(repositories).pick({
  name: true,
  url: true,
  description: true,
  tags: true,
  employerId: true,
});

export const insertAssessmentSchema = createInsertSchema(assessments).pick({
  model: true,
  title: true, // for the user's reference
  role: true, // for the user's reference
  skills: true, // for the user's reference
  description: true, // LLM input
  repositoryLink: true,
  duration: true,
  durationUnit: true,
  candidateChoices: true,
  assessmentType: true,
  userId: true,
  startDate: true,
  endDate: true,
});

export const insertCandidateAssessmentSchema = createInsertSchema(candidateAssessments).pick({
  assessmentId: true,
  candidateId: true,
  startDate: true,
  dueDate: true,
  status: true,
});

// Types
export type User = typeof users.$inferSelect;
export type InsertUser = z.infer<typeof insertUserSchema>;

export type Repository = typeof repositories.$inferSelect;
export type InsertRepository = z.infer<typeof insertRepositorySchema>;

export type Assessment = typeof assessments.$inferSelect;
export type InsertAssessment = z.infer<typeof insertAssessmentSchema>;

export type CandidateAssessment = typeof candidateAssessments.$inferSelect;
export type InsertCandidateAssessment = z.infer<typeof insertCandidateAssessmentSchema>;

// Update schemas
export const updateCandidateAssessmentSchema = z.object({
  status: z.enum(["not_started", "in_progress", "completed"]).optional(),
  progress: z.number().min(0).max(100).optional(),
  startDate: z.date().optional(),
  dueDate: z.date().optional(),
  completedDate: z.date().optional(),
  pullRequestUrl: z.string().url().optional(),
});

export type UpdateCandidateAssessment = z.infer<typeof updateCandidateAssessmentSchema>;
