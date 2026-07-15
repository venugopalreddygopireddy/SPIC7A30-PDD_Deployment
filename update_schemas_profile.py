import re

with open('backend/schemas.py', 'r', encoding='utf-8') as f:
    content = f.read()

new_schemas = """
# =========================================================
# PROFILE SCHEMAS
# =========================================================

class ProfileResponse(BaseModel):
    first_name: str
    last_name: str
    mobile_number: Optional[str] = ""
    dob: Optional[str] = ""
    age: int
    gender: str
    goal: Optional[str] = ""

    class Config:
        from_attributes = True

class ProfileUpdate(BaseModel):
    first_name: str
    last_name: str
    mobile_number: Optional[str] = ""
    dob: Optional[str] = ""
    age: int
    gender: str
    goal: Optional[str] = ""

"""

if 'class ProfileResponse' not in content:
    # insert before CHECK-IN REQUEST
    content = content.replace('# =========================================================\n# CHECK-IN REQUEST', new_schemas + '# =========================================================\n# CHECK-IN REQUEST')

with open('backend/schemas.py', 'w', encoding='utf-8') as f:
    f.write(content)
