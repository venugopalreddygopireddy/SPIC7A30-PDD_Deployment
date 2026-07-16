import React, { useState, useEffect } from 'react';
import { ArrowLeft, Sun, Moon, Settings, Check } from 'lucide-react';

interface Props {
  onBack: () => void;
}

export default function AppearanceSettings({ onBack }: Props) {
  const [theme, setTheme] = useState<'light' | 'dark' | 'system'>('dark');

  useEffect(() => {
    const savedTheme = localStorage.getItem('theme') as 'light' | 'dark' | 'system';
    if (savedTheme) {
      setTheme(savedTheme);
    }
  }, []);

  const handleThemeChange = (newTheme: 'light' | 'dark' | 'system') => {
    setTheme(newTheme);
    localStorage.setItem('theme', newTheme);
    
    // In a real app with Tailwind dark mode configured:
    if (newTheme === 'dark') {
      document.documentElement.classList.remove('light-theme');
    } else if (newTheme === 'light') {
      document.documentElement.classList.add('light-theme');
    } else {
      // System default logic
      if (window.matchMedia('(prefers-color-scheme: light)').matches) {
        document.documentElement.classList.add('light-theme');
      } else {
        document.documentElement.classList.remove('light-theme');
      }
    }
  };

  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
      <div className="flex items-center mb-6">
        <button onClick={onBack} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
          <ArrowLeft size={24} />
        </button>
      </div>
      
      <h1 className="text-3xl font-bold text-white mb-1">Appearance</h1>
      <p className="text-slate-400 text-sm mb-8">Choose your preferred theme</p>

      <h3 className="text-white font-bold text-lg mb-4">Current Theme Preview</h3>
      
      <div className="bg-slate-900/50 border border-slate-800 rounded-3xl p-1 mb-8 overflow-hidden">
        <div className="bg-purple-900/30 rounded-2xl p-4 mb-2">
          <div className="flex items-center mb-4">
            <div className="w-12 h-12 bg-emerald-500/20 rounded-xl flex items-center justify-center mr-4">
               <div className="w-6 h-6 bg-emerald-500 rounded-full flex items-center justify-center">
                 <div className="w-2 h-2 bg-[#050810] rounded-full"></div>
               </div>
            </div>
            <div>
              <h4 className="text-white font-bold text-lg">CortiSense</h4>
              <p className="text-purple-300/70 text-sm">Stress Score: 35</p>
            </div>
          </div>
        </div>
        <div className="flex justify-between px-6 py-2 text-xs text-slate-400 font-medium">
          <span>Heart rate</span>
          <span>Sleep</span>
          <span>Mood</span>
        </div>
      </div>

      <h3 className="text-slate-400 text-sm font-bold uppercase tracking-wider mb-4">SELECT THEME</h3>
      
      <div className="space-y-4 mb-10">
        <button 
          onClick={() => handleThemeChange('light')}
          className={`w-full flex items-center justify-between p-5 rounded-2xl transition-colors border ${
            theme === 'light' ? 'bg-emerald-500/10 border-emerald-500/50 text-emerald-400' : 'bg-slate-900/50 border-slate-800 hover:bg-slate-800/50 text-white'
          }`}
        >
          <div className="flex items-center">
            <Sun className={theme === 'light' ? 'text-emerald-400 mr-4' : 'text-slate-400 mr-4'} size={24} />
            <div className="text-left">
              <span className={`block font-bold text-lg ${theme === 'light' ? 'text-emerald-400' : 'text-white'}`}>Light mode</span>
              <span className="text-slate-400 text-sm">Classic light interface</span>
            </div>
          </div>
          {theme === 'light' && <Check className="text-emerald-500" size={24} />}
        </button>

        <button 
          onClick={() => handleThemeChange('dark')}
          className={`w-full flex items-center justify-between p-5 rounded-2xl transition-colors border ${
            theme === 'dark' ? 'bg-emerald-500/10 border-emerald-500/50 text-emerald-400' : 'bg-slate-900/50 border-slate-800 hover:bg-slate-800/50 text-white'
          }`}
        >
          <div className="flex items-center">
            <Moon className={theme === 'dark' ? 'text-emerald-400 mr-4' : 'text-slate-400 mr-4'} size={24} />
            <div className="text-left">
              <span className={`block font-bold text-lg ${theme === 'dark' ? 'text-emerald-400' : 'text-white'}`}>Dark Mode</span>
              <span className="text-slate-400 text-sm">Easy on the eyes in low light</span>
            </div>
          </div>
          {theme === 'dark' && <Check className="text-emerald-500" size={24} />}
        </button>

        <button 
          onClick={() => handleThemeChange('system')}
          className={`w-full flex items-center justify-between p-5 rounded-2xl transition-colors border ${
            theme === 'system' ? 'bg-emerald-500/10 border-emerald-500/50 text-emerald-400' : 'bg-slate-900/50 border-slate-800 hover:bg-slate-800/50 text-white'
          }`}
        >
          <div className="flex items-center">
            <Settings className={theme === 'system' ? 'text-emerald-400 mr-4' : 'text-slate-400 mr-4'} size={24} />
            <div className="text-left">
              <span className={`block font-bold text-lg ${theme === 'system' ? 'text-emerald-400' : 'text-white'}`}>System Default</span>
              <span className="text-slate-400 text-sm">Adapts to your device settings</span>
            </div>
          </div>
          {theme === 'system' && <Check className="text-emerald-500" size={24} />}
        </button>
      </div>

    </div>
  );
}
