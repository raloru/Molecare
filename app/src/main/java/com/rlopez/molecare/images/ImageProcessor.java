package com.rlopez.molecare.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;

public class ImageProcessor {

    // Rotate the bitmap if necessary to get the correct orientation
    public static Bitmap cropAndRotatePhoto(File photoFile, int trimDimension) throws IOException {

        // Get the photo exif (information about image generation)
        ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());

        // Get current rotation
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (rotation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotation = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotation = 0;
                break;
            default:
                rotation = 90;
        }

        // Rotate the image if needed
        Bitmap original = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        int width = original.getWidth();
        int height = original.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);

        Bitmap rotated = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);

        return cropBitmap(rotated, trimDimension);
    }

    // Get a center square crop from a bitmap
    private static Bitmap cropBitmap(Bitmap imgBitmap, int trimDimension) {

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
        return croppedBitmap;
    }

}
