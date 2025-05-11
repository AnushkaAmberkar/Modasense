package com.example.modasense;

public class Product {
    private String name;
    private String category;
    private String image_url;

    public Product() {
        // Needed for Firestore
    }

    public Product(String name, String category, String image_url) {
        this.name = name;
        this.category = category;
        this.image_url = image_url;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getImage_url() {
        return image_url;
    }
}

