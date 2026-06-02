const fs = require('fs');
let content = fs.readFileSync('c:/Users/venug/AndroidStudioProjects/CortiSense/web/src/app/page.tsx', 'utf8');
const startIdx = content.indexOf('const renderFactors = () => {');
const endStr = 'const renderHistory = () => {';
const endIdx = content.indexOf(endStr);
if(startIdx!==-1 && endIdx!==-1) {
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
      <div className="flex items-center py-3">
        <Icon size={24} className="text-slate-300 mr-4" />
        <div className="flex-1">
          <div className="flex justify-between items-center mb-2">
            <div>
              <div className="text-white font-bold text-sm">{title}</div>
              <div className="text-slate-400 text-[10px]">{impact}</div>
            </div>
            <div className="text-white font-bold text-sm">{percentage}%</div>
          </div>
          <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
            <div className={\`h-full \${bgClass}\`} style={{ width: \`\${percentage}%\` }}></div>
          </div>
        </div>
      </div>
    );

    return (
      <div className="space-y-6">
        <div className="text-white">
          <h2 className="text-2xl font-bold mb-1">Factor Breakdown</h2>
          <p className="text-slate-400 text-sm">What influences your stress?</p>
        </div>

        <div className="bg-[#1e2132] border border-slate-800 rounded-[24px] p-6 shadow-lg">
          <div className="flex items-center mb-4">
            <Target size={20} className="text-[#82e0aa] mr-3" />
            <h3 className="text-white font-bold">Contribution Analysis</h3>
          </div>
          <p className="text-slate-400 text-xs mb-6">Each factor's impact on your stress levels.</p>
          
          <div className="space-y-2">
            <FactorItem icon={Moon} title="Sleep Duration" impact={\`\${factorsData.sleep_avg.toFixed(1)} hrs avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Smartphone} title="Screen Time" impact={\`\${factorsData.screen_time_avg.toFixed(1)} hrs avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Coffee} title="Caffeine Intake" impact={\`\${factorsData.caffeine_avg.toFixed(1)} cups avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={RunIcon} title="Physical Activity" impact={\`\${factorsData.physical_activity_avg.toFixed(1)} hrs avg\`} percentage={100} colorClass="text-[#82e0aa]" bgClass="bg-[#82e0aa]" />
            <FactorItem icon={Smile} title="Top Mood" impact={factorsData.top_mood} percentage={100} colorClass="text-yellow-500" bgClass="bg-yellow-500" />
            <FactorItem icon={Briefcase} title="Top Workload" impact={factorsData.top_workload} percentage={100} colorClass="text-rose-500" bgClass="bg-rose-500" />
          </div>
        </div>

        <div className="bg-emerald-900/20 border border-emerald-500/20 rounded-[20px] p-6 shadow-lg">
          <h3 className="text-emerald-400 font-bold mb-4">AI Recommendation</h3>
          
          {lastCheckinReasons.length === 0 ? (
            <p className="text-emerald-100/70 text-sm">Add more check-ins to see personalized recommendations.</p>
          ) : (
            <div className="space-y-4">
              {lastCheckinReasons.slice(0, 2).map((reason: string, i: number) => (
                <div key={i}>
                  <p className="text-[#82e0aa] font-bold text-sm mb-1">• {getTranslatedReason(reason)}</p>
                  <p className="text-emerald-100/80 text-[13px] leading-relaxed pl-3">
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
