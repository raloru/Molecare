package com.rlopez.molecare.utils;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileManager {

    // Create an image file to save the photo
    public static File createTempPhotoFile(String path) throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageName = "JPEG_" + timeStamp + "_";
        File currentFolder = new File(path);
        return File.createTempFile(imageName, ".jpg", currentFolder);
    }

}
