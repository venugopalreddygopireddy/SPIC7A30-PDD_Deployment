import re

with open('mobile/app/src/main/java/com/cortisense/app/ApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# I need to add ProfileResponse and ProfileUpdate classes, and the endpoints.
# I will just write a python script to append them.

classes = """
data class ProfileResponse(
    val first_name: String,
    val last_name: String,
    val mobile_number: String?,
    val dob: String?,
    val age: Int,
    val gender: String,
    val goal: String?
)

data class ProfileUpdate(
    val first_name: String,
    val last_name: String,
    val mobile_number: String,
    val dob: String,
    val age: Int,
    val gender: String,
    val goal: String
)
"""

endpoints = """
    @GET("/users/me/profile")
    suspend fun getProfile(): ProfileResponse

    @PUT("/users/me/profile")
    suspend fun updateProfile(@Body profile: ProfileUpdate): ProfileResponse
"""

if 'ProfileResponse' not in content:
    content = content.replace('data class StressCheckInResponse(', classes + '\ndata class StressCheckInResponse(')
    content = content.replace('suspend fun getHistory(): List<StressCheckInResponse>', 'suspend fun getHistory(): List<StressCheckInResponse>\n' + endpoints)

with open('mobile/app/src/main/java/com/cortisense/app/ApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
