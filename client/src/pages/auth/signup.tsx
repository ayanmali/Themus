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
import { Select, SelectContent, SelectGroup, SelectItem, SelectLabel, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import { z } from "zod"
import { useState } from "react"
import { AuthPageHeader } from "@/components/layout/auth-page-header"
import { navigate } from "wouter/use-browser-location"
import { useAuth } from "@/contexts/AuthContext"
import { Link } from "wouter"
import { API_BASE_URL } from "@/lib/utils"
import { handleAuthError, getFieldSpecificError } from "@/lib/auth-error-handler"
import { LoginError } from "@/lib/types/auth"

// Zod validation schema
const signupSchema = z.object({
    name: z.string()
        .min(1, "Full name is required")
        .max(100, "Full name must be less than 100 characters"),
    email: z.string()
        .min(1, "Email is required")
        .email("Please enter a valid email address")
        .max(255, "Email must be less than 255 characters"),
    password: z.string()
        .min(8, "Password must be at least 8 characters")
        .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, "Password must contain at least one uppercase letter, one lowercase letter, and one number")
        .max(128, "Password must be less than 128 characters"),
    role: z.enum(["employer", "candidate"], {
        required_error: "Please select your role",
    }),
    organizationName: z.string()
        .min(1, "Organization name is required")
        .max(200, "Organization name must be less than 200 characters")
        .optional()
        .or(z.literal("")),
}).refine((data) => {
    if (data.role === "employer") {
        return data.organizationName && data.organizationName.trim().length > 0;
    }
    return true;
}, {
    message: "Organization name is required for employers",
    path: ["organizationName"],
});

type SignupFormData = z.infer<typeof signupSchema>

export function SignupForm({
    className,
    ...props
}: React.ComponentProps<"div">) {
    const [isLoading, setIsLoading] = useState(false)
    const [authError, setAuthError] = useState<LoginError | null>(null)
    const { setIsAuthenticated, setUser } = useAuth()

    const {
        register,
        handleSubmit,
        setValue,
        watch,
        setError,
        clearErrors,
        formState: { errors }
    } = useForm<SignupFormData>({
        resolver: zodResolver(signupSchema)
    })

    // const selectedRole = watch("role")
    // TODO: add job applicant role to app
    const selectedRole = "employer"

    const onSubmit = async (data: SignupFormData) => {
        setIsLoading(true)
        setAuthError(null)
        clearErrors()
        
        try {
            console.log("Signing up with data:", data);
            const response = await fetch(`${API_BASE_URL}/api/auth/signup/email`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    name: data.name,
                    email: data.email,
                    password: data.password,
                    role: data.role,
                    organizationName: data.organizationName
                }),
                credentials: 'include'
            });

            if (!response.ok) {
                // Try to parse error response
                let errorData;
                try {
                    errorData = await response.json();
                    console.log("Parsed error data:", errorData);
                } catch (parseError) {
                    console.log("Failed to parse error response:", parseError);
                    errorData = { message: `HTTP error! status: ${response.status}` };
                }
                
                const signupError = handleAuthError({ ...errorData, status: response.status });
                console.log("Processed signup error:", signupError);
                setAuthError(signupError);
                
                // Set field-specific errors
                const fieldError = getFieldSpecificError(signupError);
                console.log("Field error:", fieldError);
                if (fieldError.field === 'email') {
                    setError('email', { message: fieldError.message });
                } else if (fieldError.field === 'password') {
                    setError('password', { message: fieldError.message });
                }
                
                return;
            }

            const result = await response.json();
            console.log("Signup successful:", result);
            
            // Update authentication state after successful signup
            setIsAuthenticated(true);
            setUser(result);
            
            // Navigate to dashboard after auth state is updated
            navigate("/dashboard");

        } catch (error) {
            console.error("Signup request failed:", error);
            const signupError = handleAuthError(error);
            setAuthError(signupError);
            
            // Set general error for network issues
            if (signupError.type === 'UNKNOWN_ERROR') {
                setError('root', { message: signupError.userFriendlyMessage });
            }
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <Card className="bg-slate-800 text-white border-white/20">
                <CardHeader>
                    <CardTitle>Sign up</CardTitle>
                    <CardDescription className="text-muted/80">
                        Enter your information below to sign up for an account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)}>
                        <div className="flex flex-col gap-6 text-white">
                            <div className="grid gap-3">
                                <Label htmlFor="name">Full Name</Label>
                                <Input
                                    id="name"
                                    type="text"
                                    placeholder="John Doe"
                                    {...register("name")}
                                    className="bg-slate-700 border-white/20 placeholder:text-white/50"
                                />
                                {errors.name && (
                                    <p className="text-red-400 text-sm">{errors.name.message}</p>
                                )}
                            </div>

                            {/* <div className="flex items-center gap-3">
                                <Label htmlFor="roleLabel" className="w-full">I am a/an...</Label>
                                <Select onValueChange={(value: "employer" | "candidate") => setValue("role", value)}>
                                    <SelectTrigger className="w-full bg-slate-700 border-white/20">
                                        <SelectValue placeholder="Select a role" />
                                    </SelectTrigger>
                                    <SelectContent className="bg-slate-800 text-white border-white/20">
                                        <SelectGroup>
                                            <SelectItem value="employer">Employer</SelectItem>
                                            <SelectItem value="candidate">Job Applicant</SelectItem>
                                        </SelectGroup>
                                    </SelectContent>
                                </Select>
                            </div> */}
                            {/* {errors.role && (
                                <p className="text-red-400 text-sm">{errors.role.message}</p>
                            )} */}

                            {selectedRole === "employer" && (
                                <div className="grid gap-3">
                                    <Label htmlFor="organizationName">
                                        Organization Name
                                    </Label>
                                    <Input
                                        id="organizationName"
                                        type="text"
                                        placeholder={selectedRole === "employer" ? "Acme Corp" : ""}
                                        {...register("organizationName")}
                                        className="bg-slate-700 border-white/20 placeholder:text-white/50"
                                    />
                                    {errors.organizationName && (
                                        <p className="text-red-400 text-sm">{errors.organizationName.message}</p>
                                    )}
                                </div>
                            )}

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
                                <p className="text-xs text-white/60">
                                    Password must be at least 8 characters with uppercase, lowercase, and number
                                </p>
                            </div>
                            
                            {/* General error message */}
                            {authError && authError.type !== 'EMAIL_ALREADY_EXISTS' && authError.type !== 'VALIDATION_ERROR' && (
                                <div className="p-3 bg-red-500/10 border border-red-500/20 rounded-md">
                                    <p className="text-red-400 text-sm">{authError.userFriendlyMessage}</p>
                                </div>
                            )}

                            <div className="flex flex-col gap-3 items-center">
                                <Button
                                    variant="outline"
                                    type="submit"
                                    className="w-full bg-slate-700 border-white/20"
                                    disabled={isLoading}
                                >
                                    {isLoading ? "Signing up..." : "Sign up"}
                                </Button>
                                <div className="text-center text-sm">or</div>
                                <Button variant="outline" className="w-full bg-slate-700 border-white/20" type="button">
                                    Continue with Google
                                </Button>
                                <Button variant="outline" className="w-full bg-slate-700 border-white/20" type="button">
                                    Continue with GitHub
                                </Button>
                                <span className="text-sm text-white/60 mt-4">
                                    Already have an account? <Link href="/login" className="text-blue-100 hover:text-blue-300">Login</Link>
                                </span>
                            </div>
                        </div>
                    </form>
                </CardContent>
            </Card>
        </div>
    )
}

export default function SignupPage() {
    return (
        <div>
        <AuthPageHeader />
        <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10 bg-slate-800">
          <div className="w-full max-w-sm mt-10">
            <SignupForm />
          </div>
        </div>
      </div>
    )
}