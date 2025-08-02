export function formatFileSize(bytes: number): string {
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let size = bytes;
  let unitIndex = 0;
  
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024;
    unitIndex++;
  }
  
  return `${size.toFixed(1)} ${units[unitIndex]}`;
}

export function formatDuration(seconds: number): string {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;
  
  if (hours > 0) {
    return `${hours}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  }
  
  return `${minutes}:${secs.toString().padStart(2, '0')}`;
}

export function generateFileName(title: string, format: string): string {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
  const cleanTitle = title.replace(/[^a-z0-9]/gi, '_').toLowerCase();
  return `${cleanTitle}_${timestamp}.${format}`;
}

export function validateVideoFile(file: File): { valid: boolean; error?: string } {
  const maxSize = 500 * 1024 * 1024; // 500MB
  const supportedTypes = ['video/mp4', 'video/webm', 'video/ogg'];
  
  if (file.size > maxSize) {
    return {
      valid: false,
      error: 'File size exceeds 500MB limit',
    };
  }
  
  if (!supportedTypes.includes(file.type)) {
    return {
      valid: false,
      error: 'Unsupported file type. Please use MP4, WebM, or OGG format.',
    };
  }
  
  return { valid: true };
}

export function getStorageQuota(): Promise<{ used: number; total: number } | null> {
  if ('storage' in navigator && 'estimate' in navigator.storage) {
    return navigator.storage.estimate().then(estimate => ({
      used: estimate.usage || 0,
      total: estimate.quota || 0,
    }));
  }
  
  return Promise.resolve(null);
}
