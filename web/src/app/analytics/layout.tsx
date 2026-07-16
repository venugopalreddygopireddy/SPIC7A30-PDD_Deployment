"use client";

import React from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { Share2 } from 'lucide-react';
import MainLayout from '@/components/Layout/MainLayout';

export default function AnalyticsLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();

  const tabs = [
    { name: 'Trends', path: '/analytics/trends' },
    { name: 'Weekly', path: '/analytics/weekly' },
    { name: 'Monthly', path: '/analytics/monthly' },
    { name: 'Factors', path: '/analytics/factors' },
  ];

  const isExportPage = pathname === '/analytics/export';

  return (
    <MainLayout activeTab="Analytics">
      <div className="flex-1 overflow-y-auto bg-[#050810] min-h-full">
        {isExportPage ? (
          <div className="h-full">
            {children}
          </div>
        ) : (
          <div className="p-6">
          <div className="flex justify-between items-center mb-6 mt-4">
            <h2 className="text-2xl font-bold text-white">Insights</h2>
            <button 
              onClick={() => router.push('/analytics/export')}
              className="w-10 h-10 flex items-center justify-center bg-slate-800/50 rounded-xl hover:bg-slate-700 transition-colors"
            >
              <Share2 size={18} className="text-slate-200" />
            </button>
          </div>
          
          {/* Elegant Sub-tabs */}
          <div className="flex gap-6 overflow-x-auto pb-4 mb-6 border-b border-slate-800/50 scrollbar-hide">
            {tabs.map((tab) => (
              <button 
                key={tab.name}
                onClick={() => router.push(tab.path)}
                className={`text-base font-bold whitespace-nowrap pb-2 border-b-[3px] transition-all ${
                  pathname === tab.path || (pathname === '/analytics' && tab.name === 'Trends') 
                    ? 'border-emerald-500 text-white' 
                    : 'border-transparent text-slate-500 hover:text-slate-300'
                }`}
              >
                {tab.name}
              </button>
            ))}
          </div>

          <div className="mt-4">
            {children}
          </div>
        </div>
        )}
      </div>
    </MainLayout>
  );
}
