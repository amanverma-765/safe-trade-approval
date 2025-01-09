'use client';

import React, { useEffect, useState } from 'react';
import {
  TableHead,
  TableRow,
  TableHeader,
  TableBody,
  Table
} from '@/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import CircularLoader from '@/components/ui/loader';
import DeleteButton from '@/components/ui/deleteButton';
import { useSession } from 'next-auth/react';

interface TrademarkEntry {
  applicationNo: string;
  trademark: string;
  classNo: string;
  status: string;
}

const url = process.env.NEXT_PUBLIC_API_URL;

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
            {trademarks.reverse().map((trademark) => (
              <TableRow key={trademark.applicationNo}>
                <TableHead>{trademark.applicationNo}</TableHead>
                <TableHead>{trademark.trademark}</TableHead>
                <TableHead>{trademark.classNo}</TableHead>
                <TableHead>{trademark.status}</TableHead>
                <TableHead>
                  {deleting ? (
                    <CircularLoader />
                  ) : (
                    <DeleteButton
                      onClick={() => handleDelete(trademark.applicationNo)}
                    />
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

const CompanySelectionComponent = () => {
  const [applicationId, setApplicationId] = useState<string>('');
  const [trademarks, setTrademarks] = useState<TrademarkEntry[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [deleting, setDeleting] = useState<boolean>(false);
  const { data: sessionData, status } = useSession();

  const fetchTrademarks = () => {
    setLoading(true);
    const finalUrl = `${url}/get/our_trademarks`;

    if (status === 'authenticated') {
      fetch(finalUrl, {
        headers: {
          Authorization: `Bearer ${sessionData.user.token}`
        }
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error('Network response was not ok');
          }
          return response.json();
        })
        .then((data) => {
          const mappedTrademarks: TrademarkEntry[] = data.map(
            (item: {
              applicationNumber: any;
              tmAppliedFor: any;
              tmClass: any;
              status: any;
            }) => ({
              applicationNo: item.applicationNumber,
              trademark: item.tmAppliedFor,
              classNo: item.tmClass,
              status: item.status
            })
          );
          setTrademarks(mappedTrademarks);
        })
        .catch((error) => {
          console.error('There was a problem with the fetch operation:', error);
        })
        .finally(() => {
          setLoading(false);
        });
    }
  };

  const handleDelete = (applicationId: string) => {
    setDeleting(true);
    const finalUrl = `${url}/delete/our/application/${applicationId}`;

    fetch(finalUrl, {
      headers: {
        Authorization: `Bearer ${sessionData.user.token}`
      }
    })
      .then((response) => {
        if (!response.ok) {
          return response.json().then((errorResponse) => {
            const errorMessage = `Error: ${response.status} ${errorResponse.message}`;
            alert(errorMessage);
            throw new Error(errorMessage);
          });
        }
        console.log('Deletion successful');
        fetchTrademarks();
      })
      .catch((error) => {
        console.error('Error during fetch operation:', error);
      })
      .finally(() => {
        setDeleting(false);
      });
  };

  const addTrademark = () => {
    if (applicationId.trim() === '') {
      alert("This field can't be empty");
      return;
    }

    const applicationIds = applicationId.split(',').map((id) => id.trim());
    if (applicationIds.length === 0 || applicationIds.some((id) => id === '')) {
      alert('Please provide valid application IDs separated by commas.');
      return;
    }

    setLoading(true);
    const finalUrl = `${url}/scrape/our/application`;

    fetch(finalUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${sessionData.user.token}`
      },
      body: JSON.stringify(applicationIds)
    })
      .then(async (response) => {
        if (!response.ok) {
          return response.json().then((errorResponse) => {
            const firstError =
              errorResponse[0]?.message || 'An unknown error occurred.';
            alert(firstError);
          });
        }
        return response.json();
      })
      .then((data) => {
        console.log('Response data:', data);
        fetchTrademarks();
      })
      .catch((error) => {
        console.error('Error during fetch operation:', error);
      })
      .finally(() => {
        setApplicationId('');
        setLoading(false);
      });
  };

  useEffect(() => {
    fetchTrademarks();
  }, []);

  return (
    <div>
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
            <Button
              onClick={addTrademark}
              className="bg-black text-white hover:bg-gray-800 transition-all duration-300"
            >
              Add New Trademark
            </Button>
          </div>
        </CardContent>
      </Card>

      {loading ? (
        <CircularLoader />
      ) : (
        <TrademarkTable
          trademarks={trademarks}
          handleDelete={handleDelete}
          deleting={deleting}
        />
      )}
    </div>
  );
};

const Page = () => {
  return (
    <div>
      <CompanySelectionComponent />
    </div>
  );
};

export default Page;
