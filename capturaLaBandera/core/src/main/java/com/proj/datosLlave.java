package com.proj;

public class datosLlave {
    public float x;
    public float y;
    public float width;
    public float height;
    public String keyOwnerId;
    public boolean pickedUp;

    public datosLlave() {
    }

    public datosLlave(float x, float y, float width, float height, String keyOwnerId, boolean pickedUp) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.keyOwnerId = keyOwnerId;
        this.pickedUp = pickedUp;
    }
}
