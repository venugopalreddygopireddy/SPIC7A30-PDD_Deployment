"use client";

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ArrowLeft, Clock, Activity, Calendar, CheckCircle2 } from 'lucide-react';
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

        {/* Detailed Metrics Breakdown */}
        <div className="space-y-6">
          <h3 className="text-white font-bold text-lg px-2">Detailed Metrics</h3>
          
          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Personal</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Age</span><span className="text-white font-semibold text-sm">{details.age}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Gender</span><span className="text-white font-semibold text-sm">{details.gender}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Occupation</span><span className="text-white font-semibold text-sm">{details.occupation}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Marital Status</span><span className="text-white font-semibold text-sm">{details.marital_status}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Sleep</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Duration</span><span className="text-white font-semibold text-sm">{details.sleep_duration} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Quality</span><span className="text-white font-semibold text-sm">{details.sleep_quality}/5</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Wake/Bed</span><span className="text-white font-semibold text-sm">{details.wake_up_time} / {details.bed_time}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Lifestyle</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Activity Level</span><span className="text-white font-semibold text-sm">{details.physical_activity}/5</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Screen Time</span><span className="text-white font-semibold text-sm">{details.screen_time} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Caffeine/Alcohol</span><span className="text-white font-semibold text-sm">{details.caffeine_intake} / {details.alcohol_intake}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Smoking</span><span className="text-white font-semibold text-sm">{details.smoking_habit}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Work & Routine</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Work Hours</span><span className="text-white font-semibold text-sm">{details.work_hours} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Travel Time</span><span className="text-white font-semibold text-sm">{details.travel_time} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Social Score</span><span className="text-white font-semibold text-sm">{details.social_interactions}/5</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Workload</span><span className="text-white font-semibold text-sm">{details.workload}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Health & Wellness</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Meditation</span><span className="text-white font-semibold text-sm">{details.meditation_practice} mins</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Exercise Type</span><span className="text-white font-semibold text-sm">{details.exercise_type}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Blood Pressure</span><span className="text-white font-semibold text-sm">{details.blood_pressure}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Blood Sugar</span><span className="text-white font-semibold text-sm">{details.blood_sugar_level} mg/dL</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Mental State</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Mood</span><span className="text-white font-semibold text-sm">{details.mood}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Anxiety</span><span className="text-white font-semibold text-sm">{details.anxiety}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Body Feeling</span><span className="text-white font-semibold text-sm">{details.body_feeling}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Caffeine Dependency</span><span className="text-white font-semibold text-sm">{details.caffeine_dependency}</span></div>
          </div>
        </div>

        {/* Actionable Recommendations */}
        {actions.length > 0 ? (
          <div className="w-full mb-8">
            <h3 className="text-white font-bold text-xl mb-4 px-1">Pending Actions</h3>
            <div className="space-y-4">
              {actions.map((act) => (
                <div key={act.id} className="bg-[#1C2030] border border-slate-700/50 rounded-2xl p-5 flex flex-col gap-3">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                      <CheckCircle2 size={20} />
                    </div>
                    <div className="flex-1">
                      <h4 className="text-slate-100 font-bold text-base">{act.title}</h4>
                      <p className="text-slate-400 text-sm mt-1 leading-relaxed">{act.description}</p>
                    </div>
                  </div>
                  <button 
                    onClick={() => handleCompleteAction(act.id)}
                    className="mt-2 w-full bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 text-emerald-400 font-semibold py-2.5 rounded-xl transition-colors"
                  >
                    Mark as Done
                  </button>
                </div>
              ))}
            </div>
          </div>
        ) : details.recommendation ? (
          <div className="bg-[#4a3b7a] rounded-[24px] p-6 shadow-lg">
            <h3 className="text-white font-bold text-lg mb-3">AI Analysis</h3>
            <p className="text-indigo-100/90 text-sm leading-relaxed">
              {details.recommendation}
            </p>
          </div>
        ) : null}
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
