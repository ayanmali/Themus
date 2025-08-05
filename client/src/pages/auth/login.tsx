import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { useState, useEffect } from "react"
import { Link } from "wouter"
import { AuthPageHeader } from "@/components/layout/auth-page-header"
import { navigate } from "wouter/use-browser-location"
import { useAuth } from "@/contexts/AuthContext"
import { API_BASE_URL } from "@/lib/utils"
import { handleAuthError, getFieldSpecificError } from "@/lib/auth-error-handler"
import { LoginError } from "@/lib/types/auth"

// Zod validation schema
const loginSchema = z.object({
  email: z.string()
    .min(1, "Email is required")
    .email("Please enter a valid email address")
    .max(255, "Email must be less than 255 characters"),
  password: z.string()
    .min(1, "Password is required")
    .max(128, "Password must be less than 128 characters")
})

type LoginFormData = z.infer<typeof loginSchema>

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
  const [isLoading, setIsLoading] = useState(false)
  const [authError, setAuthError] = useState<LoginError | null>(null)
  const { isAuthenticated, setIsAuthenticated, setUser, isLoading: authLoading } = useAuth();

  const {
    register,
    handleSubmit,
    formState: { errors },
    setError,
    clearErrors
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema)
  })

  // Move navigation logic to useEffect to avoid render-time state updates
  useEffect(() => {
    if (!authLoading && isAuthenticated) {
      navigate("/dashboard");
    }
  }, [isAuthenticated, authLoading]);

  const onSubmit = async (data: LoginFormData) => {
    setIsLoading(true)
    setAuthError(null)
    clearErrors()
    
    try {
      console.log("Logging in with data:", data);
      const response = await fetch(`${API_BASE_URL}/api/auth/login/email`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          email: data.email,
          password: data.password
        }),
        credentials: 'include'
      });

      if (!response.ok) {
        console.log("Login failed with status:", response.status);
        
        // Try to parse error response
        let errorData;
        try {
          errorData = await response.json();
          console.log("Parsed error data:", errorData);
        } catch (parseError) {
          console.log("Failed to parse error response:", parseError);
          errorData = { message: `HTTP error! status: ${response.status}` };
        }
        
        const loginError = handleAuthError({ ...errorData, status: response.status });
        console.log("Processed login error:", loginError);
        setAuthError(loginError);
        
        // Set field-specific errors
        const fieldError = getFieldSpecificError(loginError);
        console.log("Field error:", fieldError);
        if (fieldError.field === 'email') {
          setError('email', { message: fieldError.message });
        } else if (fieldError.field === 'password') {
          setError('password', { message: fieldError.message });
        }
        
        return;
      }

      const result = await response.json();
      console.log("Login successful:", result);
      
      // Update authentication state after successful login
      setIsAuthenticated(true);
      setUser(result);
      
      // Navigate to dashboard after auth state is updated
      navigate("/dashboard");

    } catch (error) {
      console.error("Login failed:", error);
      const loginError = handleAuthError(error);
      setAuthError(loginError);
      
      // Set general error for network issues
      if (loginError.type === 'UNKNOWN_ERROR') {
        setError('root', { message: loginError.userFriendlyMessage });
      }
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className={cn("flex flex-col gap-6", className)} {...props}>
      <Card className="bg-slate-800 text-white border-white/20">
        <CardHeader>
          <CardTitle>Login to your account</CardTitle>
          <CardDescription className="text-muted/80">
            Enter your email below to login to your account
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)}>
            <div className="flex flex-col gap-6 text-white">
              <div className="grid gap-3">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="m@example.com"
                  {...register("email")}
                  className="bg-slate-700 border-white/20 placeholder:text-white/50"
                />
                {errors.email && (
                  <p className="text-red-400 text-sm">{errors.email.message}</p>
                )}
              </div>
              <div className="grid gap-3">
                <div className="flex items-center">
                  <Label htmlFor="password">Password</Label>
                  <Link href="/forgot-password" className="text-sm text-white/50 ml-auto">
                    Forgot your password?
                  </Link>
                </div>
                <Input
                  id="password"
                  type="password"
                  {...register("password")}
                  className="bg-slate-700 border-white/20"
                />
                {errors.password && (
                  <p className="text-red-400 text-sm">{errors.password.message}</p>
                )}
              </div>
              {/* General error message */}
              {authError && authError.type !== 'USER_NOT_FOUND' && authError.type !== 'INVALID_PASSWORD' && (
                <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-md">
                  <p className="text-red-400 text-sm">{authError.userFriendlyMessage}</p>
                </div>
              )}
              
              <div className="flex flex-col gap-3">
                <Button
                  variant="outline"
                  type="submit"
                  className="w-full bg-slate-700 border-white/20"
                  disabled={isLoading}
                >
                  {isLoading ? "Logging in..." : "Login"}
                </Button>
                <div className="text-center text-sm">or</div>
                <Button variant="outline" className="w-full bg-slate-700 border-white/20" type="button">
                  Continue with Google
                </Button>
                <Button variant="outline" className="w-full bg-slate-700 border-white/20" type="button">
                  Continue with GitHub
                </Button>
              </div>
            </div>
            <div className="mt-4 text-center text-sm">
              Don&apos;t have an account?{" "}
              <a href="/signup" className="underline underline-offset-4">
                Sign up
              </a>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}

export default function LoginPage() {
  return (
    <div>
      <AuthPageHeader />
      <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10 bg-slate-800">
        <div className="w-full max-w-sm">
          <LoginForm />
        </div>
      </div>
    </div>
  )
}