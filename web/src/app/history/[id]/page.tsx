"use client";

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ArrowLeft, Clock, Activity, Calendar } from 'lucide-react';
import { getCheckinDetails } from '@/lib/api';
import MainLayout from '@/components/Layout/MainLayout';

export default function HistoryDetailScreen() {
  const router = useRouter();
  const params = useParams();
  const checkinId = params.id as string;
  
  const [details, setDetails] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    if (!checkinId) return;

    getCheckinDetails(parseInt(checkinId))
      .then(data => setDetails(data))
      .catch(err => {
        if (err.response?.status === 401) {
          localStorage.removeItem('jwtToken');
          router.push('/welcome');
        } else {
          setError('Failed to load check-in details.');
        }
      })
      .finally(() => setLoading(false));
  }, [checkinId, router]);

  const renderContent = () => {
    if (loading) return <div className="text-center py-20 text-slate-500">Loading details...</div>;
    if (error) return <div className="text-center py-20 text-rose-500">{error}</div>;
    if (!details) return <div className="text-center py-20 text-slate-500">No details found.</div>;

    const stressColor = details.score >= 80 ? 'text-rose-500' : details.score >= 60 ? 'text-orange-500' : details.score >= 40 ? 'text-yellow-500' : 'text-emerald-500';
    const stressBg = details.score >= 80 ? 'bg-rose-500/10' : details.score >= 60 ? 'bg-orange-500/10' : details.score >= 40 ? 'bg-yellow-500/10' : 'bg-emerald-500/10';
    const dateObj = new Date(details.timestamp);

    return (
      <div className="space-y-6 pb-20">
        {/* Header Navigation */}
        <div className="flex items-center gap-4 mb-6 pt-4">
          <button onClick={() => router.push('/analytics')} className="w-10 h-10 flex items-center justify-center bg-slate-800/50 hover:bg-slate-700 rounded-xl transition-colors">
            <ArrowLeft size={20} className="text-white" />
          </button>
          <h2 className="text-xl font-bold text-white">Check-in Details</h2>
        </div>

        {/* Main Score Card */}
        <div className={`${stressBg} border border-slate-800 rounded-3xl p-8 flex flex-col items-center text-center`}>
          <p className="text-slate-400 text-sm font-medium flex items-center gap-2 mb-6">
            <Calendar size={16} />
            {dateObj.toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}
            <span className="mx-2">•</span>
            <Clock size={16} />
            {dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </p>
          
          <div className="w-32 h-32 rounded-full border-8 border-slate-800 flex flex-col items-center justify-center mb-6 shadow-xl">
            <span className="text-4xl font-extrabold text-white">{details.score}</span>
          </div>
          
          <h3 className={`text-2xl font-bold ${stressColor} uppercase tracking-wider`}>{details.stress_level}</h3>
        </div>

        {/* Breakdown Factors */}
        <div className="bg-[#1e2132] border border-slate-800 rounded-[28px] p-6 shadow-lg">
          <div className="flex items-center gap-3 mb-6">
            <Activity size={20} className="text-emerald-400" />
            <h3 className="text-white font-bold text-lg">Reported Factors</h3>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Sleep</p>
              <p className="text-white font-semibold">{details.sleep_duration} hrs • {details.sleep_quality}/10</p>
            </div>
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Screen Time</p>
              <p className="text-white font-semibold">{details.screen_time} hrs</p>
            </div>
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Caffeine</p>
              <p className="text-white font-semibold">{details.caffeine_intake} cups</p>
            </div>
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Mood</p>
              <p className="text-white font-semibold">{details.mood}</p>
            </div>
          </div>
        </div>

        {/* AI Recommendation */}
        {details.recommendation && (
          <div className="bg-[#4a3b7a] rounded-[24px] p-6 shadow-lg">
            <h3 className="text-white font-bold text-lg mb-3">AI Analysis</h3>
            <p className="text-indigo-100/90 text-sm leading-relaxed">
              {details.recommendation}
            </p>
          </div>
        )}
      </div>
    );
  };

  return (
    <MainLayout activeTab="Analytics">
      <div className="px-6 h-full">
        {renderContent()}
      </div>
    </MainLayout>
  );
}
