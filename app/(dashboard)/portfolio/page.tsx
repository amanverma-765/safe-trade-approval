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
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';

// Define the type for the Trademark entries
interface TrademarkEntry {
  applicationNo: string;
  trademark: string;
  classNo: string;
  status: string;
}

// The TrademarkTable component definition
const TrademarkTable = ({ trademarks }: { trademarks: TrademarkEntry[] }) => {
  return (
    <Card className="overflow-x-auto"> {/* Enable horizontal scrolling on small screens */}
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
};

// CompanySelectionComponent with Trademark Table
const CompanySelectionComponent = () => {
  const [applicationId, setApplicationId] = useState<string>(''); // Store input value for Application ID
  const [trademarks, setTrademarks] = useState<TrademarkEntry[]>([]); // Store trademark data

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

  return (
    <div className="p-4"> {/* Add padding for smaller devices */}
      {/* Add New Trademark Section */}
      <Card className="mb-4">
        <CardHeader>
          <CardTitle>Add New Trademark</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col sm:flex-row items-center space-y-2 sm:space-y-0 sm:space-x-2">
            <Input
              value={applicationId}
              onChange={(e) => setApplicationId(e.target.value)}
              placeholder="Enter Application ID"
              className="flex-1" // Allow the input to grow
            />
            <Button
              onClick={addTrademark}
              className="bg-black text-white hover:bg-gray-800 transition-all duration-300"
            >
              Add New Trademark
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Trademark Table */}
      <TrademarkTable trademarks={trademarks} />
    </div>
  );
};

// Default export of the page component
const Page = () => {
  return (
    <div>
      <CompanySelectionComponent />
    </div>
  );
};

export default Page; // Ensure that this is the default export
