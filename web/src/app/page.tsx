"use client";

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import api, { 
  StressCheckInResponse, 
  WeeklyAnalyticsResponse, 
  MonthlyAnalyticsResponse, 
  TrendsResponse, 
  FactorsResponse,
  DashboardSummaryResponse,
  getHistory,
  getWeeklyAnalytics,
  getMonthlyAnalytics,
  getTrendsAnalytics,
  getFactorsAnalytics,
  getDashboardSummary,
  getProfile,
  completeAction,
  ActionItem
} from '@/lib/api';
import { useNotifications, AppNotification } from '@/components/NotificationProvider';
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
  LineChart,
  Check,
  Moon,
  Smartphone,
  Coffee,
  Activity as RunIcon,
  Smile,
  Briefcase,
  Flame,
  CheckCircle,
  ArrowLeft,
  AlertTriangle,
  Info,
  Clock,
  CheckCircle2,
  Trash2
} from 'lucide-react';

export default function Dashboard() {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('Home');
  const [history, setHistory] = useState<StressCheckInResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [userEmail, setUserEmail] = useState<string>('User');
  const [profileImage, setProfileImage] = useState<string>('');
  const [showAlertScore, setShowAlertScore] = useState<number | null>(null);
  const [showNotifications, setShowNotifications] = useState(false);
  const { notifications, unreadCount, markAllRead, markRead, clearAll } = useNotifications();

  // Helper for relative time
  const timeAgo = (ts: number): string => {
    const diff = Date.now() - ts;
    const mins = Math.floor(diff / 60000);
    if (mins < 1) return 'Just now';
    if (mins < 60) return `${mins}m ago`;
    const hrs = Math.floor(mins / 60);
    if (hrs < 24) return `${hrs}h ago`;
    const days = Math.floor(hrs / 24);
    return `${days}d ago`;
  };

  // Analytics States
  const [analyticsTab, setAnalyticsTab] = useState('Trends');
  const [weeklyData, setWeeklyData] = useState<WeeklyAnalyticsResponse | null>(null);
  const [monthlyData, setMonthlyData] = useState<MonthlyAnalyticsResponse | null>(null);
  const [trendsData, setTrendsData] = useState<TrendsResponse | null>(null);
  const [factorsData, setFactorsData] = useState<FactorsResponse | null>(null);
  const [dashboardSummary, setDashboardSummary] = useState<DashboardSummaryResponse | null>(null);

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
        const [hist, weekly, monthly, trends, factors, summary, profile] = await Promise.all([
          getHistory().catch(() => []),
          getWeeklyAnalytics().catch(() => null),
          getMonthlyAnalytics().catch(() => null),
          getTrendsAnalytics().catch(() => null),
          getFactorsAnalytics().catch(() => null),
          getDashboardSummary().catch(() => null),
          getProfile().catch(() => null)
        ]);
        setHistory(hist);
        if (weekly) setWeeklyData(weekly);
        if (monthly) setMonthlyData(monthly);
        if (trends) setTrendsData(trends);
        if (factors) setFactorsData(factors);
        if (summary) setDashboardSummary(summary);
        if (profile?.profile_image) setProfileImage(profile.profile_image);
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
  const stressScore = dashboardSummary?.latest_stress_score ?? 0;
  const todayCheckinsCount = dashboardSummary?.today_checkins_count ?? 0;
  const currentStreak = dashboardSummary?.current_streak ?? 0;
  
  // Calculate average or latest sleep
  const latestSleepDuration = dashboardSummary?.latest_sleep_duration ?? 0;

  const handleActionComplete = async (actionId: string) => {
    if (!latestCheckIn) return;
    try {
      await completeAction(latestCheckIn.id, actionId);
      // Optimistically update history state
      const updatedHistory = [...history];
      if (updatedHistory[0] && updatedHistory[0].actions) {
        updatedHistory[0].actions = updatedHistory[0].actions.map(a => 
          a.id === actionId ? { ...a, is_done: true } : a
        );
        setHistory(updatedHistory);
      }
    } catch (err) {
      console.error("Failed to complete action", err);
    }
  };

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
    const step = width / 6; // 7 days = 6 intervals

    // Generate Monday to Sunday of the current week
    const today = new Date();
    let currentDayOfWeek = today.getDay();
    if (currentDayOfWeek === 0) currentDayOfWeek = 7; // Sunday = 7
    
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
    
    const lastCheckin = history.length > 0 ? history[0] : null;
    const lastCheckinReasons = lastCheckin?.reasons || [];
    
    const getFactorRecommendation = (factor: string) => {
      switch(factor.toLowerCase()) {
        case 'workload': case 'work': case 'studies': return "Break large tasks into smaller steps. Prioritize urgent work and take regular 5-minute breaks.";
        case 'sleep deprivation': case 'poor sleep': case 'sleep': return "Try to maintain a consistent sleep schedule. Avoid screens 1 hour before bedtime.";
        case 'financial stress': case 'money': return "Review your budget for the week. Small financial plans can reduce uncertainty.";
        case 'relationship issues': case 'family': return "Communicate openly with your loved ones. Taking a short walk can clear your head.";
        case 'health concerns': case 'health': return "Listen to your body. Ensure you're staying hydrated and eating balanced meals.";
        default: return "Take a moment to breathe deeply. Identifying your stressor is the first step to managing it.";
      }
    };
    
    const getTranslatedReason = (factor: string) => {
      switch(factor.toLowerCase()) {
        case 'workload': return 'Workload';
        case 'sleep deprivation': return 'Poor Sleep';
        case 'financial stress': return 'Financial Stress';
        case 'relationship issues': return 'Relationship Issues';
        case 'health concerns': return 'Health Concerns';
        default: return factor.charAt(0).toUpperCase() + factor.slice(1);
      }
    };

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
      <div className="space-y-6">
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
            <FactorItem icon={Moon} title="Sleep Duration" impact={`${factorsData.sleep_avg} hrs avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Smartphone} title="Screen Time" impact={`${factorsData.screen_time_avg} hrs avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Coffee} title="Caffeine Intake" impact={`${factorsData.caffeine_avg} cups avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={RunIcon} title="Physical Activity" impact={`${factorsData.physical_activity_avg} hrs avg`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
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
        <div className="flex-1 overflow-y-auto px-6 py-12 flex flex-col min-h-full bg-[#111116]">
          
          {/* Header */}
          <div className="flex items-center justify-between mb-10 mt-2">
            <div className="flex items-center gap-4">
              <div className="w-[60px] h-[60px] rounded-full border border-emerald-400 flex flex-col items-center justify-center bg-[#172c29] overflow-hidden">
                {profileImage ? (
                  <img src={profileImage} alt="Profile" className="w-full h-full object-cover" />
                ) : (
                  <>
                    <span className="text-emerald-400 font-bold text-2xl leading-none">
                      {userEmail ? userEmail.charAt(0).toUpperCase() : 'C'}
                    </span>
                    {!userEmail && <span className="text-emerald-400 text-[6px] tracking-widest mt-0.5 uppercase">CortiSense</span>}
                  </>
                )}
              </div>
              <p className="text-slate-300 text-[17px] tracking-wide">{greeting}</p>
            </div>
            
            <div className="relative">
              <button 
                onClick={() => {
                  setShowNotifications(!showNotifications);
                  if (!showNotifications) markAllRead();
                }}
                className="relative w-11 h-11 rounded-2xl bg-[#3a355d] flex items-center justify-center text-emerald-400 hover:bg-[#4a457d] transition-colors"
              >
                <Bell size={20} className="text-emerald-400" fill="#4ade80" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1.5 -right-1.5 w-5 h-5 rounded-full bg-rose-500 text-white text-[10px] font-bold flex items-center justify-center border-2 border-[#111116]">
                    {unreadCount > 9 ? '9+' : unreadCount}
                  </span>
                )}
              </button>
              
              {/* Notifications Dropdown */}
              {showNotifications && (
                <div className="absolute top-14 right-0 w-80 bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden z-50 flex flex-col max-h-[400px]">
                  <div className="flex items-center justify-between p-4 border-b border-slate-800 bg-slate-900">
                    <h3 className="text-white font-bold">Notifications</h3>
                    {notifications.length > 0 && (
                      <button onClick={clearAll} className="text-xs text-slate-400 hover:text-rose-400 flex items-center gap-1">
                        <Trash2 size={12} /> Clear
                      </button>
                    )}
                  </div>
                  <div className="flex-1 overflow-y-auto">
                    {notifications.length === 0 ? (
                      <div className="p-8 text-center text-slate-500 text-sm">
                        No notifications yet
                      </div>
                    ) : (
                      <div className="divide-y divide-slate-800/50">
                        {notifications.map(notif => (
                          <div 
                            key={notif.id} 
                            onClick={() => markRead(notif.id)}
                            className={`p-4 cursor-pointer hover:bg-slate-800/50 transition-colors flex gap-3 ${!notif.read ? 'bg-slate-800/30' : ''}`}
                          >
                            <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0 ${notif.type === 'reminder' ? 'bg-blue-500/20 text-blue-400' : notif.type === 'completion' ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-700/50 text-slate-400'}`}>
                              {notif.type === 'reminder' ? <Clock size={16} /> : notif.type === 'completion' ? <CheckCircle2 size={16} /> : <Bell size={16} />}
                            </div>
                            <div>
                              <div className="flex items-center justify-between gap-2 mb-1">
                                <h4 className={`text-sm font-semibold ${!notif.read ? 'text-white' : 'text-slate-300'}`}>{notif.title}</h4>
                                {!notif.read && <div className="w-2 h-2 rounded-full bg-emerald-400"></div>}
                              </div>
                              <p className="text-xs text-slate-400 mb-1 leading-relaxed">{notif.body}</p>
                              <p className="text-[10px] text-slate-500 font-medium">{timeAgo(notif.timestamp)}</p>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Stress Orb Container */}
          <div className="flex-1 flex flex-col items-center justify-center py-6 relative w-full mb-12">
            {/* Soft Glowing Gradient Background */}
            <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
               <div className="w-80 h-80 rounded-full bg-emerald-100/10 blur-3xl mix-blend-screen"></div>
               <div className="absolute w-56 h-56 rounded-full bg-[#b5e0ca]/15 blur-2xl mix-blend-screen"></div>
            </div>
            
            {/* The Text within the glow */}
            <div 
              onClick={() => setShowAlertScore(stressScore)}
              className="relative z-10 flex flex-col items-center justify-center cursor-pointer"
            >
                <span className="text-6xl font-serif text-white tracking-tight">{stressScore}</span>
                <span className="text-slate-300 font-medium text-[15px] mt-1">{dashboardSummary?.latest_stress_category || 'High'}</span>
            </div>
          </div>

          {/* Main Action Button */}
          <div className="w-full mb-8">
            <button 
              onClick={() => router.push('/checkin')}
              className="w-full h-16 bg-[#82c885] hover:bg-[#6eb171] text-white font-bold text-lg rounded-[24px] shadow-lg shadow-emerald-500/10 transition-all active:scale-95"
            >
              Daily Check-in
            </button>
          </div>

          {/* Bottom Stats Row */}
          <div className="grid grid-cols-3 gap-4 w-full">
            <div className="bg-[#2c2c35] rounded-3xl p-5 flex flex-col items-start justify-center">
              <div className="text-emerald-500 mb-3">
                <CheckCircle size={20} />
              </div>
              <p className="text-white font-bold text-2xl mb-1">{todayCheckinsCount}</p>
              <p className="text-slate-400 text-xs font-semibold">Check-ins</p>
            </div>
            
            <div className="bg-[#2c2c35] rounded-3xl p-5 flex flex-col items-start justify-center">
              <div className="text-emerald-500 mb-3">
                <Flame size={20} />
              </div>
              <p className="text-white font-bold text-2xl mb-1">{currentStreak}</p>
              <p className="text-slate-400 text-xs font-semibold">Streak</p>
            </div>
            
            <div className="bg-[#2c2c35] rounded-3xl p-5 flex flex-col items-start justify-center">
              <div className="text-emerald-500 mb-3">
                <Moon size={20} />
              </div>
              <p className="text-white font-bold text-xl tracking-tight mb-1">
                 {latestSleepDuration ? `${Math.floor(latestSleepDuration)}h ${Math.round((latestSleepDuration % 1) * 60)}m` : '0h 0m'}
              </p>
              <p className="text-slate-400 text-xs font-semibold">Sleep</p>
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
            <div className="w-24 h-24 bg-emerald-500 rounded-full flex items-center justify-center text-[#050810] font-bold text-4xl mb-4 shadow-lg shadow-emerald-500/20 overflow-hidden">
              {profileImage ? (
                <img src={profileImage} alt="Profile" className="w-full h-full object-cover" />
              ) : (
                userEmail.charAt(0)
              )}
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

    const alerts = {
      Critical: { title: 'Critical Stress Alert', sub: 'Immediate action needed', mainText: 'Critical', recTitle: 'Severe stress detected', recDesc: 'Please seek immediate support, disconnect from work, and try a deep breathing exercise.' },
      High: { title: 'High Stress Alert', sub: 'Action recommended', mainText: 'High', recTitle: 'Elevated stress detected', recDesc: 'Take a 15-minute break and do a guided breathing session to lower your heart rate.' },
      Moderate: { title: 'Moderate Stress', sub: 'Stay mindful', mainText: 'High', recTitle: 'Elevated', recDesc: 'Your stress levels are a bit higher than usual. Consider taking some time to relax.' },
      Low: { title: 'Doing Great!', sub: 'Keep it up', mainText: 'Low', recTitle: 'Excellent', recDesc: 'Your stress levels are well managed. Maintain your current healthy routine and mindfulness practices.' }
    };

    const data = alerts[stressLevel as keyof typeof alerts];
    
    // Hex colors for the SVG progress bar
    let colorHex = '#4ade80'; // Low (Emerald)
    let colorTextClass = 'text-emerald-400';
    let alertIconColor = 'text-emerald-400';
    let alertBgClass = 'bg-[#1a2d26] border-[#2c453b]';
    
    if (stressLevel === 'Critical') {
      colorHex = '#f43f5e';
      colorTextClass = 'text-rose-500';
      alertIconColor = 'text-rose-500';
      alertBgClass = 'bg-[#2d1b22] border-[#452831]';
    } else if (stressLevel === 'High') {
      colorHex = '#f97316';
      colorTextClass = 'text-orange-500';
      alertIconColor = 'text-orange-500';
      alertBgClass = 'bg-[#2d221a] border-[#453123]';
    } else if (stressLevel === 'Moderate') {
      colorHex = '#ffe55c';
      colorTextClass = 'text-[#ffe55c]';
      alertIconColor = 'text-[#ffe55c]';
      alertBgClass = 'bg-[#23221b] border-[#3a3721]';
    }

    // Default static actions if API doesn't provide them yet
    const fallbackActions = [
      { id: '1', title: 'Take a 15-minute short walk', is_done: false },
      { id: '2', title: 'Drink a glass of water', is_done: false },
      { id: '3', title: '5-Minute Deep Breathing', is_done: false },
      { id: '4', title: 'Read a book for 10 minutes', is_done: false },
      { id: '5', title: 'Listen to calm music', is_done: false }
    ];

    const displayActions = latestCheckIn?.actions?.length ? latestCheckIn.actions : fallbackActions;

    return (
      <div className="flex-1 overflow-y-auto px-5 py-8 space-y-5 bg-[#111116] min-h-full">
        <button 
          onClick={() => setShowAlertScore(null)}
          className="text-white mb-2 ml-1"
        >
          <ArrowLeft size={28} />
        </button>

        {/* Top Warning Card */}
        <div className={`rounded-2xl p-4 flex items-center gap-4 border ${alertBgClass}`}>
          <div className="w-12 h-12 rounded-xl bg-[#2b2a22] flex items-center justify-center flex-shrink-0">
             {stressLevel === 'Low' ? <CheckCircle className={alertIconColor} size={24} /> : <AlertTriangle className={alertIconColor} size={24} />}
          </div>
          <div>
            <h2 className="text-white font-bold text-[18px] tracking-wide leading-snug">{data.title}</h2>
            <p className="text-slate-300 text-[15px] font-medium mt-0.5">{data.sub}</p>
          </div>
        </div>

        {/* Main Score Circle Card */}
        <div className="bg-[#1f1e26] rounded-[24px] p-8 flex flex-col items-center justify-center text-center shadow-lg">
           <div className="relative w-52 h-52 mb-6 mt-2">
             {/* Circular Progress Bar */}
             <svg className="w-full h-full transform -rotate-90 drop-shadow-md" viewBox="0 0 100 100">
                <circle cx="50" cy="50" r="40" fill="none" stroke="#373740" strokeWidth="7" />
                <circle 
                  cx="50" 
                  cy="50" 
                  r="40" 
                  fill="none" 
                  stroke={colorHex} 
                  strokeWidth="7" 
                  strokeDasharray="251.2" 
                  strokeDashoffset={251.2 * (1 - showAlertScore / 100)} 
                  strokeLinecap="round" 
                />
             </svg>
             <div className="absolute inset-0 flex flex-col items-center justify-center pb-1">
               <span className="text-5xl font-bold text-white tracking-tight">{showAlertScore}</span>
               <span className="text-[#8c8b96] text-xs font-bold tracking-wide mt-2">Stress Score</span>
             </div>
           </div>
           <h3 className={`text-[22px] font-bold ${colorTextClass}`}>{data.mainText}</h3>
           <p className="text-slate-300 text-[15px] mt-1 tracking-wide">{data.recTitle}</p>
        </div>

        {/* Recommendation Text Card */}
        <div className="bg-[#1f1e26] rounded-[24px] p-6 border border-[#2a2933]">
           <div className="flex items-center gap-3 mb-3">
             <div className="w-6 h-6 rounded-full bg-[#ffe55c] flex items-center justify-center flex-shrink-0">
                <Info size={14} className="text-[#111116] font-extrabold" />
             </div>
             <h4 className="text-white font-bold text-[17px] tracking-wide">{latestCheckIn?.recommendation ? 'Personalized Advice' : 'Stress Slightly Elevated'}</h4>
           </div>
           <p className="text-[#cbd5e1] text-[15px] leading-[1.6]">
             {latestCheckIn?.recommendation || data.recDesc}
           </p>
        </div>

        {/* Recommended Actions List Card */}
        <div className="bg-[#1f1e26] rounded-[24px] p-6 border border-[#2a2933] mb-6">
           <h4 className="text-white font-bold text-[19px] tracking-wide mb-1">Recommended Actions</h4>
           <p className="text-[#94a3b8] text-[14px] mb-6">Tasks based on your current stress score ({showAlertScore})</p>
           
           <div className="space-y-4">
              {displayActions.map(action => (
                <div key={action.id} className="flex items-center justify-between gap-4">
                   <div className="flex items-center gap-4 flex-1">
                      <div className="w-12 h-12 rounded-[14px] bg-[#312c4e] flex items-center justify-center flex-shrink-0">
                         {/* Lotus/Leaf Icon */}
                         <svg width="22" height="22" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 21C12 21 19.5 16.5 19.5 11.5C19.5 7.5 15.5 5.5 12 2C8.5 5.5 4.5 7.5 4.5 11.5C4.5 16.5 12 21 12 21Z" fill="#82c885"/>
                            <path d="M12 11.5V21" stroke="#312c4e" strokeWidth="2" strokeLinecap="round"/>
                         </svg>
                      </div>
                      <span className="text-white font-bold text-[15.5px] leading-snug pr-2">{action.title}</span>
                   </div>
                   <button 
                     onClick={() => handleActionComplete(action.id)}
                     disabled={action.is_done}
                     className={`px-5 py-2 rounded-[20px] font-bold text-[13px] tracking-wide flex-shrink-0 transition-colors ${action.is_done ? 'bg-[#3f3f46] text-[#a1a1aa]' : 'bg-[#82c885] text-[#111116] hover:bg-[#6eb171]'}`}
                   >
                     Done
                   </button>
                </div>
              ))}
           </div>
        </div>
        
        {/* Extra padding at bottom */}
        <div className="h-6"></div>
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
          
          <button onClick={() => router.push('/profile')} className={`flex items-center gap-4 p-4 rounded-2xl transition-all ${activeTab === 'Profile' ? 'bg-emerald-500/10 text-emerald-400' : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/50'}`}>
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
            onClick={() => router.push('/profile')}
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
