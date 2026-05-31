"use client";
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, ArrowRight, Loader2, CheckCircle2, User, Moon, Zap, Briefcase, Check, BrainCircuit, AlertTriangle, Activity, Activity as RunIcon } from 'lucide-react';
import { submitCheckIn, CheckInRequest, ActionItem, completeAction } from '@/lib/api';

const steps = [
  { id: 1, title: 'Personal Information', icon: <User size={28} className="text-blue-400" /> },
  { id: 2, title: 'Sleep Information', icon: <Moon size={28} className="text-yellow-400" /> },
  { id: 3, title: 'Lifestyle & Habits', icon: <Zap size={28} className="text-yellow-500" /> },
  { id: 4, title: 'Work & Daily Routine', icon: <Briefcase size={28} className="text-amber-700" /> },
  { id: 5, title: 'Health & Wellness', icon: <RunIcon size={28} className="text-rose-400" /> },
  { id: 6, title: 'Mental & Emotional State', icon: <BrainCircuit size={28} className="text-pink-400" /> }
];

// Custom Components
const FloatingInput = ({ label, value, onChange, type = "text", readOnly = false }: any) => (
  <div className="relative mt-2">
    <input 
      type={type} 
      value={value} 
      onChange={e => onChange(e.target.value)} 
      readOnly={readOnly}
      className={`peer w-full bg-transparent border ${readOnly ? 'border-slate-800 text-slate-500' : 'border-slate-700 text-white'} rounded-lg px-4 py-3 focus:border-slate-500 outline-none transition-colors`} 
    />
    <label className="absolute left-3 -top-2.5 bg-[#1C1E2B] px-1 text-xs text-slate-400 font-medium">
      {label}
    </label>
  </div>
);

const ChipGroup = ({ options, selected, onChange, multiple = false }: any) => {
  const isSelected = (opt: string) => multiple ? selected.includes(opt) : selected === opt;
  
  const toggle = (opt: string) => {
    if (multiple) {
      if (selected.includes(opt)) onChange(selected.filter((x: string) => x !== opt).join(','));
      else onChange([...(selected ? selected.split(',').filter(Boolean) : []), opt].join(','));
    } else {
      onChange(opt);
    }
  };

  return (
    <div className="flex flex-wrap gap-3 mt-2">
      {options.map((opt: string) => (
        <button
          key={opt}
          onClick={() => toggle(opt)}
          className={`px-5 py-2.5 rounded-xl text-sm font-medium border transition-all ${isSelected(opt) ? 'bg-[#3B354D] text-[#E2DCEF] border-transparent' : 'bg-transparent text-slate-300 border-slate-700'}`}
        >
          {opt}
        </button>
      ))}
    </div>
  );
};

const CustomSlider = ({ min, max, step = 1, value, onChange, showDots = false }: any) => {
  const percentage = ((value - min) / (max - min)) * 100;
  const numDots = Math.floor((max - min) / step);
  
  return (
    <div className="relative w-full h-8 flex items-center mt-2 group">
      <div className="absolute w-full h-3 bg-[#2D2B3F] rounded-full overflow-hidden">
        <div className="h-full bg-[#82e0aa] transition-all duration-150" style={{ width: `${percentage}%` }} />
      </div>
      {showDots && (
        <div className="absolute w-full h-3 flex justify-between px-1 items-center pointer-events-none">
          {Array.from({ length: numDots + 1 }).map((_, i) => (
            <div key={i} className={`w-1 h-1 rounded-full ${i * step <= value - min ? 'bg-transparent' : 'bg-[#82e0aa]/50'}`} />
          ))}
        </div>
      )}
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={e => onChange(Number(e.target.value))}
        className="absolute w-full h-full opacity-0 cursor-pointer"
      />
      <div 
        className="absolute w-2 h-7 bg-[#82e0aa] rounded-md pointer-events-none -translate-x-1/2 transition-all duration-150 shadow-sm"
        style={{ left: `${percentage}%` }}
      />
    </div>
  );
};

export default function CheckInScreen() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [analyzeStep, setAnalyzeStep] = useState(0);
  const [error, setError] = useState<string | null>(null);
  const [analysisResult, setAnalysisResult] = useState<any>(null);
  const [actions, setActions] = useState<ActionItem[]>([]);

  const handleCompleteAction = async (actionId: string) => {
    try {
      setActions(prev => prev.filter(a => a.id !== actionId));
      if (analysisResult?.id) {
        await completeAction(analysisResult.id, actionId);
      }
    } catch (e) {
      console.error('Failed to complete action', e);
    }
  };

  const [formData, setFormData] = useState<CheckInRequest>({
    age: 21,
    gender: 'Male',
    mobile_number: '7013995242',
    occupation: 'Student',
    marital_status: 'Single',
    sleep_duration: 8.3,
    sleep_quality: 5,
    wake_up_time: '06:20',
    bed_time: '22:00',
    physical_activity: 1,
    screen_time: 4.0,
    caffeine_intake: 0,
    alcohol_intake: '0',
    smoking_habit: 'None',
    work_hours: 7.0,
    travel_time: 0.75,
    social_interactions: 3,
    meditation_practice: '0',
    exercise_type: 'Walking',
    blood_pressure: 120,
    blood_sugar_level: 140,
    mood: 'Neutral',
    anxiety: 'Low',
    caffeine_dependency: 'No',
    workload: 'Light',
    body_feeling: 'Normal'
  });

  const [bpInput, setBpInput] = useState('120/80');

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
      setFormData(prev => ({ ...prev, sleep_duration: parseFloat(sleep.toFixed(1)) || 0 }));
    } catch(e) {}
  }, [formData.wake_up_time, formData.bed_time]);

  const handleNext = () => {
    if (currentStep < 6) setCurrentStep(currentStep + 1);
    else handleSubmit();
  };

  const handleBack = () => {
    if (currentStep > 1) setCurrentStep(currentStep - 1);
    else router.push('/');
  };

  const handleSubmit = async () => {
    setLoading(true);
    setIsAnalyzing(true);
    setError(null);
    setAnalyzeStep(0);
    
    try {
      // Simulate analysis steps visually
      const intervals = [
        setTimeout(() => setAnalyzeStep(1), 800),
        setTimeout(() => setAnalyzeStep(2), 1600),
        setTimeout(() => setAnalyzeStep(3), 2400),
        setTimeout(() => setAnalyzeStep(4), 3200),
      ];

      const submitData = { ...formData };
      submitData.alcohol_intake = submitData.alcohol_intake.toString();
      submitData.meditation_practice = submitData.meditation_practice.toString();
      submitData.travel_time = Math.round(submitData.travel_time); // backend expects int
      submitData.age = Number(submitData.age);
      
      console.log('Sending checkin data:', submitData);
      const result = await submitCheckIn(submitData);
      
      // Wait at least until step 4 is reached before showing result
      setTimeout(() => {
        setAnalysisResult(result);
        if (result.actions) {
          setActions(result.actions.filter((a: ActionItem) => !a.is_done));
        }
        setIsAnalyzing(false);
      }, 3500);

    } catch (err: any) {
      setIsAnalyzing(false);
      let errorDetail = err.response?.data?.detail;
      if (typeof errorDetail === 'object') {
        errorDetail = JSON.stringify(errorDetail);
      }
      setError(`Submission failed: ${errorDetail || err.message || 'Unknown error'}`);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const updateField = (field: keyof CheckInRequest, value: string | number) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const formatHrsMins = (decimalHrs: number) => {
    const h = Math.floor(decimalHrs);
    const m = Math.round((decimalHrs - h) * 60);
    return `${h}h ${m}m`;
  };

  if (isAnalyzing) {
    return (
      <div className="min-h-screen bg-[#4A3B69] flex flex-col items-center justify-center p-6 font-sans">
        <div className="w-24 h-24 bg-[#3E2E5B] rounded-[2rem] flex items-center justify-center mb-8 shadow-xl">
           <BrainCircuit size={40} className="text-[#82e0aa]" />
        </div>
        <h1 className="text-3xl font-bold text-white mb-3 text-center">Analyzing Your Data</h1>
        <p className="text-slate-300 text-center mb-12 max-w-xs leading-relaxed">Our AI is processing your inputs to predict stress levels and cortisol risk</p>
        
        <div className="w-full max-w-sm space-y-5 mb-16">
          <div className="flex items-center gap-4">
             {analyzeStep >= 0 ? <Loader2 className="animate-spin text-[#82e0aa]" size={20} /> : <div className="w-5 h-5"/>}
             <span className={`text-sm font-medium ${analyzeStep >= 0 ? 'text-white' : 'text-transparent'}`}>Processing physiological markers...</span>
          </div>
          <div className="flex items-center gap-4">
             {analyzeStep >= 1 ? <div className="w-2 h-2 rounded-full bg-[#82e0aa] ml-1.5" /> : <div className="w-2 h-2 ml-1.5"/>}
             <span className={`text-sm ${analyzeStep >= 1 ? 'text-slate-200' : 'text-transparent'}`}>Analyzing heart rate variability</span>
          </div>
          <div className="flex items-center gap-4">
             {analyzeStep >= 2 ? <div className="w-2 h-2 rounded-full bg-[#82e0aa] ml-1.5" /> : <div className="w-2 h-2 ml-1.5"/>}
             <span className={`text-sm ${analyzeStep >= 2 ? 'text-slate-200' : 'text-transparent'}`}>Evaluating sleep patterns</span>
          </div>
          <div className="flex items-center gap-4">
             {analyzeStep >= 3 ? <div className="w-2 h-2 rounded-full bg-[#82e0aa] ml-1.5" /> : <div className="w-2 h-2 ml-1.5"/>}
             <span className={`text-sm ${analyzeStep >= 3 ? 'text-slate-200' : 'text-transparent'}`}>Computing stress correlations</span>
          </div>
          <div className="flex items-center gap-4">
             {analyzeStep >= 4 ? <div className="w-2 h-2 rounded-full bg-[#82e0aa] ml-1.5" /> : <div className="w-2 h-2 ml-1.5"/>}
             <span className={`text-sm ${analyzeStep >= 4 ? 'text-slate-200' : 'text-transparent'}`}>Generating insights</span>
          </div>
        </div>

        <div className="absolute bottom-12 w-full max-w-sm flex gap-2 px-6">
           <div className={`h-1.5 flex-1 rounded-full ${analyzeStep >= 0 ? 'bg-[#82e0aa]' : 'bg-[#3E2E5B]'}`} />
           <div className={`h-1.5 flex-1 rounded-full ${analyzeStep >= 2 ? 'bg-[#82e0aa]' : 'bg-[#3E2E5B]'}`} />
           <div className={`h-1.5 flex-1 rounded-full ${analyzeStep >= 4 ? 'bg-[#82e0aa]' : 'bg-[#3E2E5B]'}`} />
           <div className="h-1.5 flex-1 rounded-full bg-[#3E2E5B]" />
        </div>
      </div>
    );
  }

  if (analysisResult) {
    const isHigh = analysisResult.stress_level === 'High' || analysisResult.stress_level === 'Critical' || analysisResult.stress_level === 'Extreme';
    const isModerate = analysisResult.stress_level === 'Moderate';
    
    // Determine colors and labels based on stress level
    let alertColor = isHigh ? 'text-rose-500' : isModerate ? 'text-amber-500' : 'text-emerald-500';
    let alertBg = isHigh ? 'bg-rose-500/10' : isModerate ? 'bg-amber-500/10' : 'bg-emerald-500/10';
    let alertBorder = isHigh ? 'border-rose-500/20' : isModerate ? 'border-amber-500/20' : 'border-emerald-500/20';
    let orbColor = isHigh ? '#f43f5e' : isModerate ? '#f59e0b' : '#10b981';

    // Recommendations parsed into list
    const recommendationsList = analysisResult.recommendation
      .split(/(?<=[.!?])\s+/)
      .filter((s: string) => s.trim().length > 5);
      
    if (recommendationsList.length === 0) {
       recommendationsList.push("Take a 5-minute deep breathing break.");
       recommendationsList.push("Step away from your screen and stretch.");
    }

    return (
      <div className="min-h-screen bg-[#0A0E17] text-slate-200 font-sans flex flex-col items-center pt-8 pb-12 px-4 md:px-6">
        
        {/* Top Alert Box */}
        <div className={`w-full max-w-md rounded-2xl border ${alertBorder} ${alertBg} p-4 mb-6 flex items-start gap-4`}>
          <div className={`mt-0.5 ${alertColor}`}>
            <AlertTriangle size={24} />
          </div>
          <div>
            <h2 className={`font-bold text-lg ${alertColor}`}>{analysisResult.stress_level} Stress Alert</h2>
            <p className="text-slate-400 text-sm mt-0.5">Stay mindful</p>
          </div>
        </div>

        {/* Score Orb Card */}
        <div className="w-full max-w-md bg-[#131722] border border-slate-800/60 rounded-3xl p-8 mb-6 flex flex-col items-center justify-center shadow-lg">
          <div className="relative mb-6">
            <svg width="180" height="180" viewBox="0 0 200 200" className="rotate-[-90deg]">
               <circle cx="100" cy="100" r="80" fill="none" stroke="#1F2332" strokeWidth="12" />
               <circle cx="100" cy="100" r="80" fill="none" stroke={orbColor} strokeWidth="12" 
                       strokeDasharray="502" strokeDashoffset={502 - (502 * analysisResult.score) / 100} strokeLinecap="round" />
            </svg>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
               <span className="text-5xl font-extrabold text-white tracking-tight">{analysisResult.score}</span>
               <span className="text-xs text-slate-400 mt-1 font-bold uppercase tracking-widest">SCORE</span>
            </div>
          </div>
          
          <h2 className={`text-2xl font-bold ${alertColor}`}>{analysisResult.stress_level}</h2>
          <p className="text-slate-400 text-sm mt-1">Current status</p>
        </div>

        {/* Info / Message Box */}
        <div className="w-full max-w-md bg-[#131722] border border-slate-800/60 rounded-3xl p-6 mb-6 shadow-lg">
          <div className="flex items-center gap-3 mb-3">
            <div className="text-amber-500">
              <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>
            </div>
            <h3 className="text-white font-bold text-lg">Slightly elevated stress</h3>
          </div>
          <p className="text-slate-400 text-sm leading-relaxed">
            {analysisResult.message || "Stay mindful and remember to take short breaks throughout your day."}
          </p>
        </div>

        {/* Recommended Actions */}
        <div className="w-full max-w-md mb-8">
          <h3 className="text-white font-bold text-xl mb-4 px-1">Actionable Recommendations</h3>
          {actions.length > 0 ? (
            <div className="space-y-4">
              {actions.map((act) => (
                <div key={act.id} className="bg-[#1C2030] border border-slate-700/50 rounded-2xl p-5 flex flex-col gap-3">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0 mt-0.5">
                      <CheckCircle2 size={20} />
                    </div>
                    <div className="flex-1">
                      <h4 className="text-slate-100 font-bold text-base">{act.title}</h4>
                      <p className="text-slate-400 text-sm mt-1 leading-relaxed">{act.description}</p>
                    </div>
                  </div>
                  <button 
                    onClick={() => handleCompleteAction(act.id)}
                    className="mt-2 w-full bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 text-emerald-400 font-semibold py-2.5 rounded-xl transition-colors"
                  >
                    Mark as Done
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="space-y-3">
              {recommendationsList.map((rec: string, idx: number) => (
                <div key={idx} className="bg-[#1C2030] border border-slate-700/50 rounded-2xl p-4 flex items-center gap-4">
                  <div className="w-10 h-10 rounded-full bg-emerald-500/20 text-emerald-400 flex items-center justify-center shrink-0">
                    <CheckCircle2 size={20} />
                  </div>
                  <p className="text-slate-300 text-sm font-medium leading-relaxed">{rec}</p>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Primary Action Button */}
        <button 
          onClick={() => window.location.href = '/'} 
          className="w-full max-w-md bg-[#FFC107] hover:bg-[#FFCA28] text-black font-bold text-lg py-4 rounded-xl shadow-lg transition-colors"
        >
          {isHigh ? 'View Insights' : 'Start Breathing Exercise'}
        </button>
        
        <button 
          onClick={() => window.location.href = '/'} 
          className="w-full max-w-md mt-4 text-slate-400 font-semibold py-3 hover:text-white transition-colors"
        >
          Back to Dashboard
        </button>

      </div>
    );
  }

  const stepInfo = steps.find(s => s.id === currentStep)!;

  return (
    <div className="min-h-screen bg-[#11131C] text-slate-200 font-sans flex flex-col relative pb-24">
      <div className="px-6 py-5 flex items-center gap-4">
        <button onClick={handleBack} className="text-white">
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-xl font-bold text-white flex-1">Daily Wellness Check-In</h1>
      </div>

      <div className="px-6 mb-6">
        <div className="flex gap-2 h-1.5 w-full bg-slate-800/0 rounded-full overflow-hidden">
          {steps.map((s, idx) => (
            <div 
              key={s.id} 
              className={`flex-1 rounded-full ${currentStep >= s.id ? 'bg-[#9BB49D]' : 'bg-[#3A3C45]'}`}
            />
          ))}
        </div>
      </div>

      <div className="flex-1 px-4 md:px-6 overflow-y-auto">
        <div className="max-w-2xl mx-auto bg-[#1C1E2B] rounded-3xl p-6 shadow-xl mb-6">
          <div className="flex items-center gap-4 mb-4">
            {stepInfo.icon}
            <h2 className="text-2xl font-bold text-white">{stepInfo.title}</h2>
          </div>
          <div className="h-px w-full bg-slate-700/50 mb-6" />

          {error && (
            <div className="mb-6 p-4 bg-red-500/10 border border-red-500/20 rounded-xl text-red-400 text-sm">
               {error}
            </div>
          )}

          {currentStep === 1 && (
            <div className="space-y-6">
              <p className="text-[#8B8D98] text-sm leading-relaxed mb-6">
                Note: If you want to change Age, Gender, or Mobile Number, please edit them in Edit Profile.
              </p>
              <FloatingInput label="Age" value={formData.age} readOnly={true} />
              <FloatingInput label="Gender" value={formData.gender} readOnly={true} />
              <FloatingInput label="Mobile Number" value={formData.mobile_number} readOnly={true} />
              <FloatingInput label="Occupation" value={formData.occupation} onChange={(v:any) => updateField('occupation', v)} />
              
              <div className="mt-4">
                <label className="text-sm font-semibold text-white">Marital Status</label>
                <ChipGroup 
                  options={['Single', 'Married', 'Divorced']} 
                  selected={formData.marital_status} 
                  onChange={(v:any) => updateField('marital_status', v)} 
                />
              </div>
            </div>
          )}

          {currentStep === 2 && (
            <div className="space-y-6">
              <FloatingInput label="Wake Up Time (HH:MM)" type="time" value={formData.wake_up_time} onChange={(v:any) => updateField('wake_up_time', v)} />
              <FloatingInput label="Bed Time (HH:MM)" type="time" value={formData.bed_time} onChange={(v:any) => updateField('bed_time', v)} />
              
              <div className="pt-4 space-y-4">
                <p className="font-semibold text-white">Calculated Sleep Duration: {formData.sleep_duration} hours</p>
                <div className="space-y-2">
                  <p className="font-semibold text-white">Calculated Sleep Quality (1-5): {formData.sleep_quality}</p>
                  <div className="flex gap-4">
                    {[1,2,3,4,5].map(q => (
                      <Check 
                        key={q} 
                        size={20} 
                        className={`cursor-pointer transition-colors ${q <= formData.sleep_quality ? 'text-[#82e0aa]' : 'text-slate-600'}`} 
                        onClick={() => updateField('sleep_quality', q)}
                      />
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {currentStep === 3 && (
            <div className="space-y-8">
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Physical Activity (Level 1-5)</label>
                <CustomSlider min={1} max={5} step={1} value={formData.physical_activity} onChange={(v:any) => updateField('physical_activity', v)} showDots={true} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Screen Time: {formatHrsMins(formData.screen_time)}</label>
                <CustomSlider min={0} max={16} step={0.5} value={formData.screen_time} onChange={(v:any) => updateField('screen_time', v)} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Caffeine Intake (Cups)</label>
                <div className="flex gap-2">
                  {[0,1,2,3,4].map(c => (
                    <button
                      key={c}
                      onClick={() => updateField('caffeine_intake', c)}
                      className={`w-12 h-12 rounded-xl flex items-center justify-center font-medium transition-colors ${formData.caffeine_intake === c ? 'bg-[#3B354D] text-[#E2DCEF]' : 'bg-[#212330] border border-slate-700 text-slate-300'}`}
                    >
                      {c}
                    </button>
                  ))}
                </div>
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Alcohol Intake (Freq 0-5)</label>
                <CustomSlider min={0} max={5} step={1} value={Number(formData.alcohol_intake)} onChange={(v:any) => updateField('alcohol_intake', v)} showDots={true} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Smoking Habit</label>
                <ChipGroup options={['None', 'Occasional', 'Frequent']} selected={formData.smoking_habit} onChange={(v:any) => updateField('smoking_habit', v)} />
              </div>
            </div>
          )}

          {currentStep === 4 && (
            <div className="space-y-8">
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Work Hours (Per Day): {formatHrsMins(formData.work_hours)}</label>
                <CustomSlider min={0} max={16} step={0.5} value={formData.work_hours} onChange={(v:any) => updateField('work_hours', v)} showDots={true} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Travel Time: {formatHrsMins(formData.travel_time)}</label>
                <CustomSlider min={0} max={5} step={0.25} value={formData.travel_time} onChange={(v:any) => updateField('travel_time', v)} showDots={true} />
              </div>
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <label className="text-sm font-semibold text-white">Social Interactions:</label>
                  <div className="flex gap-1">
                    {[1,2,3,4,5].map(i => (
                      <div key={i} className={`w-2 h-2 rounded-full ${i <= formData.social_interactions ? 'bg-white' : 'border border-slate-500'}`} />
                    ))}
                  </div>
                </div>
                <CustomSlider min={1} max={5} step={1} value={formData.social_interactions} onChange={(v:any) => updateField('social_interactions', v)} showDots={true} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Workload Level</label>
                <div className="grid grid-cols-2 gap-3">
                  {['Light', 'Normal', 'Moderate', 'Heavy', 'Extreme'].map((opt) => (
                    <button
                      key={opt}
                      onClick={() => updateField('workload', opt)}
                      className={`py-3 px-4 rounded-xl text-sm font-medium border text-left transition-colors ${formData.workload === opt ? 'bg-[#3B354D] text-[#E2DCEF] border-transparent' : 'bg-transparent text-slate-300 border-slate-700'}`}
                    >
                      {opt}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {currentStep === 5 && (
            <div className="space-y-8">
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Meditation Practice: {formData.meditation_practice} mins</label>
                <CustomSlider min={0} max={60} step={5} value={Number(formData.meditation_practice)} onChange={(v:any) => updateField('meditation_practice', v.toString())} showDots={true} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Exercise Type (Select multiple)</label>
                <ChipGroup options={['Gym', 'Yoga', 'Running', 'Walking', 'Cycling', 'Swimming']} selected={formData.exercise_type} onChange={(v:any) => updateField('exercise_type', v)} multiple={true} />
              </div>
              <FloatingInput label="Blood Pressure (mmHg)" value={bpInput} onChange={(v:any) => { setBpInput(v); const sys = parseInt(v.split('/')[0]); if(!isNaN(sys)) updateField('blood_pressure', sys); }} />
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Blood Sugar (mg/dL): {formData.blood_sugar_level}</label>
                <CustomSlider min={70} max={200} step={1} value={formData.blood_sugar_level} onChange={(v:any) => updateField('blood_sugar_level', v)} showDots={true} />
              </div>
            </div>
          )}

          {currentStep === 6 && (
            <div className="space-y-6">
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Mood</label>
                <ChipGroup options={['Happy', 'Calm', 'Neutral', 'Sad', 'Depressed']} selected={formData.mood} onChange={(v:any) => updateField('mood', v)} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Anxiety Level</label>
                <ChipGroup options={['Low', 'Mild', 'Moderate', 'High', 'Extreme']} selected={formData.anxiety} onChange={(v:any) => updateField('anxiety', v)} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Body Feeling</label>
                <ChipGroup options={['Relaxed', 'Normal', 'Tired', 'Exhausted', 'Burnout']} selected={formData.body_feeling} onChange={(v:any) => updateField('body_feeling', v)} />
              </div>
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Caffeine Dependency</label>
                <ChipGroup options={['No', 'Slight', 'Yes']} selected={formData.caffeine_dependency} onChange={(v:any) => updateField('caffeine_dependency', v)} />
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Bottom Bar */}
      <div className="fixed bottom-0 left-0 w-full bg-[#222533] border-t border-slate-800 p-4 md:p-6 flex items-center justify-between z-10">
        <span className="text-white font-medium pl-2">Step {currentStep} of 6</span>
        <button
          onClick={handleNext}
          disabled={loading || isAnalyzing}
          className="flex items-center gap-2 bg-[#9BB49D] hover:bg-[#82e0aa] text-black font-bold py-3 px-6 rounded-xl transition-colors disabled:opacity-50"
        >
          {loading && !isAnalyzing ? <Loader2 className="animate-spin" size={20} /> : (
            <>
              {currentStep === 6 ? 'Submit Analysis' : 'Next'}
              {currentStep === 6 ? <Check size={20} /> : <ArrowRight size={20} />}
            </>
          )}
        </button>
      </div>
    </div>
  );
}
