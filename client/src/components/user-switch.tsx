import { useAuth } from "@/hooks/use-auth";
import { Button } from "@/components/ui/button";

export function UserSwitch() {
  const { user, switchUserRole } = useAuth();

  if (!user) return null;

  return (
    <Button
      onClick={switchUserRole}
      size="sm"
      className="max-w-xs bg-primary hover:bg-primary/90 flex items-center text-sm rounded-full focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary px-3 py-1.5 text-white"
    >
      {user.role === "employer" ? "Switch to Candidate View" : "Switch to Employer View"}
    </Button>
  );
}
