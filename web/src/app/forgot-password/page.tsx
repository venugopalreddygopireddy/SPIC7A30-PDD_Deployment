"use client";

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2, ArrowLeft } from 'lucide-react';
import { forgotPassword } from '@/lib/api';

export default function ForgotPasswordScreen() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      setErrorMessage('Please enter your email');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      await forgotPassword({ email });
      // OTP Sent successfully
      localStorage.setItem('resetEmail', email);
      router.push('/verify-otp');
    } catch (err: any) {
      setErrorMessage(err.response?.data?.detail || "Failed to send OTP. Try again.");
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

        <h1 className="text-3xl font-bold text-white mb-2">Forgot Password</h1>
        <p className="text-sm text-slate-400 mb-10 text-center px-4">
          Enter your registered email address and we'll send you an OTP to reset your password.
        </p>

        <form onSubmit={handleSubmit} className="w-full space-y-4">
          <div className="space-y-1">
            <label className="text-xs text-slate-400 font-medium ml-1">Email Address</label>
            <input 
              type="email" 
              value={email}
              onChange={(e) => { setEmail(e.target.value); setErrorMessage(null); }}
              placeholder="john@example.com"
              className="w-full bg-slate-900/50 border border-slate-700 focus:border-emerald-500 rounded-xl px-4 py-3 outline-none transition-colors text-white"
            />
          </div>

          {errorMessage && (
            <p className="text-rose-500 text-sm mt-2 font-medium">{errorMessage}</p>
          )}

          <button 
            type="submit"
            disabled={loading}
            className="w-full bg-emerald-500 hover:bg-emerald-400 text-black font-bold py-3.5 rounded-xl transition-colors mt-6 flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {loading ? <Loader2 className="animate-spin" size={20} /> : "Send OTP"}
          </button>
        </form>
      </div>
    </div>
  );
}
