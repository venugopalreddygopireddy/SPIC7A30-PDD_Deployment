"use client";

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Eye, EyeOff, Loader2 } from 'lucide-react';
import { register } from '@/lib/api';
import axios from 'axios';

export default function SignupScreen() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);
  const [preferredLanguage, setPreferredLanguage] = useState('English');
  
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !email || !password || !confirmPassword) {
      setErrorMessage('Please fill in all fields');
      return;
    }
    
    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const [firstName, ...lastNameParts] = name.split(' ');
      const lastName = lastNameParts.join(' ');
      
      await register({ 
        first_name: firstName, 
        last_name: lastName || "", 
        email, 
        password,
        age: 25, 
        gender: "Prefer not to say" 
      });
      
      // On success, save language and redirect to login
      localStorage.setItem('appLanguage_' + email, preferredLanguage);
      localStorage.setItem('appLanguage', preferredLanguage);
      
      router.push('/login');
    } catch (err: any) {
      setErrorMessage(`Registration Failed: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#050810] flex items-center justify-center font-sans text-slate-200 py-12">
      <div className="max-w-md w-full p-8 flex flex-col">
        
        <h1 className="text-3xl font-bold text-white mb-2">Create Account</h1>
        <p className="text-sm text-slate-400 mb-10">Sign up to get started</p>

        <form onSubmit={handleSignup} className="w-full space-y-4">
          <div className="space-y-1">
            <label className="text-xs text-slate-400 font-medium ml-1">Full Name</label>
            <input 
              type="text" 
              value={name}
              onChange={(e) => { setName(e.target.value); setErrorMessage(null); }}
              placeholder="John Doe"
              className="w-full bg-slate-900/50 border border-slate-700 focus:border-emerald-500 rounded-xl px-4 py-3 outline-none transition-colors text-white"
            />
          </div>

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
            <p className="text-[10px] text-slate-500 ml-1 mt-1">Must be at least 8 characters</p>
          </div>

          <div className="space-y-1 relative">
            <label className="text-xs text-slate-400 font-medium ml-1">Confirm Password</label>
            <div className="relative">
              <input 
                type={confirmPasswordVisible ? "text" : "password"} 
                value={confirmPassword}
                onChange={(e) => { setConfirmPassword(e.target.value); setErrorMessage(null); }}
                className="w-full bg-slate-900/50 border border-slate-700 focus:border-emerald-500 rounded-xl px-4 py-3 outline-none transition-colors text-white pr-12"
              />
              <button 
                type="button"
                onClick={() => setConfirmPasswordVisible(!confirmPasswordVisible)}
                className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-200"
              >
                {confirmPasswordVisible ? <Eye size={20} /> : <EyeOff size={20} />}
              </button>
            </div>
          </div>

          {errorMessage && (
            <p className="text-rose-500 text-sm mt-2 font-medium">{errorMessage}</p>
          )}

          <div className="space-y-1 pt-2">
            <label className="text-xs text-slate-400 font-medium ml-1">Preferred Language</label>
            <select
              value={preferredLanguage}
              onChange={(e) => setPreferredLanguage(e.target.value)}
              className="w-full bg-slate-900/50 border border-slate-700 focus:border-emerald-500 rounded-xl px-4 py-3 outline-none transition-colors text-white appearance-none"
            >
              <option value="English">English</option>
              <option value="Telugu">Telugu</option>
              <option value="Hindi">Hindi</option>
              <option value="Tamil">Tamil</option>
            </select>
          </div>

          <button 
            type="submit"
            disabled={loading}
            className="w-full h-14 bg-slate-800 hover:bg-slate-700 disabled:opacity-50 text-white font-bold rounded-2xl flex items-center justify-center transition-colors text-lg mt-8"
          >
            {loading ? <Loader2 className="animate-spin" size={24} /> : "Create Account"}
          </button>
        </form>

        <div className="mt-8 flex justify-center items-center gap-2">
          <span className="text-slate-400 text-sm">Already have an account?</span>
          <button 
            onClick={() => router.push('/login')}
            className="text-emerald-400 font-bold text-sm hover:underline"
          >
            Sign in
          </button>
        </div>

      </div>
    </div>
  );
}
