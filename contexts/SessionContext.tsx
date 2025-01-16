'use client';

import Cookies from 'js-cookie';
import axios from 'axios';
import { usePathname, useRouter } from 'next/navigation';
import {
  createContext,
  FormEvent,
  ReactNode,
  useContext,
  useEffect,
  useState
} from 'react';

// Define the context type
interface SessionContextType {
  token: string | undefined;
  setToken: (token: string | undefined) => void;
  signOut: (e: FormEvent) => void;
  signIn: (e: FormEvent, email: string, password: string) => void;
}

// Create the context with the appropriate type
const SessionContext = createContext<SessionContextType | null>(null);

function SessionProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | undefined>(
    Cookies.get('jwtToken')
  );
  const pathname = usePathname();
  const router = useRouter();

  // useEffect(() => {
  //   const isLoggedIn = !!Cookies.get('jwtToken');
  //   if (!isLoggedIn && pathname !== '/login') {
  //     router.push('/login');
  //   } else if (token) {
  //     router.push('/');
  //   }
  // }, [token, pathname, router]);

  useEffect(() => {
    if (!token && pathname !== '/login') {
      router.push('/login');
    } else if (token) {
      try {
        const payload = JSON.parse(atob(token.split('.')[1])); // Decode the token
        if (payload.exp * 1000 < Date.now()) {
          Cookies.remove('jwtToken');
          setToken(undefined);
          router.push('/login');
        } else {
          setToken(token); // Token is valid
          router.back();
        }
      } catch (error) {
        console.error('Invalid token:', error);
        Cookies.remove('jwtToken');
        setToken(undefined);
        router.push('/login');
      }
    }
  }, [token]);

  async function signIn(e: FormEvent, email: string, password: string) {
    e.preventDefault();
    try {
      const response = await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL}/auth/login`,
        {
          email,
          password
        }
      );

      if (!response) {
        throw response; // Let the calling function handle it
      }

      // Store the JWT token in a cookie
      if (response.data) {
        Cookies.set('jwtToken', response.data.token, {
          expires: 1, // Expires in 1 days
          secure: process.env.NODE_ENV === 'production', // Use secure cookies in production
          sameSite: 'strict' // Prevent CSRF attacks
        });
        setToken(response.data.token);
      }

      return response.data;
    } catch (error) {
      console.error('Login error:', error);
      return { error };
    }
  }

  function signOut(e: FormEvent) {
    e.preventDefault();
    if (token) {
      Cookies.remove('jwtToken');
      setToken(undefined);
      router.push('/login');
    }
  }

  return (
    <SessionContext.Provider value={{ token, setToken, signOut, signIn }}>
      {children}
    </SessionContext.Provider>
  );
}

function useSession() {
  const context = useContext(SessionContext);
  if (!context) {
    throw new Error('useSession must be used within a SessionProvider');
  }
  return context;
}

export { SessionProvider, useSession };
