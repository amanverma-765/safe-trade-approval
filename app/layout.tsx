import { auth } from '@/lib/auth';
import './globals.css';
import { redirect } from 'next/navigation';
import Provider from './Provider';

export const metadata = {
  title: 'Trademark - SafeTradeApprovals',
  description: 'Trademark Automation Software - Powered by webxela.com',
  icons: {
    icon: '/logo.svg'
  }
};

export default function RootLayout({
  children
}: {
  children: React.ReactNode;
}) {
  const session = auth();
  if (!session) {
    redirect('/login');
  }
  return (
    <html lang="en">
      <body className="flex min-h-screen w-full flex-col">
        <Provider>{children}</Provider>
      </body>
    </html>
  );
}
