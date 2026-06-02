"use client";

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Home, BarChart3, PlusCircle, User } from 'lucide-react';
import { useTranslation } from '@/components/TranslationProvider';

export default function MainLayout({ children, activeTab }: { children: React.ReactNode, activeTab: string }) {
  const router = useRouter();
  const { t } = useTranslation();

  useEffect(() => {
    let timeoutId: NodeJS.Timeout;
    const resetTimer = () => {
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        localStorage.removeItem('jwtToken');
        router.push('/login?expired=true');
      }, 5 * 60 * 1000); // 5 minutes
    };

    const events = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart'];
    events.forEach(event => document.addEventListener(event, resetTimer));
    resetTimer();

    return () => {
      clearTimeout(timeoutId);
      events.forEach(event => document.removeEventListener(event, resetTimer));
    };
  }, [router]);

  return (
    <div className="min-h-screen bg-[#050810] text-slate-200 font-sans flex overflow-hidden">
      <div className="w-full h-screen flex flex-col md:flex-row relative bg-[#050810]">
        
        {/* Desktop Side Navigation */}
        <div className="hidden md:flex flex-col w-[300px] lg:w-[350px] border-r border-slate-800 p-6 pt-12 space-y-8 h-full bg-slate-900/30">
          <div className="flex items-center gap-3 mb-8 cursor-pointer" onClick={() => router.push('/')}>
            <div className="w-10 h-10 rounded-full flex items-center justify-center overflow-hidden border border-emerald-500/20">
              <img src="/logo.png" alt="CortiSense Logo" className="w-full h-full object-cover" />
            </div>
            <h1 className="text-xl font-bold text-white tracking-wider">CortiSense</h1>
          </div>
          
          <button onClick={() => router.push('/')} className={`flex items-center gap-4 p-4 rounded-2xl transition-all ${activeTab === 'Home' ? 'bg-emerald-500/10 text-emerald-400' : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/50'}`}>
            <Home size={24} className={activeTab === 'Home' ? 'fill-emerald-400/20' : ''} />
            <span className="font-bold text-lg">{t('Home')}</span>
          </button>
          
          <button onClick={() => router.push('/analytics')} className={`flex items-center gap-4 p-4 rounded-2xl transition-all ${activeTab === 'Analytics' ? 'bg-emerald-500/10 text-emerald-400' : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/50'}`}>
            <BarChart3 size={24} className={activeTab === 'Analytics' ? 'fill-emerald-400/20' : ''} />
            <span className="font-bold text-lg">{t('Analytics')}</span>
          </button>

          <button onClick={() => router.push('/checkin')} className="flex items-center gap-4 p-4 rounded-2xl text-slate-500 hover:text-slate-300 hover:bg-slate-800/50 transition-all">
            <PlusCircle size={24} />
            <span className="font-bold text-lg">{t('Check-in')}</span>
          </button>
          
          <button onClick={() => router.push('/profile')} className={`flex items-center gap-4 p-4 rounded-2xl transition-all ${activeTab === 'Profile' ? 'bg-emerald-500/10 text-emerald-400' : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/50'}`}>
            <User size={24} className={activeTab === 'Profile' ? 'fill-emerald-400/20' : ''} />
            <span className="font-bold text-lg">{t('Profile')}</span>
          </button>
        </div>

        {/* Dynamic Content */}
        <div className="flex-1 h-full overflow-y-auto bg-[#0a0f1c] relative">
          <div className="w-full max-w-4xl mx-auto pb-24 md:pb-12">
            {children}
          </div>
        </div>

        {/* Mobile Bottom Navigation Bar */}
        <div className="md:hidden absolute bottom-0 left-0 w-full bg-[#050810] border-t border-slate-800 px-6 py-4 flex justify-between items-center z-50">
          <button 
            onClick={() => router.push('/')}
            className={`flex flex-col items-center gap-1 ${activeTab === 'Home' ? 'text-emerald-400' : 'text-slate-500 hover:text-slate-400'}`}
          >
            <Home size={24} className={activeTab === 'Home' ? 'fill-emerald-400/20' : ''} />
            <span className="text-[10px] font-bold">{t('Home')}</span>
          </button>
          
          <button 
            onClick={() => router.push('/analytics')}
            className={`flex flex-col items-center gap-1 ${activeTab === 'Analytics' ? 'text-emerald-400' : 'text-slate-500 hover:text-slate-400'}`}
          >
            <BarChart3 size={24} className={activeTab === 'Analytics' ? 'fill-emerald-400/20' : ''} />
            <span className="text-[10px] font-bold">{t('Analytics')}</span>
          </button>
          
          <button 
            onClick={() => router.push('/checkin')}
            className="flex flex-col items-center gap-1 text-slate-500 hover:text-slate-400"
          >
            <PlusCircle size={24} />
            <span className="text-[10px] font-bold">{t('Check-in')}</span>
          </button>
          
          <button 
            onClick={() => router.push('/profile')}
            className={`flex flex-col items-center gap-1 ${activeTab === 'Profile' ? 'text-emerald-400' : 'text-slate-500 hover:text-slate-400'}`}
          >
            <User size={24} className={activeTab === 'Profile' ? 'fill-emerald-400/20' : ''} />
            <span className="text-[10px] font-bold">{t('Profile')}</span>
          </button>
        </div>

      </div>
    </div>
  );
}
