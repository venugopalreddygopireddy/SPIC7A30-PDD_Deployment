import pandas as pd
import joblib
from sklearn.preprocessing import LabelEncoder, StandardScaler

# Load dataset
df = pd.read_excel("stress_dataset.xlsx")

# Remove null rows
df = df.dropna()

# Convert datetime columns into string
datetime_columns = ['Wake_Up_Time', 'Bed_Time']

for col in datetime_columns:
    if col in df.columns:
        df[col] = df[col].astype(str)

# Save encoders
label_encoders = {}

# Encode text columns
text_columns = df.select_dtypes(include=['object', 'string']).columns

for column in text_columns:
    le = LabelEncoder()
    df[column] = le.fit_transform(df[column].astype(str))
    label_encoders[column] = le

# Save encoders
joblib.dump(label_encoders, "encoders.pkl")

# Features
X = df.drop("Stress_Detection", axis=1)

# Create scaler
scaler = StandardScaler()

# Fit scaler
scaler.fit(X)

# Save scaler
joblib.dump(scaler, "scaler.pkl")

print("Encoders and Scaler saved successfully")