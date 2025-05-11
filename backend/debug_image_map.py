import pickle
import numpy as np

# Load .pkl files
with open("fashion_model_clustered.pkl", "rb") as f:
    rf_model = pickle.load(f)

with open("kmeans_cluster_model.pkl", "rb") as f:
    kmeans_model = pickle.load(f)

with open("encoders_clustered.pkl", "rb") as f:
    encoders = pickle.load(f)

with open("article_type_encoder.pkl", "rb") as f:
    article_type_encoder = pickle.load(f)

with open("final_image_map.pkl", "rb") as f:
    image_map = pickle.load(f)

# Label encoders
le_season = encoders["season"]
le_usage = encoders["usage"]
le_gender = encoders["gender"]

# Test values
season = "Spring"
usage = "Casual"
gender = "Men"

# Encode
season_encoded = le_season.transform([season.capitalize()])[0]
usage_encoded = le_usage.transform([usage.capitalize()])[0]
gender_encoded = le_gender.transform([gender.capitalize()])[0]

# Predict
input_features = np.array([[season_encoded, usage_encoded, gender_encoded]])
cluster = rf_model.predict(input_features)[0]
print(f"\n🎯 Predicted cluster: {cluster}")

# Get valid labels only
valid_labels = set(range(len(article_type_encoder.classes_)))

# Filter valid cluster labels
cluster_articles = []
for i, c in enumerate(kmeans_model.labels_):
    if c == cluster:
        if i in valid_labels:
            article = article_type_encoder.inverse_transform([i])[0]
            cluster_articles.append(article)
        else:
            print(f"❌ Skipping invalid label index: {i}")

print(f"📦 Articles in cluster: {len(cluster_articles)} → {cluster_articles}")

# Check image_map keys
found_images = 0
for article in cluster_articles:
    key = (article, gender.capitalize(), usage.capitalize())
    if key in image_map:
        images = image_map[key]
        print(f"✅ Key found: {key} → {len(images)} images")
        found_images += len(images)
    else:
        print(f"❌ Key missing: {key}")

print(f"\n🖼️ Total images found: {found_images}")
