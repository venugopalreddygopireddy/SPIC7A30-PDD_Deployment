"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { LineChart, Moon, Smartphone, Coffee, Smile, Briefcase, Activity } from 'lucide-react';
import { getFactorsAnalytics, getHistory, FactorsResponse, StressCheckInResponse } from '@/lib/api';

export default function FactorsAnalyticsScreen() {
  const router = useRouter();
  const [factorsData, setFactorsData] = useState<FactorsResponse | null>(null);
  const [lastCheckin, setLastCheckin] = useState<StressCheckInResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    Promise.all([
      getFactorsAnalytics().catch(() => null),
      getHistory().catch(() => [])
    ]).then(([factors, hist]) => {
      if (factors) setFactorsData(factors);
      if (hist && hist.length > 0) setLastCheckin(hist[0]);
    }).catch(err => {
      setError('Failed to load factors data.');
    }).finally(() => setLoading(false));

  }, [router]);

  if (loading) return <div className="text-center py-10 text-slate-500">Loading Factors...</div>;
  if (error) return <div className="text-center py-10 text-rose-500">{error}</div>;
  if (!factorsData) return <div className="text-center py-10 text-slate-500">No factors data available yet.</div>;
  
  const FactorItem = ({ icon: Icon, title, impact, percentage, colorClass, bgClass }: any) => (
    <div className="flex items-start py-3">
      <Icon size={22} className="text-slate-300 mt-1 mr-4" />
      <div className="flex-1">
        <div className="flex justify-between items-center mb-1">
          <div className="text-white font-bold text-[15px]">{title}</div>
          <div className="text-white font-bold text-[14px]">{percentage}%</div>
        </div>
        <div className="text-slate-400 text-[11px] mb-3">{impact}</div>
        <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
          <div className={`h-full ${bgClass}`} style={{ width: `${percentage}%` }}></div>
        </div>
      </div>
    </div>
  );

  return (
    <div className="space-y-6 pb-20">
      <div className="text-white">
        <h2 className="text-[26px] font-extrabold mb-1">Factor Breakdown</h2>
        <p className="text-slate-400 text-sm">What influences your stress levels</p>
      </div>

      <div className="bg-[#1e2132] border border-slate-800/50 rounded-[32px] p-6 shadow-lg">
        <div className="flex items-center mb-4">
          <LineChart size={20} className="text-[#82e0aa] mr-3" />
          <h3 className="text-white font-bold text-lg">Contribution Analysis</h3>
        </div>
        <p className="text-slate-400 text-[13px] mb-6">Each factor's impact based on your recent check-ins</p>
        
        <div className="space-y-1">
          <FactorItem icon={Moon} title="Sleep Duration" impact={`${factorsData.sleep_avg.toFixed(1)} hrs avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
          <FactorItem icon={Smartphone} title="Screen Time" impact={`${factorsData.screen_time_avg.toFixed(1)} hrs avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
          <FactorItem icon={Coffee} title="Caffeine Intake" impact={`${factorsData.caffeine_avg.toFixed(1)} cups avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
          <FactorItem icon={Activity} title="Physical Activity" impact={`${factorsData.physical_activity_avg.toFixed(1)} hrs avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
          <FactorItem icon={Smile} title="Top Mood" impact={factorsData.top_mood || 'Neutral'} percentage={100} colorClass="text-yellow-500" bgClass="bg-[#ffd700]" />
          <FactorItem icon={Briefcase} title="Top Workload" impact={factorsData.top_workload || 'Light'} percentage={100} colorClass="text-rose-500" bgClass="bg-[#ff4b4b]" />
        </div>
      </div>

      <div className="bg-[#4a3b7a] rounded-[24px] p-6 shadow-lg">
        <h3 className="text-white font-bold text-lg mb-3">AI Recommendation</h3>
        
        {lastCheckin?.recommendation ? (
          <p className="text-indigo-100/90 text-[14px] leading-relaxed">
            {lastCheckin.recommendation}
          </p>
        ) : (
          <p className="text-indigo-100/80 text-[14px]">Add more check-ins to see personalized recommendations.</p>
        )}
      </div>
    </div>
  );
}
