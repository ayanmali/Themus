import { Switch, Route } from "wouter";
import { queryClient } from "./lib/queryClient";
import { QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "@/components/ui/toaster";
//import { AuthProvider } from "@/hooks/use-auth";
import { ProtectedRoute } from "@/lib/protected-route";

// Pages
import NotFound from "@/pages/not-found";

// Employer Pages
import EmployerDashboard from "@/pages/employer/dashboard";
import EmployerAssessments from "@/pages/employer/assessmentss";
import EmployerCandidates from "@/pages/employer/candidates";
import EmployerReports from "@/pages/employer/reports";
import CreateAssessment from "@/pages/employer/new-assessment/create-assessment";

// Candidate Pages
import CandidateAssessments from "@/pages/candidate/assessments";
import CandidateProfile from "@/pages/candidate/profile";
import LandingPage from "./pages/landing-page/lander";
import SignupPage from "./pages/auth/signup";
import LoginPage from "./pages/auth/login";
import CandidateAssessmentInvite from "./pages/candidate/assessment-invite";
import CandidateDashboard from "./pages/candidate/assessments-overview";
import {PricingPage, SubscriptionSuccessPage } from "./pages/pricing";
import ForgotPassword from "./pages/auth/forgot-password";

function Router() {
  return (
    <Switch>
      <Route path="/" component={LandingPage} />
      <Route path="/login" component={LoginPage} />
      <Route path="/signup" component={SignupPage} />
      <Route path="/forgot-password" component={ForgotPassword} />
      
      {/* Protected Employer Routes */}
      <ProtectedRoute path="/dashboard" component={EmployerDashboard} />
      <ProtectedRoute path="/assessments" component={EmployerAssessments} />
      <ProtectedRoute path="/assessments/new" component={CreateAssessment} />
      <ProtectedRoute path="/candidates" component={EmployerCandidates} />
      <ProtectedRoute path="/reports" component={EmployerReports} />
      
      {/* Public routes */}
      <Route path="/pricing" component={PricingPage} />
      <Route path="/checkout/success" component={SubscriptionSuccessPage} />

      {/* <Route path="/auth" component={AuthPage} /> */}
      {/* <ProtectedRoute path="/" component={HomePage} /> */}
      
      {/* Employer Routes */}
      {/* <ProtectedRoute path="/dashboard" component={EmployerDashboard} role="employer" /> */}
      {/* <ProtectedRoute path="/assessments" component={EmployerAssessments} role="employer" /> */}
      {/* <ProtectedRoute path="/repositories" component={EmployerRepositories} role="employer" /> */}
      {/* <ProtectedRoute path="/candidates" component={EmployerCandidates} role="employer" /> */}
      {/* <ProtectedRoute path="/reports" component={EmployerReports} role="employer" /> */}
      {/* <ProtectedRoute path="/assessments/new" component={CreateAssessment} role="employer" /> */}
      
      {/* Candidate Routes */}
      {/* <Route path="/start-assessment/{assessment_id}" component={StartAssessment} /> */}
      {/* <Route path="/candidate/dashboard" component={CandidateDashboard} /> */}
      <Route path="/invite" component={CandidateAssessmentInvite} />
      <Route path="/assessments-overview" component={CandidateDashboard} />
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
      <Router />
      <Toaster />
      {/* <AuthProvider> */}
      {/* </AuthProvider> */}
    </QueryClientProvider>
  );
}

export default App;
