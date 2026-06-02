"use client";

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ArrowLeft, AlertTriangle } from 'lucide-react';
import { getCheckinDetails, completeAction, ActionItem } from '@/lib/api';
import MainLayout from '@/components/Layout/MainLayout';

export default function HistoryDetailScreen() {
  const router = useRouter();
  const params = useParams();
  const checkinId = params.id as string;
  
  const [details, setDetails] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actions, setActions] = useState<ActionItem[]>([]);

  const handleCompleteAction = async (actionId: string) => {
    try {
      setActions(prev => prev.filter(a => a.id !== actionId));
      if (details?.id) {
        await completeAction(details.id, actionId);
      }
    } catch (e) {
      console.error('Failed to complete action', e);
    }
  };

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    if (!checkinId) return;

    getCheckinDetails(parseInt(checkinId))
      .then(data => {
        setDetails(data);
        if (data.actions) {
          setActions(data.actions.filter((a: ActionItem) => !a.is_done));
        }
      })
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
    const dateStr = String(details.timestamp).endsWith('Z') ? String(details.timestamp) : String(details.timestamp) + 'Z';
    const dateObj = new Date(dateStr);
    const dateFormatted = dateObj.toISOString().split('T')[0];
    const timeFormatted = dateObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', hour12: false });

    return (
      <div className="space-y-6 pb-20 pt-2">
        {/* Header Navigation */}
        <div className="flex items-center gap-4 mb-8">
          <button onClick={() => router.back()} className="text-white hover:text-slate-300 transition-colors mt-1">
            <ArrowLeft size={28} />
          </button>
          <h2 className="text-[28px] font-bold text-white">Analysis Detail</h2>
        </div>

        {/* Main Score Card */}
        <div className="bg-[#1c1e2b] rounded-[1.5rem] p-6 flex flex-col justify-center">
          <h3 className="text-[28px] font-bold text-slate-400 mb-2">{details.stress_level}</h3>
          <p className="text-slate-500 font-bold text-base mb-4">Score: {details.score}/100</p>
          <p className="text-slate-500 text-sm font-bold">{dateFormatted} at {timeFormatted}</p>
        </div>

        <div className="pt-2">
          <h3 className="text-white font-bold text-[22px] mb-4">AI Recommendation</h3>
          
          <div className="bg-[#1c1e2b] rounded-[1.5rem] p-6 shadow-sm mb-4 border border-[#2a2d3c]">
            <p className="text-white text-[15px] font-medium leading-relaxed">
              {details.recommendation || "Maintain your current healthy routines and continue mindfulness practices."}
            </p>
          </div>

          {details.score >= 50 && (
            <div className="bg-rose-500/10 border border-rose-500/20 rounded-[1.5rem] p-6 flex items-center gap-4">
              <AlertTriangle className="text-rose-500 shrink-0" size={28} />
              <p className="text-rose-500 font-bold text-[15px]">Escalation protocol was active for this session.</p>
            </div>
          )}
        </div>

        {/* Detailed Metrics Breakdown */}
        <div className="pt-4">
          <h3 className="text-white font-bold text-[22px] mb-6">Detailed Metrics</h3>
          
          <div className="mb-6">
            <h4 className="text-slate-400 text-base font-bold mb-3 px-1">Personal</h4>
            <div className="bg-[#1c1e2b] rounded-[1.5rem] p-5 space-y-4">
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Age</span><span className="text-white font-bold text-[15px]">{details.age}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Gender</span><span className="text-white font-bold text-[15px]">{details.gender}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Occupation</span><span className="text-white font-bold text-[15px]">{details.occupation}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Marital Status</span><span className="text-white font-bold text-[15px]">{details.marital_status}</span></div>
            </div>
          </div>

          <div className="mb-6">
            <h4 className="text-slate-400 text-base font-bold mb-3 px-1">Sleep</h4>
            <div className="bg-[#1c1e2b] rounded-[1.5rem] p-5 space-y-4">
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Duration</span><span className="text-white font-bold text-[15px]">{details.sleep_duration} hrs</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Quality</span><span className="text-white font-bold text-[15px]">{details.sleep_quality}/5</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Wake/Bed</span><span className="text-white font-bold text-[15px]">{details.wake_up_time} / {details.bed_time}</span></div>
            </div>
          </div>

          <div className="mb-6">
            <h4 className="text-slate-400 text-base font-bold mb-3 px-1">Lifestyle</h4>
            <div className="bg-[#1c1e2b] rounded-[1.5rem] p-5 space-y-4">
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Activity Level</span><span className="text-white font-bold text-[15px]">{details.physical_activity}/5</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Screen Time</span><span className="text-white font-bold text-[15px]">{details.screen_time} hrs</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Caffeine/Alcohol</span><span className="text-white font-bold text-[15px]">{details.caffeine_intake} / {details.alcohol_intake}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Smoking</span><span className="text-white font-bold text-[15px]">{details.smoking_habit}</span></div>
            </div>
          </div>

          <div className="mb-6">
            <h4 className="text-slate-400 text-base font-bold mb-3 px-1">Work & Routine</h4>
            <div className="bg-[#1c1e2b] rounded-[1.5rem] p-5 space-y-4">
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Work Hours</span><span className="text-white font-bold text-[15px]">{details.work_hours} hrs</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Travel Time</span><span className="text-white font-bold text-[15px]">{details.travel_time} hrs</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Social Score</span><span className="text-white font-bold text-[15px]">{details.social_interactions}/5</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Workload</span><span className="text-white font-bold text-[15px]">{details.workload}</span></div>
            </div>
          </div>

          <div className="mb-6">
            <h4 className="text-slate-400 text-base font-bold mb-3 px-1">Health & Wellness</h4>
            <div className="bg-[#1c1e2b] rounded-[1.5rem] p-5 space-y-4">
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Meditation</span><span className="text-white font-bold text-[15px]">{details.meditation_practice} mins</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Exercise Type</span><span className="text-white font-bold text-[15px]">{details.exercise_type}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Blood Pressure</span><span className="text-white font-bold text-[15px]">{details.blood_pressure}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Blood Sugar</span><span className="text-white font-bold text-[15px]">{details.blood_sugar_level} mg/dL</span></div>
            </div>
          </div>

          <div className="mb-6">
            <h4 className="text-slate-400 text-base font-bold mb-3 px-1">Mental State</h4>
            <div className="bg-[#1c1e2b] rounded-[1.5rem] p-5 space-y-4">
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Mood</span><span className="text-white font-bold text-[15px]">{details.mood}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Anxiety</span><span className="text-white font-bold text-[15px]">{details.anxiety}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Body Feeling</span><span className="text-white font-bold text-[15px]">{details.body_feeling}</span></div>
              <div className="flex justify-between items-center"><span className="text-slate-400 text-[15px] font-bold">Caffeine Dependency</span><span className="text-white font-bold text-[15px]">{details.caffeine_dependency}</span></div>
            </div>
          </div>
        </div>

      </div>
    );
  };

  return (
    <MainLayout activeTab="Profile">
      <div className="px-6 h-full bg-[#11131c]">
        {renderContent()}
      </div>
    </MainLayout>
  );
}
