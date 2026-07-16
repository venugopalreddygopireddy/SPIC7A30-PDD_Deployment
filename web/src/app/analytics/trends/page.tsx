"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getTrendsAnalytics, getMonthlyAnalytics, TrendsResponse, MonthlyAnalyticsResponse } from '@/lib/api';
import { Check } from 'lucide-react';

export default function TrendsScreen() {
  const router = useRouter();
  const [trendsData, setTrendsData] = useState<TrendsResponse | null>(null);
  const [monthlyData, setMonthlyData] = useState<MonthlyAnalyticsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }

    Promise.all([
      getTrendsAnalytics(),
      getMonthlyAnalytics()
    ])
      .then(([trends, monthly]) => {
        setTrendsData(trends);
        setMonthlyData(monthly);
      })
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

      {/* Streak Calendar */}
      {monthlyData && (() => {
        const today = new Date();
        const currentYear = today.getFullYear();
        const currentMonth = today.getMonth();
        const currentDay = today.getDate();
        
        const firstDayOfMonth = new Date(currentYear, currentMonth, 1);
        const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
        const firstDayOfWeek = firstDayOfMonth.getDay(); // 0 is Sunday
        
        const monthName = firstDayOfMonth.toLocaleString('default', { month: 'long', year: 'numeric' });
        const checkinDates = new Set(Object.keys(monthlyData.calendar_activity));
        
        const checkedThisMonth = Array.from(checkinDates).filter(d => {
          return d.startsWith(`${currentYear}-${String(currentMonth + 1).padStart(2, '0')}`);
        }).length;
        
        const passedDays = currentDay;
        const pct = passedDays > 0 ? Math.round((checkedThisMonth / passedDays) * 100) : 0;
        
        const totalCells = daysInMonth + firstDayOfWeek;
        const cells = [];
        for (let i = 0; i < Math.ceil(totalCells / 7) * 7; i++) {
          const day = i - firstDayOfWeek + 1;
          if (day > 0 && day <= daysInMonth) {
            const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            const isCheckedIn = checkinDates.has(dateStr);
            const isFuture = day > currentDay;
            
            let bg = 'bg-[#2b2d42]';
            let textClass = 'text-slate-400';
            if (isCheckedIn) {
              bg = 'bg-[#82e0aa]';
              textClass = 'text-slate-900';
            } else if (isFuture) {
              bg = 'bg-transparent';
              textClass = 'text-slate-600';
            }
            
            cells.push(
              <div key={i} className={`aspect-square rounded-[10px] ${bg} flex flex-col items-center justify-center m-[4px]`}>
                {isCheckedIn && <Check size={12} className="text-slate-900 mb-[2px]" strokeWidth={3} />}
                <span className={`font-bold text-[10px] ${textClass}`}>{day}</span>
              </div>
            );
          } else {
            cells.push(<div key={i} className="aspect-square m-[4px] bg-transparent" />);
          }
        }

        return (
          <div className="bg-[#1e2132] border border-slate-800 rounded-[28px] p-6 shadow-lg">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-[#82e0aa] font-extrabold text-xl">{monthName}</h3>
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-1.5">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#82e0aa]"></div>
                  <span className="text-slate-300 text-xs font-bold tracking-wide">Visited</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <div className="w-2.5 h-2.5 rounded-full bg-[#475569]"></div>
                  <span className="text-slate-300 text-xs font-bold tracking-wide">Missed</span>
                </div>
              </div>
            </div>
            
            <div className="grid grid-cols-7 mb-2">
              {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(day => (
                <div key={day} className="text-center text-slate-100 text-xs font-extrabold mb-2">{day}</div>
              ))}
            </div>
            
            <div className="grid grid-cols-7 mb-6">
              {cells}
            </div>
            
            <div className="h-[1px] w-full bg-slate-700/50 mb-4"></div>
            
            <div className="flex justify-between items-center">
              <div>
                <div className="text-slate-200 font-bold text-sm">{checkedThisMonth} / {passedDays} days</div>
                <div className="text-slate-400 text-xs font-medium">checked in this month</div>
              </div>
              <div className="w-12 h-12 rounded-full bg-[#82e0aa]/10 flex items-center justify-center">
                <span className="text-[#82e0aa] font-extrabold text-sm">{pct}%</span>
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
}
