import React, { useState } from 'react';
import { ArrowLeft, Shield, Download, Trash2, X, FileText, FileSpreadsheet } from 'lucide-react';
import { useRouter } from 'next/navigation';
import jsPDF from 'jspdf';
import { getHistory, getProfile, deleteAccount } from '@/lib/api';

interface Props {
  onBack: () => void;
}

export default function PrivacySettings({ onBack }: Props) {
  const router = useRouter();
  const [showDownloadModal, setShowDownloadModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isGenerating, setIsGenerating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const generateData = async (format: 'csv' | 'pdf') => {
    setIsGenerating(true);
    try {
      const [history, profile] = await Promise.all([getHistory(), getProfile()]);
      
      let content = "";
      let filename = "";
      let mimeType = "";

      if (format === 'csv') {
        const header = "Date,Stress Level,Score,Escalated,Reasons,Recommendation\n";
        const rows = history.map(h => {
          const date = new Date(h.timestamp).toISOString();
          const reasons = h.reasons ? h.reasons.join('; ') : '';
          const recommendation = h.recommendation ? h.recommendation.replace(/,/g, '') : '';
          return `${date},${h.stress_level},${h.score},${h.is_escalated},${reasons},${recommendation}`;
        }).join('\n');
        
        content = header + rows;
        filename = `CortiSense_MyData.csv`;
        mimeType = "text/csv;charset=utf-8;";

        const blob = new Blob([content], { type: mimeType });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);
      } else {
        const doc = new jsPDF();
        doc.setFont("times", "normal");
        
        // Add CortiSense Logo text
        doc.setFontSize(24);
        doc.setTextColor(34, 197, 94); // Emerald 500
        doc.text("C", 20, 20);
        
        doc.setFontSize(20);
        doc.setTextColor(0, 0, 0);
        doc.text("ortiSense Complete User Data", 28, 20);
        
        doc.setFontSize(12);
        doc.text(`Generated: ${new Date().toLocaleString()}`, 20, 30);
        
        let yPos = 45;
        
        if (profile) {
          doc.setFontSize(14);
          doc.setFont("times", "bold");
          doc.text("User Profile", 20, yPos);
          yPos += 7;
          doc.setFont("times", "normal");
          doc.setFontSize(12);
          doc.text(`Name: ${profile.first_name} ${profile.last_name}`, 20, yPos);
          yPos += 7;
          doc.text(`Mobile: ${profile.mobile_number || 'N/A'}`, 20, yPos);
          yPos += 7;
          doc.text(`Age: ${profile.age || 'N/A'}`, 20, yPos);
          yPos += 7;
          doc.text(`Gender: ${profile.gender || 'N/A'}`, 20, yPos);
          yPos += 7;
          doc.text(`Goal: ${profile.goal || 'None'}`, 20, yPos);
          yPos += 12;
        }
        
        doc.setFontSize(14);
        doc.setFont("times", "bold");
        doc.text("Complete Check-in History", 20, yPos);
        yPos += 10;
        doc.setFont("times", "normal");
        doc.setFontSize(10);
        
        history.forEach((h) => {
          if (yPos > 270) {
            doc.addPage();
            yPos = 20;
          }
          const dateStr = new Date(h.timestamp).toLocaleString();
          const line = `[${dateStr}] Stress: ${h.stress_level} (Score: ${h.score}) | Escalated: ${h.is_escalated ? 'Yes' : 'No'}`;
          doc.text(line, 20, yPos);
          yPos += 6;
          
          if (h.reasons && h.reasons.length > 0) {
            doc.text(`Reasons: ${h.reasons.join(', ')}`, 25, yPos);
            yPos += 6;
          }
          yPos += 2;
        });
        
        doc.save(`CortiSense_MyData.pdf`);
      }
      setShowDownloadModal(false);
    } catch (err) {
      console.error("Failed to generate data", err);
      alert("Failed to fetch user data.");
    } finally {
      setIsGenerating(false);
    }
  };

  const confirmDelete = async () => {
    setIsDeleting(true);
    try {
      await deleteAccount();
      localStorage.clear();
      router.push('/welcome');
    } catch (err) {
      console.error("Failed to delete account", err);
      alert("Failed to delete account. Please try again.");
      setIsDeleting(false);
    }
  };

  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810] relative">
      <div className="flex items-center mb-6">
        <button onClick={onBack} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
          <ArrowLeft size={24} />
        </button>
      </div>
      
      <h1 className="text-3xl font-bold text-white mb-1">Privacy & Data</h1>
      <p className="text-slate-400 text-sm mb-6">Control your data and privacy</p>

      <div className="bg-purple-900/20 border border-purple-500/20 p-5 rounded-2xl mb-8 flex items-start">
        <Shield className="text-emerald-500 mt-1 mr-4 shrink-0" size={24} />
        <div>
          <h3 className="text-white font-bold text-lg mb-2">Your Data is Protected</h3>
          <p className="text-purple-200/80 text-sm leading-relaxed">
            All your health data is encrypted and stored securely. We never share your personal information with third parties.
          </p>
        </div>
      </div>

      <h3 className="text-slate-400 text-sm font-bold uppercase tracking-wider mb-4">Privacy Policy</h3>
      
      <div className="bg-slate-900/50 border border-slate-800 p-6 rounded-3xl mb-8 space-y-6">
        
        <div>
          <h4 className="text-white font-bold text-base mb-1">1. Information Collection</h4>
          <p className="text-slate-400 text-sm leading-relaxed">
            We collect account details, mood logs, stress levels, and usage analytics to provide personalized insights.
          </p>
        </div>

        <div>
          <h4 className="text-white font-bold text-base mb-1">2. How Data is Used</h4>
          <p className="text-slate-400 text-sm leading-relaxed">
            Your data is used exclusively to generate wellness insights, improve our AI accuracy, and provide relevant notifications.
          </p>
        </div>

        <div>
          <h4 className="text-white font-bold text-base mb-1">3. Data Sharing & Security</h4>
          <p className="text-slate-400 text-sm leading-relaxed">
            We do not sell your personal data. All health and account data is encrypted and stored securely.
          </p>
        </div>

        <div>
          <h4 className="text-white font-bold text-base mb-1">4. User Rights</h4>
          <p className="text-slate-400 text-sm leading-relaxed">
            You have the right to access, export, or permanently delete your data at any time using the options below.
          </p>
        </div>

        <div>
          <h4 className="text-white font-bold text-base mb-1">5. Contact</h4>
          <p className="text-slate-400 text-sm leading-relaxed">
            For privacy inquiries, contact privacy@cortisense.com.
          </p>
        </div>

      </div>

      <h3 className="text-slate-400 text-sm font-bold uppercase tracking-wider mb-4">YOUR DATA</h3>
      
      <div className="bg-slate-900/50 border border-slate-800 rounded-3xl mb-10 overflow-hidden">
        
        <button 
          className="w-full flex items-center p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={() => setShowDownloadModal(true)}
        >
          <Download className="text-slate-400 mr-4 shrink-0" size={24} />
          <div className="text-left">
            <span className="block font-bold text-lg text-white">Download My Data</span>
            <span className="text-slate-400 text-sm">Export all your data</span>
          </div>
        </button>

        <button 
          className="w-full flex items-center p-5 hover:bg-rose-500/10 transition-colors"
          onClick={() => setShowDeleteModal(true)}
        >
          <Trash2 className="text-rose-500 mr-4 shrink-0" size={24} />
          <div className="text-left">
            <span className="block font-bold text-lg text-rose-500">Delete My Account</span>
            <span className="text-rose-500/70 text-sm">Permanently remove all data</span>
          </div>
        </button>

      </div>

      {/* Download Modal */}
      {showDownloadModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#111116] border border-slate-800 rounded-3xl p-6 w-full max-w-sm relative">
            <button 
              onClick={() => setShowDownloadModal(false)}
              className="absolute top-4 right-4 p-2 text-slate-400 hover:text-white bg-slate-800/50 rounded-full"
            >
              <X size={20} />
            </button>
            <h2 className="text-2xl font-bold text-white mb-2">Download Data</h2>
            <p className="text-slate-400 mb-6">Choose a format to export all your account data and history.</p>
            
            <div className="grid grid-cols-2 gap-4">
              <button 
                onClick={() => generateData('pdf')}
                disabled={isGenerating}
                className="bg-slate-800 hover:bg-slate-700 p-4 rounded-2xl flex flex-col items-center justify-center transition-colors disabled:opacity-50 border border-slate-700 hover:border-emerald-500/50"
              >
                <FileText className="text-emerald-400 mb-2" size={32} />
                <span className="font-bold text-white">PDF</span>
              </button>
              <button 
                onClick={() => generateData('csv')}
                disabled={isGenerating}
                className="bg-slate-800 hover:bg-slate-700 p-4 rounded-2xl flex flex-col items-center justify-center transition-colors disabled:opacity-50 border border-slate-700 hover:border-emerald-500/50"
              >
                <FileSpreadsheet className="text-emerald-400 mb-2" size={32} />
                <span className="font-bold text-white">CSV</span>
              </button>
            </div>
            {isGenerating && <p className="text-emerald-400 text-center mt-4 font-medium animate-pulse">Generating your data...</p>}
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {showDeleteModal && (
        <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-[#111116] border border-rose-500/30 rounded-3xl p-6 w-full max-w-sm relative">
            <button 
              onClick={() => !isDeleting && setShowDeleteModal(false)}
              className="absolute top-4 right-4 p-2 text-slate-400 hover:text-white bg-slate-800/50 rounded-full"
              disabled={isDeleting}
            >
              <X size={20} />
            </button>
            <div className="w-12 h-12 bg-rose-500/20 rounded-full flex items-center justify-center mb-4 text-rose-500">
              <Trash2 size={24} />
            </div>
            <h2 className="text-2xl font-bold text-white mb-2">Delete Account?</h2>
            <p className="text-slate-400 mb-6">
              Are you sure you want to permanently delete your account? All your check-ins, history, and credentials will be removed from the database forever. This action <span className="font-bold text-rose-400">cannot be undone</span>.
            </p>
            
            <div className="flex flex-col gap-3">
              <button 
                onClick={confirmDelete}
                disabled={isDeleting}
                className="w-full bg-rose-600 hover:bg-rose-500 text-white font-bold py-3 rounded-xl transition-colors disabled:opacity-50"
              >
                {isDeleting ? 'Deleting...' : 'Yes, Delete Permanently'}
              </button>
              <button 
                onClick={() => setShowDeleteModal(false)}
                disabled={isDeleting}
                className="w-full bg-slate-800 hover:bg-slate-700 text-white font-bold py-3 rounded-xl transition-colors disabled:opacity-50"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
