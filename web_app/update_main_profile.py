import re

with open('backend/main.py', 'r', encoding='utf-8') as f:
    content = f.read()

new_endpoints = """
# ============================================
# PROFILE API
# ============================================

@app.get("/users/me/profile", response_model=schemas.ProfileResponse)
def get_user_profile(
    db: Session = Depends(get_db),
    token: str = Depends(oauth2_scheme)
):
    user_email = crud.decode_access_token(token)
    if user_email is None:
        raise HTTPException(status_code=401, detail="Invalid token")

    db_user = crud.get_user_by_email(db, email=user_email)
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")

    return db_user

@app.put("/users/me/profile", response_model=schemas.ProfileResponse)
def update_user_profile(
    profile_data: schemas.ProfileUpdate,
    db: Session = Depends(get_db),
    token: str = Depends(oauth2_scheme)
):
    user_email = crud.decode_access_token(token)
    if user_email is None:
        raise HTTPException(status_code=401, detail="Invalid token")

    db_user = crud.get_user_by_email(db, email=user_email)
    if db_user is None:
        raise HTTPException(status_code=404, detail="User not found")

    db_user.first_name = profile_data.first_name
    db_user.last_name = profile_data.last_name
    db_user.mobile_number = profile_data.mobile_number
    db_user.dob = profile_data.dob
    db_user.age = profile_data.age
    db_user.gender = profile_data.gender
    db_user.goal = profile_data.goal

    db.commit()
    db.refresh(db_user)

    return db_user

"""

if '@app.get("/users/me/profile"' not in content:
    # insert before CHECK-IN API
    content = content.replace('# ============================================  \n# CHECK-IN API', new_endpoints + '# ============================================  \n# CHECK-IN API')
    if new_endpoints not in content: # If the first replace didn't work due to space differences
        content = content.replace('# ============================================\n# CHECK-IN API', new_endpoints + '# ============================================\n# CHECK-IN API')


with open('backend/main.py', 'w', encoding='utf-8') as f:
    f.write(content)
