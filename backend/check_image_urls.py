import pickle

# ✅ Load the image map file
with open("final_image_map.pkl", "rb") as f:
    image_map = pickle.load(f)

# 🔍 Choose a key to test — update these as needed
test_key = ("Shirts", "Men", "Casual")  # Capitalized values!

# 🔁 Check if key exists and print URLs
if test_key in image_map:
    urls = image_map[test_key]
    print(f"✅ Found {len(urls)} image URLs for key: {test_key}\n")
    for i, url in enumerate(urls[:10]):  # Limit to 10 for preview
        print(f"{i+1}. {url}")
else:
    print(f"❌ Key not found: {test_key}")
