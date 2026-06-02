"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getTrendsAnalytics, TrendsResponse } from '@/lib/api';

export default function TrendsScreen() {
  const router = useRouter();
  const [trendsData, setTrendsData] = useState<TrendsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    getTrendsAnalytics()
      .then(data => setTrendsData(data))
      .catch(err => {
        if (err.response?.status === 401) {
          localStorage.removeItem('jwtToken');
          router.push('/welcome');
        } else {
          setError('Failed to load trends data. Please try again.');
        }
      })
      .finally(() => setLoading(false));
  }, [router]);

  if (loading) return <div className="text-center py-10 text-slate-500">Loading Trends...</div>;
  if (error) return <div className="text-center py-10 text-rose-500">{error}</div>;
  if (!trendsData || trendsData.trends.length === 0) {
    return <div className="text-center py-10 text-slate-500">No trends data available yet. Start checking in!</div>;
  }

  // Process trends to draw SVG
  const maxScore = 100;
  const graphHeight = 160;
  const paddingBottom = 40; 
  const height = graphHeight + paddingBottom;
  const width = 1000; 
  const step = width / 6; 

  const today = new Date();
  let currentDayOfWeek = today.getDay();
  if (currentDayOfWeek === 0) currentDayOfWeek = 7; 
  
  const monday = new Date(today);
  monday.setDate(today.getDate() - (currentDayOfWeek - 1));
  
  const weekDays: string[] = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    const dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    weekDays.push(dateStr);
  }

  const dailyScores: Record<string, number> = {};
  trendsData.trends.forEach(t => {
    dailyScores[t.date] = t.score;
  });

  let pathD = "";
  const points: {x: number, y: number, score: number, dateStr: string}[] = [];

  weekDays.forEach((dateStr, index) => {
    const score = dailyScores[dateStr] || 0;
    const x = index * step;
    const y = graphHeight - (score / maxScore) * graphHeight;
    points.push({ x, y, score, dateStr });

    if (index === 0) {
      pathD += `M ${x} ${y} `;
    } else {
      const prevX = (index - 1) * step;
      const prevScore = dailyScores[weekDays[index - 1]] || 0;
      const prevY = graphHeight - (prevScore / maxScore) * graphHeight;
      pathD += `C ${prevX + step / 2} ${prevY}, ${x - step / 2} ${y}, ${x} ${y} `;
    }
  });

  const fillPathD = pathD ? `${pathD} L ${width} ${graphHeight} L 0 ${graphHeight} Z` : "";

  return (
    <div className="space-y-6 pb-20">
      <div className="bg-[#1e2132] border border-slate-800 rounded-[32px] p-6 shadow-lg">
        <h3 className="text-white font-bold text-lg">Stress Trends</h3>
        <p className="text-slate-400 text-xs mb-8">Daily Average Stress Score</p>
        
        <div className="w-full overflow-x-auto pb-4 scrollbar-thin scrollbar-thumb-slate-700 scrollbar-track-transparent">
          <div className="relative w-[600px] sm:w-full h-[200px] min-w-[600px]">
            <svg viewBox={`-20 -20 ${width + 40} ${height + 20}`} preserveAspectRatio="none" className="w-full h-full overflow-visible">
              <defs>
                <linearGradient id="waveGradient" x1="0" x2="0" y1="0" y2="1">
                  <stop offset="0%" stopColor="#82e0aa" stopOpacity="0.2" />
                  <stop offset="100%" stopColor="#82e0aa" stopOpacity="0" />
                </linearGradient>
              </defs>

              <line x1="0" y1="0" x2={width} y2="0" stroke="#334155" strokeWidth="1" strokeDasharray="10 10" opacity="0.5" />
              <line x1="0" y1={graphHeight / 2} x2={width} y2={graphHeight / 2} stroke="#334155" strokeWidth="1" strokeDasharray="10 10" opacity="0.5" />
              <line x1="0" y1={graphHeight} x2={width} y2={graphHeight} stroke="#334155" strokeWidth="1" strokeDasharray="10 10" opacity="0.5" />

              {fillPathD && <path d={fillPathD} fill="url(#waveGradient)" />}
              {pathD && <path d={pathD} fill="none" stroke="#82e0aa" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />}

              {points.map((p, i) => (
                <g key={i}>
                  <circle cx={p.x} cy={p.y} r="10" fill="white" stroke="#82e0aa" strokeWidth="4" />
                  <text x={p.x} y={p.y - 20} fill="white" fontSize="22" fontWeight="bold" textAnchor="middle">
                    {p.score}
                  </text>
                  <text x={p.x} y={graphHeight + 35} fill="#94a3b8" fontSize="18" fontWeight="bold" textAnchor="middle">
                    {new Date(p.dateStr).toLocaleDateString('en-US', { weekday: 'short' })}
                  </text>
                </g>
              ))}
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
}
