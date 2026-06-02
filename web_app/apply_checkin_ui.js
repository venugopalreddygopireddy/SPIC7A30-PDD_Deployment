const fs = require('fs');

const code = `"use client";
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, ArrowRight, Loader2, CheckCircle2, User, Moon, Zap, Briefcase, Activity as RunIcon, Check, BrainCircuit } from 'lucide-react';
import { submitCheckIn, CheckInRequest } from '@/lib/api';

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
      className={\`peer w-full bg-transparent border \${readOnly ? 'border-slate-800 text-slate-500' : 'border-slate-700 text-white'} rounded-lg px-4 py-3 focus:border-slate-500 outline-none transition-colors\`} 
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
          className={\`px-5 py-2.5 rounded-xl text-sm font-medium border transition-all \${isSelected(opt) ? 'bg-[#3B354D] text-[#E2DCEF] border-transparent' : 'bg-transparent text-slate-300 border-slate-700'}\`}
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
      {/* Track Background */}
      <div className="absolute w-full h-3 bg-[#2D2B3F] rounded-full overflow-hidden">
        {/* Filled Track */}
        <div className="h-full bg-[#82e0aa] transition-all duration-150" style={{ width: \`\${percentage}%\` }} />
      </div>
      
      {/* Dots for discrete sliders */}
      {showDots && (
        <div className="absolute w-full h-3 flex justify-between px-1 items-center pointer-events-none">
          {Array.from({ length: numDots + 1 }).map((_, i) => (
            <div key={i} className={\`w-1 h-1 rounded-full \${i * step <= value - min ? 'bg-transparent' : 'bg-[#82e0aa]/50'}\`} />
          ))}
        </div>
      )}
      
      {/* Native Range Input with Custom Styling */}
      <input
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={e => onChange(Number(e.target.value))}
        className="absolute w-full h-full opacity-0 cursor-pointer"
      />
      
      {/* Custom Thumb overlay */}
      <div 
        className="absolute w-2 h-7 bg-[#82e0aa] rounded-md pointer-events-none -translate-x-1/2 transition-all duration-150 shadow-sm"
        style={{ left: \`\${percentage}%\` }}
      />
    </div>
  );
};

export default function CheckInScreen() {
  const router = useRouter();
  const [currentStep, setCurrentStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [analysisResult, setAnalysisResult] = useState<any>(null);

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
    travel_time: 0.75, // 45 mins
    social_interactions: 3,
    meditation_practice: '0',
    exercise_type: 'Walking',
    blood_pressure: 120, // Only sending systolic for backend compatibility, but UI shows 120/80
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
    setError(null);
    try {
      const submitData = { ...formData };
      submitData.alcohol_intake = submitData.alcohol_intake.toString();
      submitData.meditation_practice = submitData.meditation_practice.toString();
      
      const result = await submitCheckIn(submitData);
      setAnalysisResult(result);
    } catch (err: any) {
      setError(\`Submission failed: \${err.message}\`);
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
    return \`\${h}h \${m}m\`;
  };

  if (analysisResult) {
    return (
      <div className="min-h-screen bg-[#0E1015] text-slate-200 font-sans flex flex-col md:items-center md:justify-center p-4">
        <div className="w-full max-w-2xl bg-gradient-to-br from-[#1C1E2B] to-[#13151D] border border-slate-800 rounded-3xl p-6 md:p-10 shadow-2xl text-center">
          <div className="mb-6 inline-flex items-center justify-center w-20 h-20 rounded-full bg-emerald-500/20 text-[#82e0aa]">
            <CheckCircle2 size={40} />
          </div>
          <h2 className="text-3xl font-bold text-white mb-2">Check-In Complete!</h2>
          <p className="text-slate-400 mb-8">Here is your AI Stress Analysis</p>

          <div className="bg-slate-800/30 rounded-2xl p-6 mb-8 text-left border border-slate-700/50">
            <div className="flex justify-between items-center mb-4">
              <span className="text-slate-400 font-medium">Stress Level</span>
              <span className={\`font-bold \${analysisResult.stress_level === 'Low' ? 'text-emerald-400' : analysisResult.stress_level === 'Moderate' ? 'text-yellow-400' : 'text-rose-400'}\`}>
                {analysisResult.stress_level} ({analysisResult.score}/100)
              </span>
            </div>
            <div className="mb-4">
              <span className="text-slate-400 font-medium block mb-2">Analysis</span>
              <p className="text-white text-sm leading-relaxed">{analysisResult.message}</p>
            </div>
            <div>
              <span className="text-slate-400 font-medium block mb-2">Recommendation</span>
              <p className="text-emerald-100/90 text-sm leading-relaxed bg-[#82e0aa]/10 p-4 rounded-xl border border-[#82e0aa]/20">
                {analysisResult.recommendation}
              </p>
            </div>
          </div>

          <button 
            onClick={() => window.location.href = '/'}
            className="px-8 py-4 bg-[#82e0aa] hover:bg-emerald-400 text-black font-bold rounded-2xl w-full transition-colors"
          >
            Go to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const stepInfo = steps.find(s => s.id === currentStep)!;

  return (
    <div className="min-h-screen bg-[#11131C] text-slate-200 font-sans flex flex-col">
      {/* Top Bar */}
      <div className="px-6 py-5 flex items-center gap-4">
        <button onClick={handleBack} className="text-white">
          <ArrowLeft size={24} />
        </button>
        <h1 className="text-xl font-bold text-white flex-1">Daily Wellness Check-In</h1>
      </div>

      {/* Progress Bar */}
      <div className="px-6 mb-6">
        <div className="flex gap-2 h-1.5 w-full bg-slate-800/0 rounded-full overflow-hidden">
          {steps.map((s, idx) => (
            <div 
              key={s.id} 
              className={\`flex-1 rounded-full \${currentStep >= s.id ? 'bg-[#9BB49D]' : 'bg-[#3A3C45]'}\`}
            />
          ))}
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 px-4 md:px-6 pb-24 overflow-y-auto">
        <div className="max-w-2xl mx-auto bg-[#1C1E2B] rounded-3xl p-6 shadow-xl mb-6">
          {/* Step Header */}
          <div className="flex items-center gap-4 mb-4">
            {stepInfo.icon}
            <h2 className="text-2xl font-bold text-white">{stepInfo.title}</h2>
          </div>
          <div className="h-px w-full bg-slate-700/50 mb-6" />

          {/* Step 1: Personal Info */}
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

          {/* Step 2: Sleep Info */}
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
                        className={\`cursor-pointer transition-colors \${q <= formData.sleep_quality ? 'text-[#82e0aa]' : 'text-slate-600'}\`} 
                        onClick={() => updateField('sleep_quality', q)}
                      />
                    ))}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Step 3: Lifestyle */}
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
                      className={\`w-12 h-12 rounded-xl flex items-center justify-center font-medium transition-colors \${formData.caffeine_intake === c ? 'bg-[#3B354D] text-[#E2DCEF]' : 'bg-[#212330] border border-slate-700 text-slate-300'}\`}
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
                <ChipGroup 
                  options={['None', 'Occasional', 'Frequent']} 
                  selected={formData.smoking_habit} 
                  onChange={(v:any) => updateField('smoking_habit', v)} 
                />
              </div>
            </div>
          )}

          {/* Step 4: Work & Daily Routine */}
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
                      <div key={i} className={\`w-2 h-2 rounded-full \${i <= formData.social_interactions ? 'bg-white' : 'border border-slate-500'}\`} />
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
                      className={\`py-3 px-4 rounded-xl text-sm font-medium border text-left transition-colors \${formData.workload === opt ? 'bg-[#3B354D] text-[#E2DCEF] border-transparent' : 'bg-transparent text-slate-300 border-slate-700'}\`}
                    >
                      {opt}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Step 5: Health & Wellness */}
          {currentStep === 5 && (
            <div className="space-y-8">
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Meditation Practice: {formData.meditation_practice} mins</label>
                <CustomSlider min={0} max={60} step={5} value={Number(formData.meditation_practice)} onChange={(v:any) => updateField('meditation_practice', v.toString())} showDots={true} />
              </div>
              
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Exercise Type (Select multiple)</label>
                <ChipGroup 
                  options={['Gym', 'Yoga', 'Running', 'Walking', 'Cycling', 'Swimming']} 
                  selected={formData.exercise_type} 
                  onChange={(v:any) => updateField('exercise_type', v)} 
                  multiple={true}
                />
              </div>

              <FloatingInput 
                label="Blood Pressure (mmHg)" 
                value={bpInput} 
                onChange={(v:any) => {
                  setBpInput(v);
                  const sys = parseInt(v.split('/')[0]);
                  if(!isNaN(sys)) updateField('blood_pressure', sys);
                }} 
              />
              
              <div>
                <label className="text-sm font-semibold text-white mb-2 block">Blood Sugar (mg/dL): {formData.blood_sugar_level}</label>
                <CustomSlider min={70} max={200} step={1} value={formData.blood_sugar_level} onChange={(v:any) => updateField('blood_sugar_level', v)} showDots={true} />
              </div>
            </div>
          )}

          {/* Step 6: Mental State */}
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
          disabled={loading}
          className="flex items-center gap-2 bg-[#9BB49D] hover:bg-[#82e0aa] text-black font-bold py-3 px-6 rounded-xl transition-colors disabled:opacity-50"
        >
          {loading ? <Loader2 className="animate-spin" size={20} /> : (
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
`;

fs.writeFileSync('c:/Users/venug/AndroidStudioProjects/CortiSense/web/src/app/checkin/page.tsx', code);
console.log('Successfully applied new Check-in UI!');
