"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, ChevronRight } from 'lucide-react';
import { getHistory } from '@/lib/api';
import MainLayout from '@/components/Layout/MainLayout';

export default function HealthHistoryScreen() {
  const router = useRouter();
  const [history, setHistory] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    getHistory(0, 1000).then(data => {
      setHistory(data);
    }).catch(e => {
      console.error(e);
    }).finally(() => {
      setLoading(false);
    });
  }, [router]);

  return (
    <MainLayout activeTab="Profile">
      <div className="flex-1 overflow-y-auto px-5 py-6 h-full bg-[#11131c]">
        {/* Header Navigation */}
        <div className="flex items-center gap-4 mb-8 pt-2">
          <button onClick={() => router.back()} className="text-white hover:text-slate-300 transition-colors">
            <ArrowLeft size={28} />
          </button>
          <h2 className="text-[28px] font-bold text-white">Health History</h2>
        </div>

        {loading ? (
          <div className="text-center py-20 text-slate-500 font-medium">Loading history...</div>
        ) : history.length === 0 ? (
          <div className="text-center py-20 text-slate-500 font-medium">No history found.</div>
        ) : (
          <div className="space-y-4 pb-20">
            {history.map((item, idx) => {
              const d = new Date(item.timestamp);
              // Extract date like 2026-06-01
              const dateStr = d.toISOString().split('T')[0];
              const yearMonth = dateStr.substring(0, 8); // "2026-06-"
              const day = dateStr.substring(8); // "01"

              const recSnippet = item.recommendation || "Maintain your current healthy routines and continue mindfulness practices.";

              return (
                <div 
                  key={item.id || idx}
                  onClick={() => router.push(`/history/${item.id}`)}
                  className="bg-[#1c1e2b] rounded-[1.5rem] p-5 flex items-center justify-between cursor-pointer hover:bg-[#252838] transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-[64px] h-[64px] rounded-full bg-[#2a2d3c] flex items-center justify-center shrink-0">
                      <span className="text-slate-400 font-bold text-[22px]">{item.score}</span>
                    </div>
                    <div className="flex flex-col justify-center">
                      <div className="flex items-start">
                        <span className="text-slate-400 font-bold text-[20px] mr-2">{item.stress_level}</span>
                        <span className="text-slate-400 font-bold text-[20px] mr-2">•</span>
                        <div className="text-slate-400 text-[13px] font-medium leading-tight mt-1">
                          <div>{yearMonth}</div>
                          <div>{day}</div>
                        </div>
                      </div>
                      <p className="text-slate-200 text-sm mt-2 line-clamp-1 truncate max-w-[220px] font-medium">
                        {recSnippet}
                      </p>
                    </div>
                  </div>
                  <ChevronRight size={24} className="text-white ml-2 shrink-0" />
                </div>
              );
            })}
          </div>
        )}
      </div>
    </MainLayout>
  );
}
