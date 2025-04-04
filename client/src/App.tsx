import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
import { AuthProvider } from "@/hooks/use-auth";
import { ProtectedRoute } from "@/lib/protected-route";

// Pages
import AuthPage from "@/pages/auth-page";
import HomePage from "@/pages/home-page";
import NotFound from "@/pages/not-found";

// Employer Pages
import EmployerDashboard from "@/pages/employer/dashboard";
import EmployerAssessments from "@/pages/employer/assessments";
import EmployerRepositories from "@/pages/employer/repositories";
import EmployerCandidates from "@/pages/employer/candidates";
import EmployerReports from "@/pages/employer/reports";
import CreateAssessment from "@/pages/employer/create-assessment";

// Candidate Pages
import CandidateAssessments from "@/pages/candidate/assessments";
import CandidateProfile from "@/pages/candidate/profile";

function Router() {
  return (
    <Switch>
      <Route path="/auth" component={AuthPage} />
      <ProtectedRoute path="/" component={HomePage} />
      
      {/* Employer Routes */}
      <ProtectedRoute path="/employer/dashboard" component={EmployerDashboard} role="employer" />
      <ProtectedRoute path="/employer/assessments" component={EmployerAssessments} role="employer" />
      <ProtectedRoute path="/employer/repositories" component={EmployerRepositories} role="employer" />
      <ProtectedRoute path="/employer/candidates" component={EmployerCandidates} role="employer" />
      <ProtectedRoute path="/employer/reports" component={EmployerReports} role="employer" />
      <ProtectedRoute path="/employer/create-assessment" component={CreateAssessment} role="employer" />
      
      {/* Candidate Routes */}
      <ProtectedRoute path="/candidate/assessments" component={CandidateAssessments} role="candidate" />
      <ProtectedRoute path="/candidate/profile" component={CandidateProfile} role="candidate" />
      
      {/* Fallback to 404 */}
      <Route component={NotFound} />
    </Switch>
  );
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <Router />
        <Toaster />
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
