import re

with open('web/src/lib/api.ts', 'r', encoding='utf-8') as f:
    content = f.read()

dashboard_interface = '''export interface DashboardSummaryResponse {
  total_checkins: number;
  latest_stress_score: number;
  latest_sleep_duration: number;
  latest_stress_category: string;
  current_streak: number;
  longest_streak: number;
  today_checkins_count: number;
  today_lowest_score: number;
  avg_stress_this_week: number;
  best_day_this_week: string;
}

'''

dashboard_api = '''export const getDashboardSummary = async (): Promise<DashboardSummaryResponse> => {
  const response = await api.get('/dashboard/summary');
  return response.data;
};

'''

if 'export interface DashboardSummaryResponse' not in content:
    content = content.replace('export const getHistory', dashboard_interface + 'export const getHistory')
if 'export const getDashboardSummary' not in content:
    content = content.replace('export const getHistory', dashboard_api + 'export const getHistory')

with open('web/src/lib/api.ts', 'w', encoding='utf-8') as f:
    f.write(content)
