"use client";

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import MainLayout from '@/components/Layout/MainLayout';

export default function ProfileScreen() {
  const router = useRouter();
  const [userEmail, setUserEmail] = useState('');

  useEffect(() => {
    const email = localStorage.getItem('userEmail');
    const token = localStorage.getItem('jwtToken');
    if (!token) {
      router.push('/welcome');
    } else if (email) {
      setUserEmail(email);
    }
  }, [router]);

  return (
    <MainLayout activeTab="Profile">
      <div className="flex-1 overflow-y-auto px-6 py-12 flex flex-col h-full">
        <div className="flex flex-col items-center mb-10 mt-10">
          <div className="w-24 h-24 bg-emerald-500 rounded-full flex items-center justify-center text-[#050810] font-bold text-4xl mb-4 shadow-lg shadow-emerald-500/20">
            {userEmail ? userEmail.charAt(0).toUpperCase() : 'U'}
          </div>
          <h2 className="text-2xl font-bold text-white mb-1">{userEmail}</h2>
        </div>

        <div className="space-y-3">
          <button className="w-full bg-slate-900/50 border border-slate-800 p-5 rounded-2xl text-left text-white font-medium hover:bg-slate-800 transition-colors">
            Edit Profile
          </button>
          <button className="w-full bg-slate-900/50 border border-slate-800 p-5 rounded-2xl text-left text-white font-medium hover:bg-slate-800 transition-colors">
            Achievements
          </button>
          <button className="w-full bg-slate-900/50 border border-slate-800 p-5 rounded-2xl text-left text-white font-medium hover:bg-slate-800 transition-colors">
            Settings
          </button>
          
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
    </MainLayout>
  );
}
