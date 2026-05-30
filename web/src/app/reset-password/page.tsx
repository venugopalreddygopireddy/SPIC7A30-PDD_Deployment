"use client";

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Eye, EyeOff, Loader2, ArrowLeft } from 'lucide-react';
import { resetPassword } from '@/lib/api';

export default function ResetPasswordScreen() {
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordVisible, setPasswordVisible] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

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
    if (!password || !confirmPassword) {
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
      await resetPassword({ email, new_password: password });
      setSuccessMessage('Password reset successfully. Redirecting to login...');
      setTimeout(() => {
        localStorage.removeItem('resetEmail');
        router.push('/login');
      }, 2000);
    } catch (err: any) {
      setErrorMessage(err.response?.data?.detail || "Failed to reset password. Try again.");
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

        <h1 className="text-3xl font-bold text-white mb-2">Create New Password</h1>
        <p className="text-sm text-slate-400 mb-10 text-center px-4">
          Your new password must be different from previously used passwords.
        </p>

        <form onSubmit={handleSubmit} className="w-full space-y-4">
          <div className="space-y-1 relative">
            <label className="text-xs text-slate-400 font-medium ml-1">New Password</label>
            <div className="relative">
              <input 
                type={passwordVisible ? "text" : "password"} 
                value={password}
                onChange={(e) => { setPassword(e.target.value); setErrorMessage(null); }}
                placeholder="********"
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

          <div className="space-y-1 relative">
            <label className="text-xs text-slate-400 font-medium ml-1">Confirm New Password</label>
            <div className="relative">
              <input 
                type={passwordVisible ? "text" : "password"} 
                value={confirmPassword}
                onChange={(e) => { setConfirmPassword(e.target.value); setErrorMessage(null); }}
                placeholder="********"
                className="w-full bg-slate-900/50 border border-slate-700 focus:border-emerald-500 rounded-xl px-4 py-3 outline-none transition-colors text-white pr-12"
              />
            </div>
          </div>

          {errorMessage && (
            <p className="text-rose-500 text-sm mt-2 font-medium">{errorMessage}</p>
          )}

          {successMessage && (
            <p className="text-emerald-400 text-sm mt-2 font-medium">{successMessage}</p>
          )}

          <button 
            type="submit"
            disabled={loading || successMessage !== null}
            className="w-full bg-emerald-500 hover:bg-emerald-400 text-black font-bold py-3.5 rounded-xl transition-colors mt-6 flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {loading ? <Loader2 className="animate-spin" size={20} /> : "Reset Password"}
          </button>
        </form>
      </div>
    </div>
  );
}
