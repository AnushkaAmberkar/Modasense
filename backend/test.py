import pickle

# Load the final image map
with open("final_image_map.pkl", "rb") as f:
    image_map = pickle.load(f)

# Print first 20 keys
for i, key in enumerate(image_map.keys()):
    print(f"{i+1}. {key}")
    if i >= 20:
        break

print(f"\n✅ Total Keys in Map: {len(image_map)}")
