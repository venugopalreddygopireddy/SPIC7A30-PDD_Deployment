import re

with open('mobile/app/src/main/java/com/cortisense/app/ApiService.kt', 'r', encoding='utf-8') as f:
    content = f.read()

if 'import retrofit2.http.PUT' not in content:
    content = content.replace('import retrofit2.http.GET', 'import retrofit2.http.GET\nimport retrofit2.http.PUT')

with open('mobile/app/src/main/java/com/cortisense/app/ApiService.kt', 'w', encoding='utf-8') as f:
    f.write(content)
