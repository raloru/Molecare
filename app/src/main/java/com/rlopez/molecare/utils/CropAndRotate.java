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
    public static Bitmap cropAndRotatePhoto(File photoFile) throws IOException {
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
        return cropBitmap(adjustedBitmap);
    }

    public static Bitmap cropPhoto(File photoFile) {
        return cropBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
    }

    // Get a center square crop from a bitmap
    private static Bitmap cropBitmap(Bitmap imgBitmap) {
        Bitmap croppedBitmap = imgBitmap;
        int startPixel;
        int width = imgBitmap.getWidth();
        int height = imgBitmap.getHeight();
        int sideDimension;
        if (imgBitmap.getWidth() > imgBitmap.getHeight()) {
            // Set side dimension
            sideDimension = height;
            // Center the square
            startPixel = width / 2 - height / 2;
            // Crop the image
            croppedBitmap = Bitmap.createBitmap(imgBitmap, startPixel, 0, sideDimension, sideDimension);
        } else if (imgBitmap.getWidth() < imgBitmap.getHeight()) {
            // Set side dimension
            sideDimension = width;
            // Center the square
            startPixel = height / 2 - width / 2;
            // Crop the image
            croppedBitmap = Bitmap.createBitmap(imgBitmap, 0, startPixel, sideDimension, sideDimension);
        }
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

    public static Point getImageSize(WindowManager w) {
        Display display = w.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

}
