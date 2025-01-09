import 'next-auth';

declare module 'next-auth' {
  interface User {
    token: string;
  }

  interface Session extends DefaultSession {
    user: User;
    expires_in: string;
    error: string;
  }
}
