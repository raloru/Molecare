/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

package com.rlopez.molecare.configuration;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rlopez.molecare.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static java.io.File.separator;

// Maps JSON config file
public class Configuration {

    private Paths paths;
    private ImageConfiguration imageConfiguration;

    public Configuration(String imagesPath) {
        paths = new Paths();
        imageConfiguration = new ImageConfiguration();
        paths.setImagesPath(imagesPath);
        paths.setHeadImagesPath(imagesPath + separator + "Head moles");
        paths.setTorsoImagesPath(imagesPath + separator + "Torso moles");
        paths.setLeftArmImagesPath(imagesPath + separator + "Left arm moles");
        paths.setRightArmImagesPath(imagesPath + separator + "Right arm moles");
        paths.setLeftLegImagesPath(imagesPath + separator + "Left leg moles");
        paths.setRightLegImagesPath(imagesPath + separator + "Right leg moles");
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