"use client";

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2, ArrowLeft } from 'lucide-react';
import { verifyOTP } from '@/lib/api';

export default function VerifyOTPScreen() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    const savedEmail = localStorage.getItem('resetEmail');
    if (savedEmail) {
      setEmail(savedEmail);
    } else {
      router.push('/forgot-password');
    }
  }, [router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp) {
      setErrorMessage('Please enter the OTP');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      await verifyOTP({ email, otp });
      // OTP verified successfully
      router.push('/reset-password');
    } catch (err: any) {
      setErrorMessage(err.response?.data?.detail || "Invalid OTP. Try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#050810] flex items-center justify-center font-sans text-slate-200">
      <div className="max-w-md w-full p-8 flex flex-col items-center relative">
        
        <button 
          onClick={() => router.back()} 
          className="absolute top-8 left-8 text-slate-400 hover:text-white"
        >
          <ArrowLeft size={24} />
        </button>

        <div className="w-20 h-20 bg-gradient-to-br from-emerald-400 to-cyan-500 rounded-full mb-8 shadow-lg shadow-emerald-500/20 flex items-center justify-center text-[#050810] font-bold text-2xl mt-10">
          C
        </div>

        <h1 className="text-3xl font-bold text-white mb-2">Verify OTP</h1>
        <p className="text-sm text-slate-400 mb-10 text-center px-4">
          Enter the 6-digit OTP sent to {email}
        </p>

        <form onSubmit={handleSubmit} className="w-full space-y-4">
          <div className="space-y-1">
            <label className="text-xs text-slate-400 font-medium ml-1">OTP</label>
            <input 
              type="text" 
              maxLength={6}
              value={otp}
              onChange={(e) => { setOtp(e.target.value.replace(/\D/g, '')); setErrorMessage(null); }}
              placeholder="123456"
              className="w-full bg-slate-900/50 border border-slate-700 focus:border-emerald-500 rounded-xl px-4 py-3 outline-none transition-colors text-white tracking-widest text-center text-xl font-mono"
            />
          </div>

          {errorMessage && (
            <p className="text-rose-500 text-sm mt-2 font-medium">{errorMessage}</p>
          )}

          <button 
            type="submit"
            disabled={loading || otp.length !== 6}
            className="w-full bg-emerald-500 hover:bg-emerald-400 text-black font-bold py-3.5 rounded-xl transition-colors mt-6 flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {loading ? <Loader2 className="animate-spin" size={20} /> : "Verify OTP"}
          </button>
        </form>
      </div>
    </div>
  );
}
