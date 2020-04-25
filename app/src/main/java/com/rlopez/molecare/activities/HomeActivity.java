package com.rlopez.molecare.activities;

import android.content.Intent;
import android.os.Bundle;

import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    // To get current configuration
    File configFile;
    Configuration configuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get configuration file and read it
        configFile = new File(getIntent().getStringExtra("CONFIGURATION_FILE_PATH"));
        configuration = Configuration.readConfigurationJSON(configFile, getApplicationContext());

        // Get elements from view
        ImageView headImg = findViewById(R.id.headImage);
        ImageView torsoImg = findViewById(R.id.torsoImage);
        ImageView leftArmImg = findViewById(R.id.leftArmImage);
        ImageView rightArmImg = findViewById(R.id.rightArmImage);
        ImageView leftLegImg = findViewById(R.id.leftLegImage);
        ImageView rightLegImg = findViewById(R.id.rightLegImage);

        // Create new head images activity
        headImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MolesActivity.class);
                intent.putExtra("BODY_PART_PATH", configuration.getPaths().getHeadImagesPath());
                intent.putExtra("CONFIGURATION_FILE_PATH", configFile.getAbsolutePath());
                startActivity(intent);
            }
        });

        // Create new torso images activity
        torsoImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MolesActivity.class);
                intent.putExtra("BODY_PART_PATH", configuration.getPaths().getTorsoImagesPath());
                intent.putExtra("CONFIGURATION_FILE_PATH", configFile.getAbsolutePath());
                startActivity(intent);
            }
        });

        // Create new left arm images activity
        leftArmImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MolesActivity.class);
                intent.putExtra("BODY_PART_PATH", configuration.getPaths().getLeftArmImagesPath());
                intent.putExtra("CONFIGURATION_FILE_PATH", configFile.getAbsolutePath());
                startActivity(intent);
            }
        });

        // Create new right arm images activity
        rightArmImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MolesActivity.class);
                intent.putExtra("BODY_PART_PATH", configuration.getPaths().getRightArmImagesPath());
                intent.putExtra("CONFIGURATION_FILE_PATH", configFile.getAbsolutePath());
                startActivity(intent);
            }
        });

        // Create new left leg images activity
        leftLegImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MolesActivity.class);
                intent.putExtra("BODY_PART_PATH", configuration.getPaths().getLeftLegImagesPath());
                intent.putExtra("CONFIGURATION_FILE_PATH", configFile.getAbsolutePath());
                startActivity(intent);
            }
        });

        // Create new right leg images activity
        rightLegImg.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, MolesActivity.class);
                intent.putExtra("BODY_PART_PATH", configuration.getPaths().getRightLegImagesPath());
                intent.putExtra("CONFIGURATION_FILE_PATH", configFile.getAbsolutePath());
                startActivity(intent);
            }
        });
    }

}
