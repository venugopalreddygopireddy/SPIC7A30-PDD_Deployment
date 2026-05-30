"use client";
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, ArrowRight, Loader2, CheckCircle2 } from 'lucide-react';
import { submitCheckIn, CheckInRequest } from '@/lib/api';

const steps = [
  { id: 1, title: 'Personal Info' },
  { id: 2, title: 'Sleep' },
  { id: 3, title: 'Lifestyle' },
  { id: 4, title: 'Work' },
  { id: 5, title: 'Health' },
  { id: 6, title: 'Mental State' }
];

export default function CheckInScreen() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [analysisResult, setAnalysisResult] = useState<any>(null);

  const [formData, setFormData] = useState<CheckInRequest>({
    age: 25,
    gender: 'Male',
    occupation: '',
    marital_status: 'Single',
    sleep_duration: 7.0,
    sleep_quality: 3,
    wake_up_time: '07:00',
    bed_time: '23:00',
    physical_activity: 3,
    screen_time: 8.0,
    caffeine_intake: 2,
    alcohol_intake: '0', // Mapping to string since schemas.py says str, Android uses 0-5
    smoking_habit: 'None',
    work_hours: 8.0,
    travel_time: 1,
    social_interactions: 3,
    meditation_practice: '0',
    exercise_type: 'None',
    blood_pressure: 120,
    blood_sugar_level: 90,
    mood: 'Neutral',
    anxiety: 'Low',
    caffeine_dependency: 'No',
    workload: 'Normal',
    body_feeling: 'Normal'
  });

  // Calculate sleep duration when times change
  useEffect(() => {
    try {
      const [wakeH, wakeM] = formData.wake_up_time.split(':').map(Number);
      const [bedH, bedM] = formData.bed_time.split(':').map(Number);
      
      let sleep = 0;
      if (wakeH !== undefined && bedH !== undefined) {
        if (wakeH >= bedH) {
          sleep = (wakeH - bedH) + (wakeM - bedM) / 60;
        } else {
          sleep = (24 - bedH + wakeH) + (wakeM - bedM) / 60;
        }
      }
      setFormData(prev => ({ ...prev, sleep_duration: parseFloat(sleep.toFixed(1)) || 7.0 }));
    } catch(e) {}
  }, [formData.wake_up_time, formData.bed_time]);

  const handleNext = () => {
    if (currentStep < 6) setCurrentStep(currentStep + 1);
  };

  const handleBack = () => {
    if (currentStep > 1) setCurrentStep(currentStep - 1);
    else router.push('/');
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    try {
      // Ensure specific types are matching Python schema
      const submitData = { ...formData };
      submitData.alcohol_intake = submitData.alcohol_intake.toString();
      submitData.meditation_practice = submitData.meditation_practice.toString();
      
      const result = await submitCheckIn(submitData);
      setAnalysisResult(result);
      setLoading(false);
    } catch (err: any) {
      setError(`Submission failed: ${err.message}`);
      setLoading(false);
    }
  };

  const updateField = (field: keyof CheckInRequest, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const renderStep = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="space-y-4">
            <p className="text-slate-400 text-sm mb-4">Personal Information</p>
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
            <p className="text-slate-400 text-sm mb-4">Sleep Information</p>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Wake Up Time (HH:MM)</label>
              <input type="time" value={formData.wake_up_time} onChange={e => updateField('wake_up_time', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Bed Time (HH:MM)</label>
              <input type="time" value={formData.bed_time} onChange={e => updateField('bed_time', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="p-4 bg-slate-800/50 rounded-xl">
              <p className="text-sm text-emerald-400 font-bold">Calculated Sleep Duration: {formData.sleep_duration} hours</p>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Calculated Sleep Quality (1-5)</label>
              <input type="range" min="1" max="5" value={formData.sleep_quality} onChange={e => updateField('sleep_quality', Number(e.target.value))} className="w-full accent-emerald-500" />
              <div className="text-right text-emerald-400 font-bold">{formData.sleep_quality}</div>
            </div>
          </div>
        );
      case 3:
        return (
          <div className="space-y-4">
            <p className="text-slate-400 text-sm mb-4">Lifestyle & Habits</p>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Physical Activity (Level 1-5)</label>
              <input type="range" min="1" max="5" value={formData.physical_activity} onChange={e => updateField('physical_activity', Number(e.target.value))} className="w-full accent-emerald-500" />
              <div className="text-right text-emerald-400 font-bold">{formData.physical_activity}</div>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Screen Time: {formData.screen_time}h</label>
              <input type="range" min="0" max="16" step="0.5" value={formData.screen_time} onChange={e => updateField('screen_time', Number(e.target.value))} className="w-full accent-emerald-500" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Caffeine Intake (Cups 0-4)</label>
              <input type="range" min="0" max="4" value={formData.caffeine_intake} onChange={e => updateField('caffeine_intake', Number(e.target.value))} className="w-full accent-emerald-500" />
              <div className="text-right text-emerald-400 font-bold">{formData.caffeine_intake}</div>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Alcohol Intake (Freq 0-5)</label>
              <input type="range" min="0" max="5" value={Number(formData.alcohol_intake)} onChange={e => updateField('alcohol_intake', e.target.value)} className="w-full accent-emerald-500" />
              <div className="text-right text-emerald-400 font-bold">{formData.alcohol_intake}</div>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Smoking Habit</label>
              <select value={formData.smoking_habit} onChange={e => updateField('smoking_habit', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>None</option><option>Occasional</option><option>Frequent</option>
              </select>
            </div>
          </div>
        );
      case 4:
        return (
          <div className="space-y-4">
            <p className="text-slate-400 text-sm mb-4">Work & Daily Routine</p>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Work Hours (Per Day): {formData.work_hours}h</label>
              <input type="range" min="0" max="16" step="0.5" value={formData.work_hours} onChange={e => updateField('work_hours', Number(e.target.value))} className="w-full accent-emerald-500" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Travel Time: {formData.travel_time}h</label>
              <input type="range" min="0" max="5" step="1" value={formData.travel_time} onChange={e => updateField('travel_time', Number(e.target.value))} className="w-full accent-emerald-500" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Social Interactions (Level 1-5)</label>
              <input type="range" min="1" max="5" value={formData.social_interactions} onChange={e => updateField('social_interactions', Number(e.target.value))} className="w-full accent-emerald-500" />
              <div className="text-right text-emerald-400 font-bold">{formData.social_interactions}</div>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Workload Level</label>
              <select value={formData.workload} onChange={e => updateField('workload', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Light</option><option>Normal</option><option>Moderate</option><option>Heavy</option><option>Extreme</option>
              </select>
            </div>
          </div>
        );
      case 5:
        return (
          <div className="space-y-4">
            <p className="text-slate-400 text-sm mb-4">Health & Wellness</p>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Meditation Practice (mins)</label>
              <input type="range" min="0" max="30" value={Number(formData.meditation_practice)} onChange={e => updateField('meditation_practice', e.target.value)} className="w-full accent-emerald-500" />
              <div className="text-right text-emerald-400 font-bold">{formData.meditation_practice} mins</div>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Exercise Type</label>
              <select value={formData.exercise_type} onChange={e => updateField('exercise_type', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>None</option><option>Gym</option><option>Yoga</option><option>Running</option><option>Walking</option><option>Cycling</option><option>Swimming</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Blood Pressure (mmHg)</label>
              <input type="number" value={formData.blood_pressure} onChange={e => updateField('blood_pressure', Number(e.target.value))} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white" />
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Blood Sugar (mg/dL)</label>
              <input type="range" min="70" max="200" value={formData.blood_sugar_level} onChange={e => updateField('blood_sugar_level', Number(e.target.value))} className="w-full accent-emerald-500" />
              <div className="text-right text-emerald-400 font-bold">{formData.blood_sugar_level}</div>
            </div>
          </div>
        );
      case 6:
        return (
          <div className="space-y-4">
            <p className="text-slate-400 text-sm mb-4">Mental & Emotional State</p>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Mood</label>
              <select value={formData.mood} onChange={e => updateField('mood', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Happy</option><option>Calm</option><option>Neutral</option><option>Sad</option><option>Depressed</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Anxiety Level</label>
              <select value={formData.anxiety} onChange={e => updateField('anxiety', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Low</option><option>Mild</option><option>Moderate</option><option>High</option><option>Extreme</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Body Feeling</label>
              <select value={formData.body_feeling} onChange={e => updateField('body_feeling', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>Relaxed</option><option>Normal</option><option>Tired</option><option>Exhausted</option><option>Burnout</option>
              </select>
            </div>
            <div className="space-y-1">
              <label className="text-xs text-slate-400 font-medium">Caffeine Dependency</label>
              <select value={formData.caffeine_dependency} onChange={e => updateField('caffeine_dependency', e.target.value)} className="w-full bg-slate-900/50 border border-slate-700 rounded-xl px-4 py-3 text-white">
                <option>No</option><option>Slight</option><option>Yes</option>
              </select>
            </div>
          </div>
        );
      default: return null;
    }
  };

  if (analysisResult) {
    return (
      <div className="min-h-screen bg-[#050810] text-slate-200 font-sans flex flex-col md:items-center md:justify-center p-4">
        <div className="w-full max-w-2xl bg-gradient-to-br from-slate-900 to-[#080B16] border border-slate-800 rounded-3xl p-6 md:p-10 shadow-2xl text-center">
          <div className="mb-6 inline-flex items-center justify-center w-20 h-20 rounded-full bg-emerald-500/20 text-emerald-400">
            <CheckCircle2 size={40} />
          </div>
          <h2 className="text-3xl font-bold text-white mb-2">Check-In Complete!</h2>
          <p className="text-slate-400 mb-8">Here is your AI Stress Analysis</p>

          <div className="bg-slate-800/50 rounded-2xl p-6 mb-8 text-left border border-slate-700">
            <div className="flex justify-between items-center mb-4">
              <span className="text-slate-400 font-medium">Stress Level</span>
              <span className={`font-bold ${analysisResult.stress_level === 'Low' ? 'text-emerald-400' : analysisResult.stress_level === 'Moderate' ? 'text-yellow-400' : 'text-rose-400'}`}>
                {analysisResult.stress_level} ({analysisResult.score}/100)
              </span>
            </div>
            <div className="mb-4">
              <span className="text-slate-400 font-medium block mb-2">Analysis</span>
              <p className="text-white text-sm leading-relaxed">{analysisResult.message}</p>
            </div>
            <div>
              <span className="text-slate-400 font-medium block mb-2">Recommendation</span>
              <p className="text-emerald-100/90 text-sm leading-relaxed bg-emerald-900/30 p-4 rounded-xl border border-emerald-500/20">
                {analysisResult.recommendation}
              </p>
            </div>
          </div>

          <button 
            onClick={() => window.location.href = '/'}
            className="px-8 py-4 bg-emerald-500 hover:bg-emerald-600 text-[#050810] font-bold rounded-2xl w-full transition-colors"
          >
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#050810] text-slate-200 font-sans flex flex-col md:items-center md:justify-center p-4">
      <div className="w-full max-w-2xl bg-gradient-to-br from-slate-900 to-[#080B16] border border-slate-800 rounded-3xl p-6 md:p-10 shadow-2xl">
        
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <button onClick={handleBack} className="p-2 rounded-full hover:bg-slate-800 text-slate-400 transition-colors">
            <ArrowLeft size={24} />
          </button>
          <h2 className="text-xl font-bold text-white">Daily Check-in</h2>
          <div className="w-10"></div>
        </div>

        {/* Progress Bar */}
        <div className="flex justify-between items-center mb-8 relative">
          <div className="absolute top-1/2 left-0 w-full h-1 bg-slate-800 -translate-y-1/2 z-0"></div>
          <div className="absolute top-1/2 left-0 h-1 bg-emerald-500 -translate-y-1/2 z-0 transition-all duration-300" style={{ width: `${((currentStep - 1) / 5) * 100}%` }}></div>
          
          {steps.map(step => (
            <div key={step.id} className={`relative z-10 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-colors ${currentStep >= step.id ? 'bg-emerald-500 text-[#050810]' : 'bg-slate-800 text-slate-500'}`}>
              {currentStep > step.id ? <CheckCircle2 size={16} /> : step.id}
            </div>
          ))}
        </div>

        <h3 className="text-2xl font-bold text-emerald-400 mb-6">{steps[currentStep - 1].title}</h3>

        {error && <p className="text-rose-500 text-sm mb-4">{error}</p>}

        <div className="mb-10 h-auto min-h-[300px]">
          {renderStep()}
        </div>

        <div className="flex justify-end mt-4">
          {currentStep < 6 ? (
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
