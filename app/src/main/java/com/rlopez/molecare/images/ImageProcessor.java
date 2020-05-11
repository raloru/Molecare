package com.rlopez.molecare.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;

import androidx.exifinterface.media.ExifInterface;

import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

public class CropAndRotate {

    // Rotate the bitmap if necessary to get the correct orientation
    public static Bitmap cropAndRotatePhoto(File photoFile, int trimDimension) throws IOException {

        // Get the photo exif (information about image generation)
        ExifInterface exif = new ExifInterface(photoFile.getAbsolutePath());

        // Get current rotation in degrees
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);

        // Use a matrix to rotate the image
        Matrix matrix = new Matrix();
        if (rotation != 0) {
            matrix.preRotate(rotationInDegrees);
        }

        // Create new bitmap with corresponding rotation and crop it
        Bitmap oldBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        Bitmap adjustedBitmap = Bitmap.createBitmap(oldBitmap, 0, 0, oldBitmap.getWidth(), oldBitmap.getHeight(), matrix, true);

        return cropBitmap(adjustedBitmap, trimDimension);
    }

    // Get a center square crop from a bitmap
    private static Bitmap cropBitmap(Bitmap imgBitmap, int trimDimension) {

        Bitmap croppedBitmap = imgBitmap;
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

    // Transform exif to degress
    private static int exifToDegrees(int exifOrientation) {
        int degrees = 0;
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            degrees = 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            degrees = 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            degrees = 270;
        }
        return degrees;
    }

}
