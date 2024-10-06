'use client';

import React from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

// Define the type for the PDF report entries
interface PDFReport {
  title: string;
  url: string;
}

// Sample reports data
const reports: PDFReport[] = [
  {
    title: 'Report-1',
    url: '/reports/report-1.pdf', // Path to your PDF file
  },
  {
    title: 'Report-2',
    url: '/reports/report-2.pdf', // Path to your PDF file
  },
];

const ReportViewer = () => {
  return (
    <div className="flex flex-col w-full h-full bg-gray-100 p-4">
      <Card className="w-full">
        <CardHeader>
          <CardTitle className="text-2xl font-bold text-left">Reports</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {reports.map((report) => (
              <div key={report.title} className="flex justify-between items-center bg-white p-4 rounded-lg shadow hover:shadow-lg transition-all duration-300">
                <span className="font-medium">{report.title}</span>
                <div className="flex space-x-2">
                  <Button
                    onClick={() => window.open(report.url, '_blank')}
                    className="bg-blue-600 text-white hover:bg-blue-500 transition-all duration-300"
                  >
                    View
                  </Button>
                  <Button
                    onClick={() => {
                      const link = document.createElement('a');
                      link.href = report.url;
                      link.download = report.title; // Sets the filename
                      link.click();
                    }}
                    className="bg-green-600 text-white hover:bg-green-500 transition-all duration-300"
                  >
                    Download
                  </Button>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default ReportViewer;
