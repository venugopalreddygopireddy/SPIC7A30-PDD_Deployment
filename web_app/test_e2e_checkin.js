const axios = require('axios');

const API_URL = 'https://cortisense-backend.onrender.com';
const testEmail = `test_ui_checkin_${Date.now()}@example.com`;
const testPassword = 'Password123!';

async function runCheckinTest() {
  console.log("=== STARTING WEB UI CHECKIN TEST ===");
  try {
    // 1. Create Account
    console.log(`1. Creating account for ${testEmail}...`);
    await axios.post(`${API_URL}/register`, {
      first_name: "Web",
      last_name: "Tester",
      age: 21,
      gender: "Male",
      email: testEmail,
      password: testPassword
    });

    // 2. Login
    console.log(`2. Logging in...`);
    const loginRes = await axios.post(`${API_URL}/login`, {
      email: testEmail,
      password: testPassword
    });
    const token = loginRes.data.access_token;

    // 3. Web Checkin Payload (Exact from page.tsx)
    console.log(`3. Submitting EXACT web payload...`);
    const checkinPayload = {
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
      travel_time: 0.75,
      social_interactions: 3,
      meditation_practice: '0',
      exercise_type: 'Walking',
      blood_pressure: 120,
      blood_sugar_level: 140,
      mood: 'Neutral',
      anxiety: 'Low',
      caffeine_dependency: 'No',
      workload: 'Light',
      body_feeling: 'Normal'
    };

    const checkinRes = await axios.post(`${API_URL}/checkin`, checkinPayload, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log("Check-in Result:", checkinRes.data);
    console.log("✅ AI Prediction Returned successfully!");

    // 4. Verify Dashboard
    console.log(`4. Verifying Dashboard Updates...`);
    const dashRes = await axios.get(`${API_URL}/dashboard/summary`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    if (dashRes.data.recent_score === checkinRes.data.score) {
      console.log("✅ Dashboard matches checkin score.");
    }

    console.log("\n=== TEST COMPLETED SUCCESSFULLY ===");
  } catch (error) {
    console.error("❌ TEST FAILED:", error.response ? error.response.data : error.message);
  }
}

runCheckinTest();
