"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/Layout/MainLayout';
import { getProfile, updateProfile, getDashboardSummary, DashboardSummaryResponse } from '@/lib/api';
import { ChevronRight, User, Palette, Globe, Bell, Shield, HelpCircle, LogOut, ArrowLeft, Flame, Activity } from 'lucide-react';

export default function ProfileScreen() {
  const router = useRouter();
  const [userEmail, setUserEmail] = useState('');
  
  // Profile state
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [mobile, setMobile] = useState('');
  const [dob, setDob] = useState('');
  const [age, setAge] = useState<number>(0);
  const [gender, setGender] = useState('');
  const [goal, setGoal] = useState('');
  const [profileImage, setProfileImage] = useState<string | undefined>('');

  // Dashboard state
  const [dashboard, setDashboard] = useState<DashboardSummaryResponse | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    const email = localStorage.getItem('userEmail');
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }
    if (email) setUserEmail(email);

    loadData();
  }, [router]);

  const loadData = async () => {
    try {
      const [profileData, dashboardData] = await Promise.all([
        getProfile(),
        getDashboardSummary()
      ]);
      
      setFirstName(profileData.first_name || '');
      setLastName(profileData.last_name || '');
      setMobile(profileData.mobile_number || '');
      setDob(profileData.dob || '');
      setAge(profileData.age || 0);
      setGender(profileData.gender || '');
      setGoal(profileData.goal || '');
      setProfileImage(profileData.profile_image || '');
      
      setDashboard(dashboardData);
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

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await updateProfile({
        first_name: firstName,
        last_name: lastName,
        mobile_number: mobile,
        dob: dob,
        age: age,
        gender: gender,
        goal: goal,
        profile_image: profileImage
      });
      alert('Profile saved successfully!');
      setIsEditing(false);
    } catch (e) {
      console.error(e);
      alert('Failed to save profile.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (event) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        const MAX_WIDTH = 400;
        const MAX_HEIGHT = 400;
        let width = img.width;
        let height = img.height;

        if (width > height) {
          if (width > MAX_WIDTH) {
            height *= MAX_WIDTH / width;
            width = MAX_WIDTH;
          }
        } else {
          if (height > MAX_HEIGHT) {
            width *= MAX_HEIGHT / height;
            height = MAX_HEIGHT;
          }
        }
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        ctx?.drawImage(img, 0, 0, width, height);
        const dataUrl = canvas.toDataURL('image/jpeg', 0.8);
        setProfileImage(dataUrl);
      };
      img.src = event.target?.result as string;
    };
    reader.readAsDataURL(file);
  };

  const handleLogout = () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('userEmail');
    router.push('/welcome');
  };

  if (isLoading) {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 flex justify-center items-center h-full bg-[#050810]">
          <div className="text-emerald-500 font-medium">Loading...</div>
        </div>
      </MainLayout>
    );
  }

  const fullName = [firstName, lastName].filter(Boolean).join(' ');

  if (isEditing) {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 overflow-y-auto px-6 py-6 flex flex-col h-full bg-[#050810]">
          <div className="flex items-center mb-6">
            <button onClick={() => setIsEditing(false)} className="text-white p-2 -ml-2 rounded-full hover:bg-slate-800 transition-colors">
              <ArrowLeft size={24} />
            </button>
            <h1 className="text-xl font-bold text-white ml-2">Edit Profile</h1>
          </div>

          <div className="flex flex-col items-center mb-8 mt-2">
            <label className="cursor-pointer relative group">
              <div className="w-24 h-24 bg-emerald-500 rounded-3xl flex items-center justify-center text-[#050810] font-bold text-4xl shadow-lg shadow-emerald-500/20 overflow-hidden">
                {profileImage ? (
                  <img src={profileImage} alt="Profile" className="w-full h-full object-cover" />
                ) : (
                  firstName ? firstName.charAt(0).toUpperCase() : (userEmail ? userEmail.charAt(0).toUpperCase() : 'U')
                )}
              </div>
              <div className="absolute inset-0 bg-black/40 rounded-3xl flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                <span className="text-white text-xs font-bold">Upload</span>
              </div>
              <input type="file" accept="image/*" className="hidden" onChange={handleImageUpload} />
            </label>
            {fullName && <h2 className="text-xl font-bold text-white mb-1 mt-4">{fullName}</h2>}
            <p className="text-slate-400 text-sm font-medium">{userEmail}</p>
          </div>

          <div className="space-y-4">
            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">First Name</label>
              <input 
                type="text" 
                value={firstName} 
                onChange={(e) => setFirstName(e.target.value)}
                placeholder="Enter First Name"
                className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"
              />
            </div>

            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Last Name</label>
              <input 
                type="text" 
                value={lastName} 
                onChange={(e) => setLastName(e.target.value)}
                placeholder="Enter Last Name"
                className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"
              />
            </div>

            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Mobile Number</label>
              <input 
                type="text" 
                value={mobile} 
                onChange={(e) => setMobile(e.target.value)}
                placeholder="Enter Mobile Number"
                className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"
              />
            </div>

            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Date of Birth (DD/MM/YYYY)</label>
              <input 
                type="text" 
                value={dob} 
                onChange={handleDobChange}
                placeholder="DD/MM/YYYY"
                className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"
              />
            </div>

            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Age</label>
              <input 
                type="text" 
                value={age} 
                readOnly
                className="w-full mt-1 bg-slate-900/30 border border-slate-800 p-4 rounded-2xl text-slate-400 outline-none"
              />
            </div>

            <div>
              <label className="text-slate-400 text-sm font-medium ml-1">Gender</label>
              <input 
                type="text" 
                value={gender} 
                onChange={(e) => setGender(e.target.value)}
                placeholder="Enter Gender"
                className="w-full mt-1 bg-slate-900/50 border border-slate-800 p-4 rounded-2xl text-white outline-none focus:border-emerald-500 transition-colors"
              />
            </div>

            <div className="pt-4">
              <h3 className="text-white font-bold mb-3 ml-1">Primary Goal</h3>
              <div className="space-y-3">
                {['Reduce Stress', 'Better Sleep', 'Improve Focus'].map((g) => (
                  <button 
                    key={g}
                    onClick={() => setGoal(g)}
                    className={`w-full p-4 rounded-2xl text-left flex items-center transition-colors ${
                      goal === g 
                        ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/30' 
                        : 'bg-slate-900/50 text-white border border-slate-800'
                    }`}
                  >
                    <div className={`w-5 h-5 rounded-full border-2 mr-3 flex items-center justify-center ${
                      goal === g ? 'border-emerald-400' : 'border-slate-500'
                    }`}>
                      {goal === g && <div className="w-2.5 h-2.5 bg-emerald-400 rounded-full"></div>}
                    </div>
                    <span className="font-medium">{g}</span>
                  </button>
                ))}
              </div>
            </div>

            <div className="pt-8 pb-12">
              <button 
                onClick={handleSave}
                disabled={isSaving}
                className="w-full bg-emerald-500 text-[#050810] font-bold p-4 rounded-2xl hover:bg-emerald-400 transition-colors"
              >
                {isSaving ? 'Saving...' : 'Save Profile'}
              </button>
            </div>
          </div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout activeTab="Profile">
      <div className="flex-1 overflow-y-auto px-6 py-8 flex flex-col h-full bg-[#050810]">
        
        {/* HEADER */}
        <div className="flex flex-col items-center mb-8 mt-2">
          <div className="w-24 h-24 bg-emerald-500 rounded-3xl flex items-center justify-center text-[#050810] font-bold text-4xl mb-4 shadow-lg shadow-emerald-500/20 overflow-hidden">
            {profileImage ? (
              <img src={profileImage} alt="Profile" className="w-full h-full object-cover" />
            ) : (
              firstName ? firstName.charAt(0).toUpperCase() : (userEmail ? userEmail.charAt(0).toUpperCase() : 'U')
            )}
          </div>
          {fullName && <h2 className="text-xl font-bold text-white mb-1">{fullName}</h2>}
          <p className="text-slate-400 text-sm font-medium">{userEmail}</p>
        </div>

        {/* SECTION 1 - Streak Card */}
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-5 mb-4 flex items-center justify-between">
          <div className="flex items-center">
            <div className="w-12 h-12 bg-orange-500/20 rounded-2xl flex items-center justify-center mr-4">
              <Flame className="text-orange-500" size={24} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">{dashboard?.current_streak || 0} Days Streak</h3>
              <p className="text-slate-400 text-sm">Check in today to activate streak</p>
            </div>
          </div>
        </div>

        {/* SECTION 2 - Check-in & Clinical History */}
        <div className="bg-slate-900/50 border border-slate-800 rounded-2xl p-5 mb-8 flex items-center justify-between cursor-pointer hover:bg-slate-800/50 transition-colors"
             onClick={() => router.push('/history')}>
          <div className="flex items-center">
            <div className="w-12 h-12 bg-emerald-500/20 rounded-2xl flex items-center justify-center mr-4">
              <Activity className="text-emerald-500" size={24} />
            </div>
            <div>
              <h3 className="text-white font-bold text-lg">Check-in & Clinical History</h3>
              <p className="text-slate-400 text-sm">{dashboard?.total_checkins || 0} Total Check-ins</p>
            </div>
          </div>
          <ChevronRight className="text-slate-500" size={20} />
        </div>

        {/* ACCOUNT SECTION */}
        <div className="mb-4">
          <h3 className="text-slate-400 text-sm font-bold uppercase tracking-wider mb-4 ml-2">Account Settings</h3>
          <div className="bg-slate-900/50 border border-slate-800 rounded-3xl overflow-hidden">
            
            <button onClick={() => setIsEditing(true)} className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <User className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">Edit Profile</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Palette className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">Appearance</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Globe className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">Language</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Bell className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">Notifications</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors border-b border-slate-800/50">
              <div className="flex items-center">
                <Shield className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">Privacy & Data</span>
              </div>
              <ChevronRight className="text-slate-500" size={18} />
            </button>

            <button className="w-full flex items-center justify-between p-5 hover:bg-slate-800/50 transition-colors">
              <div className="flex items-center">
                <HelpCircle className="text-slate-400 mr-4" size={20} />
                <span className="text-white font-medium">About & Help</span>
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
            Logout
          </button>
        </div>
      </div>
    </MainLayout>
  );
}
