import React, { useState, useEffect } from 'react';
import { ArrowLeft, Search, Check, Globe } from 'lucide-react';

interface Props {
  onBack: () => void;
}

const LANGUAGES = [
  { id: 'en', name: 'English', native: 'English', flag: '🇺🇸' },
  { id: 'hi', name: 'Hindi', native: 'हिंदी', flag: '🇮🇳' },
  { id: 'ta', name: 'Tamil', native: 'தமிழ்', flag: '🇮🇳' },
  { id: 'te', name: 'Telugu', native: 'తెలుగు', flag: '🇮🇳' }
];

export default function LanguageSettings({ onBack }: Props) {
  const [language, setLanguage] = useState('en');
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    const savedLang = localStorage.getItem('language');
    if (savedLang) {
      setLanguage(savedLang);
    }
  }, []);

  const handleLanguageChange = (id: string) => {
    setLanguage(id);
    localStorage.setItem('language', id);
    
    // Set Google Translate cookie
    const date = new Date();
    date.setTime(date.getTime() + (365 * 24 * 60 * 60 * 1000));
    const expires = "; expires=" + date.toUTCString();
    // Google translate uses /en/target_lang
    document.cookie = `googtrans=/en/${id}${expires}; path=/`;
    
    // Reload to apply translation immediately
    window.location.reload();
  };

  const filteredLanguages = LANGUAGES.filter(l => 
    l.name.toLowerCase().includes(searchQuery.toLowerCase()) || 
    l.native.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810] notranslate">
      <div className="flex items-center mb-6">
        <button onClick={onBack} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
          <ArrowLeft size={24} />
        </button>
      </div>
      
      <div className="flex items-center mb-6">
        <div className="w-12 h-12 bg-purple-500/20 rounded-xl flex items-center justify-center mr-4">
          <Globe className="text-purple-400" size={24} />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Language</h1>
          <p className="text-slate-400 text-sm">Select Language</p>
        </div>
      </div>

      <div className="relative mb-6">
        <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
          <Search className="text-slate-400" size={20} />
        </div>
        <input 
          type="text" 
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Language"
          className="w-full bg-slate-900/80 border border-slate-800 p-4 pl-12 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"
        />
      </div>

      <div className="space-y-4 mb-8">
        {filteredLanguages.map(lang => (
          <button 
            key={lang.id}
            onClick={() => handleLanguageChange(lang.id)}
            className={`w-full flex items-center justify-between p-5 rounded-2xl transition-colors border ${
              language === lang.id ? 'bg-emerald-500/10 border-emerald-500/50' : 'bg-slate-900/50 border-slate-800 hover:bg-slate-800/50'
            }`}
          >
            <div className="flex items-center">
              <span className="text-3xl mr-4">{lang.flag}</span>
              <div className="text-left">
                <span className="block font-bold text-lg text-white">{lang.native}</span>
                <span className="text-slate-400 text-sm">{lang.name}</span>
              </div>
            </div>
            {language === lang.id && <Check className="text-emerald-500" size={24} />}
          </button>
        ))}
        {filteredLanguages.length === 0 && (
          <p className="text-slate-400 text-center py-4">No languages found.</p>
        )}
      </div>

      <div className="mt-auto pt-8">
        <div className="bg-purple-900/20 border border-purple-500/20 p-5 rounded-2xl text-center">
          <p className="text-purple-300 font-medium">Predict Stress. Prevent Burnout.</p>
        </div>
      </div>

    </div>
  );
}
