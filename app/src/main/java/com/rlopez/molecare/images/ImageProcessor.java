/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

package com.rlopez.molecare.images;

import android.graphics.Bitmap;
import android.graphics.Matrix;


public class ImageProcessor {

    // Get a center square crop from a bitmap
    public static Bitmap cropAndRotateBitmap(Bitmap imgBitmap, int trimDimension) {

        Bitmap croppedBitmap;
        int startPixelX;
        int startPixelY;
        int width = imgBitmap.getWidth();
        int height = imgBitmap.getHeight();

        // Get start pixels
        startPixelX = width / 2 - trimDimension / 2;
        startPixelY = height / 2 - trimDimension / 2;

        // Trim the image
        croppedBitmap = Bitmap.createBitmap(imgBitmap, startPixelX, startPixelY, trimDimension, trimDimension);

        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        return Bitmap.createBitmap(croppedBitmap, 0, 0, croppedBitmap.getWidth(), croppedBitmap.getHeight(), matrix, true);
    }

}
