'use client';

import React, { useState } from 'react';
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
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { useRouter } from 'next/navigation';

// Define the type for the Trademark entries
interface TrademarkEntry {
  applicationNo: string;
  trademark: string;
  classNo: string;
  status: string;
}

// The TrademarkTable component definition
export function TrademarkTable({ trademarks }: { trademarks: TrademarkEntry[] }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Trademark Table</CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Application No.</TableHead>
              <TableHead>Trademark</TableHead>
              <TableHead>Class</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {trademarks.map((trademark) => (
              <TableRow key={trademark.applicationNo}>
                <TableHead>{trademark.applicationNo}</TableHead>
                <TableHead>{trademark.trademark}</TableHead>
                <TableHead>{trademark.classNo}</TableHead>
                <TableHead>{trademark.status}</TableHead>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}

// HomePage component definition
const HomePage = () => {
  const [trademarks, setTrademarks] = useState<TrademarkEntry[]>([]); // Store trademark data
  const [applicationId, setApplicationId] = useState<string>(''); // Store input value for Application ID

  // Function to handle adding new trademarks
  const addTrademark = () => {
    if (applicationId.trim() === '') return;

    const newTrademark: TrademarkEntry = {
      applicationNo: applicationId,
      trademark: 'Sample Trademark', // Placeholder data
      classNo: '35', // Placeholder data
      status: 'Formalities Chk Pass' // Placeholder status
    };

    // Add new trademark to the list
    setTrademarks([...trademarks, newTrademark]);
    setApplicationId(''); // Clear the input field
  };

  // Function to handle Excel generation (placeholder for now)
  const generateExcel = () => {
    console.log("Generating Excel...");
    // Implement your Excel generation logic here
  };

  return (
    <div>
      {/* Add New Trademark Section */}
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Add New Trademark</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center space-x-2">
            <Input
              value={applicationId}
              onChange={(e) => setApplicationId(e.target.value)}
              placeholder="Enter Application ID"
            />
            <Button onClick={addTrademark} className="bg-black text-white hover:bg-gray-800 transition-all duration-300">
              Add New Trademark
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Trademark Table */}
      <TrademarkTable trademarks={trademarks} />

      {/* Generate Excel Button - Shown only when there are trademarks */}
      {trademarks.length > 0 && (
        <div className="flex justify-start mt-4 mb-4">
          <Button onClick={generateExcel} className="bg-blue-600 text-white hover:bg-blue-500 transition-all duration-300">
            Generate Excel
          </Button>
        </div>
      )}
    </div>
  );
};

export default HomePage;
