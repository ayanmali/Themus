AI-based online assessments and take home assignments, designed to assess real technical and problem-solving skills.

# What is Themus?

# Why?

# Technical Overview

## Tech Stack:

### Frontend
TypeScript, React, Tailwind CSS, TanStack Query, Shadcn/ui, Vite
### Backend
#### Main Service
Java, Spring Boot, Redis, RabbitMQ, PostgreSQL, Maven
#### Companion Service
Python, FastAPI, PostgreSQL, RabbitMQ
### LLMs
Spring AI + Openrouter API
### Infra (TODO)
GCP, Docker, Kubernetes
### Other
GitHub API, Resend API

# Architecture
The main components of this application are as follows:
- A main service that handles:
    - CRUD operations for Users, Assessments, Candidates, and the Attempts that Candidates can have for an assessment
    - Coding agent that designs assessments and creates GitHub repositories
    - Sending emails
- A companion service that handles:
    - Storing and retrieving screen recordings of candidate attempts in a GCP storage bucket
    - Processing screen recordings, capturing individual frames with OpenCV and OCR/vision API (TODO) to analyze candidate behaviour in the IDE/browser
- 