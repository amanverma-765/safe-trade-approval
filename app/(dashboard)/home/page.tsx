'use client';

import React from 'react';
import {
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  Table
} from '@/components/ui/table';
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { useRouter } from 'next/navigation'; // Correctly importing useRouter
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';

// Define the type for the Journal entries
interface JournalEntry {
  journalNo: string;
  dateOfPublication: string;
  lastDate: string;
}

// The ProductsTable component definition
export function ProductsTable({
  journals,
  offset,
  totalJournals
}: {
  journals: JournalEntry[];
  offset: number;
  totalJournals: number;
}) {
  const router = useRouter();
  const journalsPerPage = 5;

  function prevPage() {
    router.push(`/?offset=${Math.max(offset - journalsPerPage, 0)}`, { scroll: false });
  }

  function nextPage() {
    router.push(`/?offset=${Math.min(offset + journalsPerPage, totalJournals - 1)}`, { scroll: false });
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Latest Search Report</CardTitle>
        <CardDescription>
          Download Excel | Search Report from Previous Journal
        </CardDescription>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Journal No.</TableHead>
              <TableHead>Date of Publication</TableHead>
              <TableHead>Last Date</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {journals.map((journal) => (
              <TableRow key={journal.journalNo}>
                <TableHead>{journal.journalNo}</TableHead>
                <TableHead>{journal.dateOfPublication}</TableHead>
                <TableHead>{journal.lastDate}</TableHead>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
      <CardFooter>
        <form className="flex items-center w-full justify-between">
          <div className="text-xs text-muted-foreground">
            Showing{' '}
            <strong>
              {Math.min(offset, totalJournals) + 1}-{Math.min(offset + journalsPerPage, totalJournals)}
            </strong>{' '}
            of <strong>{totalJournals}</strong> journals
          </div>
          <div className="flex">
            <Button
              onClick={prevPage}
              variant="ghost"
              size="sm"
              disabled={offset === 0}
            >
              <ChevronLeft className="mr-2 h-4 w-4" />
              Prev
            </Button>
            <Button
              onClick={nextPage}
              variant="ghost"
              size="sm"
              disabled={offset + journalsPerPage >= totalJournals}
            >
              Next
              <ChevronRight className="ml-2 h-4 w-4" />
            </Button>
          </div>
        </form>
      </CardFooter>
    </Card>
  );
}

// HomePage component definition
const HomePage = () => {
  // Sample journal data
  const journals: JournalEntry[] = [
    { journalNo: 'J-1000', dateOfPublication: '2023-01-01', lastDate: '2023-01-15' },
    { journalNo: 'J-1001', dateOfPublication: '2023-02-01', lastDate: '2023-02-15' },
    { journalNo: 'J-1002', dateOfPublication: '2023-03-01', lastDate: '2023-03-15' },
    { journalNo: 'J-1003', dateOfPublication: '2023-04-01', lastDate: '2023-04-15' },
    { journalNo: 'J-1004', dateOfPublication: '2023-05-01', lastDate: '2023-05-15' },
    { journalNo: 'J-1005', dateOfPublication: '2023-06-01', lastDate: '2023-06-15' },
    { journalNo: 'J-1006', dateOfPublication: '2023-07-01', lastDate: '2023-07-15' },
    { journalNo: 'J-1007', dateOfPublication: '2023-08-01', lastDate: '2023-08-15' },
    { journalNo: 'J-1008', dateOfPublication: '2023-09-01', lastDate: '2023-09-15' },
    { journalNo: 'J-1009', dateOfPublication: '2023-10-01', lastDate: '2023-10-15' },
  ];

  const offset = 0; // Set this to the appropriate offset for your application
  const totalJournals = journals.length;

  return (
    <div>
      <ProductsTable journals={journals.slice(offset, offset + 5)} offset={offset} totalJournals={totalJournals} />
    </div>
  );
};

export default HomePage;
