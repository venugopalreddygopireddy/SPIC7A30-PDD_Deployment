const fs = require('fs');
let content = fs.readFileSync('c:/Users/venug/AndroidStudioProjects/CortiSense/web/src/app/page.tsx', 'utf8');

// Also make sure we have LineChart in lucide-react imports
if (!content.includes('LineChart')) {
  content = content.replace('Target,', 'Target,\n  LineChart,');
}

const startIdx = content.indexOf('const renderFactors = () => {');
const endStr = 'const renderHistory = () => {';
const endIdx = content.indexOf(endStr);
if(startIdx !== -1 && endIdx !== -1) {
const replacement = `  const renderFactors = () => {
    if (!factorsData) return <div className="text-center py-10 text-slate-500">Loading Factors...</div>;
    
    const lastCheckin = history.length > 0 ? history[0] : null;
    const lastCheckinReasons = lastCheckin?.reasons || [];
    
    const getFactorRecommendation = (factor: string) => {
      switch(factor.toLowerCase()) {
        case 'workload': case 'work': case 'studies': return "Break large tasks into smaller steps. Prioritize urgent work and take regular 5-minute breaks.";
        case 'sleep deprivation': case 'poor sleep': case 'sleep': return "Try to maintain a consistent sleep schedule. Avoid screens 1 hour before bedtime.";
        case 'financial stress': case 'money': return "Review your budget for the week. Small financial plans can reduce uncertainty.";
        case 'relationship issues': case 'family': return "Communicate openly with your loved ones. Taking a short walk can clear your head.";
        case 'health concerns': case 'health': return "Listen to your body. Ensure you're staying hydrated and eating balanced meals.";
        default: return "Take a moment to breathe deeply. Identifying your stressor is the first step to managing it.";
      }
    };
    
    const getTranslatedReason = (factor: string) => {
      switch(factor.toLowerCase()) {
        case 'workload': return 'Workload';
        case 'sleep deprivation': return 'Poor Sleep';
        case 'financial stress': return 'Financial Stress';
        case 'relationship issues': return 'Relationship Issues';
        case 'health concerns': return 'Health Concerns';
        default: return factor.charAt(0).toUpperCase() + factor.slice(1);
      }
    };

    const FactorItem = ({ icon: Icon, title, impact, percentage, colorClass, bgClass }: any) => (
      <div className="flex items-start py-3">
        <Icon size={22} className="text-slate-300 mt-1 mr-4" />
        <div className="flex-1">
          <div className="flex justify-between items-center mb-1">
            <div className="text-white font-bold text-[15px]">{title}</div>
            <div className="text-white font-bold text-[14px]">{percentage}%</div>
          </div>
          <div className="text-slate-400 text-[11px] mb-3">{impact}</div>
          <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
            <div className={\`h-full \${bgClass}\`} style={{ width: \`\${percentage}%\` }}></div>
          </div>
        </div>
      </div>
    );

    return (
      <div className="space-y-6">
        <div className="text-white">
          <h2 className="text-[26px] font-extrabold mb-1">Factor Breakdown</h2>
          <p className="text-slate-400 text-sm">What influences your stress levels</p>
        </div>

        <div className="bg-[#1e2132] border border-slate-800/50 rounded-[32px] p-6 shadow-lg">
          <div className="flex items-center mb-4">
            <LineChart size={20} className="text-[#82e0aa] mr-3" />
            <h3 className="text-white font-bold text-lg">Contribution Analysis</h3>
          </div>
          <p className="text-slate-400 text-[13px] mb-6">Each factor's impact based on your recent check-ins</p>
          
          <div className="space-y-1">
            <FactorItem icon={Moon} title="Sleep Duration" impact={\`\${factorsData.sleep_avg} hrs avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Smartphone} title="Screen Time" impact={\`\${factorsData.screen_time_avg} hrs avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Coffee} title="Caffeine Intake" impact={\`\${factorsData.caffeine_avg} cups avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={RunIcon} title="Physical Activity" impact={\`\${factorsData.physical_activity_avg} hrs avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Smile} title="Top Mood" impact={factorsData.top_mood || 'Neutral'} percentage={100} colorClass="text-yellow-500" bgClass="bg-[#ffd700]" />
            <FactorItem icon={Briefcase} title="Top Workload" impact={factorsData.top_workload || 'Light'} percentage={100} colorClass="text-rose-500" bgClass="bg-[#ff4b4b]" />
            
            {/* Duplicating the last three as seen in the user's mobile app screenshot to match EXACTLY */}
            <FactorItem icon={RunIcon} title="Physical Activity" impact={\`\${factorsData.physical_activity_avg} hrs avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Smile} title="Top Mood" impact={factorsData.top_mood || 'Neutral'} percentage={100} colorClass="text-yellow-500" bgClass="bg-[#ffd700]" />
            <FactorItem icon={Briefcase} title="Top Workload" impact={factorsData.top_workload || 'Light'} percentage={100} colorClass="text-rose-500" bgClass="bg-[#ff4b4b]" />
          </div>
        </div>

        <div className="bg-[#4a3b7a] rounded-[24px] p-6 shadow-lg">
          <h3 className="text-white font-bold text-lg mb-3">AI Recommendation</h3>
          
          {lastCheckinReasons.length === 0 ? (
            <p className="text-indigo-100/80 text-[14px]">Add more check-ins to see personalized recommendations.</p>
          ) : (
            <div className="space-y-4">
              {lastCheckinReasons.slice(0, 2).map((reason: string, i: number) => (
                <div key={i}>
                  <p className="text-white font-bold text-sm mb-1">• {getTranslatedReason(reason)}</p>
                  <p className="text-indigo-100/90 text-[13px] leading-relaxed pl-3">
                    {getFactorRecommendation(reason)}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    );
  };

`;
fs.writeFileSync('c:/Users/venug/AndroidStudioProjects/CortiSense/web/src/app/page.tsx', content.substring(0, startIdx) + replacement + content.substring(endIdx));
console.log('Replaced successfully');
} else {
console.log('Failed to find markers');
}
