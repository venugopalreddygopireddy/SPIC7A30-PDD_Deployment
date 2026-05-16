import React, { useState, useEffect } from 'react';
import api, { StressCheckInResponse, getHistory } from '@/lib/api';
import { 
  Activity, 
  Brain, 
  Heart, 
  Wind, 
  History, 
  User, 
  Settings, 
  Bell, 
  Search,
  ArrowUpRight,
  ShieldCheck,
  TrendingDown,
  Calendar,
  LucideIcon,
  Loader2
} from 'lucide-react';

// --- Dashboard Layout Component ---
export default function Dashboard() {
  const [activeTab, setActiveTab] = useState('home');
  const [history, setHistory] = useState<StressCheckInResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchData() {
      try {
        setLoading(true);
        const data = await getHistory();
        setHistory(data);
        setError(null);
      } catch (err) {
        console.error("Failed to fetch history:", err);
        setError("Could not connect to backend");
      } finally {
        setLoading(false);
      }
    }
    fetchData();
  }, []);

  const latestCheckIn = history[0];
  const stressScore = latestCheckIn?.score ?? 0;
  const stressLevel = latestCheckIn?.stress_level ?? 'No Data';

  if (loading) {
    return (
      <div className="min-h-screen bg-[#050810] flex items-center justify-center text-emerald-400">
        <Loader2 className="animate-spin mr-2" /> Initializing Sanctuary...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#050810] text-slate-200 font-sans flex">
      {/* --- Sidebar --- */}
      <aside className="w-64 border-r border-slate-800/50 bg-[#080B16] hidden lg:flex flex-col">
        <div className="p-8">
          <h1 className="text-2xl font-bold bg-gradient-to-r from-emerald-400 to-cyan-400 bg-clip-text text-transparent">
            CortiSense
          </h1>
          <p className="text-xs text-slate-500 mt-1 uppercase tracking-widest font-semibold">AI Wellness</p>
        </div>

        <nav className="flex-1 px-4 space-y-2">
          <NavItem Icon={Activity} label="Dashboard" active={activeTab === 'home'} onClick={() => setActiveTab('home')} />
          <NavItem Icon={Brain} label="AI Chat" active={activeTab === 'chat'} onClick={() => setActiveTab('chat')} />
          <NavItem Icon={History} label="History" active={activeTab === 'history'} onClick={() => setActiveTab('history')} />
          <NavItem Icon={Wind} label="Exercises" active={activeTab === 'exercises'} onClick={() => setActiveTab('exercises')} />
        </nav>

        <div className="p-6 border-t border-slate-800/50 space-y-4">
          <NavItem Icon={Settings} label="Settings" />
          <div className="flex items-center gap-3 p-3 rounded-xl bg-slate-800/30">
            <div className="w-10 h-10 rounded-full bg-emerald-500/20 flex items-center justify-center text-emerald-400">
              <User size={20} />
            </div>
            <div>
              <p className="text-sm font-medium">Venugopal</p>
              <p className="text-[10px] text-slate-500">Premium Member</p>
            </div>
          </div>
        </div>
      </aside>

      {/* --- Main Content --- */}
      <main className="flex-1 overflow-y-auto">
        {/* --- Header --- */}
        <header className="h-20 border-b border-slate-800/50 flex items-center justify-between px-8 sticky top-0 bg-[#050810]/80 backdrop-blur-md z-10">
          <div className="relative w-96">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={18} />
            <input 
              type="text" 
              placeholder="Search analytics, history..." 
              className="w-full bg-slate-900/50 border border-slate-800 rounded-full py-2 pl-10 pr-4 focus:outline-none focus:ring-2 focus:ring-emerald-500/20 transition-all"
            />
          </div>
          
          <div className="flex items-center gap-4">
            <button className="p-2 text-slate-400 hover:text-white transition-colors relative">
              <Bell size={20} />
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-emerald-500 rounded-full border-2 border-[#050810]"></span>
            </button>
            <button className="bg-emerald-500 hover:bg-emerald-600 text-white px-4 py-2 rounded-full text-sm font-semibold transition-all shadow-lg shadow-emerald-500/20 flex items-center gap-2">
              New Check-in
            </button>
          </div>
        </header>

        <div className="p-8 max-w-7xl mx-auto space-y-8">
          {/* --- Hero Section --- */}
          <div className="flex flex-col md:flex-row gap-6">
            {/* Stress Card */}
            <div className="flex-1 bg-gradient-to-br from-slate-900 to-[#080B16] border border-slate-800 p-8 rounded-3xl relative overflow-hidden group">
              <div className="absolute top-0 right-0 p-8">
                <TrendingDown className="text-emerald-400 opacity-50 group-hover:scale-110 transition-transform" size={120} strokeWidth={1} />
              </div>
              <div className="relative z-10">
                <p className="text-slate-400 font-medium">Current Stress Score</p>
                <div className="flex items-baseline gap-2 mt-2">
                  <h2 className="text-7xl font-bold text-white tracking-tighter">{stressScore}</h2>
                  <span className="text-slate-500 text-xl">/ 100</span>
                </div>
                <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/10 text-emerald-400 text-xs font-semibold mt-4 border border-emerald-500/20">
                  <ShieldCheck size={14} /> Stress is improving
                </div>
              </div>
            </div>

            {/* AI Insights Card */}
            <div className="w-full md:w-96 bg-[#0E1528] border border-blue-500/20 p-8 rounded-3xl shadow-2xl shadow-blue-500/10 relative">
              <div className="flex items-center gap-2 text-blue-400 mb-4">
                <Brain size={20} />
                <span className="text-xs font-bold uppercase tracking-widest">AI Wellness Assistant</span>
              </div>
              <p className="text-slate-300 leading-relaxed italic font-serif text-lg">
                &quot;I noticed your stress levels are improving compared to your previous check-ins. Great job on staying mindful!&quot;
              </p>
              <button className="mt-6 text-sm text-blue-400 font-semibold hover:text-blue-300 flex items-center gap-1 group">
                Ask for advice <ArrowUpRight size={16} className="group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform" />
              </button>
            </div>
          </div>

          {/* --- Grid Content --- */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <StatCard 
              title="Sleep Quality" 
              value="8.2h" 
              desc="Normal range" 
              Icon={Calendar} 
              color="cyan"
            />
            <StatCard 
              title="Heart Rate" 
              value="72 bpm" 
              desc="Resting avg" 
              Icon={Heart} 
              color="rose"
            />
            <StatCard 
              title="Cognitive Load" 
              value="Low" 
              desc="Excellent focus" 
              Icon={Brain} 
              color="purple"
            />
          </div>

          {/* --- Recent Activity --- */}
          <div className="bg-slate-900/40 border border-slate-800 rounded-3xl p-8">
            <div className="flex justify-between items-center mb-8">
              <h3 className="text-xl font-bold">Recent History</h3>
              <button className="text-sm text-emerald-400 font-medium hover:underline">View All</button>
            </div>
            <div className="space-y-4">
              {history.length > 0 ? (
                history.map((item) => (
                  <HistoryItem 
                    key={item.id} 
                    date={new Date(item.timestamp).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} 
                    score={item.score} 
                    status={item.stress_level as any} 
                  />
                ))
              ) : (
                <div className="text-center py-12 text-slate-500">
                  <Activity className="mx-auto mb-4 opacity-20" size={48} />
                  <p>No health records found yet. Start your first check-in!</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

// --- Helper Components ---

interface NavItemProps {
  Icon: LucideIcon;
  label: string;
  active?: boolean;
  onClick?: () => void;
}

function NavItem({ Icon, label, active = false, onClick = () => {} }: NavItemProps) {
  return (
    <button 
      onClick={onClick}
      className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all ${
        active 
          ? 'bg-emerald-500 text-white shadow-lg shadow-emerald-500/20' 
          : 'text-slate-500 hover:text-slate-300 hover:bg-slate-800/30'
      }`}
    >
      <Icon size={20} />
      <span className="font-medium text-sm">{label}</span>
    </button>
  );
}

interface StatCardProps {
  title: string;
  value: string;
  desc: string;
  Icon: LucideIcon;
  color: 'cyan' | 'rose' | 'purple' | 'emerald';
}

function StatCard({ title, value, desc, Icon, color }: StatCardProps) {
  const colors = {
    cyan: 'bg-cyan-500/10 border-cyan-500/20 text-cyan-400',
    rose: 'bg-rose-500/10 border-rose-500/20 text-rose-400',
    purple: 'bg-purple-500/10 border-purple-500/20 text-purple-400',
    emerald: 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400',
  };

  return (
    <div className="bg-slate-900/50 border border-slate-800 p-6 rounded-2xl hover:border-slate-700 transition-colors group">
      <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-4 transition-transform group-hover:scale-110 ${colors[color]}`}>
        <Icon size={20} />
      </div>
      <p className="text-slate-500 text-sm font-medium">{title}</p>
      <h4 className="text-2xl font-bold mt-1 text-white">{value}</h4>
      <p className="text-slate-600 text-xs mt-1">{desc}</p>
    </div>
  );
}

interface HistoryItemProps {
  date: string;
  score: number;
  status: 'Low' | 'Moderate' | 'High';
}

function HistoryItem({ date, score, status }: HistoryItemProps) {
  const statusColors = {
    Low: 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20',
    Moderate: 'text-cyan-400 bg-cyan-500/10 border-cyan-500/20',
    High: 'text-rose-400 bg-rose-500/10 border-rose-500/20',
  };

  return (
    <div className="flex items-center justify-between p-4 rounded-2xl border border-slate-800/50 bg-slate-900/20 hover:bg-slate-900/40 transition-all group">
      <div className="flex items-center gap-4">
        <div className="w-10 h-10 rounded-full bg-slate-800 flex items-center justify-center text-xs font-bold text-slate-400">
          {date.split(' ')[1]}
        </div>
        <div>
          <p className="font-medium text-slate-200">{date}</p>
          <p className="text-xs text-slate-500">Daily Stress Check-in</p>
        </div>
      </div>
      <div className="flex items-center gap-6">
        <div className="text-right hidden sm:block">
          <p className="text-sm font-bold text-white">{score}</p>
          <p className="text-[10px] text-slate-600 uppercase tracking-tighter font-bold">Score</p>
        </div>
        <div className={`px-4 py-1.5 rounded-full text-xs font-bold border ${statusColors[status]}`}>
          {status}
        </div>
        <button className="p-2 text-slate-600 hover:text-emerald-400 transition-colors">
          <ArrowUpRight size={20} />
        </button>
      </div>
    </div>
  );
}
