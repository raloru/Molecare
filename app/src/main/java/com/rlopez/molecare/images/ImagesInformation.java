package com.rlopez.molecare.images;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rlopez.molecare.R;
import com.rlopez.molecare.models.ImageModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ImagesInformation {

    private List<ImageModel> imageModels;

    public ImagesInformation(List<ImageModel> imageModels) {
        this.imageModels = imageModels;
    }

    public List<ImageModel> getImageModels() {
        return imageModels;
    }

    public void setImageModels(List<ImageModel> imageModels) {
        this.imageModels = imageModels;
    }

    public void addImageModel(ImageModel imageModel) {
        this.imageModels.add(imageModel);
    }

    public void deleteImageModel(String name) {
        ImageModel imageModelDelete;
        for (ImageModel imageModel : imageModels) {
            imageModelDelete = imageModel;
            if (imageModelDelete.getName().equals(name)) {
                imageModels.remove(imageModelDelete);
                break;
            }
        }
    }

    public double getImageFocus(String name) {
        double focus = 0;
        ImageModel imageModel;
        for (ImageModel model : imageModels) {
            imageModel = model;
            if (imageModel.getName().equals(name)) {
                focus = Double.parseDouble(imageModel.getFocusDistance());
                break;
            }
        }
        return focus;
    }

    // Read the configuration JSON and return it as a string
    public static ImagesInformation readImagesInformationJSON(File imagesInformationFile, Context context) {
        StringBuilder jsonContent = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(imagesInformationFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonContent.append(line);
                jsonContent.append('\n');
            }
            bufferedReader.close();
        } catch (IOException e) {
            Toast.makeText(context, R.string.error_unexpected, Toast.LENGTH_SHORT).show();
        }
        Gson gson = new Gson();
        return gson.fromJson(jsonContent.toString(), ImagesInformation.class);
    }
}
