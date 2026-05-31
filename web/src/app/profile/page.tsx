"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/Layout/MainLayout';
import { getProfile, updateProfile } from '@/lib/api';

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

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    const email = localStorage.getItem('userEmail');
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
      return;
    }
    if (email) setUserEmail(email);

    loadProfile();
  }, [router]);

  const loadProfile = async () => {
    try {
      const data = await getProfile();
      setFirstName(data.first_name || '');
      setLastName(data.last_name || '');
      setMobile(data.mobile_number || '');
      setDob(data.dob || '');
      setAge(data.age || 0);
      setGender(data.gender || '');
      setGoal(data.goal || '');
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
        goal: goal
      });
      alert('Profile saved successfully!');
    } catch (e) {
      console.error(e);
      alert('Failed to save profile.');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <MainLayout activeTab="Profile">
        <div className="flex-1 flex justify-center items-center">
          <div className="text-emerald-500">Loading...</div>
        </div>
      </MainLayout>
    );
  }

  return (
    <MainLayout activeTab="Profile">
      <div className="flex-1 overflow-y-auto px-6 py-8 flex flex-col h-full bg-[#050810]">
        <div className="flex flex-col items-center mb-8 mt-4">
          <div className="w-24 h-24 bg-emerald-500 rounded-3xl flex items-center justify-center text-[#050810] font-bold text-4xl mb-4 shadow-lg shadow-emerald-500/20">
            {userEmail ? userEmail.charAt(0).toUpperCase() : 'U'}
          </div>
          <h2 className="text-xl font-bold text-white mb-1">{userEmail}</h2>
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

          <div className="pt-8 pb-4 space-y-4">
            <button 
              onClick={handleSave}
              disabled={isSaving}
              className="w-full bg-emerald-500 text-[#050810] font-bold p-4 rounded-2xl hover:bg-emerald-400 transition-colors"
            >
              {isSaving ? 'Saving...' : 'Save Profile'}
            </button>
            
            <button 
              onClick={() => {
                localStorage.removeItem('jwtToken');
                localStorage.removeItem('userEmail');
                router.push('/welcome');
              }}
              className="w-full bg-rose-500/10 border border-rose-500/20 p-4 rounded-2xl text-rose-500 font-bold hover:bg-rose-500/20 transition-colors"
            >
              Logout
            </button>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
