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
            reportId: string;
            report: string;
            journalNumber: string;
          }) => ({
            title: item.report,
            report_id: item.reportId,
            journal_number: item.journalNumber
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
  <div className="flex justify-between items-center bg-white p-4 rounded-lg shadow hover:shadow-lg transition-all duration-300">
    <span className="font-medium">{report.title}</span>
    <span>{report.journal_number}</span>
    <div className="flex space-x-2">
      <Button
        onClick={() => onView(report.report_id)}
        className="bg-blue-600 text-white hover:bg-blue-500 transition-all duration-300"
        disabled={disabled}
      >
        View
      </Button>
      <Button
        onClick={() => onDownload(report.report_id)}
        className="bg-green-600 text-white hover:bg-green-500 transition-all duration-300"
        disabled={disabled}
      >
        Download
      </Button>
      <div>
        <DeleteButton onClick={() => onDelete(report.report_id)} />
      </div>
    </div>
  </div>
);

const ReportViewer = () => {
  const { reports, isLoading, isDeleting, handleDelete } = useReports();
  const { handlePdf, isPdfProcessing } = usePdfHandler();

  const isProcessing = isLoading || isDeleting || isPdfProcessing;

  return (
    <div>
      {isProcessing ? (
        <CircularLoader />
      ) : (
        <div className="flex flex-col w-full h-full bg-gray-100 p-4">
          <Card className="w-full">
            <CardHeader>
              <CardTitle className="text-2xl font-bold text-left">
                Reports
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {reports.toReversed().map((report) => (
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
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
};

export default ReportViewer;