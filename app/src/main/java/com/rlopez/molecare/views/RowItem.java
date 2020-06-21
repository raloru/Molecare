package com.rlopez.molecare.views;

/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

import android.graphics.Bitmap;

// This class represents each row in used lists. It has a name and an image
public class RowItem {

    private String name;
    private Bitmap image;

    public RowItem(String name, Bitmap image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
