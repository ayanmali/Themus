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

export function LoginForm({
  className,
  ...props
}: React.ComponentProps<"div">) {
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
          <form>
            <div className="flex flex-col gap-6 text-white">
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
                  <a
                    href="#"
                    className="ml-auto inline-block text-sm underline-offset-4 hover:underline"
                  >
                    Forgot your password?
                  </a>
                </div>
                <Input id="password" type="password" required className="bg-slate-700 border-white/20" />
              </div>
              <div className="flex flex-col gap-3">
                <Button variant="outline" type="submit" className="w-full bg-slate-700 border-white/20">
                  Login
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
    <div className="flex min-h-svh w-full items-center justify-center p-6 md:p-10 bg-slate-800">
      <div className="w-full max-w-sm">
        <LoginForm />
      </div>
    </div>
  )
}