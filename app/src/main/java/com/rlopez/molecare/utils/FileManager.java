/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

package com.rlopez.molecare.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rlopez.molecare.R;
import com.rlopez.molecare.images.ImagesInformation;

import java.io.File;
import java.io.FileWriter;
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

    // Delete a mole folder and all the content inside
    public static void deleteFolderAndChildren(File folder) {
        String[] children = folder.list();
        assert children != null;
        for (String child : children) {
            File toDelete = new File(folder, child);
            if (toDelete.isDirectory()) {
                deleteFolderAndChildren(toDelete);
            } else {
                toDelete.delete();
            }
        }
        folder.delete();
    }

    // Delete an original mole photo and its associated images
    public static void deletePhotoFile(File photoFileDelete, File parentFolder, Activity activity) {
        File parent = photoFileDelete.getParentFile();
        assert parent != null;
        String[] children = parent.list();
        assert children != null;
        if (children.length == 4) {
            for (String child : children) {
                File toDelete = new File(parent, child);
                if (toDelete.isDirectory()) {
                    deleteFolderAndChildren(toDelete);
                } else {
                    toDelete.delete();
                }
            }
            parent.delete();
            Toast.makeText(activity.getApplicationContext(), parent.getName() + " " + activity.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
            activity.finish();
        } else {
            File imagesInformationFile = new File(parentFolder, "ImagesInformation.json");
            ImagesInformation imagesInformation = ImagesInformation.readImagesInformationJSON(imagesInformationFile, activity.getApplicationContext());
            imagesInformation.deleteImageModel(photoFileDelete.getName());
            Gson gson = new Gson();
            String imagesInformationJSON = gson.toJson(imagesInformation);
            try {
                // Update the JSON file
                FileWriter JSONWriter = new FileWriter(imagesInformationFile);
                JSONWriter.write(imagesInformationJSON);
                JSONWriter.flush();
                JSONWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            File binary = new File(parent + File.separator + "binary", photoFileDelete.getName());
            File coloured = new File(parent + File.separator + "coloured", photoFileDelete.getName());
            photoFileDelete.delete();
            binary.delete();
            coloured.delete();
            Toast.makeText(activity.getApplicationContext(), photoFileDelete.getName() + " " + activity.getString(R.string.deleted), Toast.LENGTH_SHORT).show();
        }

    }

}
