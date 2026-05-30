"use client";

import React from 'react';
import { useRouter } from 'next/navigation';
import { Heart, ArrowRight } from 'lucide-react';

export default function WelcomeScreen() {
  const router = useRouter();

  return (
    <div className="min-h-screen bg-[#050810] flex items-center justify-center font-sans text-slate-200">
      <div className="max-w-md w-full p-8 flex flex-col items-center justify-center">
        
        {/* Icon */}
        <div className="w-24 h-24 bg-emerald-500 rounded-3xl flex items-center justify-center mb-12 shadow-lg shadow-emerald-500/20">
          <Heart size={48} className="text-[#050810]" />
        </div>

        {/* Title */}
        <h1 className="text-3xl font-bold text-white text-center leading-tight mb-6">
          Your Personal Sanctuary<br />for Mental Wellness
        </h1>

        {/* Subtitle */}
        <p className="text-base text-slate-400 text-center mb-4">
          Discover peace of mind with AI-driven insights tailored just for you.
        </p>

        {/* Description */}
        <p className="text-sm text-slate-500 text-center mb-12">
          Track, analyze, and conquer stress through personalized actionable recommendations.
        </p>

        {/* Buttons */}
        <div className="w-full space-y-4">
          <button 
            onClick={() => router.push('/signup')}
            className="w-full h-14 bg-emerald-500 hover:bg-emerald-600 text-[#050810] font-bold rounded-2xl flex items-center justify-center transition-colors text-lg"
          >
            Get Started
            <ArrowRight size={20} className="ml-2" />
          </button>

          <button 
            onClick={() => router.push('/login')}
            className="w-full h-14 bg-transparent border border-slate-700 hover:border-slate-500 text-slate-300 font-bold rounded-2xl flex items-center justify-center transition-colors text-lg"
          >
            Sign In
          </button>
        </div>

      </div>
    </div>
  );
}
