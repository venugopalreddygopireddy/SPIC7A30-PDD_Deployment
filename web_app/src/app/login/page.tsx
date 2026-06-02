"use client";

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { login } from '@/lib/api';
import axios from 'axios';

export default function LoginScreen() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const searchParams = new URLSearchParams(window.location.search);
      if (searchParams.get('expired') === 'true') {
        setErrorMessage("Session expired, login again");
      }
    }
  }, []);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      setErrorMessage('Please fill in all fields');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const res = await login({ email, password });
      localStorage.setItem('jwtToken', res.access_token);
      localStorage.setItem('userEmail', email);
      
      const userLang = localStorage.getItem('appLanguage_' + email);
      if (userLang) {
        localStorage.setItem('appLanguage', userLang);
      } else {
        localStorage.setItem('appLanguage', 'English');
      }
      
      router.push('/');
    } catch (err: any) {
      if (axios.isAxiosError(err) && err.response?.status === 401) {
        setErrorMessage("Wrong password or wrong email ID. Try again");
      } else if (err.message?.includes("401")) {
        setErrorMessage("Wrong password or wrong email ID. Try again");
      } else {
        setErrorMessage(`Login Failed: ${err.message}`);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#050810] flex items-center justify-center font-sans text-slate-200">
      <div className="max-w-md w-full p-8 flex flex-col items-center">
        
        {/* App Logo */}
        <div className="w-24 h-24 mb-8 flex items-center justify-center">
          <img src="/logo.png" alt="CortiSense Logo" className="w-full h-full object-contain rounded-full shadow-lg shadow-emerald-500/20" />
        </div>

        <h1 className="text-3xl font-bold text-white mb-2">Welcome Back</h1>
        <p className="text-sm text-slate-400 mb-10 text-center">Sign in to continue to CortiSense</p>

        <form onSubmit={handleLogin} className="w-full space-y-4">
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

          <div className="space-y-1 relative">
            <label className="text-xs text-slate-400 font-medium ml-1">Password</label>
            <div className="relative">
              <input 
                type={passwordVisible ? "text" : "password"} 
                value={password}
                onChange={(e) => { setPassword(e.target.value); setErrorMessage(null); }}
                className="w-full bg-slate-900/50 border border-slate-700 focus:border-emerald-500 rounded-xl px-4 py-3 outline-none transition-colors text-white pr-12"
              />
              <button 
                type="button"
                onClick={() => setPasswordVisible(!passwordVisible)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-200"
              >
                {passwordVisible ? <Eye size={20} /> : <EyeOff size={20} />}
              </button>
            </div>
          </div>

          {errorMessage && (
            <p className="text-rose-500 text-sm mt-2 font-medium">{errorMessage}</p>
          )}

          <div className="flex justify-end pt-2">
            <button type="button" onClick={() => router.push('/forgot-password')} className="text-sm text-emerald-400 font-semibold hover:text-emerald-300">
              Forgot Password?
            </button>
          </div>

          <button 
            type="submit"
            disabled={loading}
            className="w-full h-14 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-[#050810] font-bold rounded-2xl flex items-center justify-center transition-colors text-lg mt-6"
          >
            {loading ? <Loader2 className="animate-spin" size={24} /> : "Sign In"}
          </button>
        </form>

        <div className="mt-8 flex items-center gap-2">
          <span className="text-slate-400 text-sm">Don't have an account?</span>
          <button 
            onClick={() => router.push('/signup')}
            className="text-emerald-400 font-bold text-sm hover:underline"
          >
            Sign up
          </button>
        </div>

      </div>
    </div>
  );
}
