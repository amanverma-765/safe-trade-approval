import { SessionProvider } from 'contexts/SessionContext';
import './globals.css';
import { ReactNode } from 'react';

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
  children: ReactNode;
}) {

  return (
    <html lang="en">
      <body className="flex min-h-screen w-full flex-col">
        <SessionProvider>
        {children}
        </SessionProvider>
      </body>
    </html>
  );
}
