"use client";

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, ArrowRight, Loader2, CheckCircle2 } from 'lucide-react';
import { submitCheckIn, CheckInRequest } from '@/lib/api';

const steps = [
  { id: 1, title: 'Basic Info' },
  { id: 2, title: 'Health & Sleep' },
  { id: 3, title: 'Work & Lifestyle' },
  { id: 4, title: 'Well-being' }
];

export default function CheckInScreen() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Form Data State
  const [formData, setFormData] = useState<CheckInRequest>({
    age: 25,
    gender: 'Male',
    marital_status: 'Single',
    occupation: 'Software Engineer',
    sleep_duration: 7.0,
    sleep_quality: 7.0,
    wake_up_time: '07:00',
    bed_time: '23:00',
    physical_activity: 30,
    screen_time: 8.0,
    caffeine_intake: 2.0,
    alcohol_intake: 'Rarely',
    smoking_habit: 'Never',
    work_hours: 8.0,
    travel_time: 1.0,
    social_interactions: 3.0,
    meditation_practice: 'Rarely',
    exercise_type: 'Cardio',
    blood_pressure: 120.0,
    blood_sugar_level: 90.0,
    mood: 'Neutral',
    anxiety: 'Low',
    caffeine_dependency: 'Low',
    workload: 'Moderate',
    body_feeling: 'Normal'
  });

  const handleNext = () => {
    if (currentStep < 4) setCurrentStep(currentStep + 1);
  };

  const handleBack = () => {
    if (currentStep > 1) setCurrentStep(currentStep - 1);
    else router.push('/');
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    try {
      const result = await submitCheckIn(formData);
      // We can pass the result to the dashboard or show a summary modal, 
      // but for now, redirecting to Dashboard refreshes the history.
      router.push('/');
    } catch (err: any) {
      setError(`Submission failed: ${err.message}`);
      setLoading(false);
    }
  };

  const updateField = (field: keyof CheckInRequest, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  // Render Form based on Step
  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Age</label>
              <input type="number" value={formData.age} onChange={e => updateField('age', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Gender</label>
              <select value={formData.gender} onChange={e => updateField('gender', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Male</option><option>Female</option><option>Other</option><option>Prefer not to say</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Occupation</label>
              <input type="text" value={formData.occupation} onChange={e => updateField('occupation', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Marital Status</label>
              <select value={formData.marital_status} onChange={e => updateField('marital_status', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Single</option><option>Married</option><option>Divorced</option><option>Widowed</option>
              </select>
            </div>
          </div>
        );
      case 2:
        return (
          <div className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Sleep Duration (hrs)</label>
              <input type="number" step="0.5" value={formData.sleep_duration} onChange={e => updateField('sleep_duration', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Sleep Quality (1-10)</label>
              <input type="number" min="1" max="10" value={formData.sleep_quality} onChange={e => updateField('sleep_quality', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Bed Time (HH:MM)</label>
              <input type="time" value={formData.bed_time} onChange={e => updateField('bed_time', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Wake Up Time (HH:MM)</label>
              <input type="time" value={formData.wake_up_time} onChange={e => updateField('wake_up_time', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Blood Pressure (Systolic)</label>
              <input type="number" value={formData.blood_pressure} onChange={e => updateField('blood_pressure', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
          </div>
        );
      case 3:
        return (
          <div className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Work Hours (daily)</label>
              <input type="number" value={formData.work_hours} onChange={e => updateField('work_hours', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Screen Time (hrs)</label>
              <input type="number" value={formData.screen_time} onChange={e => updateField('screen_time', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Physical Activity (mins)</label>
              <input type="number" value={formData.physical_activity} onChange={e => updateField('physical_activity', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Caffeine Intake (cups)</label>
              <input type="number" value={formData.caffeine_intake} onChange={e => updateField('caffeine_intake', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
          </div>
        );
      case 4:
        return (
          <div className="space-y-4">
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Current Mood</label>
              <select value={formData.mood} onChange={e => updateField('mood', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Happy</option><option>Neutral</option><option>Stressed</option><option>Sad</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Anxiety Level</label>
              <select value={formData.anxiety} onChange={e => updateField('anxiety', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>None</option><option>Low</option><option>Moderate</option><option>High</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Workload</label>
              <select value={formData.workload} onChange={e => updateField('workload', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Light</option><option>Moderate</option><option>Heavy</option><option>Overwhelming</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Body Feeling</label>
              <select value={formData.body_feeling} onChange={e => updateField('body_feeling', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Energetic</option><option>Normal</option><option>Fatigued</option><option>Pain</option>
              </select>
            </div>
          </div>
        );
      default: return null;
    }
  };

  return (
    <div className="min-h-screen bg-[#050810] text-slate-200 font-sans flex flex-col md:items-center md:justify-center p-4">
      <div className="w-full max-w-2xl bg-gradient-to-br from-slate-900 to-[#080B16] border border-slate-800 rounded-3xl p-6 md:p-10 shadow-2xl">
        
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <button onClick={handleBack} className="p-2 rounded-full hover:bg-slate-800 text-slate-400 transition-colors">
            <ArrowLeft size={24} />
          </button>
          <h2 className="text-xl font-bold text-white">Daily Check-in</h2>
          <div className="w-10"></div> {/* Spacer */}
        </div>

        {/* Progress Bar */}
        <div className="flex justify-between items-center mb-8 relative">
          <div className="absolute top-1/2 left-0 w-full h-1 bg-slate-800 -translate-y-1/2 z-0"></div>
          <div className="absolute top-1/2 left-0 h-1 bg-emerald-500 -translate-y-1/2 z-0 transition-all duration-300" style={{ width: `${((currentStep - 1) / 3) * 100}%` }}></div>
          
          {steps.map(step => (
            <div key={step.id} className={`relative z-10 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-colors ${currentStep >= step.id ? 'bg-emerald-500 text-[#050810]' : 'bg-slate-800 text-slate-500'}`}>
              {currentStep > step.id ? <CheckCircle2 size={16} /> : step.id}
            </div>
          ))}
        </div>

        <h3 className="text-2xl font-bold text-emerald-400 mb-6">{steps[currentStep - 1].title}</h3>

        {error && <p className="text-rose-500 text-sm mb-4">{error}</p>}

        {/* Form Area */}
        <div className="mb-10 h-auto min-h-[300px]">
          {renderStep()}
        </div>

        {/* Navigation */}
        <div className="flex justify-end mt-4">
          {currentStep < 4 ? (
            <button 
              onClick={handleNext}
              className="px-8 py-4 bg-emerald-500 hover:bg-emerald-600 text-[#050810] font-bold rounded-2xl flex items-center transition-colors"
            >
              Continue <ArrowRight size={20} className="ml-2" />
            </button>
          ) : (
            <button 
              onClick={handleSubmit}
              disabled={loading}
              className="px-8 py-4 bg-emerald-500 hover:bg-emerald-600 disabled:opacity-50 text-[#050810] font-bold rounded-2xl flex items-center transition-colors"
            >
              {loading ? <Loader2 className="animate-spin mr-2" /> : "Complete Check-in"}
              {!loading && <CheckCircle2 size={20} className="ml-2" />}
            </button>
          )}
        </div>

      </div>
    </div>
  );
}
