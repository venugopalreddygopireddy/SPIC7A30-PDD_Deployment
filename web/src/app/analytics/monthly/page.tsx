"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Calendar, TrendingUp } from 'lucide-react';
import { getMonthlyAnalytics, MonthlyAnalyticsResponse } from '@/lib/api';

export default function MonthlyAnalyticsScreen() {
  const router = useRouter();
  const [monthlyData, setMonthlyData] = useState<MonthlyAnalyticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    getMonthlyAnalytics()
      .then(data => setMonthlyData(data))
      .catch(err => {
        if (err.response?.status === 401) {
          localStorage.removeItem('jwtToken');
          router.push('/welcome');
        } else {
          setError('Failed to load monthly analytics. Please try again.');
        }
      })
      .finally(() => setLoading(false));
  }, [router]);

  if (loading) return <div className="text-center py-10 text-slate-500">Loading Monthly Data...</div>;
  if (error) return <div className="text-center py-10 text-rose-500">{error}</div>;
  if (!monthlyData) return <div className="text-center py-10 text-slate-500">No monthly data available yet.</div>;
  
  const lowDays = monthlyData.distribution.low;
  const moderateDays = monthlyData.distribution.moderate;
  const highDays = monthlyData.distribution.high;
  const total = monthlyData.total_checkins;
  const lowPercent = total > 0 ? Math.round((lowDays * 100) / total) : 0;
  
  return (
    <div className="space-y-6 pb-20">
      <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-6 flex flex-col items-center text-center">
        <p className="text-slate-400 text-xs mb-2 uppercase tracking-wider font-bold">Average Stress Score</p>
        <span className="text-5xl font-extrabold text-white mb-2">{monthlyData.avg_score}</span>
        <div className="flex items-center text-emerald-400 text-sm font-medium">
          <TrendingUp size={16} className="mr-1 rotate-180" />
          Calculated from {total} check-ins
        </div>
      </div>
      
      <div className="grid grid-cols-3 gap-3">
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-4 flex flex-col items-start">
          <div className="text-emerald-500 mb-2"><Calendar size={16} /></div>
          <span className="text-white font-bold text-xl">{total}</span>
          <span className="text-slate-400 text-[10px] uppercase font-bold mt-1">Check-ins</span>
        </div>
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-4 flex flex-col items-start">
          <div className="text-emerald-500 mb-2"><TrendingUp size={16} className="rotate-180" /></div>
          <span className="text-white font-bold text-xl">
            {Object.values(monthlyData.calendar_activity).length > 0 ? Math.min(...Object.values(monthlyData.calendar_activity)) : 0}
          </span>
          <span className="text-slate-400 text-[10px] uppercase font-bold mt-1">Lowest Score</span>
        </div>
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-4 flex flex-col items-start">
          <div className="text-rose-500 mb-2"><TrendingUp size={16} /></div>
          <span className="text-white font-bold text-xl">
            {Object.values(monthlyData.calendar_activity).length > 0 ? Math.max(...Object.values(monthlyData.calendar_activity)) : 0}
          </span>
          <span className="text-slate-400 text-[10px] uppercase font-bold mt-1">Highest Score</span>
        </div>
      </div>

      <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-6">
        <h3 className="text-white font-bold mb-4">Stress Distribution</h3>
        <div className="space-y-4">
          <div className="flex justify-between items-center text-sm">
            <div className="flex items-center gap-2"><div className="w-2 h-2 rounded-full bg-emerald-500"></div><span className="text-slate-200">Low</span></div>
            <span className="text-white font-bold">{lowDays} times</span>
          </div>
          <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden"><div className="h-full bg-emerald-500" style={{ width: `${total > 0 ? (lowDays/total)*100 : 0}%` }}></div></div>

          <div className="flex justify-between items-center text-sm">
            <div className="flex items-center gap-2"><div className="w-2 h-2 rounded-full bg-yellow-500"></div><span className="text-slate-200">Moderate</span></div>
            <span className="text-white font-bold">{moderateDays} times</span>
          </div>
          <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden"><div className="h-full bg-yellow-500" style={{ width: `${total > 0 ? (moderateDays/total)*100 : 0}%` }}></div></div>

          <div className="flex justify-between items-center text-sm">
            <div className="flex items-center gap-2"><div className="w-2 h-2 rounded-full bg-rose-500"></div><span className="text-slate-200">High</span></div>
            <span className="text-white font-bold">{highDays} times</span>
          </div>
          <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden"><div className="h-full bg-rose-500" style={{ width: `${total > 0 ? (highDays/total)*100 : 0}%` }}></div></div>
        </div>
      </div>

      <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-6">
        <h3 className="text-white font-bold mb-2">Monthly Achievement</h3>
        <p className="text-slate-400 text-sm leading-relaxed">
          {lowPercent}% of your days were marked with low stress. Keep up the good work and maintain your healthy habits.
        </p>
      </div>
    </div>
  );
}
