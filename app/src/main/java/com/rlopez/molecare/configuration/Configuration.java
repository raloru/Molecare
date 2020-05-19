package com.rlopez.molecare.configuration;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rlopez.molecare.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

// Maps JSON config file
public class Configuration {

    private Paths paths;
    private ImageConfiguration imageConfiguration;

    public Configuration(String imagesPath) {
        paths = new Paths();
        imageConfiguration = new ImageConfiguration();
        paths.setImagesPath(imagesPath);
        paths.setHeadImagesPath(imagesPath + File.separator + "Head moles");
        paths.setTorsoImagesPath(imagesPath + File.separator + "Torso moles");
        paths.setLeftArmImagesPath(imagesPath + File.separator + "Left arm moles");
        paths.setRightArmImagesPath(imagesPath + File.separator + "Right arm moles");
        paths.setLeftLegImagesPath(imagesPath + File.separator + "Left leg moles");
        paths.setRightLegImagesPath(imagesPath + File.separator + "Right leg moles");
        imageConfiguration.setTrimDimension("512");
    }

    public Paths getPaths() {
        return paths;
    }

    public ImageConfiguration getImageConfiguration() {
        return imageConfiguration;
    }

    // Read the configuration JSON and return it as a string
    public static Configuration readConfigurationJSON(File configFile, Context context) {
        StringBuilder jsonContent = new StringBuilder();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonContent.append(line);
                jsonContent.append('\n');
            }
            bufferedReader.close();
        } catch (IOException e) {
            Toast.makeText(context, R.string.error_config_file, Toast.LENGTH_SHORT).show();
        }
        Gson gson = new Gson();
        return gson.fromJson(jsonContent.toString(), Configuration.class);
    }

}