import React from 'react';
import { ArrowLeft, Shield, Download, Trash2 } from 'lucide-react';

interface Props {
  onBack: () => void;
}

export default function PrivacySettings({ onBack }: Props) {
  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
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
          onClick={() => alert('Data download started.')}
        >
          <Download className="text-slate-400 mr-4 shrink-0" size={24} />
          <div className="text-left">
            <span className="block font-bold text-lg text-white">Download My Data</span>
            <span className="text-slate-400 text-sm">Export all your data</span>
          </div>
        </button>

        <button 
          className="w-full flex items-center p-5 hover:bg-rose-500/10 transition-colors"
          onClick={() => {
             if (window.confirm('Are you sure you want to permanently delete your account? This action cannot be undone.')) {
               alert('Account deletion requested.');
             }
          }}
        >
          <Trash2 className="text-rose-500 mr-4 shrink-0" size={24} />
          <div className="text-left">
            <span className="block font-bold text-lg text-rose-500">Delete My Account</span>
            <span className="text-rose-500/70 text-sm">Permanently remove all data</span>
          </div>
        </button>

      </div>

    </div>
  );
}
