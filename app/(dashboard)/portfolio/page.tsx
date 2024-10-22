'use client';

import React, { useEffect, useState } from 'react';
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
import CircularLoader from '@/components/ui/loader';
import { Delete } from 'lucide-react';
import DeleteButton from '@/components/ui/deleteButton';

// Define the type for the Trademark entries
interface TrademarkEntry {
  applicationNo: string;
  trademark: string;
  classNo: string;
  status: string;
}

interface ErrorResponse {
  message: string;
  status: number;
}

const url = process.env.NEXT_PUBLIC_API_URL;

// The TrademarkTable component definition
const TrademarkTable = ({
  trademarks,
  handleDelete,
  deleting
}: {
  trademarks: TrademarkEntry[];
  handleDelete: (applicationId: string) => void;
  deleting: boolean;
}) => {
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
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {trademarks.toReversed().map((trademark) => (
              <TableRow key={trademark.applicationNo}>
                <TableHead>{trademark.applicationNo}</TableHead>
                <TableHead>{trademark.trademark}</TableHead>
                <TableHead>{trademark.classNo}</TableHead>
                <TableHead>{trademark.status}</TableHead>
                <TableHead>
                  {deleting ? (
                    <CircularLoader />
                  ) : (
                    <DeleteButton onClick={() => handleDelete(trademark.applicationNo)} />
                  )}
                </TableHead>
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
  const [loading, setLoading] = useState<boolean>(false); // For adding a trademark
  const [deleting, setDeleting] = useState<boolean>(false); // For deleting a trademark

  // Function to fetch the trademark data
  const fetchTrademarks = async () => {
    setLoading(true);
    const finalUrl = `${url}/get/our_trademarks`;

    try {
      const response = await fetch(finalUrl);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      const mappedTrademarks: TrademarkEntry[] = data.map(
        (item: { applicationNumber: any; tmAppliedFor: any; tmClass: any; status: any }) => ({
          applicationNo: item.applicationNumber,
          trademark: item.tmAppliedFor,
          classNo: item.tmClass,
          status: item.status
        })
      );
      setTrademarks(mappedTrademarks);
    } catch (error) {
      console.error('There was a problem with the fetch operation:', error);
    } finally {
      setLoading(false);
    }
  };

  // Function to handle deleting a trademark
  const handleDelete = async (applicationId: string) => {
    setDeleting(true);
    const finalUrl = `${url}/delete/our/application/${applicationId}`;

    try {
      const response = await fetch(finalUrl);
      if (!response.ok) {
        let errorResponse: ErrorResponse = await response.json();
        const errorMessage = `Error: ${response.status} ${errorResponse.message}`;
        alert(errorMessage);
        throw new Error(errorMessage);
      }
      console.log('Deletion successful');
      await fetchTrademarks(); // Refresh the table after successful deletion
    } catch (error) {
      console.error('Error during fetch operation:', error);
    } finally {
      setDeleting(false);
    }
  };

  // Function to handle adding new trademarks
  const addTrademark = async () => {
    if (applicationId.trim() === '') {
      alert("This field can't be empty");
      return;
    }

    setLoading(true);
    const finalUrl = `${url}/scrape/our/application/${applicationId}`;

    try {
      const response = await fetch(finalUrl);

      if (!response.ok) {
        let errorResponse: ErrorResponse = await response.json();
        const errorMessage = `Error: ${response.status} ${errorResponse.message}`;
        alert(errorMessage);
        throw new Error(errorMessage);
      }
      const data = await response.json();
      console.log('Response data:', data);
      await fetchTrademarks(); // Refresh the table after adding a new trademark
    } catch (error) {
      console.error('Error during fetch operation:', error);
    } finally {
      setApplicationId("")
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTrademarks();
  }, []);

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

      {/* Show Loader if loading is true */}
      {loading ? (
        <CircularLoader />
      ) : (
        <TrademarkTable trademarks={trademarks} handleDelete={handleDelete} deleting={deleting} />
      )}
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

export default Page;
