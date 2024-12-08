'use client';

import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import DeleteButton from '@/components/ui/deleteButton';
import CircularLoader from '@/components/ui/loader';

interface PDFReport {
  title: string;
  report_id: string;
  journal_number: string;
  our_app_id: string;
  journal_app_id: string;
}

const url = process.env.NEXT_PUBLIC_API_URL;

const usePdfHandler = () => {
  const [isPdfProcessing, setIsPdfProcessing] = useState(false);

  const getPdfViewerHTML = (pdfUrl: string): string => `
    <!DOCTYPE html>
    <html style="margin:0;padding:0;height:100%;" lang="en">
      <head>
        <title>PDF Viewer</title>
        <style>
          body, html {
            margin: 0;
            padding: 0;
            height: 100%;
            overflow: hidden;
          }
          iframe {
            display: block;
            border: none;
            width: 100vw;
            height: 100vh;
          }
        </style>
      </head>
      <body>
        <iframe src="${pdfUrl}"></iframe>
      </body>
    </html>
  `;

  const downloadPdf = (blobUrl: string, reportId: string) => {
    const link = document.createElement('a');
    link.href = blobUrl;
    link.download = `report_${reportId}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(blobUrl);
  };

  const viewPdf = (blobUrl: string) => {
    const pdfWindow = window.open('');
    if (pdfWindow) {
      pdfWindow.document.write(getPdfViewerHTML(blobUrl));
      pdfWindow.onbeforeunload = () => {
        URL.revokeObjectURL(blobUrl);
      };
      return;
    }
    const tab = window.open(blobUrl, '_blank');
    if (!tab) {
      window.location.href = blobUrl;
    }
  };

  const handlePdf = (reportId: string, download: boolean = true) => {
    const fileUrl = `${url}/get/download_report/${reportId}`;

    setIsPdfProcessing(true);

    fetch(fileUrl)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.blob();
      })
      .then(blob => {
        const pdfBlob = new Blob([blob], { type: 'application/pdf' });
        const blobUrl = URL.createObjectURL(pdfBlob);

        if (download) {
          downloadPdf(blobUrl, reportId);
        } else {
          viewPdf(blobUrl);
        }
      })
      .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
      })
      .finally(() => {
        setTimeout(() => {
          setIsPdfProcessing(false);
        }, 200);
      });
  };

  return { handlePdf, isPdfProcessing };
};

const useReports = () => {
  const [reports, setReports] = useState<PDFReport[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchReports = () => {
    setIsLoading(true);

    fetch(`${url}/get/generated_reports`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        const mappedReport: PDFReport[] = data.map(
          (item: {
            report: string;
            reportId: string;
            journalNumber: string;
            ourAppId: string;
            journalAppId: string;
          }) => ({
            title: item.report.replace(/_/g, ' ').replace(/\.pdf$/, ''),
            report_id: item.reportId,
            journal_number: item.journalNumber,
            our_app_id: item.ourAppId,
            journal_app_id: item.journalAppId
          })
        );
        setReports(mappedReport);
      })
      .catch(error => {
        console.error('There was a problem with the fetch operation:', error);
      })
      .finally(() => {
        setTimeout(() => {
          setIsLoading(false);
        }, 200);
      });
  };
  
  const handleDelete = (reportId: string) => {
    setIsDeleting(true);

    fetch(`${url}/delete/report/${reportId}`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return fetchReports();
      })
      .catch(error => {
        console.error('There was a problem with the delete operation:', error);
      })
      .finally(() => {
        setTimeout(() => {
          setIsDeleting(false);
        }, 200);
      });
  };

  useEffect(() => {
    fetchReports();
  }, []);

  return { reports, isLoading, isDeleting, handleDelete };
};

const ReportCard: React.FC<{
  report: PDFReport;
  onView: (reportId: string) => void;
  onDownload: (reportId: string) => void;
  onDelete: (reportId: string) => void;
  disabled?: boolean;
}> = ({ report, onView, onDownload, onDelete, disabled }) => (
  <div className="bg-white p-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-300">
    <div className="flex justify-between items-center space-x-4">
      {/* Report Title */}
      <div className="flex-1">
        <h3 className="text-lg font-bold text-gray-800 truncate">
          {report.title}
        </h3>
        <div className="text-sm text-gray-600 mt-2">
          <p className="mb-2">
            <span className="font-medium mr-2">Journal Number:</span>
            <span className="text-gray-800">{report.journal_number}</span>
          </p>
          <p className="mb-2">
            <span className="font-medium mr-2">Our Trademark:</span>
            <span className="text-gray-800">{report.our_app_id}</span>
          </p>
          <p>
            <span className="font-medium mr-2">Conflicting Trademark:</span>
            <span className="text-gray-800">{report.journal_app_id}</span>
          </p>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex space-x-2">
        <Button
          onClick={() => onView(report.report_id)}
          className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-400 transition duration-200"
          disabled={disabled}
        >
          View
        </Button>
        <Button
          onClick={() => onDownload(report.report_id)}
          className="bg-green-500 text-white px-4 py-2 rounded-md hover:bg-green-400 transition duration-200"
          disabled={disabled}
        >
          Download
        </Button>
        <Button
          onClick={() => onDelete(report.report_id)}
          className="bg-red-500 text-white px-4 py-2 rounded-md hover:bg-red-400 transition duration-200"
          disabled={disabled}
        >
          Delete
        </Button>
      </div>
    </div>
  </div>
);


const ReportViewer = () => {
  const { reports, isLoading, isDeleting, handleDelete } = useReports();
  const { handlePdf, isPdfProcessing } = usePdfHandler();

  const isProcessing = isLoading || isDeleting || isPdfProcessing;

  return (
    <div className="p-4">
      {isProcessing ? (
        <CircularLoader />
      ) : (
        <div className="flex flex-col w-full h-full bg-gray-100">
          <Card className="w-full">
            <CardHeader>
              <CardTitle className="text-2xl font-bold text-left">Reports</CardTitle>
            </CardHeader>
            <CardContent>
              {reports.length === 0 ? (
                <div className="text-center text-gray-600 py-8">
                  <p className="text-xl">No opposition reports available</p>
                  <p className="text-sm">Once reports are generated, they will appear here.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {reports.reverse().map((report) => (
                    <ReportCard
                      key={report.report_id}
                      report={report}
                      onView={(id) => handlePdf(id, false)}
                      onDownload={(id) => handlePdf(id, true)}
                      onDelete={handleDelete}
                      disabled={isProcessing}
                    />
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
};

export default ReportViewer;