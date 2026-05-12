import os

file_path = 'app/src/main/java/com/cortisense/app/MainActivity.kt'
with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

replacements = {
    'text = "Welcome Back"': 'text = stringResource(R.string.welcome_back)',
    'text = "Sign In"': 'text = stringResource(R.string.sign_in)',
    'text = "Sign Up"': 'text = stringResource(R.string.signup)',
    'text = "Create Account"': 'text = stringResource(R.string.create_account)',
    'text = "Full Name"': 'text = stringResource(R.string.full_name)',
    'label = "Full Name"': 'label = stringResource(R.string.full_name)',
    'text = "Email Address"': 'text = stringResource(R.string.email_address)',
    'label = "Email Address"': 'label = stringResource(R.string.email_address)',
    'text = "Password"': 'text = stringResource(R.string.password)',
    'label = "Password"': 'label = stringResource(R.string.password)',
    'text = "Forgot Password?"': 'text = stringResource(R.string.forgot_password)',
    'text = "Change Photo"': 'text = stringResource(R.string.edit_profile)', # reuse
    'text = "Age"': 'text = stringResource(R.string.age)',
    'label = "Age"': 'label = stringResource(R.string.age)',
    'text = "Gender"': 'text = stringResource(R.string.gender)',
    'label = "Gender"': 'label = stringResource(R.string.gender)',
    'text = "Save Changes"': 'text = stringResource(R.string.save_profile)',
    'text = "More languages coming soon"': 'text = stringResource(R.string.tagline)',
    'text = "Good morning,"': 'text = stringResource(R.string.good_morning)',
    'placeholder = "Enter name"': 'placeholder = stringResource(R.string.fullname_placeholder)',
    'placeholder = "Enter email"': 'placeholder = stringResource(R.string.email_placeholder)',
    'placeholder = "Enter age"': 'placeholder = stringResource(R.string.enter_age)',
    'placeholder = "Select gender"': 'placeholder = stringResource(R.string.select_gender)',
}

for old, new in replacements.items():
    content = content.replace(old, new)

with open(file_path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Replacement complete 2.")
