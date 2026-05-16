import tensorflow as tf
import os
import subprocess
import sys
import shutil

# Paths
h5_path = "backend/stress_model.h5"
temp_saved_model = "backend/temp_model"
onnx_path = "backend/stress_model.onnx"

if not os.path.exists(h5_path):
    print(f"Error: {h5_path} not found!")
    exit(1)

if os.path.exists(temp_saved_model):
    shutil.rmtree(temp_saved_model)

print("Step 1: Exporting as SavedModel...")
model = tf.keras.models.load_model(h5_path)
model.export(temp_saved_model)

print("Step 2: Converting SavedModel to ONNX...")
subprocess.check_call([
    sys.executable, "-m", "tf2onnx.convert",
    "--saved-model", temp_saved_model,
    "--output", onnx_path,
    "--opset", "13"
])

print(f"\n✅ Success! Model converted to {onnx_path}")
shutil.rmtree(temp_saved_model)
