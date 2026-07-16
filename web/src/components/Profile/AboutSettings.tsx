import React from 'react';
import { ArrowLeft, Info, FileText, Mail, Shield } from 'lucide-react';

interface Props {
  onBack: () => void;
}

export default function AboutSettings({ onBack }: Props) {
  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
      <div className="flex items-center mb-6">
        <button onClick={onBack} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
          <ArrowLeft size={24} />
        </button>
      </div>
      
      <div className="flex flex-col items-center mb-8">
        <div className="w-20 h-20 bg-emerald-500 rounded-3xl flex items-center justify-center mb-4 shadow-lg shadow-emerald-500/20">
           <div className="w-10 h-10 bg-[#050810] rounded-full"></div>
        </div>
        <h1 className="text-2xl font-bold text-white mb-1">CortiSense</h1>
        <p className="text-slate-400 text-sm mb-4">Version 1.0.0</p>
      </div>

      <div className="bg-slate-900/50 border border-slate-800 rounded-3xl mb-10 overflow-hidden">
        
        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={() => alert('Terms of Service coming soon.')}
        >
          <div className="flex items-center">
            <FileText className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Terms of Service</span>
          </div>
        </button>

        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={() => alert('Privacy Policy coming soon.')}
        >
          <div className="flex items-center">
            <Shield className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Privacy Policy</span>
          </div>
        </button>
        
        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={() => window.location.href = 'mailto:support@cortisense.com'}
        >
          <div className="flex items-center">
            <Mail className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Contact Support</span>
          </div>
        </button>

        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors"
          onClick={() => alert('FAQs coming soon.')}
        >
          <div className="flex items-center">
            <Info className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">FAQs</span>
          </div>
        </button>

      </div>

      <div className="mt-auto pb-8 text-center">
        <p className="text-slate-500 text-sm">© 2026 CortiSense. All rights reserved.</p>
      </div>

    </div>
  );
}


