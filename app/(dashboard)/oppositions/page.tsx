'use client';

import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { title } from 'process';
import DeleteButton from '@/components/ui/deleteButton';
import CircularLoader from '@/components/ui/loader';

// Define the type for the PDF report entries
interface PDFReport {
  title: string;
  report_id: string
}

const url = process.env.NEXT_PUBLIC_API_URL;

const ReportViewer = () => {

  const [reports, setReports] = useState<PDFReport[]>([])
  const [loading, setLoading] = useState<boolean>(false)
  const [deleting, setDeleting] = useState<boolean>(false)


  const handleDownloadAndView = async (reportId: string, download: boolean = true) => {
    const fileUrl = `${url}/get/download_report/${reportId}`;
    let blobUrl: string | undefined;
    try {
      setDeleting(true);
      const response = await fetch(fileUrl);
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      const blob = await response.blob();
      blobUrl = URL.createObjectURL(blob);
      if (download) {
        const a = document.createElement('a');
        a.href = blobUrl;
        a.download = `report_${reportId}.docx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
      } else {
        window.open(blobUrl, '_blank');
      }
    } catch (error) {
      console.error('There was a problem with the fetch operation:', error);
    } finally {
      setTimeout(() => {
        setDeleting(false);
      }, 200);
      if (blobUrl) {
        URL.revokeObjectURL(blobUrl);
      }
    }
  };


  const handelDelete = (reportId: string) => {
    const deleteUrl = `${url}/delete/report/${reportId}`
    const deleteReport = async () => {
      try {
        setDeleting(true);
        const response = await fetch(deleteUrl);
        if (!response.ok) {
          alert(response.body)
          throw new Error('Network response was not ok');
        }
      } catch (error) {
        console.error('There was a problem with the fetch operation:', error);
      } finally {
        setTimeout(() => {
          setDeleting(false);
        }, 200);
      }
    };
    deleteReport()
  }

  useEffect(() => {
    const oppositionUrl = `${url}/get/generated_reports`
    const fetchOppositionReport = async () => {
      try {
        setLoading(true);
        const response = await fetch(oppositionUrl);
        if (!response.ok) {
          alert(response.body)
          throw new Error('Network response was not ok');
        }
        const data = await response.json();
        const mappedReport: PDFReport[] = data.map((item: { reportId: string; report: string; }) => ({
          title: item.report,
          report_id: item.reportId,
        }));
        setReports(mappedReport)
      } catch (error) {
        console.error('There was a problem with the fetch operation:', error);
      } finally {
        setTimeout(() => {
          setLoading(false);
        }, 200);
      }
    };
    fetchOppositionReport()
  }, [deleting])

  return (
    <div>
      {
        deleting || loading ? (
          <CircularLoader />
        ) : (
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
                          onClick={() => handleDownloadAndView(report.report_id, false)}
                          className="bg-blue-600 text-white hover:bg-blue-500 transition-all duration-300"
                        >
                          View
                        </Button>
                        <Button
                          onClick={() => {
                            handleDownloadAndView(report.report_id);
                          }}
                          className="bg-green-600 text-white hover:bg-green-500 transition-all duration-300"
                        >
                          Download
                        </Button>
                        <div>
                          <DeleteButton onClick={() =>
                            handelDelete(report.report_id)
                          } />
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </div>
        )
      }
    </div>


  );
};

export default ReportViewer;
