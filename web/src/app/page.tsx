"use client";

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import api, { 
  StressCheckInResponse, 
  WeeklyAnalyticsResponse, 
  MonthlyAnalyticsResponse, 
  TrendsResponse, 
  FactorsResponse,
  getHistory,
  getWeeklyAnalytics,
  getMonthlyAnalytics,
  getTrendsAnalytics,
  getFactorsAnalytics
} from '@/lib/api';
import axios from 'axios';
import { 
  Bell, 
  Home, 
  BarChart3, 
  PlusCircle, 
  User, 
  Loader2,
  Calendar,
  Clock,
  Share2,
  TrendingUp,
  Activity,
  Zap,
  Target,
  Check
} from 'lucide-react';

export default function Dashboard() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('Home');
  const [history, setHistory] = useState<StressCheckInResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [userEmail, setUserEmail] = useState<string>('User');
  const [showAlertScore, setShowAlertScore] = useState<number | null>(null);

  // Analytics States
  const [analyticsTab, setAnalyticsTab] = useState('Trends');
  const [weeklyData, setWeeklyData] = useState<WeeklyAnalyticsResponse | null>(null);
  const [monthlyData, setMonthlyData] = useState<MonthlyAnalyticsResponse | null>(null);
  const [trendsData, setTrendsData] = useState<TrendsResponse | null>(null);
  const [factorsData, setFactorsData] = useState<FactorsResponse | null>(null);

  useEffect(() => {
    async function fetchData() {
      if (typeof window !== 'undefined') {
        const token = localStorage.getItem('jwtToken');
        if (!token) {
          window.location.href = '/welcome';
          return;
        }
        const email = localStorage.getItem('userEmail');
        if (email) {
          const name = email.split('@')[0];
          setUserEmail(name.charAt(0).toUpperCase() + name.slice(1));
        }
      }

      try {
        setLoading(true);
        const [hist, weekly, monthly, trends, factors] = await Promise.all([
          getHistory().catch(() => []),
          getWeeklyAnalytics().catch(() => null),
          getMonthlyAnalytics().catch(() => null),
          getTrendsAnalytics().catch(() => null),
          getFactorsAnalytics().catch(() => null)
        ]);
        setHistory(hist);
        if (weekly) setWeeklyData(weekly);
        if (monthly) setMonthlyData(monthly);
        if (trends) setTrendsData(trends);
        if (factors) setFactorsData(factors);
        setError(null);
      } catch (err: any) {
        console.error("Failed to fetch history:", err);
        if (axios.isAxiosError(err) && err.response?.status === 401) {
          localStorage.removeItem('jwtToken');
          window.location.href = '/login';
        } else {
          setError("Could not connect to backend");
        }
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  const latestCheckIn = history[0];
  const stressScore = latestCheckIn?.score ?? 0;
  
  // Dynamic stats calculation based on real data
  const todayCheckinsCount = history.filter(item => {
    const today = new Date().toISOString().split('T')[0];
    return item.timestamp.startsWith(today);
  }).length;

  const greeting = new Date().getHours() < 12 ? 'Good Morning' : 'Good Evening';

  if (loading) {
    return (
      <div className="min-h-screen bg-[#050810] flex items-center justify-center text-emerald-400">
        <Loader2 className="animate-spin mr-2" /> Initializing Sanctuary...
      </div>
    );
  }

  // ==========================================
  // Analytics Sub-tab Renderers
  // ==========================================
  const renderTrends = () => {
    if (!trendsData) return <div className="text-center py-10 text-slate-500">Loading Trends...</div>;
    
    // Process trends to draw SVG
    const maxScore = 100;
    const graphHeight = 160;
    const paddingBottom = 40; 
    const height = graphHeight + paddingBottom;
    const width = 1000; // arbitrary internal SVG width for viewBox
    const step = trendsData.trends.length > 1 ? width / (trendsData.trends.length - 1) : width;

    let pathD = "";
    const points: {x: number, y: number, score: number, dateStr: string}[] = [];

    trendsData.trends.forEach((item, index) => {
      const x = index * step;
      const y = graphHeight - (item.score / maxScore) * graphHeight;
      points.push({ x, y, score: item.score, dateStr: item.date });

      if (index === 0) {
        pathD += `M ${x} ${y} `;
      } else {
        const prevX = (index - 1) * step;
        const prevY = graphHeight - (trendsData.trends[index - 1].score / maxScore) * graphHeight;
        pathD += `C ${prevX + step / 2} ${prevY}, ${x - step / 2} ${y}, ${x} ${y} `;
      }
    });

    const fillPathD = pathD ? `${pathD} L ${width} ${graphHeight} L 0 ${graphHeight} Z` : "";

    return (
      <div className="space-y-6">
        {/* Wave Graph Card */}
        <div className="bg-[#1e2132] border border-slate-800 rounded-[32px] p-6 shadow-lg">
          <h3 className="text-white font-bold text-lg">Stress Trends</h3>
          <p className="text-slate-400 text-xs mb-8">Daily Average Stress Score</p>
          
          {/* Scrollable Container to prevent SVG squishing on mobile */}
          <div className="w-full overflow-x-auto pb-4 scrollbar-thin scrollbar-thumb-slate-700 scrollbar-track-transparent">
            <div className="relative w-[600px] sm:w-full h-[200px] min-w-[600px]">
              <svg viewBox={`-20 -20 ${width + 40} ${height + 20}`} preserveAspectRatio="none" className="w-full h-full overflow-visible">
                <defs>
                  <linearGradient id="waveGradient" x1="0" x2="0" y1="0" y2="1">
                    <stop offset="0%" stopColor="#82e0aa" stopOpacity="0.2" />
                    <stop offset="100%" stopColor="#82e0aa" stopOpacity="0" />
                  </linearGradient>
                </defs>

                {/* Grid Lines */}
                <line x1="0" y1="0" x2={width} y2="0" stroke="#334155" strokeWidth="1" strokeDasharray="10 10" opacity="0.5" />
                <line x1="0" y1={graphHeight / 2} x2={width} y2={graphHeight / 2} stroke="#334155" strokeWidth="1" strokeDasharray="10 10" opacity="0.5" />
                <line x1="0" y1={graphHeight} x2={width} y2={graphHeight} stroke="#334155" strokeWidth="1" strokeDasharray="10 10" opacity="0.5" />

                {/* Area Fill */}
                {fillPathD && (
                  <path d={fillPathD} fill="url(#waveGradient)" />
                )}
                
                {/* Stroke Path */}
                {pathD && (
                  <path d={pathD} fill="none" stroke="#82e0aa" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
                )}

                {/* Points & Labels */}
                {points.map((p, i) => (
                  <g key={i}>
                    {/* Point Circle */}
                    {/* Scaled rx/ry to counteract preserveAspectRatio="none" distortion? No, we use fixed min-width now, so it won't distort much. */}
                    <circle cx={p.x} cy={p.y} r="10" fill="white" stroke="#82e0aa" strokeWidth="4" />
                    {/* Score Text */}
                    <text 
                      x={p.x} 
                      y={p.y - 20} 
                      fill="white" 
                      fontSize="22" 
                      fontWeight="bold" 
                      textAnchor="middle"
                    >
                      {p.score}
                    </text>
                    {/* Day Label */}
                    <text 
                      x={p.x} 
                      y={graphHeight + 35} 
                      fill="#94a3b8" 
                      fontSize="18" 
                      fontWeight="bold" 
                      textAnchor="middle"
                    >
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
  };

  const renderWeekly = () => {
    if (!weeklyData) return <div className="text-center py-10 text-slate-500">Loading Weekly Data...</div>;
    
    const lowDays = weeklyData.distribution.low;
    const moderateDays = weeklyData.distribution.moderate;
    const highDays = weeklyData.distribution.high;
    const total = weeklyData.total_checkins;
    
    return (
      <div className="space-y-6">
        <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-6 flex flex-col items-center text-center">
          <p className="text-slate-400 text-xs mb-2 uppercase tracking-wider font-bold">Weekly Average</p>
          <span className="text-5xl font-extrabold text-white mb-2">{weeklyData.avg_score}</span>
          <div className="flex items-center text-emerald-400 text-sm font-medium">
            Based on {total} check-ins
          </div>
        </div>
        
        <div className="grid grid-cols-3 gap-3">
          <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-4 flex flex-col items-start">
            <div className="text-emerald-500 mb-2"><Calendar size={16} /></div>
            <span className="text-white font-bold text-xl">{total}</span>
            <span className="text-slate-400 text-[10px] uppercase font-bold mt-1">Check-ins</span>
          </div>
          <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-4 flex flex-col items-start">
            <div className="text-rose-500 mb-2"><TrendingUp size={16} /></div>
            <span className="text-white font-bold text-xl">{weeklyData.highest_score}</span>
            <span className="text-slate-400 text-[10px] uppercase font-bold mt-1">Highest Score</span>
          </div>
          <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-4 flex flex-col items-start">
            <div className="text-emerald-500 mb-2"><TrendingUp size={16} className="rotate-180" /></div>
            <span className="text-white font-bold text-xl">{weeklyData.lowest_score}</span>
            <span className="text-slate-400 text-[10px] uppercase font-bold mt-1">Lowest Score</span>
          </div>
        </div>

        <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-6">
          <h3 className="text-white font-bold mb-4">Distribution</h3>
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
      </div>
    );
  };

  const renderMonthly = () => {
    if (!monthlyData) return <div className="text-center py-10 text-slate-500">Loading Monthly Data...</div>;
    
    const lowDays = monthlyData.distribution.low;
    const moderateDays = monthlyData.distribution.moderate;
    const highDays = monthlyData.distribution.high;
    const total = monthlyData.total_checkins;
    const lowPercent = total > 0 ? Math.round((lowDays * 100) / total) : 0;
    
    return (
      <div className="space-y-6">
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
  };

  const renderFactors = () => {
    if (!factorsData) return <div className="text-center py-10 text-slate-500">Loading Factors...</div>;
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-2 gap-4">
          <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-5 flex flex-col">
            <Clock size={20} className="text-cyan-400 mb-3" />
            <span className="text-2xl font-bold text-white mb-1">{factorsData.sleep_avg}h</span>
            <span className="text-slate-400 text-[10px] font-bold uppercase">Avg Sleep</span>
          </div>
          <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-5 flex flex-col">
            <Activity size={20} className="text-purple-400 mb-3" />
            <span className="text-2xl font-bold text-white mb-1">{factorsData.screen_time_avg}h</span>
            <span className="text-slate-400 text-[10px] font-bold uppercase">Screen Time</span>
          </div>
          <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-5 flex flex-col">
            <Zap size={20} className="text-yellow-400 mb-3" />
            <span className="text-2xl font-bold text-white mb-1">{factorsData.caffeine_avg}c</span>
            <span className="text-slate-400 text-[10px] font-bold uppercase">Caffeine</span>
          </div>
          <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-5 flex flex-col">
            <Target size={20} className="text-emerald-400 mb-3" />
            <span className="text-2xl font-bold text-white mb-1">{factorsData.physical_activity_avg}m</span>
            <span className="text-slate-400 text-[10px] font-bold uppercase">Activity</span>
          </div>
        </div>

        <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-6">
          <h3 className="text-white font-bold mb-4">Top Influencers</h3>
          <div className="space-y-4">
            <div className="flex justify-between items-center pb-4 border-b border-slate-800">
              <span className="text-slate-400 font-medium">Frequent Mood</span>
              <span className="text-white font-bold bg-slate-800 px-3 py-1 rounded-full text-sm">{factorsData.top_mood}</span>
            </div>
            <div className="flex justify-between items-center pb-4 border-b border-slate-800">
              <span className="text-slate-400 font-medium">Main Workload</span>
              <span className="text-white font-bold bg-slate-800 px-3 py-1 rounded-full text-sm">{factorsData.top_workload}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-slate-400 font-medium">Top Exercise</span>
              <span className="text-white font-bold bg-slate-800 px-3 py-1 rounded-full text-sm">{factorsData.top_exercise}</span>
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderHistory = () => {
    return (
      <div className="space-y-4">
        {history.length > 0 ? (
          history.map((item) => (
            <div key={item.id} className="bg-slate-900/50 border border-slate-800 p-4 rounded-2xl flex justify-between items-center hover:bg-slate-800/50 transition-colors cursor-pointer">
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
    );
  };

  // Define tab content rendering
  const renderContent = () => {
    if (activeTab === 'Home') {
      return (
        <div className="flex-1 overflow-y-auto px-6 py-12 space-y-8">
          
          {/* Header */}
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 bg-emerald-500 rounded-full flex items-center justify-center text-[#050810] font-bold text-xl shadow-lg shadow-emerald-500/20">
                C
              </div>
              <div>
                <p className="text-slate-400 text-sm font-medium">{greeting}</p>
                <h1 className="text-white text-2xl font-bold truncate max-w-[150px]">{userEmail}</h1>
              </div>
            </div>
            
            <button className="relative w-12 h-12 rounded-xl bg-slate-800/50 flex items-center justify-center text-emerald-400 hover:bg-slate-800 transition-colors">
              <Bell size={24} />
              {todayCheckinsCount === 0 && (
                <div className="absolute top-3 right-3 w-2.5 h-2.5 bg-rose-500 rounded-full border-2 border-[#050810]"></div>
              )}
            </button>
          </div>

          {/* Stress Orb */}
          <div className="flex flex-col items-center justify-center py-8">
            <div 
              onClick={() => setShowAlertScore(stressScore)}
              className="relative w-64 h-64 rounded-full flex items-center justify-center shadow-2xl shadow-emerald-500/10 border-4 border-emerald-500/20 overflow-hidden cursor-pointer hover:scale-105 transition-transform"
            >
              <div className="absolute inset-0 bg-gradient-to-tr from-emerald-500/20 to-cyan-500/20 animate-pulse rounded-full"></div>
              <div className="relative z-10 flex flex-col items-center">
                <span className="text-7xl font-bold text-white tracking-tighter">{stressScore}</span>
                <span className="text-emerald-400 font-semibold tracking-widest uppercase text-sm mt-1">Score</span>
              </div>
            </div>
          </div>

          {/* Main Action Button */}
          <div className="pt-4 pb-2 w-full max-w-sm mx-auto">
            <button 
              onClick={() => router.push('/checkin')}
              className="w-full h-16 bg-emerald-500 hover:bg-emerald-600 text-white font-bold text-lg rounded-[24px] shadow-lg shadow-emerald-500/20 transition-all active:scale-95"
            >
              Daily Check-in
            </button>
          </div>

          {/* Today's Stats (Mini) */}
          <div className="grid grid-cols-3 gap-3 w-full max-w-sm mx-auto">
            <div className="bg-slate-800/40 rounded-[20px] p-4 flex flex-col items-start justify-center">
              <div className="text-emerald-500 mb-2">
                <Calendar size={18} />
              </div>
              <p className="text-white font-extrabold text-lg">{todayCheckinsCount}</p>
              <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Check-ins</p>
            </div>
            
            <div className="bg-slate-800/40 rounded-[20px] p-4 flex flex-col items-start justify-center">
              <div className="text-emerald-500 mb-2">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 2.5z"/></svg>
              </div>
              <p className="text-white font-extrabold text-lg">{history.length > 0 ? 1 : 0}</p>
              <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Streak</p>
            </div>
            
            <div className="bg-slate-800/40 rounded-[20px] p-4 flex flex-col items-start justify-center">
              <div className="text-emerald-500 mb-2">
                <Clock size={18} />
              </div>
              <p className="text-white font-extrabold text-lg">{history.length > 0 ? history[0].score : '0'}h</p>
              <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Sleep</p>
            </div>
          </div>

          {/* Add extra space at the bottom so content isn't hidden by nav bar */}
          <div className="h-10"></div>
        </div>
      );
    } else if (activeTab === 'Analytics') {
      return (
        <div className="flex-1 overflow-y-auto bg-slate-900/10">
          <div className="p-6">
            <div className="flex justify-between items-center mb-6">
              <h2 className="text-2xl font-bold text-white">Insights</h2>
              <button className="w-10 h-10 flex items-center justify-center bg-slate-800/50 rounded-xl">
                <Share2 size={18} className="text-slate-200" />
              </button>
            </div>
            
            {/* Elegant Sub-tabs */}
            <div className="flex gap-6 overflow-x-auto pb-4 mb-6 border-b border-slate-800/50 scrollbar-hide">
              {['Trends', 'Weekly', 'Monthly', 'Factors', 'History'].map((tab) => (
                <button 
                  key={tab}
                  onClick={() => setAnalyticsTab(tab)}
                  className={`text-base font-bold whitespace-nowrap pb-2 border-b-[3px] transition-all ${analyticsTab === tab ? 'border-emerald-500 text-white' : 'border-transparent text-slate-500 hover:text-slate-300'}`}
                >
                  {tab}
                </button>
              ))}
            </div>

            <div className="mt-4">
              {analyticsTab === 'Trends' && renderTrends()}
              {analyticsTab === 'Weekly' && renderWeekly()}
              {analyticsTab === 'Monthly' && renderMonthly()}
              {analyticsTab === 'Factors' && renderFactors()}
              {analyticsTab === 'History' && renderHistory()}
            </div>
          </div>
        </div>
      );
    } else if (activeTab === 'Profile') {
      return (
        <div className="flex-1 overflow-y-auto px-6 py-12 flex flex-col">
          <div className="flex flex-col items-center mb-10">
            <div className="w-24 h-24 bg-emerald-500 rounded-full flex items-center justify-center text-[#050810] font-bold text-4xl mb-4 shadow-lg shadow-emerald-500/20">
              {userEmail.charAt(0)}
            </div>
            <h2 className="text-2xl font-bold text-white mb-1">{userEmail}</h2>
            <p className="text-slate-400 text-sm">{localStorage.getItem('userEmail')}</p>
          </div>

          <div className="space-y-3">
            <button className="w-full bg-slate-900/50 border border-slate-800 p-5 rounded-2xl text-left text-white font-medium hover:bg-slate-800 transition-colors">Achievements</button>
            <button className="w-full bg-slate-900/50 border border-slate-800 p-5 rounded-2xl text-left text-white font-medium hover:bg-slate-800 transition-colors">Streak Tracker</button>
            <button className="w-full bg-slate-900/50 border border-slate-800 p-5 rounded-2xl text-left text-white font-medium hover:bg-slate-800 transition-colors">Clinical History</button>
            
            <div className="pt-4">
              <button 
                onClick={() => {
                  localStorage.removeItem('jwtToken');
                  localStorage.removeItem('userEmail');
                  router.push('/welcome');
                }}
                className="w-full bg-rose-500/10 border border-rose-500/20 p-5 rounded-2xl text-left text-rose-500 font-bold hover:bg-rose-500/20 transition-colors"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      );
    }
    return null;
  };

  const renderStressAlert = () => {
    if (showAlertScore === null) return null;
    
    let stressLevel = 'Low';
    if (showAlertScore >= 85) stressLevel = 'Critical';
    else if (showAlertScore >= 70) stressLevel = 'High';
    else if (showAlertScore >= 40) stressLevel = 'Moderate';

    const themeColors = {
      Critical: 'text-rose-500 border-rose-500/20 bg-rose-500/10',
      High: 'text-orange-500 border-orange-500/20 bg-orange-500/10',
      Moderate: 'text-yellow-500 border-yellow-500/20 bg-yellow-500/10',
      Low: 'text-emerald-400 border-emerald-500/20 bg-emerald-500/10'
    };

    const buttonColors = {
      Critical: 'bg-rose-500 hover:bg-rose-600',
      High: 'bg-orange-500 hover:bg-orange-600',
      Moderate: 'bg-yellow-500 hover:bg-yellow-600 text-slate-900',
      Low: 'bg-emerald-500 hover:bg-emerald-600 text-slate-900'
    };

    const alerts = {
      Critical: { title: 'Critical Stress Alert', sub: 'Immediate action needed', mainText: 'Critical', recTitle: 'Severe stress detected', recDesc: 'Please seek immediate support, disconnect from work, and try a deep breathing exercise.' },
      High: { title: 'High Stress Alert', sub: 'Action recommended', mainText: 'High', recTitle: 'Elevated stress detected', recDesc: 'Take a 15-minute break and do a guided breathing session to lower your heart rate.' },
      Moderate: { title: 'Moderate Stress Alert', sub: 'Stay mindful', mainText: 'Moderate', recTitle: 'Slightly elevated stress', recDesc: 'Stay mindful and remember to take short breaks throughout your day.' },
      Low: { title: 'Doing Great!', sub: 'Keep it up', mainText: 'Low', recTitle: 'Excellent', recDesc: 'Your stress levels are well managed. Maintain your current healthy routine and mindfulness practices.' }
    };

    const data = alerts[stressLevel as keyof typeof alerts];
    const colorClass = themeColors[stressLevel as keyof typeof themeColors];
    const btnClass = buttonColors[stressLevel as keyof typeof buttonColors];

    return (
      <div className="flex-1 overflow-y-auto px-6 py-10 space-y-6 max-w-lg mx-auto w-full">
        <button 
          onClick={() => setShowAlertScore(null)}
          className="flex items-center gap-2 text-slate-400 hover:text-white transition-colors mb-4"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m15 18-6-6 6-6"/></svg>
          Back
        </button>

        {/* Alert Header */}
        <div className={`p-4 rounded-2xl border flex items-center gap-4 ${colorClass}`}>
          <div className="w-12 h-12 bg-white/10 rounded-xl flex items-center justify-center">
            {stressLevel === 'Low' ? <Calendar size={24} /> : <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3Z"/><path d="M12 9v4"/><path d="M12 17h.01"/></svg>}
          </div>
          <div>
            <h2 className="font-bold text-lg">{data.title}</h2>
            <p className="opacity-80 text-sm">{data.sub}</p>
          </div>
        </div>

        {/* Main Score Card */}
        <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-8 flex flex-col items-center text-center">
          <div className={`w-40 h-40 rounded-full border-[10px] flex flex-col items-center justify-center mb-6 ${colorClass.split(' ')[0].replace('text-', 'border-')}`}>
            <span className="text-5xl font-extrabold text-white">{showAlertScore}</span>
            <span className="text-xs font-bold text-slate-500 uppercase tracking-widest mt-1">Score</span>
          </div>
          <h3 className={`text-2xl font-bold ${colorClass.split(' ')[0]}`}>{data.mainText}</h3>
          <p className="text-slate-400 text-sm mt-1">Current status</p>
        </div>

        {/* Recommendation Card */}
        <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-6">
          <div className="flex items-center gap-3 mb-3">
            <svg className={colorClass.split(' ')[0]} xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4"/><path d="M12 8h.01"/></svg>
            <h4 className="font-bold text-white text-lg">{data.recTitle}</h4>
          </div>
          <p className="text-slate-400 text-sm leading-relaxed">{data.recDesc}</p>
        </div>

        {/* Action Button */}
        <button className={`w-full py-4 rounded-2xl font-bold text-white mt-4 shadow-lg active:scale-95 transition-all ${btnClass}`}>
          {stressLevel === 'Critical' || stressLevel === 'High' ? 'Start Emergency Breathing' : 'Start Breathing Exercise'}
        </button>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-[#050810] text-slate-200 font-sans flex overflow-hidden">
      <div className="w-full h-screen flex flex-col md:flex-row relative bg-[#050810]">
        
        {/* Desktop Side Navigation */}
        <div className="hidden md:flex flex-col w-[300px] lg:w-[350px] border-r border-slate-800 p-6 pt-12 space-y-8 h-full bg-slate-900/30">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-10 h-10 bg-emerald-500 rounded-full flex items-center justify-center text-[#050810] font-bold">C</div>
            <h1 className="text-xl font-bold text-white tracking-wider">CortiSense</h1>
          </div>
          
          <button onClick={() => setActiveTab('Home')} className={`flex items-center gap-4 p-4 rounded-2xl transition-all ${activeTab === 'Home' ? 'bg-emerald-500/10 text-emerald-400' : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/50'}`}>
            <Home size={24} className={activeTab === 'Home' ? 'fill-emerald-400/20' : ''} />
            <span className="font-bold text-lg">Home</span>
          </button>
          
          <button onClick={() => setActiveTab('Analytics')} className={`flex items-center gap-4 p-4 rounded-2xl transition-all ${activeTab === 'Analytics' ? 'bg-emerald-500/10 text-emerald-400' : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/50'}`}>
            <BarChart3 size={24} className={activeTab === 'Analytics' ? 'fill-emerald-400/20' : ''} />
            <span className="font-bold text-lg">Analytics</span>
          </button>

          <button onClick={() => router.push('/checkin')} className="flex items-center gap-4 p-4 rounded-2xl text-slate-500 hover:text-slate-300 hover:bg-slate-800/50 transition-all">
            <PlusCircle size={24} />
            <span className="font-bold text-lg">Check-in</span>
          </button>
          
          <button onClick={() => setActiveTab('Profile')} className={`flex items-center gap-4 p-4 rounded-2xl transition-all ${activeTab === 'Profile' ? 'bg-emerald-500/10 text-emerald-400' : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/50'}`}>
            <User size={24} className={activeTab === 'Profile' ? 'fill-emerald-400/20' : ''} />
            <span className="font-bold text-lg">Profile</span>
          </button>
        </div>

        {/* Dynamic Content */}
        <div className="flex-1 h-full overflow-y-auto bg-[#0a0f1c] relative">
          <div className="w-full max-w-4xl mx-auto pb-24 md:pb-12">
            {showAlertScore !== null ? renderStressAlert() : renderContent()}
          </div>
        </div>

        {/* Mobile Bottom Navigation Bar */}
        <div className="md:hidden absolute bottom-0 left-0 w-full bg-[#050810] border-t border-slate-800 px-6 py-4 flex justify-between items-center z-50">
          <button 
            onClick={() => setActiveTab('Home')}
            className={`flex flex-col items-center gap-1 ${activeTab === 'Home' ? 'text-emerald-400' : 'text-slate-500 hover:text-slate-400'}`}
          >
            <Home size={24} className={activeTab === 'Home' ? 'fill-emerald-400/20' : ''} />
            <span className="text-[10px] font-bold">Home</span>
          </button>
          
          <button 
            onClick={() => setActiveTab('Analytics')}
            className={`flex flex-col items-center gap-1 ${activeTab === 'Analytics' ? 'text-emerald-400' : 'text-slate-500 hover:text-slate-400'}`}
          >
            <BarChart3 size={24} className={activeTab === 'Analytics' ? 'fill-emerald-400/20' : ''} />
            <span className="text-[10px] font-bold">Analytics</span>
          </button>
          
          <button 
            onClick={() => router.push('/checkin')}
            className="flex flex-col items-center gap-1 text-slate-500 hover:text-slate-400"
          >
            <PlusCircle size={24} />
            <span className="text-[10px] font-bold">Check-in</span>
          </button>
          
          <button 
            onClick={() => setActiveTab('Profile')}
            className={`flex flex-col items-center gap-1 ${activeTab === 'Profile' ? 'text-emerald-400' : 'text-slate-500 hover:text-slate-400'}`}
          >
            <User size={24} className={activeTab === 'Profile' ? 'fill-emerald-400/20' : ''} />
            <span className="text-[10px] font-bold">Profile</span>
          </button>
        </div>

      </div>
    </div>
  );
}
