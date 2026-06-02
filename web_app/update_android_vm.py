import re

with open('mobile/app/src/main/java/com/cortisense/app/MainViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Update updateProfile to call API
old_update = """    fun updateProfile(firstName: String, lastName: String, email: String, dob: String, age: String, gender: String, goal: String, mobile: String, imageUri: String) {
        viewModelScope.launch {
            preferenceManager.updateProfile(firstName, lastName, email, dob, age, gender, goal, mobile, imageUri)
        }
    }"""

new_update = """    fun updateProfile(firstName: String, lastName: String, email: String, dob: String, age: String, gender: String, goal: String, mobile: String, imageUri: String) {
        viewModelScope.launch {
            try {
                // Ignore imageUri for backend
                val profileUpdate = ProfileUpdate(
                    first_name = firstName,
                    last_name = lastName,
                    mobile_number = mobile,
                    dob = dob,
                    age = age.toIntOrNull() ?: 0,
                    gender = gender,
                    goal = goal
                )
                val response = RetrofitClient.instance.updateProfile(profileUpdate)
                
                // Update local DataStore
                preferenceManager.updateProfile(
                    response.first_name, 
                    response.last_name, 
                    email, 
                    response.dob ?: "", 
                    response.age.toString(), 
                    response.gender, 
                    response.goal ?: "", 
                    response.mobile_number ?: "", 
                    imageUri
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getProfile()
                
                // Read local to see if migration is needed
                val localMobile = _userMobile.value
                val localDob = _userDob.value
                
                if (response.mobile_number.isNullOrEmpty() && localMobile.isNotEmpty()) {
                    // Legacy user: migrate local to cloud
                    val profileUpdate = ProfileUpdate(
                        first_name = _userFirstName.value,
                        last_name = _userLastName.value,
                        mobile_number = localMobile,
                        dob = localDob,
                        age = _userAge.value.toIntOrNull() ?: 0,
                        gender = _userGender.value,
                        goal = _userGoal.value
                    )
                    RetrofitClient.instance.updateProfile(profileUpdate)
                } else {
                    // Sync cloud down to local
                    preferenceManager.updateProfile(
                        response.first_name, 
                        response.last_name, 
                        _userEmail.value, 
                        response.dob ?: "", 
                        response.age.toString(), 
                        response.gender, 
                        response.goal ?: "", 
                        response.mobile_number ?: "", 
                        "" // Static image
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }"""

if 'fun fetchProfile()' not in content:
    content = content.replace(old_update, new_update)

with open('mobile/app/src/main/java/com/cortisense/app/MainViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
