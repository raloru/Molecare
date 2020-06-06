package com.rlopez.molecare.models;

public class ImageModel {

    private String name;
    private String focusDistance;
    private String focalLength;

    public ImageModel(String name, String focusDistance, String focalLength) {
        this.name = name;
        this.focusDistance = focusDistance;
        this.focalLength = focalLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFocusDistance() {
        return focusDistance;
    }

    public void setFocusDistance(String focusDistance) {
        this.focusDistance = focusDistance;
    }

    public String getFocalLength() {
        return focalLength;
    }

    public void setFocalLength(String focalLength) {
        this.focalLength = focalLength;
    }
}
