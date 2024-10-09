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
import { useRouter } from 'next/navigation';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';

// Define the type for the Journal entries
interface JournalEntry {
  journalNo: string;
  dateOfPublication: string;
  lastDate: string;
}

// Define the type for the table that shows after generating Excel
interface ExcelData {
  applicationNoJournalTM: string; // Application no of Journal TM
  applicationNoOurTM: string; // Application no of Our TM
  journalTM: string; // Journal TM
  ourTM: string[]; // Our TM (multiple values)
  class: string; // Class
}

// Function to handle Excel file generation
const generateExcel = (selectedJournals: JournalEntry[]) => {
  console.log('Generating Excel file for selected journals:', selectedJournals);
  // Your Excel generation logic here
};

// The ProductsTable component definition
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
  const [excelData, setExcelData] = useState<ExcelData[]>([]);
  const [uniqueOurTM, setUniqueOurTM] = useState<Set<string>>(new Set());
  const [selectedUniqueOurTM, setSelectedUniqueOurTM] = useState<Set<string>>(new Set());

  function prevPage() {
    router.push(`/?offset=${Math.max(offset - journalsPerPage, 0)}`, { scroll: false });
  }

  function nextPage() {
    router.push(`/?offset=${Math.min(offset + journalsPerPage, totalJournals - 1)}`, { scroll: false });
  }

  const handleRowSelect = (journalNo: string) => {
    setSelectedRows(prevSelected => {
      const updatedSelected = new Set(prevSelected);
      if (updatedSelected.has(journalNo)) {
        updatedSelected.delete(journalNo);
      } else {
        updatedSelected.add(journalNo);
      }
      return updatedSelected;
    });
  };

  const handleGenerateExcel = () => {
    const selectedJournals = journals.filter(journal => selectedRows.has(journal.journalNo));

    const preparedExcelData: ExcelData[] = selectedJournals.map((journal) => ({
      applicationNoJournalTM: journal.journalNo,
      applicationNoOurTM: '12345', // Placeholder
      journalTM: 'TM-001', // Placeholder
      ourTM: ['Our-TM-001', 'Our-TM-002'], // Example
      class: 'Class A', // Placeholder
    }));

    setExcelData(preparedExcelData);

    const allOurTM = new Set<string>();
    preparedExcelData.forEach(data => {
      data.ourTM.forEach(ourTMValue => allOurTM.add(ourTMValue));
    });
    setUniqueOurTM(allOurTM);
  };

  const handleGenerateIndividualOpposition = () => {
    console.log('Generating individual opposition for selected Our TM:', Array.from(selectedUniqueOurTM));
    // Implement the logic for generating individual opposition here
  };

  const handleUniqueOurTMSelect = (ourTMValue: string) => {
    setSelectedUniqueOurTM(prevSelected => {
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
      <Card>
        <CardHeader>
          <CardTitle>Latest Search Report</CardTitle>
          <CardDescription>
            Download Excel | Search Report from Previous Journal
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto"> {/* Makes the table scrollable */}
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
          </div>
        </CardContent>
        <CardFooter className="flex flex-col sm:flex-row justify-between items-center">
          <Button
            onClick={handleGenerateExcel}
            className="bg-black text-white hover:bg-gray-800 transition-all duration-300 mr-4"
          >
            Generate Search Report
          </Button>
          <form className="flex flex-col sm:flex-row items-center w-full justify-between">
            <div className="text-xs text-muted-foreground">
              Showing{' '}
              <strong>
                {Math.min(offset, totalJournals) + 1}-{Math.min(offset + journalsPerPage, totalJournals)}
              </strong>{' '}
              of <strong>{totalJournals}</strong> journals
            </div>
            <div className="flex mt-2 sm:mt-0"> {/* Add margin for smaller screens */}
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

      {excelData.length > 0 && (
        <Card className="mt-4">
          <CardHeader>
            <CardTitle>Generated Search Reports</CardTitle>
            <CardDescription>
              Data from the selected journals
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto"> {/* Makes the table scrollable */}
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
                  {excelData.map((data, index) => (
                    <TableRow key={index}>
                      <TableHead>
                        <input
                          type="checkbox"
                          checked={selectedRows.has(index.toString())}
                          onChange={() => handleRowSelect(index.toString())}
                        />
                      </TableHead>
                      <TableHead>{data.applicationNoJournalTM}</TableHead>
                      <TableHead>{data.applicationNoOurTM}</TableHead>
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
            </div>
          </CardContent>
          <CardFooter className="flex justify-between items-center">
            <Button
              onClick={handleGenerateIndividualOpposition}
              className="bg-blue-600 text-white hover:bg-blue-700 transition-all duration-300 mr-4"
            >
              Generate Individual Opposition
            </Button>
            <div className="text-xs text-muted-foreground">
              Showing{' '}
              <strong>{uniqueOurTM.size} unique Our TM values</strong>
            </div>
          </CardFooter>
        </Card>
      )}
    </>
  );
}

// HomePage component definition
const HomePage = () => {
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
    <div className="container mx-auto mt-8">
      <ProductsTable journals={journals} offset={offset} totalJournals={totalJournals} />

      {/* Footer with Copyright */}
      <footer className="mt-8 text-center text-gray-500 text-xs">
        © 2024 Yatri Cloud. All rights reserved.
      </footer>
    </div>
  );
};

export default HomePage;
