import pandas as pd
import numpy as np
import joblib

from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import StandardScaler
from sklearn.model_selection import train_test_split

from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout, BatchNormalization
from tensorflow.keras.utils import to_categorical
from tensorflow.keras.callbacks import EarlyStopping
from tensorflow.keras.regularizers import l2

# =========================================================
# LOAD DATASET
# =========================================================

print("Loading dataset...")

df = pd.read_excel("improved_stress_dataset.xlsx")
print(df.head())

# =========================================================
# REMOVE EMPTY VALUES
# =========================================================

df = df.dropna()

# =========================================================
# TARGET COLUMN
# =========================================================

target_column = "Stress_Detection"

# =========================================================
# MANUAL STRESS MAPPING
# =========================================================

stress_mapping = {

    "Low": 0,

    "Moderate": 1,

    "High": 2,

    "Critical": 3
}

# =========================================================
# FEATURES & TARGET
# =========================================================

X = df.drop(target_column, axis=1)

y = df[target_column].map(stress_mapping)

# =========================================================
# ENCODE CATEGORICAL FEATURES
# =========================================================

print("Encoding categorical features...")

label_encoders = {}

for column in X.columns:

    if not pd.api.types.is_numeric_dtype(X[column]):

        le = LabelEncoder()

        X[column] = le.fit_transform(
            X[column].astype(str)
        )

        label_encoders[column] = le

# =========================================================
# CONVERT TARGET TO CATEGORICAL
# =========================================================

num_classes = len(np.unique(y))

y_categorical = to_categorical(
    y,
    num_classes=num_classes
)

# =========================================================
# FEATURE SCALING
# =========================================================

print("Scaling features...")

scaler = StandardScaler()

X_scaled = scaler.fit_transform(X)

# =========================================================
# SPLIT DATASET
# =========================================================

X_train, X_test, y_train, y_test = train_test_split(

    X_scaled,
    y_categorical,

    test_size=0.2,

    random_state=42,

    shuffle=True
)

# =========================================================
# BUILD ADVANCED NEURAL NETWORK
# =========================================================

model = Sequential([

    Dense(
        128,
        activation='relu',
        input_shape=(X_train.shape[1],),
        kernel_regularizer=l2(0.001)
    ),

    BatchNormalization(),

    Dropout(0.4),

    Dense(
        64,
        activation='relu',
        kernel_regularizer=l2(0.001)
    ),

    BatchNormalization(),

    Dropout(0.3),

    Dense(
        32,
        activation='relu',
        kernel_regularizer=l2(0.001)
    ),

    Dropout(0.2),

    Dense(
        num_classes,
        activation='softmax'
    )
])

# =========================================================
# COMPILE MODEL
# =========================================================

model.compile(

    optimizer='adam',

    loss='categorical_crossentropy',

    metrics=['accuracy']
)

# =========================================================
# EARLY STOPPING
# =========================================================

early_stop = EarlyStopping(

    monitor='val_loss',

    patience=10,

    restore_best_weights=True
)

# =========================================================
# TRAIN MODEL
# =========================================================

print("Starting training...")

history = model.fit(

    X_train,
    y_train,

    validation_split=0.2,

    epochs=50,

    batch_size=32,

    callbacks=[early_stop],

    verbose=1
)

# =========================================================
# EVALUATE MODEL
# =========================================================

loss, accuracy = model.evaluate(
    X_test,
    y_test
)

print(f"\nFinal Test Accuracy: {accuracy * 100:.2f}%")

print("\nStress Class Mapping:")

print("Low → 0")
print("Moderate → 1")
print("High → 2")
print("Critical → 3")

# =========================================================
# SAVE MODEL & ARTIFACTS
# =========================================================

print("\nSaving model and preprocessing artifacts...")

model.save("stress_model.h5")

joblib.dump(
    label_encoders,
    "encoders.pkl"
)

joblib.dump(
    scaler,
    "scaler.pkl"
)

joblib.dump(
    stress_mapping,
    "stress_mapping.pkl"
)

print("\nAll artifacts saved successfully.")