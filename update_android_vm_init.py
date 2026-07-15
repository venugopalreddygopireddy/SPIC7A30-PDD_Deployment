import re

with open('mobile/app/src/main/java/com/cortisense/app/MainViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

old_init = """    init {
        viewModelScope.launch {
            preferenceManager.isLoggedInFlow.collect { loggedIn ->
                isLoggedIn = loggedIn
            }
        }"""

new_init = """    init {
        viewModelScope.launch {
            preferenceManager.isLoggedInFlow.collect { loggedIn ->
                isLoggedIn = loggedIn
                if (loggedIn) {
                    fetchProfile()
                }
            }
        }"""

content = content.replace(old_init, new_init)

with open('mobile/app/src/main/java/com/cortisense/app/MainViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
