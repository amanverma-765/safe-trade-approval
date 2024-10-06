"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Label } from "@/components/ui/label"; // You may need to create a simple Label component without Radix
import { Building2, CheckCircle } from "lucide-react";

// Define the CompanySelectionComponent
export function CompanySelectionComponent() {
  const [selectedCompany, setSelectedCompany] = useState<string | null>(null);

  const handleCompanySelect = (value: string) => {
    setSelectedCompany(value);
  };

  const handleContinue = () => {
    if (selectedCompany) {
      console.log(`Selected company: ${selectedCompany}`);
      // Here you would typically navigate to the next page or perform some action
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gradient-to-b from-primary/20 to-background p-4">
      <Card className="w-full max-w-md">
        <CardHeader>
          <CardTitle className="text-2xl font-bold text-center">Select Your Company</CardTitle>
          <CardDescription className="text-center">Choose the company you want to access</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div
              className={`flex items-center space-x-2 border rounded-lg p-4 hover:bg-accent cursor-pointer ${selectedCompany === "Firm1" ? "bg-accent" : ""}`}
              onClick={() => handleCompanySelect("Firm1")}
            >
              <Building2 className="h-5 w-5 text-primary" />
              <span>Firm1</span>
              {selectedCompany === "Firm1" && <CheckCircle className="h-5 w-5 text-primary ml-auto" />}
            </div>
            <div
              className={`flex items-center space-x-2 border rounded-lg p-4 hover:bg-accent cursor-pointer ${selectedCompany === "Firm2" ? "bg-accent" : ""}`}
              onClick={() => handleCompanySelect("Firm2")}
            >
              <Building2 className="h-5 w-5 text-primary" />
              <span>Firm2</span>
              {selectedCompany === "Firm2" && <CheckCircle className="h-5 w-5 text-primary ml-auto" />}
            </div>
          </div>
        </CardContent>
        <Button 
          className="w-full" 
          onClick={handleContinue}
          disabled={!selectedCompany}
        >
          Continue
        </Button>
      </Card>
    </div>
  );
}

// Default export of the page
export default function Page() {
  return <CompanySelectionComponent />;
}
