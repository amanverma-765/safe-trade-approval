"use client"; // Client component

import { Button } from '@/components/ui/button';
import Image from 'next/image';
import { Bell, LogOut } from 'lucide-react'; // Import notification and logout icons
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger
} from '@/components/ui/dropdown-menu';
import Link from 'next/link';
import { useState } from 'react'; // For notification state
import { signOut } from 'next-auth/react';

// Mock notification data (you can replace it with real data later)
const notifications = [
  { id: 1, message: 'Status changed to: Formalities Chk Pass' },
  { id: 2, message: 'Status changed to: Marked for examination' }
];

export function User({ user }: { user: any }) {
  // Expect the user prop
  const [hasUnread, setHasUnread] = useState(true);

  return (
    <div className="flex items-center gap-4">
      {/* Notification Icon */}
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="icon">
            <Bell className="h-5 w-5" />
            {/* Notification dot if there are unread notifications */}
            {hasUnread && (
              <span className="absolute top-0 right-0 h-2 w-2 bg-red-500 rounded-full"></span>
            )}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuLabel>Notifications</DropdownMenuLabel>
          <DropdownMenuSeparator />
          {/* Notification items */}
          {notifications.length ? (
            notifications.map((notification) => (
              <DropdownMenuItem key={notification.id}>
                {notification.message}
              </DropdownMenuItem>
            ))
          ) : (
            <DropdownMenuItem>No new notifications</DropdownMenuItem>
          )}
          <DropdownMenuSeparator />
          <DropdownMenuItem onClick={() => setHasUnread(false)}>
            Mark all as read
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>

      {/* User Profile */}
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="outline"
            size="icon"
            className="overflow-hidden rounded-full"
          >
            <Image
              src={user?.image ?? '/placeholder-user.jpg'}
              width={36}
              height={36}
              alt="Avatar"
              className="overflow-hidden rounded-full"
            />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuLabel>My Account</DropdownMenuLabel>
          {/* <DropdownMenuSeparator />
          <DropdownMenuItem>Settings</DropdownMenuItem>
          <DropdownMenuItem>Support</DropdownMenuItem>
          <DropdownMenuSeparator /> */}
          {user ? (
            <DropdownMenuItem>
              <form
                onSubmit={async () => await signOut({ callbackUrl: '/' })}
                method="post"
              >
                <button type="submit" className="flex items-center">
                  <LogOut className="h-4 w-4 mr-2" /> Sign Out
                </button>
              </form>
            </DropdownMenuItem>
          ) : (
            <DropdownMenuItem>
              <Link href="/login">Sign In</Link>
            </DropdownMenuItem>
          )}
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}
