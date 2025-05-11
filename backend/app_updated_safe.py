
from fastapi import FastAPI, Query
import numpy as np
import pickle
import random

app = FastAPI()

# 🔹 Load trained models and encoders
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

@app.get("/")
def root():
    return {"message": "🧠 ModaSense API Running with Hybrid Model!"}

@app.get("/recommend")
def recommend(
    season: str = Query(...),
    usage: str = Query(...),
    gender: str = Query(...)
):
    try:
        # 🔹 Encode input values
        season_encoded = le_season.transform([season.capitalize()])[0]
        usage_encoded = le_usage.transform([usage.capitalize()])[0]
        gender_encoded = le_gender.transform([gender.capitalize()])[0]

        # 🔹 Predict using Random Forest
        input_features = np.array([[season_encoded, usage_encoded, gender_encoded]])
        cluster = rf_model.predict(input_features)[0]

        # 🔹 Get valid article types in that cluster
        cluster_articles = []
        for label_idx, assigned_cluster in enumerate(kmeans_model.labels_):
            if assigned_cluster == cluster:
                try:
                    decoded = article_type_encoder.inverse_transform([label_idx])[0]
                    cluster_articles.append(decoded)
                except Exception:
                    print(f"❌ Skipping invalid label index: {label_idx}")

        print(f"🎯 Cluster {cluster} → Articles: {len(cluster_articles)}")

        # 🔹 Fetch image URLs
        image_urls = []
        for article in cluster_articles:
            key = (article, gender.capitalize(), usage.capitalize())
            print(f"🔍 Searching key: {key}")
            if key in image_map:
                images = image_map[key]
                print(f"✅ Found {len(images)} images")
                image_urls.extend(images)
            else:
                print(f"❌ No match for key: {key}")

        random.shuffle(image_urls)

        print(f"🖼️ Total returned: {len(image_urls)}")
        return {
            "season": season,
            "usage": usage,
            "gender": gender,
            "cluster": int(cluster),
            "article_types": cluster_articles,
            "image_urls": image_urls
        }

    except Exception as e:
        return {"error": str(e)}
