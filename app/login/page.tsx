'use client';
import { FormEvent, useState } from 'react';
import { Button } from '@/components/ui/button';
import {
  Card,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { useRouter } from 'next/navigation';
import { useSession } from 'contexts/SessionContext';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const { signIn } = useSession();

  async function handleLogin(e: FormEvent) {
    const result = await signIn(e, email, password);
    if (result.error) {
      setError(result.error); // Set the error message
    } else {
      setError(''); // Clear the error message on successful login
    }
  }


  return (
    <div className="min-h-screen flex justify-center items-start md:items-center p-8">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-2xl">Login</CardTitle>
        </CardHeader>
        <CardFooter>
          <form className="w-full space-y-4" onSubmit={handleLogin}>
            <div>
              <label className="block text-sm font-medium">Email</label>
              <input
                type="text"
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
            {error && <p className="text-red-500 text-sm">{error}</p>} {/* Show error */}
            <Button type="submit" className="w-full">
              Sign in
            </Button>
          </form>
        </CardFooter>
      </Card>
    </div>
  );
}
