package com.rlopez.molecare.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.gson.Gson;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    File configFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Create "configuration" and "images" folders if they don't exist
        File configPath = new File(getExternalFilesDir(null), "Config");
        File imagesPath = new File(getExternalFilesDir(null), "Images");
        configPath.mkdirs();
        imagesPath.mkdirs();

        // If "configuration" JSON doesn't exist, create it and add default configuration
        configFile = new File(configPath, "configuration.json");
        if (!configFile.exists()) {
            try {
                Gson gson = new Gson();
                Configuration defaultConfig = new Configuration(imagesPath.toString());
                String configJSON = gson.toJson(defaultConfig);

                // Create the new JSON file
                FileWriter JSONWriter = new FileWriter(configFile);
                JSONWriter.append(configJSON);
                JSONWriter.flush();
                JSONWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Read JSON configuration file
        Configuration configuration = Configuration.readConfigurationJSON(configFile, getApplicationContext());

        // Create necessary folders under Image path
        createFolders(configuration.getPaths().getBodyPartsList());

        // Wait WAIT_TIME_MS and go to home activity
        // Wait time in milliseconds
        int WAIT_TIME_MS = 2000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent homeIntent = new Intent(WelcomeActivity.this, HomeActivity.class);
                homeIntent.putExtra("CONFIGURATION_FILE_PATH", configFile.getAbsolutePath());
                startActivity(homeIntent);
            }
        }, WAIT_TIME_MS);

    }

    // Create necessary folders
    private void createFolders(List<String> folderPaths) {
        for (String fn : folderPaths) {
            File folder = new File(fn);
            folder.mkdirs();
        }
    }

}
