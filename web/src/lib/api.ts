import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export interface CheckInRequest {
  age: number;
  gender: string;
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
}

export const getHistory = async (): Promise<StressCheckInResponse[]> => {
  const response = await api.get('/history');
  return response.data;
};

export const submitCheckIn = async (data: CheckInRequest): Promise<AIAnalysisResult> => {
  const response = await api.post('/checkin', data);
  return response.data;
};

export default api;
