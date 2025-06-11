import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const minutesToHours = (minutes: number) => {
  return Math.floor(minutes / 60);
}
