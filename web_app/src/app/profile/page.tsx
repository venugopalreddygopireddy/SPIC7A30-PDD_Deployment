"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/Layout/MainLayout';
import { getProfile, updateProfile, getDashboardSummary, DashboardSummaryResponse, getHistory, deleteAccount } from '@/lib/api';
import { ChevronRight, User, Palette, Globe, Bell, Shield, HelpCircle, LogOut, ArrowLeft, Flame, Activity, Check, Download, Trash2, Clock, Sparkles, Lightbulb, Trophy, Heart, Info, FileText, Mail, Star, Leaf, TrendingUp, Headphones } from 'lucide-react';
import { useTranslation } from '@/components/TranslationProvider';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

export default function ProfileScreen() {
  const router = useRouter();
  const { t, language, setLanguage: setAppLanguage } = useTranslation();
  const [userEmail, setUserEmail] = useState('');
  
  // Profile state
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [mobile, setMobile] = useState('');
  const [dob, setDob] = useState('');
  const [age, setAge] = useState<number>(0);
  const [gender, setGender] = useState('');
  const [goal, setGoal] = useState('');

  // Dashboard state
  const [dashboard, setDashboard] = useState<DashboardSummaryResponse | null>(null);
  const [history, setHistory] = useState<any[]>([]);

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  
  // View state
  const [activeView, setActiveView] = useState('main');
  
  // Modals
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [showDownloadModal, setShowDownloadModal] = useState(false);
  const [showWhatsNewModal, setShowWhatsNewModal] = useState(false);
  const [showTermsModal, setShowTermsModal] = useState(false);
  const [showContactModal, setShowContactModal] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  // App Settings State
  const [theme, setTheme] = useState('dark');
  const [dailyCheckinReminders, setDailyCheckinReminders] = useState(true);
  const [stressAlerts, setStressAlerts] = useState(true);
  const [recommendationUpdates, setRecommendationUpdates] = useState(true);
  const [achievementNotifications, setAchievementNotifications] = useState(true);

  useEffect(() => {
    const email = localStorage.getItem('userEmail');
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }
    if (email) setUserEmail(email);

    // Load Local Settings
    const savedTheme = localStorage.getItem('appTheme') || 'dark';
    setTheme(savedTheme);
    
    const applyTheme = (tName: string) => {
      let isLight = false;
      if (tName === 'light') isLight = true;
      else if (tName === 'system') {
        isLight = window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches;
      }
      
      if (isLight) {
        document.documentElement.style.filter = 'invert(1) hue-rotate(180deg)';
      } else {
        document.documentElement.style.filter = 'none';
      }
    };
    
    applyTheme(savedTheme);

    const mediaQuery = window.matchMedia('(prefers-color-scheme: light)');
    const handleSystemThemeChange = (e: MediaQueryListEvent) => {
      if (localStorage.getItem('appTheme') === 'system') {
        if (e.matches) {
          document.documentElement.style.filter = 'invert(1) hue-rotate(180deg)';
        } else {
          document.documentElement.style.filter = 'none';
        }
      }
    };
    
    mediaQuery.addEventListener('change', handleSystemThemeChange);

    setDailyCheckinReminders(localStorage.getItem('appDailyCheckinReminders_' + email) !== 'false');
    setStressAlerts(localStorage.getItem('appStressAlerts_' + email) !== 'false');
    setRecommendationUpdates(localStorage.getItem('appRecommendationUpdates_' + email) !== 'false');
    setAchievementNotifications(localStorage.getItem('appAchievementNotifications_' + email) !== 'false');

    loadData();

    return () => {
      mediaQuery.removeEventListener('change', handleSystemThemeChange);
    };
  }, [router]);

  const loadData = async () => {
    try {
      const [profileData, dashboardData, historyData] = await Promise.all([
        getProfile(),
        getDashboardSummary(),
        getHistory(0, 100)
      ]);
      
      setFirstName(profileData.first_name || '');
      setLastName(profileData.last_name || '');
      setMobile(profileData.mobile_number || '');
      setDob(profileData.dob || '');
      setAge(profileData.age || 0);
      setGender(profileData.gender || '');
      setGoal(profileData.goal || '');
      
      setDashboard(dashboardData);
      setHistory(historyData);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  const calculateAge = (dateString: string) => {
    if (!dateString) return 0;
    const parts = dateString.split('/');
    if (parts.length !== 3) return 0;
    const birthDate = new Date(parseInt(parts[2]), parseInt(parts[1]) - 1, parseInt(parts[0]));
    const today = new Date();
    let calculatedAge = today.getFullYear() - birthDate.getFullYear();
    const m = today.getMonth() - birthDate.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
      calculatedAge--;
    }
    return Math.max(0, calculatedAge);
  };

  const handleDobChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value;
    setDob(val);
    setAge(calculateAge(val));
  };

  const handleSaveProfile = async () => {
    setIsSaving(true);
    try {
      await updateProfile({
        first_name: firstName,
        last_name: lastName,
        mobile_number: mobile,
        dob: dob,
        age: age,
        gender: gender,
        goal: goal
      });
      alert('Profile saved successfully!');
      setActiveView('main');
    } catch (e) {
      console.error(e);
      alert('Failed to save profile.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleRateUs = () => {
    if (typeof window !== 'undefined') {
      const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
      if (isMobile) {
        window.open('https://play.google.com/store/apps/details?id=com.cortisense.app.notfound', '_blank');
      } else {
        window.open('ms-windows-store://pdp/?ProductId=CortiSenseApp123', '_blank');
      }
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userEmail');
    router.push('/login');
  };

  const handleThemeChange = (newTheme: string) => {
    setTheme(newTheme);
    localStorage.setItem('appTheme', newTheme);
    
    let isLight = false;
    if (newTheme === 'light') isLight = true;
    else if (newTheme === 'system') {
      isLight = window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches;
    }
    
    if (isLight) {
      document.documentElement.style.filter = 'invert(1) hue-rotate(180deg)';
    } else {
      document.documentElement.style.filter = 'none';
    }
  };

  const handleLanguageChange = (lang: string) => {
    setAppLanguage(lang as any);
    if (userEmail) {
      localStorage.setItem('appLanguage_' + userEmail, lang);
    }
  };

  const handleNotificationToggle = async (key: string, currentValue: boolean, setter: React.Dispatch<React.SetStateAction<boolean>>) => {
    const newValue = !currentValue;
    if (newValue && !("Notification" in window)) {
      alert("This browser does not support desktop notification");
      return;
    }
    if (newValue && Notification.permission !== "granted") {
      const permission = await Notification.requestPermission();
      if (permission !== "granted") {
        alert("Notifications are blocked by your browser.");
        return;
      }
    }
    setter(newValue);
    if (userEmail) {
      localStorage.setItem(`app${key}_${userEmail}`, newValue.toString());
    }
  };

  const handleExportData = async (format: 'csv' | 'pdf') => {
    try {
      setIsExporting(true);
      const history = await getHistory(0, 10000);
      
      if (format === 'csv') {
        if (!history || history.length === 0) {
          alert('No data available to export.');
          setIsExporting(false);
          setShowDownloadModal(false);
          return;
        }
        
        const headers = Object.keys(history[0]).join(',');
        const rows = history.map(row => 
          Object.values(row).map(val => `"${val !== null && val !== undefined ? String(val).replace(/"/g, '""') : ''}"`).join(',')
        );
        const csvContent = headers + '\n' + rows.join('\n');
        
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.setAttribute('href', url);
        link.setAttribute('download', 'CortiSense_Data.csv');
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
      } else if (format === 'pdf') {
        const doc = new jsPDF();
        doc.setFontSize(18);
        doc.text('CortiSense Data Export', 14, 22);
        doc.setFontSize(11);
        doc.setTextColor(100);
        
        if (history && history.length > 0) {
          doc.text(`Total Records: ${history.length}`, 14, 30);
          const tableData = history.map((h: any) => [
            new Date(h.timestamp).toLocaleDateString(),
            h.stress_level || 'N/A',
            h.score ? h.score.toString() : '0',
            h.sleep_duration ? h.sleep_duration.toString() : '0',
            h.mood || 'N/A'
          ]);
          
          autoTable(doc, {
            startY: 40,
            head: [['Date', 'Stress Level', 'Score', 'Sleep', 'Mood']],
            body: tableData,
          });
        } else {
          doc.text('No check-in data available.', 14, 30);
        }
        
        doc.save('CortiSense_Data.pdf');
      }
      
      setShowDownloadModal(false);
    } catch (err) {
      console.error('Export error:', err);
      alert('Failed to export data.');
    } finally {
      setIsExporting(false);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      await deleteAccount();
      localStorage.clear();
      router.push('/signup');
    } catch (err) {
      console.error('Failed to delete account:', err);
      alert('Failed to delete account. Please try again.');
    }
  };

  if (isLoading) {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 flex justify-center items-center h-full bg-[#050810]">
          <div className="text-emerald-500 font-medium animate-pulse">{t("Loading...")}</div>
        </div>
      </MainLayout>
    );
  }

  const fullName = [firstName, lastName].filter(Boolean).join(' ');

  const renderHeader = (title: string) => (
    <div className="flex items-center mb-6">
      <button onClick={() => setActiveView('main')} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
        <ArrowLeft size={24} />
      </button>
      <h1 className="text-xl font-bold text-white ml-2">{title}</h1>
    </div>
  );

  if (activeView === 'edit_profile') {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
          {renderHeader(t('Edit Profile'))}
          <div className="flex flex-col items-center mb-8 mt-2">
            <div className="w-24 h-24 bg-emerald-500 rounded-3xl flex items-center justify-center text-[#050810] font-bold text-4xl mb-4 shadow-lg shadow-emerald-500/20">
              {firstName ? firstName.charAt(0).toUpperCase() : (userEmail ? userEmail.charAt(0).toUpperCase() : 'U')}
            </div>
            {fullName && <h2 className="text-xl font-bold text-white mb-1">{fullName}</h2>}
            <p className="text-slate-400 text-sm font-medium">{userEmail}</p>
          </div>

          <div className="space-y-4">
            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">First Name</label>
              <input type="text" value={firstName} onChange={(e) => setFirstName(e.target.value)} className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"/>
            </div>
            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Last Name</label>
              <input type="text" value={lastName} onChange={(e) => setLastName(e.target.value)} className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"/>
            </div>
            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Mobile Number</label>
              <input type="text" value={mobile} onChange={(e) => setMobile(e.target.value)} className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"/>
            </div>
            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Date of Birth (DD/MM/YYYY)</label>
              <input type="text" value={dob} onChange={handleDobChange} placeholder="DD/MM/YYYY" className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"/>
            </div>
            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Age</label>
              <input type="text" value={age} readOnly className="w-full mt-1 bg-slate-900/30 border border-slate-800 p-4 rounded-2xl text-slate-400 outline-none"/>
            </div>
            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Gender</label>
              <input type="text" value={gender} onChange={(e) => setGender(e.target.value)} className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"/>
            </div>

            <div className="pt-4">
              <h3 className="text-white font-bold mb-3 ml-1">Primary Goal</h3>
              <div className="space-y-3">
                {['Reduce Stress', 'Better Sleep', 'Improve Focus'].map((g) => (
                  <button key={g} onClick={() => setGoal(g)} className={`w-full p-4 rounded-2xl text-left flex items-center transition-colors ${goal === g ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-900/50 text-white border border-slate-800'}`}>
                    <div className={`w-5 h-5 rounded-full border-2 mr-3 flex items-center justify-center ${goal === g ? 'border-emerald-400' : 'border-slate-500'}`}>
                      {goal === g && <div className="w-2.5 h-2.5 bg-emerald-400 rounded-full"></div>}
                    </div>
                    <span className="font-medium">{t(g)}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="pt-8 pb-12">
              <button onClick={handleSaveProfile} disabled={isSaving} className="w-full bg-emerald-500 text-[#050810] font-bold p-4 rounded-2xl hover:bg-emerald-400 transition-colors">
                {isSaving ? t('Saving...') : t('Save Profile')}
              </button>
            </div>
          </div>
        </div>
      </MainLayout>
    );
  }

  if (activeView === 'appearance') {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
          {renderHeader(t('Appearance'))}
          <div className="mt-4 space-y-4">
            
            <h3 className="text-white font-bold mb-3">{t('Theme')}</h3>
            <div className="space-y-3">
              <button onClick={() => handleThemeChange('system')} className={`w-full p-4 rounded-2xl text-left flex items-center transition-colors ${theme === 'system' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-900/50 text-white border border-slate-800'}`}>
                <div className={`w-5 h-5 rounded-full border-2 mr-3 flex items-center justify-center ${theme === 'system' ? 'border-emerald-400' : 'border-slate-500'}`}>
                  {theme === 'system' && <div className="w-2.5 h-2.5 bg-emerald-400 rounded-full"></div>}
                </div>
                <span className="font-medium">{t('System Default')}</span>
              </button>

              <button onClick={() => handleThemeChange('dark')} className={`w-full p-4 rounded-2xl text-left flex items-center transition-colors ${theme === 'dark' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-900/50 text-white border border-slate-800'}`}>
                <div className={`w-5 h-5 rounded-full border-2 mr-3 flex items-center justify-center ${theme === 'dark' ? 'border-emerald-400' : 'border-slate-500'}`}>
                  {theme === 'dark' && <div className="w-2.5 h-2.5 bg-emerald-400 rounded-full"></div>}
                </div>
                <span className="font-medium">{t('Dark Mode (Recommended)')}</span>
              </button>

              <button onClick={() => handleThemeChange('light')} className={`w-full p-4 rounded-2xl text-left flex items-center transition-colors ${theme === 'light' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-900/50 text-white border border-slate-800'}`}>
                <div className={`w-5 h-5 rounded-full border-2 mr-3 flex items-center justify-center ${theme === 'light' ? 'border-emerald-400' : 'border-slate-500'}`}>
                  {theme === 'light' && <div className="w-2.5 h-2.5 bg-emerald-400 rounded-full"></div>}
                </div>
                <span className="font-medium">{t('Light Mode')}</span>
              </button>
            </div>
          </div>
        </div>
      </MainLayout>
    );
  }

  if (activeView === 'language') {
    const languages = ['English', 'Telugu', 'Hindi', 'Tamil'];
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
          {renderHeader(t('Language'))}
          <div className="mt-4 space-y-4">
            <p className="text-slate-400 text-sm mb-6">{t('Select your preferred language')}</p>
            <div className="space-y-3">
              {languages.map((lang) => (
                <button key={lang} onClick={() => handleLanguageChange(lang)} className={`w-full p-4 rounded-2xl text-left flex items-center transition-colors ${language === lang ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' : 'bg-slate-900/50 text-white border border-slate-800'}`}>
                  <div className="flex-1 font-medium">{lang}</div>
                  {language === lang && <Check size={20} className="text-emerald-400" />}
                </button>
              ))}
            </div>
          </div>
        </div>
      </MainLayout>
    );
  }

  if (activeView === 'notifications') {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
          {renderHeader(t('Notifications'))}
          <div className="mt-4 space-y-4">
            
            <div className="bg-[#2a2246] border border-[#3b325c] rounded-2xl p-5 mb-6">
              <p className="text-[#a79bc7] text-sm font-medium leading-relaxed">
                {t('Notifications help you stay on track with your wellness goals. You can customize which alerts you receive.')}
              </p>
            </div>

            <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-5 flex items-center justify-between">
              <div className="flex items-center flex-1">
                <div className="w-12 h-12 bg-[#12141c] rounded-2xl flex items-center justify-center mr-4">
                  <Clock className="text-emerald-500" size={20} />
                </div>
                <div>
                  <h3 className="text-white font-bold text-base">{t('Daily Check-in Reminders')}</h3>
                  <p className="text-slate-400 text-xs mt-1 font-medium">{t('Remind me to log my daily data')}</p>
                </div>
              </div>
              <button onClick={() => handleNotificationToggle('DailyCheckinReminders', dailyCheckinReminders, setDailyCheckinReminders)} 
                      className={`w-14 h-8 rounded-full p-1 transition-colors ${dailyCheckinReminders ? 'bg-[#82c885]' : 'bg-[#2a2d3c]'}`}>
                <div className={`w-6 h-6 rounded-full bg-[#11131c] transition-transform ${dailyCheckinReminders ? 'translate-x-6' : 'translate-x-0'}`}></div>
              </button>
            </div>

            <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-5 flex items-center justify-between">
              <div className="flex items-center flex-1">
                <div className="w-12 h-12 bg-[#12141c] rounded-2xl flex items-center justify-center mr-4">
                  <Sparkles className="text-emerald-500" size={20} />
                </div>
                <div>
                  <h3 className="text-white font-bold text-base">{t('Stress Alerts')}</h3>
                  <p className="text-slate-400 text-xs mt-1 font-medium">{t('Notify when stress levels are high')}</p>
                </div>
              </div>
              <button onClick={() => handleNotificationToggle('StressAlerts', stressAlerts, setStressAlerts)} 
                      className={`w-14 h-8 rounded-full p-1 transition-colors ${stressAlerts ? 'bg-[#82c885]' : 'bg-[#2a2d3c]'}`}>
                <div className={`w-6 h-6 rounded-full bg-[#11131c] transition-transform ${stressAlerts ? 'translate-x-6' : 'translate-x-0'}`}></div>
              </button>
            </div>

            <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-5 flex items-center justify-between">
              <div className="flex items-center flex-1">
                <div className="w-12 h-12 bg-[#12141c] rounded-2xl flex items-center justify-center mr-4">
                  <Lightbulb className="text-emerald-500" size={20} />
                </div>
                <div>
                  <h3 className="text-white font-bold text-base">{t('Recommendation Updates')}</h3>
                  <p className="text-slate-400 text-xs mt-1 font-medium">{t('New personalized suggestions')}</p>
                </div>
              </div>
              <button onClick={() => handleNotificationToggle('RecommendationUpdates', recommendationUpdates, setRecommendationUpdates)} 
                      className={`w-14 h-8 rounded-full p-1 transition-colors ${recommendationUpdates ? 'bg-[#82c885]' : 'bg-[#2a2d3c]'}`}>
                <div className={`w-6 h-6 rounded-full bg-[#11131c] transition-transform ${recommendationUpdates ? 'translate-x-6' : 'translate-x-0'}`}></div>
              </button>
            </div>

            <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-5 flex items-center justify-between">
              <div className="flex items-center flex-1">
                <div className="w-12 h-12 bg-[#12141c] rounded-2xl flex items-center justify-center mr-4">
                  <Trophy className="text-emerald-500" size={20} />
                </div>
                <div>
                  <h3 className="text-white font-bold text-base">{t('Achievement Notifications')}</h3>
                  <p className="text-slate-400 text-xs mt-1 font-medium">{t('Badges and milestone celebrations')}</p>
                </div>
              </div>
              <button onClick={() => handleNotificationToggle('AchievementNotifications', achievementNotifications, setAchievementNotifications)} 
                      className={`w-14 h-8 rounded-full p-1 transition-colors ${achievementNotifications ? 'bg-[#82c885]' : 'bg-[#2a2d3c]'}`}>
                <div className={`w-6 h-6 rounded-full bg-[#11131c] transition-transform ${achievementNotifications ? 'translate-x-6' : 'translate-x-0'}`}></div>
              </button>
            </div>

          </div>
        </div>
      </MainLayout>
    );
  }


  if (activeView === 'privacy') {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
          <div className="flex items-center mb-2">
            <button onClick={() => setActiveView('main')} className="mr-4 mt-2">
              <ArrowLeft className="text-white" size={24} />
            </button>
            <div className="flex-1">
              <h2 className="text-white text-3xl font-bold">{t('Privacy & Data')}</h2>
              <p className="text-slate-400 text-sm">{t('Control your data and privacy')}</p>
            </div>
          </div>

          <div className="mt-6 space-y-6">
            
            <div className="bg-[#2a2246] border border-[#3b325c] rounded-2xl p-5 mb-6 flex items-start">
              <Shield className="text-emerald-500 mr-4 mt-1 flex-shrink-0" size={24} />
              <div>
                <h3 className="text-white font-bold text-base mb-2">{t('Your Data is Protected')}</h3>
                <p className="text-[#a79bc7] text-sm font-medium leading-relaxed">
                  {t('All your health data is encrypted and stored securely. We never share your personal information with third parties.')}
                </p>
              </div>
            </div>

            <div>
              <h3 className="text-white font-bold mb-4 ml-1">{t('Privacy Policy')}</h3>
              <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-6">
                <div className="space-y-6">
                  <div>
                    <h4 className="text-white font-bold mb-1">1. {t('Information Collection')}</h4>
                    <p className="text-slate-400 text-sm leading-relaxed">{t('We collect account details, mood logs, stress levels, and usage analytics to provide personalized insights.')}</p>
                  </div>
                  <div>
                    <h4 className="text-white font-bold mb-1">2. {t('How Data is Used')}</h4>
                    <p className="text-slate-400 text-sm leading-relaxed">{t('Your data is used exclusively to generate wellness insights, improve our AI accuracy, and provide relevant notifications.')}</p>
                  </div>
                  <div>
                    <h4 className="text-white font-bold mb-1">3. {t('Data Sharing & Security')}</h4>
                    <p className="text-slate-400 text-sm leading-relaxed">{t('We do not sell your personal data. All health and account data is encrypted and stored securely.')}</p>
                  </div>
                  <div>
                    <h4 className="text-white font-bold mb-1">4. {t('User Rights')}</h4>
                    <p className="text-slate-400 text-sm leading-relaxed">{t('You have the right to access, export, or permanently delete your data at any time using the options below.')}</p>
                  </div>
                  <div>
                    <h4 className="text-white font-bold mb-1">5. {t('Contact')}</h4>
                    <p className="text-slate-400 text-sm leading-relaxed">{t('For privacy inquiries, contact privacy@cortisense.com.')}</p>
                  </div>
                </div>
              </div>
            </div>

            <div>
              <h3 className="text-white font-bold mb-4 ml-1 uppercase text-xs tracking-wider">{t('YOUR DATA')}</h3>
              <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-2">
                <button onClick={() => setShowDownloadModal(true)} className="w-full flex items-center p-4 transition-colors hover:bg-slate-800/50 rounded-2xl">
                  <Download className="text-white mr-4" size={24} />
                  <div className="text-left flex-1">
                    <h4 className="text-white font-bold text-base">{t('Download My Data')}</h4>
                    <p className="text-slate-400 text-xs font-medium">{t('Export all your data')}</p>
                  </div>
                </button>
                
                <div className="h-px bg-slate-800/50 mx-4 my-1"></div>
                
                <button onClick={() => setShowDeleteModal(true)} className="w-full flex items-center p-4 transition-colors hover:bg-slate-800/50 rounded-2xl">
                  <Trash2 className="text-red-500 mr-4" size={24} />
                  <div className="text-left flex-1">
                    <h4 className="text-red-500 font-bold text-base">{t('Delete My Account')}</h4>
                    <p className="text-slate-400 text-xs font-medium">{t('Permanently remove all data')}</p>
                  </div>
                </button>
              </div>
            </div>

          </div>

          {/* Delete Account Modal */}
          {showDeleteModal && (
            <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-6">
              <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-6 w-full max-w-sm">
                <div className="w-16 h-16 rounded-full bg-red-500/20 flex items-center justify-center mx-auto mb-4">
                  <Trash2 className="text-red-500" size={32} />
                </div>
                <h3 className="text-white text-xl font-bold text-center mb-2">{t('Delete Account?')}</h3>
                <p className="text-slate-400 text-center text-sm mb-6">
                  {t('Do you really want to delete your account? This action cannot be undone and all your data will be permanently lost.')}
                </p>
                <div className="space-y-3">
                  <button onClick={handleDeleteAccount} className="w-full py-4 rounded-full bg-red-500 text-white font-bold">
                    {t('Yes, Delete My Account')}
                  </button>
                  <button onClick={() => setShowDeleteModal(false)} className="w-full py-4 rounded-full bg-slate-800 text-white font-bold">
                    {t('Cancel')}
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Download Data Modal */}
          {showDownloadModal && (
            <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-6">
              <div className="bg-[#1c1e2b] border border-slate-800 rounded-3xl p-6 w-full max-w-sm">
                <h3 className="text-white text-xl font-bold text-center mb-2">{t('Download Data')}</h3>
                <p className="text-slate-400 text-center text-sm mb-6">
                  {t('Choose a format to export your data.')}
                </p>
                <div className="space-y-3">
                  <button onClick={() => handleExportData('pdf')} disabled={isExporting} className="w-full py-4 rounded-full bg-emerald-500 text-white font-bold flex items-center justify-center disabled:opacity-50">
                    {isExporting ? t('Exporting...') : t('Download as PDF')}
                  </button>
                  <button onClick={() => handleExportData('csv')} disabled={isExporting} className="w-full py-4 rounded-full bg-blue-500 text-white font-bold flex items-center justify-center disabled:opacity-50">
                    {isExporting ? t('Exporting...') : t('Download as CSV')}
                  </button>
                  <button onClick={() => setShowDownloadModal(false)} className="w-full py-4 rounded-full bg-slate-800 text-white font-bold">
                    {t('Cancel')}
                  </button>
                </div>
              </div>
            </div>
          )}

        </div>
      </MainLayout>
    );
  }

  if (activeView === 'about') {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#11131c]">
          <div className="flex items-center mb-6">
            <button onClick={() => setActiveView('main')} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
              <ArrowLeft size={24} />
            </button>
          </div>
          
          <div className="flex flex-col items-center mb-8">
            <div className="w-24 h-24 mb-4 flex items-center justify-center">
              <img src="/logo.png" alt="CortiSense Logo" className="w-full h-full object-contain rounded-full shadow-lg shadow-emerald-500/20" />
            </div>
            <h2 className="text-3xl font-bold text-white">CortiSense</h2>
            <p className="text-[#8c8c9a] text-base mt-1 font-medium">Version 1.1.0</p>
          </div>

          <div className="bg-[#1c1e2b] border border-[#2a2d3c] rounded-[1.5rem] p-6 mb-6">
            <h3 className="text-white font-bold text-lg mb-3">About CortiSense</h3>
            <p className="text-[#a79bc7] text-sm leading-relaxed font-medium">
              CortiSense is an AI-powered stress monitoring application that helps you predict and prevent burnout by analyzing your physiological, psychological, and lifestyle data.
            </p>
          </div>

          <div className="space-y-4 mb-6">
            <button onClick={() => setShowWhatsNewModal(true)} className="w-full bg-[#1c1e2b] border border-[#2a2d3c] rounded-2xl p-5 flex items-center justify-between hover:bg-[#252838] transition-colors">
              <div className="flex items-center">
                <div className="w-6 h-6 bg-[#2a2d3c] rounded-full flex items-center justify-center mr-4">
                  <Info className="text-slate-300" size={14} />
                </div>
                <span className="text-white font-bold text-base">What's New</span>
              </div>
              <ChevronRight className="text-white" size={20} />
            </button>

            <button onClick={() => setShowTermsModal(true)} className="w-full bg-[#1c1e2b] border border-[#2a2d3c] rounded-2xl p-5 flex items-center justify-between hover:bg-[#252838] transition-colors">
              <div className="flex items-center">
                <FileText className="text-slate-300 mr-4" size={20} />
                <span className="text-white font-bold text-base">Terms & Privacy</span>
              </div>
              <ChevronRight className="text-white" size={20} />
            </button>

            <button onClick={() => setShowContactModal(true)} className="w-full bg-[#1c1e2b] border border-[#2a2d3c] rounded-2xl p-5 flex items-center justify-between hover:bg-[#252838] transition-colors">
              <div className="flex items-center">
                <Mail className="text-slate-300 mr-4" size={20} />
                <span className="text-white font-bold text-base">Contact Support</span>
              </div>
              <ChevronRight className="text-white" size={20} />
            </button>

            <button onClick={handleRateUs} className="w-full bg-[#1c1e2b] border border-[#2a2d3c] rounded-2xl p-5 flex items-center justify-between hover:bg-[#252838] transition-colors mt-2">
              <div className="flex items-center">
                <Star className="text-slate-300 mr-4" size={20} fill="currentColor" />
                <span className="text-white font-bold text-base">Rate Us</span>
              </div>
              <ChevronRight className="text-white" size={20} />
            </button>
          </div>

          <a href="sms:+917981821290" className="mt-6 mb-8 text-center block hover:opacity-80 transition-opacity">
            <h3 className="text-white font-bold text-lg">Need Help?</h3>
          </a>

          {/* What's New Modal */}
          {showWhatsNewModal && (
            <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
              <div className="bg-[#2a2d3c] rounded-[2rem] p-6 w-full max-w-[400px] shadow-2xl relative">
                <h3 className="text-white text-2xl font-bold mb-6">What's New in v1.1.0 🚀</h3>
                
                <div className="space-y-6 mb-8 max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
                  <div className="flex items-start">
                    <div className="w-12 h-12 bg-[#3b443c] rounded-full flex items-center justify-center mr-4 flex-shrink-0">
                      <TrendingUp className="text-[#82c885]" size={24} />
                    </div>
                    <div>
                      <h4 className="text-white font-bold text-lg mb-1">Personalized Stress Insights</h4>
                      <p className="text-[#a79bc7] text-sm leading-relaxed">
                        CortiSense AI now delivers deeper analysis of your daily check-ins to identify hidden stress patterns.
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start">
                    <div className="w-12 h-12 bg-[#3b443c] rounded-full flex items-center justify-center mr-4 flex-shrink-0">
                      <Leaf className="text-[#82c885]" size={24} />
                    </div>
                    <div>
                      <h4 className="text-white font-bold text-lg mb-1">New Stress Relief Tasks</h4>
                      <p className="text-[#a79bc7] text-sm leading-relaxed">
                        Earn coins by completing daily tasks like Deep Breathing, Walking, and Hydration to reach your wellness goals.
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start">
                    <div className="w-12 h-12 bg-[#3b443c] rounded-full flex items-center justify-center mr-4 flex-shrink-0">
                      <Bell className="text-[#82c885]" size={24} />
                    </div>
                    <div>
                      <h4 className="text-white font-bold text-lg mb-1">Smart Reminders</h4>
                      <p className="text-[#a79bc7] text-sm leading-relaxed">
                        Never miss a check-in or task with our new adaptive notification system.
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start">
                    <div className="w-12 h-12 bg-[#3b443c] rounded-full flex items-center justify-center mr-4 flex-shrink-0">
                      <Headphones className="text-[#82c885]" size={24} />
                    </div>
                    <div>
                      <h4 className="text-white font-bold text-lg mb-1">Expanded Support</h4>
                      <p className="text-[#a79bc7] text-sm leading-relaxed">
                        Need help? You can now instantly reach our support team via WhatsApp, Email, or Phone.
                      </p>
                    </div>
                  </div>
                </div>

                <div className="flex justify-end">
                  <button onClick={() => setShowWhatsNewModal(false)} className="text-[#82c885] font-bold text-lg px-2 hover:opacity-80 transition-opacity">
                    Great!
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Terms & Privacy Modal */}
          {showTermsModal && (
            <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
              <div className="bg-[#2a2d3c] rounded-[2rem] p-6 w-full max-w-[400px] shadow-2xl relative">
                <h3 className="text-white text-2xl font-bold mb-1">Terms & Privacy 🔒</h3>
                <p className="text-[#8c8c9a] text-sm mb-6">Effective Date: May 26, 2026</p>
                
                <div className="space-y-5 mb-8 max-h-[60vh] overflow-y-auto pr-2 custom-scrollbar">
                  <div>
                    <h4 className="text-white font-bold text-lg mb-1">1. Introduction</h4>
                    <p className="text-[#a79bc7] text-sm leading-relaxed">
                      Welcome to CortiSense. By using our app, you agree to these Terms and Conditions. Please read them carefully. CortiSense is designed to help you track stress and well-being.
                    </p>
                  </div>

                  <div>
                    <h4 className="text-white font-bold text-lg mb-1">2. Health Disclaimer</h4>
                    <p className="text-[#a79bc7] text-sm leading-relaxed">
                      CortiSense is not a medical device. The insights, suggestions, and AI chat provided are for informational purposes only and do not constitute professional medical advice, diagnosis, or treatment.
                    </p>
                  </div>

                  <div>
                    <h4 className="text-white font-bold text-lg mb-1">3. Data Privacy & Security</h4>
                    <p className="text-[#a79bc7] text-sm leading-relaxed">
                      We take your privacy seriously. Your clinical history, mood records, and chat logs are stored securely. We do not sell your personal data to third parties. For AI features, anonymized text may be processed securely.
                    </p>
                  </div>

                  <div>
                    <h4 className="text-white font-bold text-lg mb-1">4. Premium Services</h4>
                    <p className="text-[#a79bc7] text-sm leading-relaxed">
                      Premium features can be unlocked using in-app coins. Coins are earned through daily activity. Virtual currency holds no real-world monetary value and cannot be exchanged for cash.
                    </p>
                  </div>

                  <div>
                    <h4 className="text-white font-bold text-lg mb-1">5. User Conduct</h4>
                    <p className="text-[#a79bc7] text-sm leading-relaxed">
                      You agree to use CortiSense for lawful purposes only. Misuse of the AI support system or exploiting coin mechanisms is strictly prohibited.
                    </p>
                  </div>

                  <div>
                    <h4 className="text-white font-bold text-lg mb-1">6. Changes to Terms</h4>
                    <p className="text-[#a79bc7] text-sm leading-relaxed">
                      We reserve the right to modify these terms at any time. Continued use of the app signifies your acceptance of any updated terms.
                    </p>
                  </div>
                </div>

                <div className="flex justify-end pt-2 border-t border-[#3b325c]">
                  <button onClick={() => setShowTermsModal(false)} className="text-[#82c885] font-bold text-lg px-2 hover:opacity-80 transition-opacity">
                    I Understand
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Contact Support Modal */}
          {showContactModal && (
            <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
              <div className="bg-[#2a2d3c] rounded-[2rem] p-6 w-full max-w-[320px] shadow-2xl relative">
                <h3 className="text-white text-2xl font-normal mb-8">Contact Support</h3>
                
                <div className="flex flex-col items-center space-y-6 mb-10">
                  <a href="mailto:cortisense.support@gmail.com" className="text-[#82c885] font-medium text-base hover:opacity-80 transition-opacity">
                    Mail Support
                  </a>
                  
                  <a href="https://wa.me/917013995242" target="_blank" rel="noopener noreferrer" className="text-[#82c885] font-medium text-base hover:opacity-80 transition-opacity">
                    WhatsApp Chat
                  </a>
                  
                  <a href="tel:+917013995242" className="text-[#82c885] font-medium text-base hover:opacity-80 transition-opacity">
                    Call Support
                  </a>
                </div>

                <div className="flex justify-end">
                  <button onClick={() => setShowContactModal(false)} className="text-[#82c885] font-medium text-base px-2 hover:opacity-80 transition-opacity">
                    Close
                  </button>
                </div>
              </div>
            </div>
          )}

        </div>
      </MainLayout>
    );
  }

  if (activeView === 'streak_tracker') {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();
    const daysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();
    const firstDay = new Date(currentYear, currentMonth, 1).getDay();

    const monthNames = ["January", "February", "March", "April", "May", "June",
      "July", "August", "September", "October", "November", "December"
    ];

    const visitedDays = new Set<number>();
    history.forEach(h => {
      const d = new Date(h.timestamp);
      if (d.getMonth() === currentMonth && d.getFullYear() === currentYear) {
        visitedDays.add(d.getDate());
      }
    });

    const totalVisitedThisMonth = visitedDays.size;
    const daysPassedThisMonth = now.getDate();
    const percentage = daysPassedThisMonth > 0 ? Math.round((totalVisitedThisMonth / daysPassedThisMonth) * 100) : 0;

    const blanks = Array.from({ length: firstDay }, (_, i) => null);
    const days = Array.from({ length: daysInMonth }, (_, i) => i + 1);
    const allCells = [...blanks, ...days];

    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#0a0c14]">
          <div className="flex items-center mb-8">
            <button onClick={() => setActiveView('main')} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
              <ArrowLeft size={24} />
            </button>
            <h1 className="text-2xl font-bold text-white ml-2">Streak Tracker</h1>
          </div>

          <div className="flex flex-col items-center mb-8">
            <div className="w-32 h-32 bg-[#1a2e22] rounded-[2rem] flex flex-col items-center justify-center mb-6 shadow-lg shadow-emerald-900/20">
              <Flame className="text-[#fb8c00]" size={64} fill="currentColor" />
            </div>
            <div className="flex items-baseline gap-2 mb-1">
              <span className="text-[56px] font-extrabold text-white leading-none">{dashboard?.current_streak || 0}</span>
              <span className="text-2xl font-bold text-slate-300">Days</span>
            </div>
            <p className="text-[#82e0aa] font-bold text-lg">Streak Active!</p>
          </div>

          <div className="grid grid-cols-2 gap-4 mb-8">
            <div className="bg-[#1c1e2b] border border-[#2a2d3c] rounded-[1.5rem] p-5 flex flex-col items-center justify-center">
              <span className="text-slate-300 font-bold text-sm mb-2">Longest</span>
              <span className="text-[#82e0aa] font-bold text-3xl">{dashboard?.longest_streak || 0}</span>
            </div>
            <div className="bg-[#1c1e2b] border border-[#2a2d3c] rounded-[1.5rem] p-5 flex flex-col items-center justify-center">
              <span className="text-slate-300 font-bold text-sm mb-2">Today's Checkins</span>
              <span className="text-[#82e0aa] font-bold text-3xl">{dashboard?.today_checkins_count || 0}</span>
            </div>
          </div>

          <h3 className="text-white font-bold text-xl mb-4">Check-in Calendar</h3>
          
          <div className="bg-[#1c1e2b] rounded-[2rem] p-6 mb-8">
            <div className="flex justify-between items-center mb-6">
              <h4 className="text-[#82e0aa] font-bold text-xl">{monthNames[currentMonth]} {currentYear}</h4>
              <div className="flex items-center gap-4 text-xs font-bold text-slate-300">
                <div className="flex items-center gap-1.5"><div className="w-2.5 h-2.5 rounded-full bg-[#82e0aa]"></div> Visited</div>
                <div className="flex items-center gap-1.5"><div className="w-2.5 h-2.5 rounded-full bg-[#3b443c]"></div> Missed</div>
              </div>
            </div>

            <div className="grid grid-cols-7 gap-y-6 gap-x-2 text-center mb-6">
              {['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'].map(day => (
                <div key={day} className="text-white font-bold text-sm">{day}</div>
              ))}
              
              {allCells.map((day, idx) => {
                if (day === null) return <div key={`blank-${idx}`}></div>;
                const isVisited = visitedDays.has(day);
                const isFuture = day > daysPassedThisMonth;
                
                if (isVisited) {
                  return (
                    <div key={day} className="flex justify-center">
                      <div className="w-10 h-10 bg-[#82e0aa] rounded-xl flex flex-col items-center justify-center">
                        <Check size={14} className="text-[#111116] mb-0.5 font-bold" strokeWidth={3} />
                        <span className="text-[#111116] text-xs font-bold leading-none">{day}</span>
                      </div>
                    </div>
                  );
                } else {
                  return (
                    <div key={day} className="flex justify-center items-center h-10">
                      <span className={`text-sm font-bold ${isFuture ? 'text-[#2a2d3c]' : 'text-[#3b443c]'}`}>{day}</span>
                    </div>
                  );
                }
              })}
            </div>

            <div className="border-t border-[#2a2d3c] pt-5 flex justify-between items-center">
              <div>
                <div className="text-white font-bold text-sm">{totalVisitedThisMonth} / {daysPassedThisMonth} days</div>
                <div className="text-slate-400 text-xs font-medium">checked in this month</div>
              </div>
              <div className="w-12 h-12 rounded-full bg-[#1a2e22] flex items-center justify-center text-[#82e0aa] font-bold text-sm">
                {percentage}%
              </div>
            </div>
          </div>

          <div className="text-center pb-8">
            <h3 className="text-white font-bold text-lg mb-1">Don't break the chain!</h3>
            <p className="text-slate-400 text-sm">Each daily check-in adds 1 day to your streak.</p>
          </div>

        </div>
      </MainLayout>
    );
  }

  // ==========================================
  // MAIN PROFILE VIEW
  // ==========================================

  return (
    <MainLayout activeTab="Profile">
      <div className="flex-1 overflow-y-auto px-6 py-8 flex flex-col h-full bg-[#050810]">
        
        {/* HEADER */}
        <div className="flex flex-col items-center mb-8 mt-2">
          <div className="w-24 h-24 mb-4 flex items-center justify-center">
            <img src="/logo.png" alt="CortiSense Logo" className="w-full h-full object-contain rounded-full shadow-lg shadow-emerald-500/20" />
          </div>
          {fullName && <h2 className="text-xl font-bold text-white mb-1">{fullName}</h2>}
          <p className="text-slate-400 text-sm font-medium">{userEmail}</p>
        </div>

        {/* SECTION 1 - Streak Card */}
        <div 
          onClick={() => setActiveView('streak_tracker')}
          className="bg-slate-900/50 border border-slate-800 rounded-2xl p-5 mb-4 flex items-center justify-between cursor-pointer hover:bg-slate-800/50 transition-colors"
        >
          <div className="flex items-center">
            <div className="w-12 h-12 bg-orange-500/20 rounded-2xl flex items-center justify-center mr-4">
              <Flame className="text-orange-500" size={24} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">{dashboard?.current_streak || 0} {t('Days Streak')}</h3>
            </div>
          </div>
          <ChevronRight className="text-slate-500" size={20} />
        </div>

        {/* SECTION 2 - Check-in & Clinical History */}
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-5 mb-8 flex items-center justify-between cursor-pointer hover:bg-slate-800/50 transition-colors"
             onClick={() => router.push('/history')}>
          <div className="flex items-center">
            <div className="w-12 h-12 bg-emerald-500/20 rounded-2xl flex items-center justify-center mr-4">
              <Activity className="text-emerald-500" size={24} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">{t('Clinical and Checkins History')}</h3>
            </div>
          </div>
          <ChevronRight className="text-slate-500" size={20} />
        </div>

        {/* ACCOUNT SECTION */}
        <div className="mb-4">
          <div className="bg-slate-900/50 border border-slate-800 rounded-3xl overflow-hidden">
            
            <button onClick={() => setActiveView('edit_profile')} className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <User className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">{t('Edit Profile')}</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button onClick={() => setActiveView('appearance')} className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Palette className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">{t('Appearance')}</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button onClick={() => setActiveView('language')} className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Globe className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">{t('Language')}</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button onClick={() => setActiveView('notifications')} className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Bell className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">{t('Notifications')}</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button onClick={() => setActiveView('privacy')} className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Shield className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">{t('Privacy & Data')}</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button onClick={() => setActiveView('about')} className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors">
              <div className="flex items-center">
                <HelpCircle className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">{t('About & Help')}</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>
            
          </div>
        </div>

        {/* FOOTER */}
        <div className="pt-8 pb-12 mt-auto">
          <button 
            onClick={handleLogout}
            className="w-full bg-rose-500/10 border border-rose-500/20 p-4 rounded-2xl flex items-center justify-center text-rose-500 font-bold hover:bg-rose-500/20 transition-colors"
          >
            <LogOut className="mr-2" size={20} />
            {t('Logout')}
          </button>
        </div>
      </div>
    </MainLayout>
  );
}
