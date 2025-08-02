// import { useEffect } from "react";
// import { useLocation } from "wouter";
// import { useAuth } from "@/hooks/use-auth";
// import { Loader2 } from "lucide-react";

// export default function HomePage() {
//   const { user, isLoading } = useAuth();
//   const [, navigate] = useLocation();

//   useEffect(() => {
//     if (!isLoading && user) {
//       // Redirect based on user role
//       if (user.role === "employer") {
//         navigate("/employer/dashboard");
//       } else if (user.role === "candidate") {
//         navigate("/candidate/assessments");
//       }
//     }
//   }, [user, isLoading, navigate]);

//   if (isLoading) {
//     return (
//       <div className="flex items-center justify-center min-h-screen">
//         <Loader2 className="h-8 w-8 animate-spin text-primary" />
//       </div>
//     );
//   }

//   return (
//     <div className="flex items-center justify-center min-h-screen">
//       <p>Redirecting to dashboard...</p>
//     </div>
//   );
// }
