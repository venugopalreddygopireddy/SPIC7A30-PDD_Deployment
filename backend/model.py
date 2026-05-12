import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, StandardScaler
import joblib
import os

# ====================== NEURAL NETWORK MODEL ======================
class StressNN(nn.Module):
    def __init__(self, input_size=20):
        super(StressNN, self).__init__()
        self.layers = nn.Sequential(
            nn.Linear(input_size, 128),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(128, 64),
            nn.ReLU(),
            nn.Dropout(0.3),
            nn.Linear(64, 32),
            nn.ReLU(),
            nn.Linear(32, 4)   # 4 classes: Low, Medium, High, Critical
        )
    
    def forward(self, x):
        return self.layers(x)

# ====================== LOAD AND PREPARE DATA ======================
def load_and_train_model():
    # Load your dataset
    df = pd.read_excel('../stress_dataset.xlsx')   # Adjust path if needed
    
    # Features we will use
    features = ['Sleep_Duration', 'Sleep_Quality', 'Physical_Activity', 'Screen_Time', 
                'Caffeine_Intake', 'Work_Hours', 'Mood', 'Anxiety', 'Workload']
    
    X = df[features].copy()
    y = df['Stress_Detection']
    
    # Convert categorical to numbers
    label_encoders = {}
    for col in ['Mood', 'Anxiety', 'Workload']:
        if col in X.columns:
            le = LabelEncoder()
            X[col] = le.fit_transform(X[col].astype(str))
            label_encoders[col] = le
    
    # Scale numerical features
    scaler = StandardScaler()
    X = scaler.fit_transform(X)
    
    # Train-test split
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    
    # Convert to PyTorch tensors
    X_train = torch.FloatTensor(X_train)
    # (We will train later)
    
    # Save preprocessors
    joblib.dump(scaler, 'scaler.pkl')
    joblib.dump(label_encoders, 'label_encoders.pkl')
    
    print("✅ Data prepared successfully!")
    return X_train.shape[1]  # return input size

if __name__ == "__main__":
    input_size = load_and_train_model()
    print(f"Input size for model: {input_size}")