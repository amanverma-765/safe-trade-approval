import NextAuth from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';

const { NEXT_PUBLIC_API_URL: BACKEND_URL } = process.env;

export const { signIn, signOut, handlers, auth } = NextAuth({
  session: {
    strategy: 'jwt',
    maxAge: 30 * 24 * 60 * 60
  },
  providers: [
    CredentialsProvider({
      name: 'Login using email and password',
      type: 'credentials',
      credentials: {
        email: { label: 'Email', type: 'text' },
        password: { label: 'Password', type: 'password' }
      },
      async authorize(credentials) {
        try {
          const resp = await fetch(`${BACKEND_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              email: credentials?.email,
              password: credentials?.password
            })
          });

          if (!resp.ok) throw new Error('Invalid email or password');

          const user = await resp.json();

          return user ?? null;
        } catch (error) {
          console.error('Login failed:', error);
          return null;
        }
      }
    })
  ],
  pages: {
    signIn: '/login' // Redirects to your custom login page
  },
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.token = user.token; // Add token to JWT
      }
      return token;
    },
    async session({ session, token }) {
      session.user = {
        ...session.user,
        token: token.token // Add token to session
      };
      return session;
    }
  }
});
