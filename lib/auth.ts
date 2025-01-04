import NextAuth from 'next-auth';
import GitHub from 'next-auth/providers/github';
import CredentialsProvider from 'next-auth/providers/credentials';

const {
  NEXT_PUBLIC_USERNAME: USERNAME,
  NEXT_PUBLIC_PASSWORD: PASSWORD,
  NEXT_PUBLIC_API_URL: BACKEND_URL
} = process.env;

console.log(USERNAME, PASSWORD);

export const { signIn, signOut, handlers, auth } = NextAuth({
  session: {
    strategy: 'jwt',
    maxAge: 30 * 24 * 60 * 60
  },
  providers: [
    GitHub,
    CredentialsProvider({
      name: 'Login using email and password',
      type: 'credentials',
      credentials: {
        email: { label: 'Email', type: 'text' },
        password: { label: 'Password', type: 'password' }
      },
      async authorize(credentials) {
        try {
          // const resp = await fetch(`${BACKEND_URL}/auth/login`, {
          //   method: 'POST',
          //   headers: { 'Content-Type': 'application/json' },
          //   body: JSON.stringify({
          //     email: credentials?.email,
          //     password: credentials?.password
          //   })
          // });

          // if (!resp.ok) throw new Error('Invalid email or password');
          // const user = await resp.json();
          // return user
          //   ? { id: user.id, name: user.name, email: user.email }
          //   : null;
          console.log(credentials);
          if (
            credentials.email === USERNAME &&
            credentials.password === PASSWORD
          ) {
            return { email: credentials.email, password: credentials.password };
          }
          return null;
        } catch (error) {
          console.error('Login failed:', error);
          return null;
        }
      }
    })
  ]
});

