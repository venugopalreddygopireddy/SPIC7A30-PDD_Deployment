"use client";

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Calendar, History, FileText, FileSpreadsheet, CheckCircle2 } from 'lucide-react';
import { getHistory, getProfile, StressCheckInResponse, ProfileResponse } from '@/lib/api';
import jsPDF from 'jspdf';

const TIME_PERIODS = [
  { id: '7days', label: 'Last 7 Days', size: '84 KB', icon: Calendar, days: 7 },
  { id: '30days', label: 'Last 30 Days', size: '320 KB', icon: Calendar, days: 30 },
  { id: '3months', label: 'Last 3 Months', size: '950 KB', icon: Calendar, days: 90 },
  { id: 'all', label: 'All Time', size: '2.1 MB', icon: History, days: 9999 },
];

const FORMATS = [
  { id: 'pdf', label: 'PDF', icon: FileText },
  { id: 'csv', label: 'CSV', icon: FileSpreadsheet },
];

export default function ExportReportScreen() {
  const router = useRouter();
  const [selectedPeriod, setSelectedPeriod] = useState('30days');
  const [selectedFormat, setSelectedFormat] = useState('pdf');
  const [isGenerating, setIsGenerating] = useState(false);
  const [historyData, setHistoryData] = useState<StressCheckInResponse[]>([]);
  const [profileData, setProfileData] = useState<ProfileResponse | null>(null);

  useEffect(() => {
    // Pre-fetch data
    Promise.all([getHistory(), getProfile()])
      .then(([history, profile]) => {
        setHistoryData(history);
        setProfileData(profile);
      })
      .catch(err => console.error("Failed to fetch export data", err));
  }, []);

  const handleGenerateReport = async () => {
    setIsGenerating(true);
    try {
      const periodInfo = TIME_PERIODS.find(p => p.id === selectedPeriod);
      const days = periodInfo ? periodInfo.days : 30;
      
      const cutoffDate = new Date();
      cutoffDate.setDate(cutoffDate.getDate() - days);
      
      // Filter history based on time period
      const filteredHistory = historyData.filter(h => new Date(h.timestamp) >= cutoffDate);
      
      let content = "";
      let filename = "";
      let mimeType = "";

      if (selectedFormat === 'csv') {
        // Generate CSV
        const header = "Date,Stress Level,Score,Escalated,Reasons,Recommendation\n";
        const rows = filteredHistory.map(h => {
          const date = new Date(h.timestamp).toISOString().split('T')[0];
          const reasons = h.reasons ? h.reasons.join('; ') : '';
          const recommendation = h.recommendation ? h.recommendation.replace(/,/g, '') : '';
          return `${date},${h.stress_level},${h.score},${h.is_escalated},${reasons},${recommendation}`;
        }).join('\n');
        
        content = header + rows;
        filename = `cortisense_report_${selectedPeriod}.csv`;
        mimeType = "text/csv;charset=utf-8;";
      } else {
        // Generate real PDF
        const doc = new jsPDF();
        doc.setFont("times", "normal");
        
        // Add CortiSense Logo text
        doc.setFontSize(24);
        doc.setTextColor(34, 197, 94); // Emerald 500 for the C
        doc.text("C", 20, 20);
        
        doc.setFontSize(20);
        doc.setTextColor(0, 0, 0);
        doc.text("ortiSense Wellness Report", 28, 20);
        
        doc.setFontSize(12);
        doc.text(`Time Period: ${periodInfo?.label}`, 20, 30);
        doc.text(`Generated: ${new Date().toLocaleString()}`, 20, 37);
        
        let yPos = 50;
        
        if (profileData) {
          doc.setFontSize(14);
          doc.setFont("times", "bold");
          doc.text("User Profile", 20, yPos);
          yPos += 7;
          doc.setFont("times", "normal");
          doc.setFontSize(12);
          doc.text(`Name: ${profileData.first_name} ${profileData.last_name}`, 20, yPos);
          yPos += 7;
          doc.text(`Goal: ${profileData.goal || 'None'}`, 20, yPos);
          yPos += 12;
        }
        
        doc.setFontSize(14);
        doc.setFont("times", "bold");
        doc.text("Check-in History", 20, yPos);
        yPos += 10;
        doc.setFont("times", "normal");
        doc.setFontSize(10);
        
        filteredHistory.forEach((h, index) => {
          if (yPos > 270) {
            doc.addPage();
            yPos = 20;
          }
          const dateStr = new Date(h.timestamp).toLocaleDateString();
          const line = `[${dateStr}] Stress: ${h.stress_level} (Score: ${h.score}) | Escalated: ${h.is_escalated ? 'Yes' : 'No'}`;
          doc.text(line, 20, yPos);
          yPos += 6;
          
          if (h.reasons && h.reasons.length > 0) {
            doc.text(`Reasons: ${h.reasons.join(', ')}`, 25, yPos);
            yPos += 6;
          }
          yPos += 2;
        });
        
        doc.save(`cortisense_report_${selectedPeriod}.pdf`);
        
        // Simulate a small delay for UX
        await new Promise(resolve => setTimeout(resolve, 800));
        setIsGenerating(false);
        return;
      }

      // Trigger standard browser download for CSV
      const blob = new Blob([content], { type: mimeType });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.setAttribute("href", url);
      link.setAttribute("download", filename);
      link.style.visibility = 'hidden';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);

      // Simulate a small delay for UX
      await new Promise(resolve => setTimeout(resolve, 800));
      
    } catch (err) {
      console.error("Export failed", err);
      alert("Failed to generate report. Please try again.");
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#f8fafc] text-slate-900">
      
      {/* Header */}
      <div className="flex flex-col mb-8 mt-2">
        <button onClick={() => router.back()} className="text-slate-800 p-2 -ml-2 rounded-full hover:bg-slate-200 transition-colors w-fit mb-4">
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-3xl font-extrabold text-[#2a3042] mb-1 tracking-tight">Export Report</h1>
        <p className="text-slate-500 font-medium">Download your wellness data</p>
      </div>

      <div className="flex-1 space-y-8">
        
        {/* Time Period Section */}
        <section>
          <h3 className="text-[#2a3042] font-bold text-lg mb-4">Select Time Period</h3>
          <div className="space-y-3">
            {TIME_PERIODS.map(period => (
              <button
                key={period.id}
                onClick={() => setSelectedPeriod(period.id)}
                className={`w-full flex items-center justify-between p-4 rounded-2xl transition-all border-2 ${
                  selectedPeriod === period.id 
                    ? 'border-[#829986] bg-[#829986]/10' 
                    : 'border-slate-200 bg-white hover:border-slate-300'
                }`}
              >
                <div className="flex items-center">
                  <period.icon className={selectedPeriod === period.id ? 'text-[#829986] mr-3' : 'text-slate-400 mr-3'} size={20} />
                  <span className={`font-semibold ${selectedPeriod === period.id ? 'text-[#2a3042]' : 'text-slate-600'}`}>
                    {period.label}
                  </span>
                </div>
                <span className="text-slate-400 text-sm font-medium">{period.size}</span>
              </button>
            ))}
          </div>
        </section>

        {/* Export Format Section */}
        <section>
          <h3 className="text-[#2a3042] font-bold text-lg mb-4">Export Format</h3>
          <div className="grid grid-cols-2 gap-4">
            {FORMATS.map(format => (
              <button
                key={format.id}
                onClick={() => setSelectedFormat(format.id)}
                className={`flex flex-col items-center justify-center py-6 rounded-2xl transition-all border-2 ${
                  selectedFormat === format.id 
                    ? 'border-[#829986] bg-[#829986]/10' 
                    : 'border-slate-200 bg-white hover:border-slate-300'
                }`}
              >
                <format.icon className={`mb-2 ${selectedFormat === format.id ? 'text-[#829986]' : 'text-slate-400'}`} size={28} />
                <span className={`font-bold ${selectedFormat === format.id ? 'text-[#2a3042]' : 'text-slate-500'}`}>
                  {format.label}
                </span>
              </button>
            ))}
          </div>
        </section>

        {/* Report Includes Section */}
        <section className="bg-[#f3efff] p-6 rounded-3xl mb-8">
          <h3 className="text-[#2a3042] font-bold text-lg mb-5">Report Includes</h3>
          <div className="space-y-4">
            {[
              "Daily stress scores and trends",
              "Physiological data (heart rate, sleep)",
              "Psychological assessments",
              "Lifestyle factors and patterns",
              "AI insights and recommendations"
            ].map((item, i) => (
              <div key={i} className="flex items-center">
                <CheckCircle2 className="text-[#a8a3a3] mr-3 flex-shrink-0" size={20} fill="#b9b8b8" color="white" />
                <span className="text-slate-600 font-medium">{item}</span>
              </div>
            ))}
          </div>
        </section>
      </div>

      {/* Generate Button */}
      <div className="mt-8 mb-4">
        <button 
          onClick={handleGenerateReport}
          disabled={isGenerating}
          className="w-full bg-[#9ea797] hover:bg-[#8f9988] text-white font-bold text-lg py-5 rounded-2xl shadow-sm transition-colors flex items-center justify-center disabled:opacity-70"
        >
          {isGenerating ? 'Generating...' : 'Generate Report'}
        </button>
      </div>

    </div>
  );
}
