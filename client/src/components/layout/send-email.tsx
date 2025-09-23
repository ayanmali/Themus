import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogTrigger, DialogFooter } from "../ui/dialog";
import { DropdownMenuItem } from "../ui/dropdown-menu";
import { Mail } from "lucide-react";
import { Input } from "../ui/input";
import { Textarea } from "../ui/textarea";
import { Button } from "../ui/button";
import { Loader2 } from "lucide-react";
import { useMutation } from "@tanstack/react-query";
import { toast } from "@/hooks/use-toast";

export interface SendEmailDialogProps {
    apiCall: any;
    isEmailDialogOpen: boolean;
    setIsEmailDialogOpen: (open: boolean) => void;
    id?: number;
    name?: string;
    email?: string;
    emailSubject: string;
    setEmailSubject: (subject: string) => void;
    emailMessage: string;
    setEmailMessage: (message: string) => void;
    sendEmailMutation: any;
    setSelectedCandidateForEmail?: (candidate: any) => void;
}

export const sendEmailMutation = (apiCall: any, setIsEmailDialogOpen: any, setEmailSubject: any, setEmailMessage: any) => {return useMutation({
  mutationFn: async ({ candidateId, subject, text }: { candidateId: number; subject: string; text: string }) => {
    const response = await apiCall(`/api/email/send`, {
      method: 'POST',
      body: JSON.stringify({
        candidateId: candidateId,
        subject: subject,
        text: text
      }),
    });
    return response;
  },
  onSuccess: () => {
    // Close dialog and reset form
    setIsEmailDialogOpen(false);
    setEmailSubject('');
    setEmailMessage('');

    toast({
      title: "Success",
      description: "Email sent successfully",
    });
  },
  onError: (error: any) => {
    toast({
      title: "Error",
      description: error.message || "Failed to send email",
      variant: "destructive",
    });
  },
});}

// Broadcast email mutation
export const broadcastEmailMutation = (apiCall: any, setIsBroadcastDialogOpen: any, setBroadcastSubject: any, setBroadcastMessage: any) => {return useMutation({
    mutationFn: async ({ candidateIds, subject, text }: { candidateIds: number[]; subject: string; text: string }) => {
        const response = await apiCall(`/api/email/broadcast`, {
            method: 'POST',
            body: JSON.stringify({
                candidateIds: candidateIds,
                subject: subject,
                text: text
            }),
        });
        return response;
    },
    onSuccess: () => {
        setIsBroadcastDialogOpen(false);
        setBroadcastSubject('');
        setBroadcastMessage('');
        toast({
            title: "Success",
            description: "Email broadcast to all candidates successfully",
        });
    },
    onError: (error: any) => {
        toast({
            title: "Error",
            description: error.message || "Failed to send broadcast email",
            variant: "destructive",
        });
    }
});}

export function SendEmailDialog({ apiCall, isEmailDialogOpen, setIsEmailDialogOpen, id, name, email, emailSubject, setEmailSubject, emailMessage, setEmailMessage, setSelectedCandidateForEmail }: SendEmailDialogProps) {
    const emailMutation = sendEmailMutation(apiCall, setIsEmailDialogOpen, setEmailSubject, setEmailMessage);
    return (
<Dialog open={isEmailDialogOpen} onOpenChange={setIsEmailDialogOpen}>
                <DialogContent className="sm:max-w-[600px] bg-slate-800 text-white border-slate-500">
                    <DialogHeader>
                        <DialogTitle className="flex items-center gap-2">
                            <Mail size={20} />
                            Send Email to {name || email}
                        </DialogTitle>
                        <DialogDescription className="text-gray-300">
                            Send an email to the candidate.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <label htmlFor="email-subject" className="text-sm font-medium text-gray-300">
                                Subject
                            </label>
                            <Input
                                id="email-subject"
                                value={emailSubject}
                                onChange={(e) => setEmailSubject(e.target.value)}
                                placeholder="Enter email subject..."
                                className="bg-gray-700 text-white border-gray-600 focus:border-blue-400"
                            />
                        </div>

                        <div className="space-y-2">
                            <label htmlFor="email-message" className="text-sm font-medium text-gray-300">
                                Message
                            </label>
                            <Textarea
                                id="email-message"
                                value={emailMessage}
                                onChange={(e) => setEmailMessage(e.target.value)}
                                placeholder="Enter your message..."
                                className="bg-gray-700 text-white border-gray-600 focus:border-blue-400 min-h-[200px] resize-none"
                            />
                        </div>
                    </div>

                    <DialogFooter>
                        <Button
                            variant="secondary"
                            onClick={() => {
                                setIsEmailDialogOpen(false);
                                setEmailSubject('');
                                setEmailMessage('');
                                setSelectedCandidateForEmail?.(null);
                            }}
                            disabled={emailMutation.isPending}
                        >
                            Cancel
                        </Button>
                        <Button
                            onClick={() => {
                                if (id && emailSubject.trim() && emailMessage.trim()) {
                                    emailMutation.mutate({
                                        candidateId: id,
                                        subject: emailSubject.trim(),
                                        text: emailMessage.trim()
                                    });
                                }
                            }}
                            disabled={!emailSubject.trim() || !emailMessage.trim() || emailMutation.isPending}
                            className="bg-blue-600 hover:bg-blue-700 text-white"
                        >
                            {emailMutation.isPending ? (
                                <>
                                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                                    Sending...
                                </>
                            ) : (
                                <>
                                    <Mail size={16} className="mr-2" />
                                    Send Email
                                </>
                            )}
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
    );
}