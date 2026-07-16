import React, { useState } from 'react';
import { ArrowLeft, Info, FileText, Mail, Star, Heart, ChevronRight, X } from 'lucide-react';

interface Props {
  onBack: () => void;
  onNavigateToPrivacy: () => void;
}

export default function AboutSettings({ onBack, onNavigateToPrivacy }: Props) {
  const [showWhatsNew, setShowWhatsNew] = useState(false);

  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
      {showWhatsNew && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/60 backdrop-blur-sm">
          <div className="bg-slate-900 border border-slate-800 p-6 rounded-3xl w-full max-w-sm relative shadow-2xl">
            <button 
              onClick={() => setShowWhatsNew(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-white"
            >
              <X size={24} />
            </button>
            <h2 className="text-xl font-bold text-white mb-4">What's New in 1.1.0</h2>
            <ul className="text-slate-300 text-sm space-y-3 list-disc pl-5">
              <li>Added comprehensive profile settings.</li>
              <li>New clinical history and check-in tracking.</li>
              <li>Performance improvements and bug fixes.</li>
              <li>Enhanced AI stress predictions.</li>
            </ul>
            <button 
              onClick={() => setShowWhatsNew(false)}
              className="w-full mt-6 bg-emerald-500 text-[#050810] font-bold py-3 rounded-xl hover:bg-emerald-400 transition-colors"
            >
              Got it
            </button>
          </div>
        </div>
      )}

      <div className="flex items-center mb-6">
        <button onClick={onBack} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
          <ArrowLeft size={24} />
        </button>
      </div>
      
      <div className="flex flex-col items-center mb-8">
        <div className="w-20 h-20 bg-emerald-500/20 rounded-3xl flex items-center justify-center mb-4 border border-emerald-500/30">
           <Heart className="text-emerald-500 fill-emerald-500" size={32} />
        </div>
        <h1 className="text-2xl font-bold text-white mb-1">CortiSense</h1>
        <p className="text-slate-400 text-sm mb-4">Version 1.1.0</p>
      </div>

      <div className="bg-slate-900/50 border border-slate-800 p-5 rounded-3xl mb-6">
        <h3 className="text-white font-bold text-lg mb-3">About CortiSense</h3>
        <p className="text-slate-400 text-sm leading-relaxed">
          CortiSense is an AI-powered stress monitoring application that helps you predict and prevent burnout by analyzing your physiological, psychological, and lifestyle data.
        </p>
      </div>

      <div className="bg-slate-900/50 border border-slate-800 rounded-3xl mb-10 overflow-hidden">
        
        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={() => setShowWhatsNew(true)}
        >
          <div className="flex items-center">
            <Info className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">What's New</span>
          </div>
          <ChevronRight className="text-slate-500" size={18} />
        </button>

        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={onNavigateToPrivacy}
        >
          <div className="flex items-center">
            <FileText className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Terms & Privacy</span>
          </div>
          <ChevronRight className="text-slate-500" size={18} />
        </button>
        
        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={() => window.location.href = 'mailto:support@cortisense.com'}
        >
          <div className="flex items-center">
            <Mail className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Contact Support</span>
          </div>
          <ChevronRight className="text-slate-500" size={18} />
        </button>

        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors"
          onClick={() => window.open('https://play.google.com', '_blank')}
        >
          <div className="flex items-center">
            <Star className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Rate Us</span>
          </div>
          <ChevronRight className="text-slate-500" size={18} />
        </button>

      </div>

    </div>
  );
}
