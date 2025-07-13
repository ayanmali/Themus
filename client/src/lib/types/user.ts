export type User = {
  name: string;
  role: 'employer' | 'candidate';
  email: string;
  password: string;
  id: string;
  createdAt?: Date;
  organizationName?: string;
}