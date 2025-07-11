import { API_URL, cn, authUtils } from "@/lib/utils"
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

// Zod validation schema
const signupSchema = z.object({
    name: z.string()
        .min(1, "Full name is required")
        .max(100, "Full name must be less than 100 characters")
        .trim(),
    role: z.enum(["employer", "candidate"], {
        required_error: "Please select a role"
    }),
    organizationName: z.string()
        .max(100, "Organization name must be less than 100 characters")
        .trim()
        .optional(),
    email: z.string()
        .min(1, "Email is required")
        .email("Please enter a valid email address")
        .max(255, "Email must be less than 255 characters"),
    password: z.string()
        .min(8, "Password must be at least 8 characters")
        .max(128, "Password must be less than 128 characters")
        .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, "Password must contain at least one uppercase letter, one lowercase letter, and one number")
}).refine((data) => {
    // Make organizationName required only for employers
    if (data.role === "employer" && (!data.organizationName || data.organizationName.trim() === "")) {
        return false;
    }
    return true;
}, {
    message: "Organization name is required for employers",
    path: ["organizationName"] // This tells Zod which field the error belongs to
})

type SignupFormData = z.infer<typeof signupSchema>

export function SignupForm({
    className,
    ...props
}: React.ComponentProps<"div">) {
    const [isLoading, setIsLoading] = useState(false)

    const {
        register,
        handleSubmit,
        setValue,
        watch,
        formState: { errors }
    } = useForm<SignupFormData>({
        resolver: zodResolver(signupSchema)
    })

    const selectedRole = watch("role")

    const onSubmit = async (data: SignupFormData) => {
        setIsLoading(true)
        try {
            console.log("Signing up with data:", data);
            const response = await fetch(`${API_URL}/api/auth/signup/email`, {
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
                })
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            console.log("Signup successful:", result);
            
            // Store the access token in cookie if signup returns one (auto-login)
            if (result.accessToken) {
                authUtils.setAccessToken(result.accessToken);
                console.log("Access token stored successfully after signup");
                
                // Redirect to dashboard or onboarding page after successful signup
                // window.location.href = '/dashboard'
                // Or if using a router: navigate('/dashboard')
            }
            
            // Handle successful signup (redirect, show success message, etc.)
        } catch (error) {
            console.error("Signup failed:", error);
            // Handle error (show error message, etc.)
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
                            
                            <div className="flex items-center gap-3">
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
                            </div>
                            {errors.role && (
                                <p className="text-red-400 text-sm">{errors.role.message}</p>
                            )}

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
                            
                            <div className="flex flex-col gap-3">
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
        <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10 bg-slate-800">
            <div className="w-full max-w-sm">
                <SignupForm />
            </div>
        </div>
    )
}