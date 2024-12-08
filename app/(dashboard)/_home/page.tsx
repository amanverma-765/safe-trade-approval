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

interface OurTm {
  applicationNumber: string;
  status: string;
  tmAppliedFor: string;
  tmClass: string;
}

// Define the type for the table that shows after generating Excel
interface OurTmData {
  tmApplicationNumber: string;
  tmAppliedFor: string;
}

interface MatchingData {
  applicationNoJournalTM: string;
  ourMatchedTrademarks: OurTmData[];
  journalTM: string;
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
  currentPage,
  totalJournals,
  onPrevPage,
  onNextPage
}: {
  journals: JournalEntry[];
  currentPage: number;
  totalJournals: number;
  onPrevPage: () => void;
  onNextPage: () => void;
}) {
  const router = useRouter();
  const journalsPerPage = 16;

  const [selectedRows, setSelectedRows] = useState<Set<string>>(new Set());
  // const [matchingData, setMatchingData] = useState<MatchingData[]>([]);
  const [matchingData, setMatchingData] = useState<Map<string, MatchingData[]>>(
    new Map()
  );
  const [uniqueOurTM, setUniqueOurTM] = useState<Set<string>>(new Set());
  const [selectedOurTM, setSelectedOurTM] = useState<
    Map<string, Map<string, Set<string>>>
  >(new Map());
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [currentReportPage, setCurrentReportPage] = useState(0);
  const reportsPerPage = 25; // You can adjust this number

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

  const handleSearchReport = async () => {
    const selectedJournals = journals.filter((journal) =>
      selectedRows.has(journal.journalNo)
    );
    if (selectedJournals.length === 0) {
      alert('At least one Journal should be selected');
      return;
    }

    setIsLoading(true);
    await Promise.all(
      selectedJournals.map(async (selectedJournal) => {
        try {
          const matchedTrademarks = await fetch(
            `${url}/match_trademarks/${selectedJournal.journalNo}`
          );

          if (!matchedTrademarks.ok) {
            return matchedTrademarks.json().then((errorData) => {
              throw new Error(
                `Error: ${errorData.status} ${errorData.message}`
              );
            });
          }

          const data = await matchedTrademarks.json();

          const mappedData: MatchingData[] = data.map(
            (item: {
              journalNumber: string;
              tmClass: string;
              journalTrademark: {
                tmApplicationNumber: string;
                tmAppliedFor: string;
                tmClass: string;
              };
              ourTrademarks: Array<{
                tmApplicationNumber: string;
                tmAppliedFor: string;
                tmClass: string;
              }>;
            }) => {
              return {
                applicationNoJournalTM: item.journalTrademark.tmApplicationNumber,
                ourMatchedTrademarks: item.ourTrademarks.map((tm) => ({
                  tmApplicationNumber: tm.tmApplicationNumber,
                  tmAppliedFor: tm.tmAppliedFor,
                })),
                journalTM: item.journalTrademark.tmAppliedFor,
                class: item.tmClass,
              };
            }
          );

          setMatchingData((prevMappedMap) => {
            const updatedMappedMap = new Map(prevMappedMap);
            updatedMappedMap.set(selectedJournal.journalNo, mappedData);
            return updatedMappedMap;
          });

          // const allOurTM = new Set<string>();
          // mappedData.forEach((data: { ourTM: { tmAppliedFor: string }[] }) => {
          //   data.ourTM.forEach((ourTMObject: { tmAppliedFor: string }) =>
          //     allOurTM.add(ourTMObject.tmAppliedFor)
          //   );
          // });
          // setUniqueOurTM((prevTM) => prevTM.union(allOurTM));
          setIsLoading(false);
          // return response.json();
        } catch (error) {
          console.error('Fetch operation failed:', error);
          return null;
        } finally {
          setIsLoading(false);
        }
      })
    );
  };

  const handleGenerateIndividualOpposition = async () => {
    // Gather data for selected rows
    try {
      const oppositionData: Array<{
        journalAppId: string;
        journalNumber: string;
        ourAppIdList: string[];
      }> = [];

      selectedOurTM.forEach((journalAppIdMap, journalNumber) => {
        journalAppIdMap.forEach((ourAppIdList, journalAppId) => {
          oppositionData.push({
            journalAppId,
            journalNumber,
            ourAppIdList: Array.from(ourAppIdList)
          });
        });
      });

      const res = await Promise.all(
        oppositionData.map((opposition) =>
          fetch(`${url}/generate_report`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify(opposition)
          })
        )
      );

      console.log(res);

      const data = await Promise.all(res.map((entry) => entry.json()));
      console.log('Individual opposition generated successfully:', data);
      alert(
        'Individual opposition generated successfully for selected entries.'
      );
    } catch (error) {
      console.error(error);
    }
  };

  const handleUniqueOurTMSelect = (
    journalNo: string,
    applicationNoJournalTM: string,
    ourTMValue: string
  ) => {
    console.log('Selecting:', {
      journalNo,
      applicationNoJournalTM,
      ourTMValue
    }); // Debug log

    setSelectedOurTM((prevSelected) => {
      // Create new Map instances to ensure state updates
      const updatedSelected = new Map(prevSelected);
      const journalMap = new Map(updatedSelected.get(journalNo) || new Map());
      const applicationSet = new Set(
        journalMap.get(applicationNoJournalTM) || new Set()
      );

      // Toggle the value
      if (applicationSet.has(ourTMValue)) {
        applicationSet.delete(ourTMValue);
      } else {
        applicationSet.add(ourTMValue);
      }

      // Update the nested maps
      journalMap.set(applicationNoJournalTM, applicationSet);
      updatedSelected.set(journalNo, journalMap);

      return updatedSelected;
    });
  };

  const handleAllOurTMSelect = (rowIndex: number) => {};

  const prevReportPage = () => {
    setCurrentReportPage((prev) => Math.max(0, prev - 1));
  };

  const nextReportPage = () => {
    const totalReports = Array.from(matchingData.values()).flat().length;
    const maxPage = Math.ceil(totalReports / reportsPerPage) - 1;
    setCurrentReportPage((prev) => Math.min(maxPage, prev + 1));
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
                  {currentPage * journalsPerPage + 1}-
                  {Math.min((currentPage + 1) * journalsPerPage, totalJournals)}
                </strong>{' '}
                of <strong>{totalJournals}</strong> journals
              </div>
              <div className="flex">
                <Button
                  onClick={(e) => {
                    e.preventDefault();
                    onPrevPage();
                  }}
                  variant="ghost"
                  size="sm"
                  disabled={currentPage === 0}
                >
                  <ChevronLeft className="mr-2 h-4 w-4" />
                  Prev
                </Button>
                <Button
                  onClick={(e) => {
                    e.preventDefault();
                    onNextPage();
                  }}
                  variant="ghost"
                  size="sm"
                  disabled={
                    (currentPage + 1) * journalsPerPage >= totalJournals
                  }
                >
                  Next
                  <ChevronRight className="ml-2 h-4 w-4" />
                </Button>
              </div>
            </form>
          </CardFooter>
        </Card>
      )}

      {matchingData.size > 0 && (
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
                  <TableHead>Application No. of Journal TM</TableHead>
                  <TableHead>Application No. of Our TM</TableHead>
                  <TableHead>Journal TM</TableHead>
                  <TableHead>Our TM</TableHead>
                  <TableHead>Class</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {Array.from(matchingData.entries())
                  .flatMap(([journalNumber, dataArray]) =>
                    dataArray.map((data, index) => ({
                      journalNumber,
                      data,
                      index,
                    }))
                  )
                  .slice(
                    currentReportPage * reportsPerPage,
                    (currentReportPage + 1) * reportsPerPage
                  )
                  .map(({ journalNumber, data, index }) => (
                    <TableRow key={`${journalNumber}-${index}`}>
                      {/* Select Checkbox */}
                      <TableHead>
                        <input
                          type="checkbox"
                          checked={selectedRows.has(data.applicationNoJournalTM)}
                          onChange={() =>
                            handleRowSelect(data.applicationNoJournalTM)
                          }
                        />
                      </TableHead>

                      {/* Application No. of Journal TM */}
                      <TableHead>{data.applicationNoJournalTM}</TableHead>

                      {/* Application No. of Our TM */}
                      <TableHead>
                        {data.ourMatchedTrademarks.map((tm, tmIndex) => (
                          <div key={tmIndex} className="flex items-center">
                            {tm.tmApplicationNumber}
                          </div>
                        ))}
                      </TableHead>

                      {/* Journal TM */}
                      <TableHead>{data.journalTM}</TableHead>

                      {/* Our TM */}
                      <TableHead>
                        {data.ourMatchedTrademarks.map((tm, tmIndex) => (
                          <div key={tmIndex} className="flex items-center">
                            <input
                              type="checkbox"
                              checked={
                                selectedOurTM
                                  .get(journalNumber)
                                  ?.get(data.applicationNoJournalTM)
                                  ?.has(tm.tmApplicationNumber) ?? false
                              }
                              onChange={() =>
                                handleUniqueOurTMSelect(
                                  journalNumber,
                                  data.applicationNoJournalTM,
                                  tm.tmApplicationNumber
                                )
                              }
                              className="mr-2"
                            />
                            {tm.tmAppliedFor}
                          </div>
                        ))}
                      </TableHead>

                      {/* Class */}
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
            <div className="flex items-center gap-4">
              <div className="text-xs text-muted-foreground">
                Showing{' '}
                <strong>
                  {currentReportPage * reportsPerPage + 1}-
                  {Math.min(
                    (currentReportPage + 1) * reportsPerPage,
                    Array.from(matchingData.values()).flat().length
                  )}
                </strong>{' '}
                of{' '}
                <strong>
                  {Array.from(matchingData.values()).flat().length}
                </strong>{' '}
                reports
              </div>
              <div className="flex">
                <Button
                  onClick={prevReportPage}
                  variant="ghost"
                  size="sm"
                  disabled={currentReportPage === 0}
                >
                  <ChevronLeft className="mr-2 h-4 w-4" />
                  Prev
                </Button>
                <Button
                  onClick={nextReportPage}
                  variant="ghost"
                  size="sm"
                  disabled={
                    (currentReportPage + 1) * reportsPerPage >=
                    Array.from(matchingData.values()).flat().length
                  }
                >
                  Next
                  <ChevronRight className="ml-2 h-4 w-4" />
                </Button>
              </div>
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
  const [currentPage, setCurrentPage] = useState(0);
  const journalsPerPage = 16;

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

  const prevPage = () => {
    setCurrentPage((prev) => Math.max(0, prev - 1));
  };

  const nextPage = () => {
    setCurrentPage((prev) => {
      const maxPage = Math.ceil(journals.length / journalsPerPage) - 1;
      return Math.min(prev + 1, maxPage);
    });
  };

  return (
    <div>
      {loading ? (
        <CircularLoader />
      ) : (
        <ProductsTable
          journals={journals
            .sort((a, b) => Number(b.journalNo) - Number(a.journalNo))
            .slice(
              currentPage * journalsPerPage,
              (currentPage + 1) * journalsPerPage
            )}
          currentPage={currentPage}
          totalJournals={journals.length}
          onPrevPage={prevPage}
          onNextPage={nextPage}
        />
      )}
    </div>
  );
};

export default HomePage;
