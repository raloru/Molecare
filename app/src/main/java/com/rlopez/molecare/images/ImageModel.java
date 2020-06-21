/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

package com.rlopez.molecare.images;

public class ImageModel {

    private String name;
    private String focusDistance;
    private String focalLength;
    private String sensorHeight;
    private String originalImageHeight;

    public ImageModel(String name, String focusDistance, String focalLength, String sensorHeight, String originalImageHeight) {
        this.name = name;
        this.focusDistance = focusDistance;
        this.focalLength = focalLength;
        this.sensorHeight = sensorHeight;
        this.originalImageHeight = originalImageHeight;
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

    public String getSensorHeight() {
        return sensorHeight;
    }

    public void setSensorHeight(String sensorHeight) {
        this.sensorHeight = sensorHeight;
    }

    public String getOriginalImageHeight() {
        return originalImageHeight;
    }

    public void setOriginalImageHeight(String originalImageHeight) {
        this.originalImageHeight = originalImageHeight;
    }
}
