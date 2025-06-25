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

export function SignupForm({
    className,
    ...props
}: React.ComponentProps<"div">) {
    return (
        <div className={cn("flex flex-col gap-6", className)} {...props}>
            <Card className="bg-slate-800 text-white border-white/20">
                <CardHeader>
                    <CardTitle>Sign up</CardTitle>
                    <CardDescription className="text-muted/80">
                        Enter your email below to sign up for an account
                    </CardDescription>
                </CardHeader>
                <CardContent>
                    <form>
                        <div className="flex flex-col gap-6 text-white">
                            <div className="grid gap-3">
                                <Label htmlFor="name">Full Name</Label>
                                <Input
                                    id="name"
                                    type="name"
                                    placeholder="John Doe"
                                    required
                                    className="bg-slate-700 border-white/20 placeholder:text-white/50"
                                />
                            </div>
                            <div className="flex items-center gap-3">
                                <Label htmlFor="roleLabel" className="w-full">I am a/an...</Label>
                                <Select>
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
                            <div className="grid gap-3">
                                <Label htmlFor="email">Email</Label>
                                <Input
                                    id="email"
                                    type="email"
                                    placeholder="m@example.com"
                                    required
                                    className="bg-slate-700 border-white/20 placeholder:text-white/50"
                                />
                            </div>
                            <div className="grid gap-3">
                                <div className="flex items-center">
                                    <Label htmlFor="password">Password</Label>

                                </div>
                                <Input id="password" type="password" required className="bg-slate-700 border-white/20" />
                            </div>
                            <div className="flex flex-col gap-3">
                                <Button variant="outline" type="submit" className="w-full bg-slate-700 border-white/20">
                                    Sign up
                                </Button>
                                <div className="text-center text-sm">or</div>
                                <Button variant="outline" className="w-full bg-slate-700 border-white/20">
                                    Continue with Google
                                </Button>
                                <Button variant="outline" className="w-full bg-slate-700 border-white/20">
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