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

// Define the type for the Trademark entries
interface TrademarkEntry {
  applicationNo: string;
  trademark: string;
  classNo: string;
  status: string;
}

interface ErrorResponse {
  message: string,
  status: number
}

// The TrademarkTable component definition
const TrademarkTable = ({ trademarks }: { trademarks: TrademarkEntry[] }) => {
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
            {trademarks.toReversed().map((trademark) => (
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
  const [loading, setLoading] = useState<boolean>(false);
  const url = process.env.NEXT_PUBLIC_API_URL;

  // Function to handle adding new trademarks
  const addTrademark = async () => {
    if (applicationId.trim() === '') {
      alert("This field can't be empty");
      return;
    }

    setLoading(true);
    const finalUrl = `${url}/scrape/our/application/${applicationId}`

    try {
      const response = await fetch(finalUrl);

      if (!response.ok) {
        let errorResponse: ErrorResponse = await response.json()
        const errorMessage = `Error: ${response.status} ${errorResponse.message}`;
        alert(errorMessage)
        throw new Error(errorMessage);
      }
      const data = await response.json();
      console.log("Response data:", data);
    } catch (error) {
      console.error('Error during fetch operation:', error);
    } finally {
      setTimeout(() => {
        setLoading(false)
      }, 200);
    }
  };

  useEffect(() => {

    if (loading) {
      setApplicationId("Loading...")
    } else {
      setApplicationId("")
    }

    const finalUrl = url + '/get/our_trademarks'

    fetch(finalUrl)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then((data) => {
        const mappedTrademarks: TrademarkEntry[] = data.map((item: { applicationNumber: any; tmAppliedFor: any; tmClass: any; status: any; }) => ({
          applicationNo: item.applicationNumber,
          trademark: item.tmAppliedFor,
          classNo: item.tmClass,
          status: item.status
        }));

        setTrademarks(mappedTrademarks);

      })
      .catch((error) => console.error('There was a problem with the fetch operation:', error))
      .finally(() => {

      })
  }, [loading])

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
        <TrademarkTable trademarks={trademarks} />
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

export default Page; // Ensure that this is the default export
