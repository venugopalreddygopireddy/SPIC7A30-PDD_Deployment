import axios from 'axios';

let API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'https://cortisense-backend.onrender.com';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
    const token = localStorage.getItem('jwtToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

export interface CheckInRequest {
  age: number;
  gender: string;
  mobile_number?: string;
  occupation: string;
  marital_status: string;
  sleep_duration: number;
  sleep_quality: number;
  wake_up_time: string;
  bed_time: string;
  physical_activity: number;
  screen_time: number;
  caffeine_intake: number;
  alcohol_intake: string;
  smoking_habit: string;
  work_hours: number;
  travel_time: number;
  social_interactions: number;
  meditation_practice: string;
  exercise_type: string;
  blood_pressure: number;
  blood_sugar_level: number;
  mood: string;
  anxiety: string;
  caffeine_dependency: string;
  workload: string;
  body_feeling: string;
}

export interface ActionItem {
  id: string;
  title: string;
  description: string;
  is_done: boolean;
}

export interface AIAnalysisResult {
  stress_level: string;
  score: number;
  message: string;
  recommendation: string;
  actions?: ActionItem[];
  is_escalated: boolean;
}

export interface StressCheckInResponse {
  id: number;
  timestamp: string;
  stress_level: string;
  score: number;
  recommendation: string;
  actions?: ActionItem[];
  is_escalated: boolean;
  reasons?: string[];
}

export interface WeeklyAnalyticsResponse {
  avg_score: number;
  highest_score: number;
  lowest_score: number;
  total_checkins: number;
  distribution: {
    low: number;
    moderate: number;
    high: number;
  };
}

export interface MonthlyAnalyticsResponse {
  avg_score: number;
  total_checkins: number;
  distribution: {
    low: number;
    moderate: number;
    high: number;
  };
  calendar_activity: Record<string, number>;
}

export interface TrendsResponse {
  trends: {
    date: string;
    score: number;
    level: string;
  }[];
}

export interface FactorsResponse {
  sleep_avg: number;
  screen_time_avg: number;
  caffeine_avg: number;
  physical_activity_avg: number;
  top_mood: string;
  top_workload: string;
  top_exercise: string;
}

export interface DashboardSummaryResponse {
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

export const getDashboardSummary = async (): Promise<DashboardSummaryResponse> => {
  const response = await api.get('/dashboard/summary');
  return response.data;
};

export const getHistory = async (): Promise<StressCheckInResponse[]> => {
  const response = await api.get('/history');
  return response.data;
};

export const getWeeklyAnalytics = async (): Promise<WeeklyAnalyticsResponse> => {
  const response = await api.get('/analytics/weekly');
  return response.data;
};

export const getMonthlyAnalytics = async (): Promise<MonthlyAnalyticsResponse> => {
  const response = await api.get('/analytics/monthly');
  return response.data;
};

export const getTrendsAnalytics = async (): Promise<TrendsResponse> => {
  const response = await api.get('/analytics/trends');
  return response.data;
};

export const getFactorsAnalytics = async (): Promise<FactorsResponse> => {
  const response = await api.get('/analytics/factors');
  return response.data;
};

export const submitCheckIn = async (data: CheckInRequest): Promise<AIAnalysisResult> => {
  const response = await api.post('/checkin', data);
  return response.data;
};

export const getCheckinDetails = async (id: number): Promise<any> => {
  const response = await api.get(`/history/${id}`);
  return response.data;
};

export const completeAction = async (checkinId: number, actionId: string): Promise<any> => {
  const response = await api.patch(`/checkin/${checkinId}/action/${actionId}/complete`);
  return response.data;
};

export const login = async (data: any): Promise<{ access_token: string }> => {
  const response = await api.post('/login', data);
  return response.data;
};

export const register = async (data: any): Promise<any> => {
  const response = await api.post('/register', data);
  return response.data;
};

export const forgotPassword = async (data: any): Promise<any> => {
  const response = await api.post('/forgot-password', data);
  return response.data;
};

export const verifyOTP = async (data: { email: string, otp: string }): Promise<any> => {
  const response = await api.post('/verify-otp', data);
  return response.data;
};

export const resetPassword = async (data: { email: string, new_password: string }): Promise<any> => {
  const response = await api.post('/reset-password', data);
  return response.data;
};

export default api;


export interface ProfileResponse {
  first_name: string;
  last_name: string;
  mobile_number: string;
  dob: string;
  age: number;
  gender: string;
  goal: string;
  profile_image?: string;
}

export const getProfile = async (): Promise<ProfileResponse> => {
  const response = await api.get('/users/me/profile');
  return response.data;
};

export const updateProfile = async (profileData: ProfileResponse): Promise<ProfileResponse> => {
  const response = await api.put('/users/me/profile', profileData);
  return response.data;
};

export const deleteAccount = async (): Promise<any> => {
  const response = await api.delete('/users/me');
  return response.data;
};
