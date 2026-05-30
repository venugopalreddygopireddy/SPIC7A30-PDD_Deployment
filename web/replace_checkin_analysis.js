const fs = require('fs');
let content = fs.readFileSync('c:/Users/venug/AndroidStudioProjects/CortiSense/web/src/app/checkin/page.tsx', 'utf8');

// 1. Add analysisResult state
content = content.replace(
  'const [error, setError] = useState<string | null>(null);',
  'const [error, setError] = useState<string | null>(null);\n  const [analysisResult, setAnalysisResult] = useState<any>(null);'
);

// 2. Update handleSubmit
content = content.replace(
  '      const result = await submitCheckIn(submitData);\n      router.push(\'/\');',
  '      const result = await submitCheckIn(submitData);\n      setAnalysisResult(result);\n      setLoading(false);'
);

// 3. Render analysis result if present
const returnRender = `  if (analysisResult) {
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

  return (`

content = content.replace('  return (\n    <div className="min-h-screen', returnRender + '\n    <div className="min-h-screen');

fs.writeFileSync('c:/Users/venug/AndroidStudioProjects/CortiSense/web/src/app/checkin/page.tsx', content);
console.log('Successfully updated checkin page with analysis result screen');
