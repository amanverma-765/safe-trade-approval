'use client';
import { signIn } from 'next-auth/react';
import { useState } from 'react';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { useRouter } from 'next/navigation';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const router = useRouter();

  async function handleLogin(e: { preventDefault: () => void }, method) {
    e.preventDefault();

    let result;

    if (method === 'github') {
      result = await signIn('github', {
        callbackUrl: '/'
      });
    }

    if (method === 'credentials') {
      result = await signIn('credentials', {
        redirect: false,
        email,
        password
      });
    }

    if (result?.error) {
      setError('Invalid email or password');
    } else {
      setError('');
      router.push('/');
    }
  }

  return (
    <div className="min-h-screen flex justify-center items-start md:items-center p-8">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-2xl">Login</CardTitle>
          <CardDescription>
            This demo uses GitHub and email-password for authentication.
          </CardDescription>
        </CardHeader>
        <CardFooter>
          {/* Email-password login form */}
          <form
            className="w-full space-y-4"
            onSubmit={(e) => {
              e.preventDefault();
            }}
          >
            <div>
              <label className="block text-sm font-medium">Email</label>
              <input
                type="email"
                className="w-full border rounded px-3 py-2 mt-1"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div>
              <label className="block text-sm font-medium">Password</label>
              <input
                type="password"
                className="w-full border rounded px-3 py-2 mt-1"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            {error && <p className="text-red-500 text-sm">{error}</p>}
            <Button
              type="submit"
              className="w-full"
              onClick={(e) => handleLogin(e, 'credentials')}
            >
              Sign in
            </Button>

            {/* Divider */}
            <div className="mt-4 text-center text-sm text-gray-500">or</div>

            {/* GitHub authentication button */}
            <Button
              className="w-full mt-4"
              onClick={(e) => handleLogin(e, 'github')}
            >
              Sign in with GitHub
            </Button>
          </form>
        </CardFooter>
      </Card>
    </div>
  );
}
