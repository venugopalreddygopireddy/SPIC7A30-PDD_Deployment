import React, { useState } from 'react';
import { ArrowLeft, Clock, Activity, Lightbulb, Trophy } from 'lucide-react';

interface Props {
  onBack: () => void;
}

const Toggle = ({ active, onClick }: { active: boolean, onClick: () => void }) => (
  <button 
    onClick={onClick}
    className={`relative inline-flex h-8 w-14 items-center rounded-full transition-colors ${
      active ? 'bg-emerald-500' : 'bg-slate-700'
    }`}
  >
    <span 
      className={`inline-block h-6 w-6 transform rounded-full bg-[#050810] transition-transform ${
        active ? 'translate-x-7' : 'translate-x-1'
      }`}
    />
  </button>
);

export default function NotificationSettings({ onBack }: Props) {
  const [dailyCheckin, setDailyCheckin] = useState(true);
  const [stressAlerts, setStressAlerts] = useState(true);
  const [recommendations, setRecommendations] = useState(true);
  const [achievements, setAchievements] = useState(true);

  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
      <div className="flex items-center mb-6">
        <button onClick={onBack} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-2xl font-bold text-white ml-2">Notifications</h1>
      </div>
      
      <div className="bg-purple-900/20 border border-purple-500/20 p-5 rounded-2xl mb-8">
        <p className="text-purple-100 font-medium leading-relaxed">
          Notifications help you stay on track with your wellness goals. You can customize which alerts you receive.
        </p>
      </div>

      <div className="space-y-4">
        
        <div className="flex items-center justify-between p-5 bg-slate-900/50 border border-slate-800 rounded-2xl">
          <div className="flex items-center">
            <div className="w-12 h-12 bg-slate-800 rounded-xl flex items-center justify-center mr-4">
              <Clock className="text-slate-400" size={20} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">Daily Check-in Reminders</h3>
              <p className="text-slate-400 text-sm">Remind me to log my daily data</p>
            </div>
          </div>
          <Toggle active={dailyCheckin} onClick={() => setDailyCheckin(!dailyCheckin)} />
        </div>

        <div className="flex items-center justify-between p-5 bg-slate-900/50 border border-slate-800 rounded-2xl">
          <div className="flex items-center">
            <div className="w-12 h-12 bg-slate-800 rounded-xl flex items-center justify-center mr-4">
              <Activity className="text-slate-400" size={20} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">Stress Alerts</h3>
              <p className="text-slate-400 text-sm">Notify when stress levels are high</p>
            </div>
          </div>
          <Toggle active={stressAlerts} onClick={() => setStressAlerts(!stressAlerts)} />
        </div>

        <div className="flex items-center justify-between p-5 bg-slate-900/50 border border-slate-800 rounded-2xl">
          <div className="flex items-center">
            <div className="w-12 h-12 bg-slate-800 rounded-xl flex items-center justify-center mr-4">
              <Lightbulb className="text-slate-400" size={20} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">Recommendation Updates</h3>
              <p className="text-slate-400 text-sm">New personalized suggestions</p>
            </div>
          </div>
          <Toggle active={recommendations} onClick={() => setRecommendations(!recommendations)} />
        </div>

        <div className="flex items-center justify-between p-5 bg-slate-900/50 border border-slate-800 rounded-2xl">
          <div className="flex items-center">
            <div className="w-12 h-12 bg-slate-800 rounded-xl flex items-center justify-center mr-4">
              <Trophy className="text-slate-400" size={20} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">Achievement Notifications</h3>
              <p className="text-slate-400 text-sm">Badges and milestone celebrations</p>
            </div>
          </div>
          <Toggle active={achievements} onClick={() => setAchievements(!achievements)} />
        </div>

      </div>

    </div>
  );
}
