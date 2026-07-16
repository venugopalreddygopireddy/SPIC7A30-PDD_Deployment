"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/Layout/MainLayout';
import { getHistory, StressCheckInResponse } from '@/lib/api';
import { ArrowLeft } from 'lucide-react';

export default function HistoryListScreen() {
  const router = useRouter();
  const [history, setHistory] = useState<StressCheckInResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    getHistory()
      .then(data => {
        setHistory(data);
      })
      .catch(err => {
        // @ts-ignore
        if (err.response?.status === 401) {
          localStorage.removeItem('jwtToken');
          router.push('/welcome');
        }
      })
      .finally(() => setLoading(false));
  }, [router]);

  return (
    <MainLayout activeTab="Profile">
      <div className="flex-1 overflow-y-auto px-6 py-6 h-full bg-[#050810]">
        <div className="flex items-center mb-6">
          <button onClick={() => router.push('/profile')} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
            <ArrowLeft size={24} />
          </button>
          <h1 className="text-xl font-bold text-white ml-2">Clinical History</h1>
        </div>

        {loading ? (
          <div className="text-center py-20 text-slate-500">Loading history...</div>
        ) : (
          <div className="space-y-4">
            {history.length > 0 ? (
              history.map((item) => (
                <div 
                  key={item.id} 
                  onClick={() => router.push(`/history/${item.id}`)}
                  className="bg-slate-900/50 border border-slate-800 p-4 rounded-2xl flex justify-between items-center hover:bg-slate-800/50 transition-colors cursor-pointer"
                >
                  <div>
                    <p className="text-white font-bold">{new Date(item.timestamp).toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' })}</p>
                    <p className="text-xs text-slate-400 mt-1">{new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-2xl font-extrabold text-white">{item.score}</p>
                    <p className={`text-[10px] font-bold uppercase mt-1 ${item.score >= 80 ? 'text-rose-500' : item.score >= 60 ? 'text-orange-500' : item.score >= 40 ? 'text-yellow-500' : 'text-emerald-500'}`}>
                      {item.stress_level}
                    </p>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-slate-500 text-center py-10">No history available yet.</p>
            )}
          </div>
        )}
      </div>
    </MainLayout>
  );
}
