import re

with open('web/src/lib/api.ts', 'r', encoding='utf-8') as f:
    content = f.read()

new_api = """
export interface ProfileResponse {
  first_name: string;
  last_name: string;
  mobile_number: string;
  dob: string;
  age: number;
  gender: string;
  goal: string;
}

export const getProfile = async (): Promise<ProfileResponse> => {
  const response = await api.get('/users/me/profile');
  return response.data;
};

export const updateProfile = async (profileData: ProfileResponse): Promise<ProfileResponse> => {
  const response = await api.put('/users/me/profile', profileData);
  return response.data;
};
"""

if 'export const getProfile' not in content:
    content = content + '\n' + new_api

with open('web/src/lib/api.ts', 'w', encoding='utf-8') as f:
    f.write(content)
