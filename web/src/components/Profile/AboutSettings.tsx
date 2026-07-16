import React, { useState } from 'react';
import { ArrowLeft, Info, FileText, Mail, Star, Heart, ChevronRight, X, TrendingUp, Leaf, Bell, Headphones } from 'lucide-react';

interface Props {
  onBack: () => void;
  onNavigateToPrivacy: () => void;
}

export default function AboutSettings({ onBack, onNavigateToPrivacy }: Props) {
  const [showWhatsNew, setShowWhatsNew] = useState(false);
  const [showTerms, setShowTerms] = useState(false);
  const [showContact, setShowContact] = useState(false);

  const handleRateUs = () => {
    const isWindows = window.navigator.userAgent.toLowerCase().includes("win");
    if (isWindows) {
      window.open('ms-windows-store://home', '_blank');
    } else {
      window.open('https://play.google.com/store/apps', '_blank');
    }
  };

  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
      
      {/* What's New Modal */}
      {showWhatsNew && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/60 backdrop-blur-sm">
          <div className="bg-[#f0edf5] p-6 rounded-[2rem] w-full max-w-sm relative shadow-2xl overflow-y-auto max-h-[90vh]">
            <h2 className="text-[22px] font-medium text-[#2b354d] mb-6 flex items-center gap-2">
              What's New in v1.1.0 🚀
            </h2>
            
            <div className="space-y-6">
              <div className="flex gap-4">
                <div className="w-10 h-10 rounded-full bg-slate-200/50 flex items-center justify-center shrink-0">
                  <TrendingUp className="text-[#64748b]" size={20} />
                </div>
                <div>
                  <h3 className="font-bold text-[#2b354d] text-[15px] mb-1 leading-tight">Personalized Stress Insights</h3>
                  <p className="text-[#566076] text-[13px] leading-relaxed">
                    CortiSense AI now delivers deeper analysis of your daily check-ins to identify hidden stress patterns.
                  </p>
                </div>
              </div>

              <div className="flex gap-4">
                <div className="w-10 h-10 rounded-full bg-slate-200/50 flex items-center justify-center shrink-0">
                  <Leaf className="text-[#64748b]" size={20} />
                </div>
                <div>
                  <h3 className="font-bold text-[#2b354d] text-[15px] mb-1 leading-tight">New Stress Relief Tasks</h3>
                  <p className="text-[#566076] text-[13px] leading-relaxed">
                    Earn coins by completing daily tasks like Deep Breathing, Walking, and Hydration to reach your wellness goals.
                  </p>
                </div>
              </div>

              <div className="flex gap-4">
                <div className="w-10 h-10 rounded-full bg-slate-200/50 flex items-center justify-center shrink-0">
                  <Bell className="text-[#64748b]" size={20} />
                </div>
                <div>
                  <h3 className="font-bold text-[#2b354d] text-[15px] mb-1 leading-tight">Smart Reminders</h3>
                  <p className="text-[#566076] text-[13px] leading-relaxed">
                    Never miss a check-in or task with our new adaptive notification system.
                  </p>
                </div>
              </div>

              <div className="flex gap-4">
                <div className="w-10 h-10 rounded-full bg-slate-200/50 flex items-center justify-center shrink-0">
                  <Headphones className="text-[#64748b]" size={20} />
                </div>
                <div>
                  <h3 className="font-bold text-[#2b354d] text-[15px] mb-1 leading-tight">Expanded Support</h3>
                  <p className="text-[#566076] text-[13px] leading-relaxed">
                    Need help? You can now instantly reach our support team via WhatsApp, Email, or Phone.
                  </p>
                </div>
              </div>
            </div>

            <button 
              onClick={() => setShowWhatsNew(false)}
              className="w-full mt-8 text-[#98b89d] font-medium text-lg py-2"
            >
              Great!
            </button>
          </div>
        </div>
      )}

      {/* Terms Modal */}
      {showTerms && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/60 backdrop-blur-sm">
          <div className="bg-[#f0edf5] p-6 rounded-[2rem] w-full max-w-sm relative shadow-2xl overflow-y-auto max-h-[90vh]">
            <h2 className="text-[22px] font-medium text-[#2b354d] mb-4 flex items-center gap-2">
              Terms & Privacy 🔒
            </h2>
            <p className="text-[#566076] text-xs mb-4">Effective Date: May 26, 2026</p>
            
            <div className="space-y-4">
              <div>
                <h3 className="font-bold text-[#2b354d] text-[15px] mb-1">1. Introduction</h3>
                <p className="text-[#566076] text-[13px] leading-relaxed">
                  Welcome to CortiSense. By using our app, you agree to these Terms and Conditions. Please read them carefully. CortiSense is designed to help you track stress and well-being.
                </p>
              </div>

              <div>
                <h3 className="font-bold text-[#2b354d] text-[15px] mb-1">2. Health Disclaimer</h3>
                <p className="text-[#566076] text-[13px] leading-relaxed">
                  CortiSense is not a medical device. The insights, suggestions, and AI chat provided are for informational purposes only and do not constitute professional medical advice, diagnosis, or treatment.
                </p>
              </div>

              <div>
                <h3 className="font-bold text-[#2b354d] text-[15px] mb-1">3. Data Privacy & Security</h3>
                <p className="text-[#566076] text-[13px] leading-relaxed">
                  We take your privacy seriously. Your clinical history, mood records, and chat logs are stored securely. We do not sell your personal data to third parties. For AI features, anonymized text may be processed securely.
                </p>
              </div>

              <div>
                <h3 className="font-bold text-[#2b354d] text-[15px] mb-1">4. Premium Services</h3>
                <p className="text-[#566076] text-[13px] leading-relaxed">
                  Premium features can be unlocked using in-app coins. Coins are earned through daily activity. Virtual currency holds no real-world monetary value and cannot be exchanged for cash.
                </p>
              </div>

              <div>
                <h3 className="font-bold text-[#2b354d] text-[15px] mb-1">5. User Conduct</h3>
                <p className="text-[#566076] text-[13px] leading-relaxed">
                  You agree to use CortiSense for lawful purposes only. Misuse of the AI support system or exploiting coin mechanisms is strictly prohibited.
                </p>
              </div>

              <div>
                <h3 className="font-bold text-[#2b354d] text-[15px] mb-1">6. Changes to Terms</h3>
                <p className="text-[#566076] text-[13px] leading-relaxed">
                  We reserve the right to modify these terms at any time. Continued use of the app signifies your acceptance of any updated terms.
                </p>
              </div>
            </div>

            <button 
              onClick={() => setShowTerms(false)}
              className="w-full mt-6 text-[#98b89d] font-medium text-lg py-2 text-right"
            >
              I Understand
            </button>
          </div>
        </div>
      )}

      {/* Contact Support Modal */}
      {showContact && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-6 bg-black/60 backdrop-blur-sm">
          <div className="bg-[#f0edf5] p-6 rounded-[2rem] w-full max-w-sm relative shadow-2xl flex flex-col items-center">
            <h2 className="text-[22px] font-medium text-[#2b354d] mb-6 self-start">
              Contact Support
            </h2>
            
            <button 
              onClick={() => { window.location.href = 'mailto:support@cortisense.com'; setShowContact(false); }}
              className="text-[#98b89d] font-medium text-lg py-3 w-full text-center hover:bg-slate-200/50 transition-colors"
            >
              Mail Support
            </button>
            <button 
              onClick={() => { window.open('https://wa.me/917013995242', '_blank'); setShowContact(false); }}
              className="text-[#98b89d] font-medium text-lg py-3 w-full text-center hover:bg-slate-200/50 transition-colors"
            >
              WhatsApp Chat
            </button>
            <button 
              onClick={() => { window.location.href = 'tel:7013995242'; setShowContact(false); }}
              className="text-[#98b89d] font-medium text-lg py-3 w-full text-center hover:bg-slate-200/50 transition-colors"
            >
              Call Support
            </button>
            <button 
              onClick={() => { window.location.href = 'sms:7013995242'; setShowContact(false); }}
              className="text-[#98b89d] font-medium text-lg py-3 w-full text-center hover:bg-slate-200/50 transition-colors"
            >
              Need Help
            </button>

            <button 
              onClick={() => setShowContact(false)}
              className="mt-4 text-[#98b89d] font-medium text-lg py-2 w-full text-right"
            >
              Close
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
        <div className="w-20 h-20 bg-[#f0edf5] rounded-3xl flex items-center justify-center mb-4">
           <Heart className="text-[#64748b] fill-[#64748b]" size={36} />
        </div>
        <h1 className="text-2xl font-bold text-white mb-1">CortiSense</h1>
        <p className="text-slate-400 text-sm mb-4">Version 1.1.0</p>
      </div>

      <div className="bg-slate-900/50 border border-slate-800 rounded-[2rem] mb-10 overflow-hidden">
        
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
          onClick={() => setShowTerms(true)}
        >
          <div className="flex items-center">
            <FileText className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Terms & Privacy</span>
          </div>
          <ChevronRight className="text-slate-500" size={18} />
        </button>
        
        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50"
          onClick={() => setShowContact(true)}
        >
          <div className="flex items-center">
            <Mail className="text-slate-400 mr-4" size={20} />
            <span className="text-white font-medium">Contact Support</span>
          </div>
          <ChevronRight className="text-slate-500" size={18} />
        </button>

        <button 
          className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors"
          onClick={handleRateUs}
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
