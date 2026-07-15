import re

with open('web/src/app/history/[id]/page.tsx', 'r', encoding='utf-8') as f:
    content = f.read()

old_ui = """        {/* Breakdown Factors */}
        <div className="bg-[#1e2132] border border-slate-800 rounded-[28px] p-6 shadow-lg">
          <div className="flex items-center gap-3 mb-6">
            <Activity size={20} className="text-emerald-400" />
            <h3 className="text-white font-bold text-lg">Reported Factors</h3>
          </div>
          
          <div className="grid grid-cols-2 gap-4">
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Sleep</p>
              <p className="text-white font-semibold">{details.sleep_duration} hrs • {details.sleep_quality}/10</p>
            </div>
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Screen Time</p>
              <p className="text-white font-semibold">{details.screen_time} hrs</p>
            </div>
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Caffeine</p>
              <p className="text-white font-semibold">{details.caffeine_intake} cups</p>
            </div>
            <div className="bg-slate-900/50 p-4 rounded-2xl">
              <p className="text-slate-500 text-xs font-bold uppercase mb-1">Mood</p>
              <p className="text-white font-semibold">{details.mood}</p>
            </div>
          </div>
        </div>"""

new_ui = """        {/* Detailed Metrics Breakdown */}
        <div className="space-y-6">
          <h3 className="text-white font-bold text-lg px-2">Detailed Metrics</h3>
          
          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Personal</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Age</span><span className="text-white font-semibold text-sm">{details.age}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Gender</span><span className="text-white font-semibold text-sm">{details.gender}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Occupation</span><span className="text-white font-semibold text-sm">{details.occupation}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Marital Status</span><span className="text-white font-semibold text-sm">{details.marital_status}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Sleep</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Duration</span><span className="text-white font-semibold text-sm">{details.sleep_duration} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Quality</span><span className="text-white font-semibold text-sm">{details.sleep_quality}/5</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Wake/Bed</span><span className="text-white font-semibold text-sm">{details.wake_up_time} / {details.bed_time}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Lifestyle</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Activity Level</span><span className="text-white font-semibold text-sm">{details.physical_activity}/5</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Screen Time</span><span className="text-white font-semibold text-sm">{details.screen_time} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Caffeine/Alcohol</span><span className="text-white font-semibold text-sm">{details.caffeine_intake} / {details.alcohol_intake}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Smoking</span><span className="text-white font-semibold text-sm">{details.smoking_habit}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Work & Routine</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Work Hours</span><span className="text-white font-semibold text-sm">{details.work_hours} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Travel Time</span><span className="text-white font-semibold text-sm">{details.travel_time} hrs</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Social Score</span><span className="text-white font-semibold text-sm">{details.social_interactions}/5</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Workload</span><span className="text-white font-semibold text-sm">{details.workload}</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Health & Wellness</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Meditation</span><span className="text-white font-semibold text-sm">{details.meditation_practice} mins</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Exercise Type</span><span className="text-white font-semibold text-sm">{details.exercise_type}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Blood Pressure</span><span className="text-white font-semibold text-sm">{details.blood_pressure}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Blood Sugar</span><span className="text-white font-semibold text-sm">{details.blood_sugar_level} mg/dL</span></div>
          </div>

          <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-5 shadow-lg space-y-3">
            <h4 className="text-slate-400 text-xs font-bold uppercase tracking-wider mb-2">Mental State</h4>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Mood</span><span className="text-white font-semibold text-sm">{details.mood}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Anxiety</span><span className="text-white font-semibold text-sm">{details.anxiety}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Body Feeling</span><span className="text-white font-semibold text-sm">{details.body_feeling}</span></div>
            <div className="flex justify-between"><span className="text-slate-300 text-sm">Caffeine Dependency</span><span className="text-white font-semibold text-sm">{details.caffeine_dependency}</span></div>
          </div>
        </div>"""

content = content.replace(old_ui, new_ui)

with open('web/src/app/history/[id]/page.tsx', 'w', encoding='utf-8') as f:
    f.write(content)
