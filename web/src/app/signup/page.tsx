"use client";

import React, { useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { Eye, EyeOff, Loader2, Camera } from 'lucide-react';
import { register } from '@/lib/api';

const MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB

export default function SignupScreen() {
  const router = useRouter();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [profileImage, setProfileImage] = useState<string>('');
  const [profileImagePreview, setProfileImagePreview] = useState<string>('');

  const [passwordVisible, setPasswordVisible] = useState(false);
  const [confirmPasswordVisible, setConfirmPasswordVisible] = useState(false);

  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (file.size > MAX_SIZE_BYTES) {
      setErrorMessage('Profile photo must be smaller than 2 MB. Please choose a smaller image.');
      e.target.value = '';
      return;
    }

    setErrorMessage(null);
    const reader = new FileReader();
    reader.onload = (ev) => {
      const base64 = ev.target?.result as string;
      setProfileImage(base64);
      setProfileImagePreview(base64);
    };
    reader.readAsDataURL(file);
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !email || !password || !confirmPassword) {
      setErrorMessage('Please fill in all fields');
      return;
    }
    if (password.length < 8) {
      setErrorMessage('Password must be at least 8 characters');
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match');
      return;
    }
    if (!email.endsWith('@gmail.com')) {
      setErrorMessage('Only @gmail.com addresses are allowed');
      return;
    }

    setLoading(true);
    setErrorMessage(null);

    try {
      const [firstName, ...lastNameParts] = name.trim().split(' ');
      const lastName = lastNameParts.join(' ');

      await register({
        first_name: firstName,
        last_name: lastName || '',
        email,
        password,
        age: 25,
        gender: 'Prefer not to say',
        profile_image: profileImage || null,
      });

      // Keep existing flow: redirect to login
      router.push('/login');
    } catch (err: any) {
      const detail = err?.response?.data?.detail;
      setErrorMessage(detail ? detail : `Registration Failed: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#050810] flex items-center justify-center font-sans text-slate-200 py-12">
      <div className="max-w-md w-full p-8 flex flex-col">

        <h1 className="text-3xl font-bold text-white mb-2">Create Account</h1>
        <p className="text-sm text-slate-400 mb-8">Sign up to get started</p>

        {/* Profile Image Picker */}
        <div className="flex flex-col items-center mb-8">
          <div
            className="relative w-24 h-24 rounded-full cursor-pointer group"
            onClick={() => fileInputRef.current?.click()}
          >
            {profileImagePreview ? (
              <img
                src={profileImagePreview}
                alt="Profile"
                className="w-24 h-24 rounded-full object-cover border-2 border-emerald-500"
              />
            ) : (
              <div className="w-24 h-24 rounded-full bg-slate-800 border-2 border-dashed border-slate-600 flex items-center justify-center">
                <Camera size={28} className="text-slate-400" />
              </div>
            )}
            {/* Camera overlay on hover */}
            <div className="absolute inset-0 rounded-full bg-black/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
              <Camera size={22} className="text-white" />
            </div>
          </div>
          <p className="text-xs text-slate-500 mt-2">Tap to add photo (optional, max 2 MB)</p>
          <input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            className="hidden"
            onChange={handleImageSelect}
          />
        </div>

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
