import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Clipboard, ExternalLink } from "lucide-react";
import { useToast } from "@/hooks/use-toast";

interface RepositoryCardProps {
  id: number;
  name: string;
  url: string;
  description?: string | null;
  tags?: string[];
  lastUsedIn?: string;
}

export function RepositoryCard({
  id,
  name,
  url,
  description,
  tags = [],
  lastUsedIn,
}: RepositoryCardProps) {
  const { toast } = useToast();
  
  const handleCopyUrl = () => {
    navigator.clipboard.writeText(url).then(
      () => {
        toast({
          title: "URL copied",
          description: "Repository URL copied to clipboard",
        });
      },
      (err) => {
        toast({
          title: "Failed to copy",
          description: "Could not copy the URL to clipboard",
          variant: "destructive",
        });
      }
    );
  };
  
  return (
    <div className="px-4 py-4 sm:px-6 bg-white shadow rounded-lg">
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <svg className="h-8 w-8 text-gray-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 9l3 3-3 3m5 0h3M5 20h14a2 2 0 002-2V6a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <div className="ml-4">
            <div className="text-lg font-medium text-primary">{name}</div>
            <div className="text-sm text-gray-500 font-mono truncate max-w-sm">{url}</div>
          </div>
        </div>
        <div className="flex space-x-2">
          <Button size="sm" onClick={handleCopyUrl}>
            <Clipboard className="h-4 w-4 mr-1" />
            Copy URL
          </Button>
          <Button size="sm" variant="outline" asChild>
            <a href={url} target="_blank" rel="noopener noreferrer">
              <ExternalLink className="h-4 w-4 mr-1" />
              View
            </a>
          </Button>
        </div>
      </div>
      
      {description && (
        <div className="mt-2">
          <p className="text-sm text-gray-600">{description}</p>
        </div>
      )}
      
      {tags.length > 0 && (
        <div className="mt-2">
          {tags.map((tag, index) => (
            <Badge key={index} variant="secondary" className="mr-2 mb-1">
              {tag}
            </Badge>
          ))}
        </div>
      )}
      
      {lastUsedIn && (
        <div className="mt-2 text-sm text-gray-500">
          Last used in <a href="#" className="text-primary hover:underline">{lastUsedIn}</a>
        </div>
      )}
    </div>
  );
}
