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
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle
} from '@/components/ui/card';
import { useRouter } from 'next/navigation';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import CircularLoader from '@/components/ui/loader';

// Define the type for the Journal entries
interface JournalEntry {
  journalNo: string;
  dateOfPublication: string;
  lastDate: string;
}

// Define the type for the table that shows after generating Excel
interface MatchingData {
  applicationNoJournalTM: string;
  applicationNoOurTM: string[];
  journalTM: string;
  ourTM: string[];
  class: string;
}

const url = process.env.NEXT_PUBLIC_API_URL;

// Function to handle Excel file generation
const generateExcel = (selectedJournals: JournalEntry[]) => {
  console.log('Generating Excel file for selected journals:', selectedJournals);
  // Excel generation logic here
};

function ProductsTable({
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

  const [selectedRows, setSelectedRows] = useState<Set<string>>(new Set());
  const [matchingData, setMatchingData] = useState<MatchingData[]>([]);
  const [uniqueOurTM, setUniqueOurTM] = useState<Set<string>>(new Set());
  const [selectedUniqueOurTM, setSelectedUniqueOurTM] = useState<Set<string>>(
    new Set()
  );
  const [isLoading, setIsLoading] = useState<boolean>(false);

  function prevPage() {
    router.push(`/?offset=${Math.max(offset - journalsPerPage, 0)}`, {
      scroll: false
    });
  }

  function nextPage() {
    router.push(
      `/?offset=${Math.min(offset + journalsPerPage, totalJournals - 1)}`,
      { scroll: false }
    );
  }

  const handleRowSelect = (journalNo: string) => {
    setSelectedRows((prevSelected) => {
      const updatedSelected = new Set(prevSelected);
      if (updatedSelected.has(journalNo)) {
        updatedSelected.delete(journalNo);
      } else {
        updatedSelected.add(journalNo);
      }
      return updatedSelected;
    });
  };

  const fetchOurTm = async (appId: string) => {
    try {
      const response = await fetch(`${url}/scrape/our/application/${appId}`);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      return data.tmAppliedFor;
    } catch (error) {
      console.error('There was a problem with the fetch operation:', error);
      return null;
    }
  };

  const fetchJournalTm = async (appId: string) => {
    try {
      const response = await fetch(
        `${url}/scrape/journal/application/${appId}`
      );
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const data = await response.json();
      return data.tmAppliedFor;
    } catch (error) {
      console.error('There was a problem with the fetch operation:', error);
      return null;
    }
  };

  const handleSearchReport = () => {
    const selectedJournals = journals.filter((journal) =>
      selectedRows.has(journal.journalNo)
    );
    if (selectedJournals.length === 0) {
      alert('At least one Journal should be selected');
      return;
    }

    const query = selectedJournals.map((item) => item.journalNo).join('&');
    setIsLoading(true);

    fetch(`${url}/match_trademarks/${query}`)
      .then((response) => {
        if (!response.ok) {
          return response.json().then((errorData) => {
            throw new Error(`Error: ${errorData.status} ${errorData.message}`);
          });
        }
        return response.json();
      })
      .then((data) => {
        return Promise.all(
          data.map(
            async (item: {
              ourTrademarkAppNumbers: any[];
              journalAppNumber: string;
              tmClass: any;
            }) => {
              const ourTmPromises = item.ourTrademarkAppNumbers.map((tm) =>
                fetchOurTm(tm)
              );
              const journalTmPromise = fetchJournalTm(item.journalAppNumber);

              const [ourTmAppIdList, journalTm] = await Promise.all([
                Promise.all(ourTmPromises),
                journalTmPromise
              ]);
              return {
                applicationNoJournalTM: item.journalAppNumber,
                applicationNoOurTM: item.ourTrademarkAppNumbers,
                journalTM: journalTm,
                ourTM: ourTmAppIdList.filter(Boolean),
                class: item.tmClass
              };
            }
          )
        );
      })
      .then((mappedData) => {
        setMatchingData(mappedData);
        const allOurTM = new Set<string>();
        mappedData.forEach((data) => {
          data.ourTM.forEach((ourTMValue: string) => allOurTM.add(ourTMValue));
        });
        setUniqueOurTM(allOurTM);
      })
      .catch((error) => {
        console.error('Error:', error);
        alert(error.message);
      })
      .finally(() => {
        setTimeout(() => {
          setIsLoading(false);
        }, 200);
      });
  };

  const handleGenerateIndividualOpposition = () => {
    // Future implementation for generating individual opposition
    console.log('Generating individual opposition...');
  };

  const handleUniqueOurTMSelect = (ourTMValue: string) => {
    setSelectedUniqueOurTM((prevSelected) => {
      const updatedSelected = new Set(prevSelected);
      if (updatedSelected.has(ourTMValue)) {
        updatedSelected.delete(ourTMValue);
      } else {
        updatedSelected.add(ourTMValue);
      }
      return updatedSelected;
    });
  };

  return (
    <>
      {isLoading ? (
        <CircularLoader />
      ) : (
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
                  <TableHead>Select</TableHead>
                  <TableHead>Journal No.</TableHead>
                  <TableHead>Date of Publication</TableHead>
                  <TableHead>Last Date</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {journals.map((journal) => (
                  <TableRow key={journal.journalNo}>
                    <TableHead>
                      <input
                        type="checkbox"
                        checked={selectedRows.has(journal.journalNo)}
                        onChange={() => handleRowSelect(journal.journalNo)}
                      />
                    </TableHead>
                    <TableHead>{journal.journalNo}</TableHead>
                    <TableHead>{journal.dateOfPublication}</TableHead>
                    <TableHead>{journal.lastDate}</TableHead>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
          <CardFooter className="flex justify-between items-center">
            <Button
              onClick={handleSearchReport}
              className="bg-black text-white hover:bg-gray-800 transition-all duration-300 mr-4"
            >
              Generate Search Report
            </Button>
            <form className="flex items-center w-full justify-between">
              <div className="text-xs text-muted-foreground">
                Showing{' '}
                <strong>
                  {Math.min(offset, totalJournals) + 1}-
                  {Math.min(offset + journalsPerPage, totalJournals)}
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
      )}

      {matchingData.length > 0 && (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle>Generated Search Reports</CardTitle>
            <CardDescription>Data from the selected journals</CardDescription>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Select</TableHead>
                  <TableHead>Application no of Journal TM</TableHead>
                  <TableHead>Application no. of Our TM</TableHead>
                  <TableHead>Journal TM</TableHead>
                  <TableHead>Our TM</TableHead>
                  <TableHead>Class</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {matchingData.map((data, index) => (
                  <TableRow key={index}>
                    <TableHead>
                      <input
                        type="checkbox"
                        checked={selectedRows.has(index.toString())}
                        onChange={() => handleRowSelect(index.toString())}
                      />
                    </TableHead>
                    <TableHead>{data.applicationNoJournalTM}</TableHead>
                    <TableHead>
                      {data.applicationNoOurTM.map((ourTmAppId, ourTmIndex) => (
                        <div key={ourTmIndex} className="flex items-center">
                          {ourTmAppId}
                        </div>
                      ))}
                    </TableHead>
                    <TableHead>{data.journalTM}</TableHead>
                    <TableHead>
                      {data.ourTM.map((ourTMValue, ourTMIndex) => (
                        <div key={ourTMIndex} className="flex items-center">
                          <input
                            type="checkbox"
                            checked={selectedUniqueOurTM.has(ourTMValue)}
                            onChange={() => handleUniqueOurTMSelect(ourTMValue)}
                            className="mr-2"
                          />
                          {ourTMValue}
                        </div>
                      ))}
                    </TableHead>
                    <TableHead>{data.class}</TableHead>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
          <CardFooter className="flex justify-between items-center">
            <Button
              onClick={handleGenerateIndividualOpposition}
              className="bg-blue-600 text-white hover:bg-blue-700 transition-all duration-300 mr-4"
            >
              Generate Individual Opposition
            </Button>
            <div className="text-xs text-muted-foreground">
              Showing <strong>{uniqueOurTM.size} unique Our TM values</strong>
            </div>
          </CardFooter>
        </Card>
      )}
    </>
  );
}

const HomePage = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const [journals, setJournals] = useState<JournalEntry[]>([]);

  useEffect(() => {
    setLoading(true);

    fetch(`${url}/get/latest_journals`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then((data) => {
        const mappedJournals: JournalEntry[] = data.map(
          (item: {
            journalNumber: any;
            dateOfPublication: any;
            dateOfAvailability: any;
          }) => ({
            journalNo: item.journalNumber,
            dateOfPublication: item.dateOfPublication,
            lastDate: item.dateOfAvailability
          })
        );
        setJournals(mappedJournals);
      })
      .catch((error) => {
        console.error('There was a problem with the fetch operation:', error);
      })
      .finally(() => {
        setTimeout(() => {
          setLoading(false);
        }, 200);
      });
  }, []);

  const offset = 0;
  const totalJournals = journals.length;

  return (
    <div>
      {loading ? (
        <CircularLoader />
      ) : (
        <ProductsTable
          journals={journals.slice(offset, offset + 5)}
          offset={offset}
          totalJournals={totalJournals}
        />
      )}
    </div>
  );
};

export default HomePage;
