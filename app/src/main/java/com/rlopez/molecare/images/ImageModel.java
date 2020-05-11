package com.rlopez.molecare.images;

public class ImageModel {

    private String name;
    private String focus;

    public ImageModel(String name, String focus) {
        this.name = name;
        this.focus = focus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }
}
