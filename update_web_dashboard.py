import re

with open('web/src/app/page.tsx', 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Update imports
if 'DashboardSummaryResponse' not in content:
    content = content.replace('FactorsResponse,', 'FactorsResponse,\n  DashboardSummaryResponse,')
    content = content.replace('getFactorsAnalytics', 'getFactorsAnalytics,\n  getDashboardSummary')

# 2. Add state
if 'setDashboardSummary' not in content:
    state_str = "const [factorsData, setFactorsData] = useState<FactorsResponse | null>(null);\n  const [dashboardSummary, setDashboardSummary] = useState<DashboardSummaryResponse | null>(null);"
    content = content.replace('const [factorsData, setFactorsData] = useState<FactorsResponse | null>(null);', state_str)

# 3. Update fetchData
if 'getDashboardSummary()' not in content:
    promise_old = """const [hist, weekly, monthly, trends, factors] = await Promise.all([
          getHistory().catch(() => []),
          getWeeklyAnalytics().catch(() => null),
          getMonthlyAnalytics().catch(() => null),
          getTrendsAnalytics().catch(() => null),
          getFactorsAnalytics().catch(() => null)
        ]);"""
    promise_new = """const [hist, weekly, monthly, trends, factors, dashboard] = await Promise.all([
          getHistory().catch(() => []),
          getWeeklyAnalytics().catch(() => null),
          getMonthlyAnalytics().catch(() => null),
          getTrendsAnalytics().catch(() => null),
          getFactorsAnalytics().catch(() => null),
          getDashboardSummary().catch(() => null)
        ]);"""
    content = content.replace(promise_old, promise_new)
    
    set_old = "if (factors) setFactorsData(factors);"
    set_new = "if (factors) setFactorsData(factors);\n        if (dashboard) setDashboardSummary(dashboard);"
    content = content.replace(set_old, set_new)

# 4. Remove manual calculations for todayCheckinsCount and stressScore
# In page.tsx around line 103:
#   const latestCheckIn = history[0];
#   const stressScore = latestCheckIn?.score ?? 0;
#   
#   // Dynamic stats calculation based on real data
#   const todayCheckinsCount = history.filter(item => {
#     const today = new Date().toISOString().split('T')[0];
#     return item.timestamp.startsWith(today);
#   }).length;

old_calc = """  const latestCheckIn = history[0];
  const stressScore = latestCheckIn?.score ?? 0;
  
  // Dynamic stats calculation based on real data
  const todayCheckinsCount = history.filter(item => {
    const today = new Date().toISOString().split('T')[0];
    return item.timestamp.startsWith(today);
  }).length;"""

new_calc = """  const latestCheckIn = history[0];
  const stressScore = dashboardSummary?.latest_stress_score ?? 0;
  const todayCheckinsCount = dashboardSummary?.today_checkins_count ?? 0;
  const currentStreak = dashboardSummary?.current_streak ?? 0;
  const latestSleepDuration = dashboardSummary?.latest_sleep_duration ?? 0;"""

if 'dashboardSummary?.latest_stress_score' not in content:
    content = content.replace(old_calc, new_calc)

# 5. Update UI rendering for streak and sleep
# In page.tsx around line 646:
#               <p className="text-white font-extrabold text-lg">{history.length > 0 ? 1 : 0}</p>
#               <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Streak</p>
# ...
#               <p className="text-white font-extrabold text-lg">{history.length > 0 ? history[0].score : '0'}h</p>
#               <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Sleep</p>

# Streak replacement
streak_old = """<p className="text-white font-extrabold text-lg">{history.length > 0 ? 1 : 0}</p>
              <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Streak</p>"""
streak_new = """<p className="text-white font-extrabold text-lg">{currentStreak}</p>
              <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Streak</p>"""

if 'currentStreak' in content and 'history.length > 0 ? 1 : 0' in content:
    content = content.replace(streak_old, streak_new)

# Sleep replacement
sleep_old = """<p className="text-white font-extrabold text-lg">{history.length > 0 ? history[0].score : '0'}h</p>
              <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Sleep</p>"""
sleep_new = """<p className="text-white font-extrabold text-lg">{latestSleepDuration}h</p>
              <p className="text-slate-400 text-xs font-bold uppercase tracking-wider mt-1">Sleep</p>"""

if 'latestSleepDuration' in content and 'history.length > 0 ? history[0].score' in content:
    content = content.replace(sleep_old, sleep_new)

with open('web/src/app/page.tsx', 'w', encoding='utf-8') as f:
    f.write(content)
