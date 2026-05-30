import axios from 'axios';

let API_BASE_URL = 'https://cortisense-backend.onrender.com';

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

export interface AIAnalysisResult {
  stress_level: string;
  score: number;
  message: string;
  recommendation: string;
  is_escalated: boolean;
}

export interface StressCheckInResponse {
  id: number;
  timestamp: string;
  stress_level: string;
  score: number;
  recommendation: string;
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
  const response = await api.get(`/checkin/${id}`);
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
