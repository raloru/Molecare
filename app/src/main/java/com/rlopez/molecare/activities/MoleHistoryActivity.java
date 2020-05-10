package com.rlopez.molecare.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.lists.CustomArrayAdapter;
import com.rlopez.molecare.lists.RowItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MoleHistoryActivity extends AppCompatActivity {

    // Code for the camera granted permission
    private static final int REQUEST_CAMERA = 1000;

    // Mole path and folder
    private String molePath;
    private File moleFolder;

    private ListView filesView;

    // To get current configuration
    File configFilePath;
    Configuration configuration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mole_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get configuration file and read it
        configFilePath = new File(getIntent().getStringExtra("CONFIGURATION_FILE_PATH"));
        configuration = Configuration.readConfigurationJSON(configFilePath, getApplicationContext());

        // Get elements from view
        FloatingActionButton fab = findViewById(R.id.fab);
        filesView = findViewById(R.id.photosList);
        Bundle extras = getIntent().getExtras();

        // Set parent path and current folder
        molePath = extras.getString("MOLE_PATH");
        moleFolder = new File(molePath);

        // Fill list with corresponding image files
        List<RowItem> photos = new ArrayList<>();
        File[] subFiles = moleFolder.listFiles();
        if(subFiles.length > 0) {
            for (File f : subFiles) {
                if(!f.getName().contains("json")) {
                    File imgFile = new File(f.getAbsolutePath());
                    Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    photos.add(new RowItem(f.getName(), imgBitmap));
                }
            }
        }
        CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.list_item, photos);
        filesView.setAdapter(adapter);

        // TODO Handle click on list items
        filesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
    }

    private void checkCameraPermission() {
        // Only for Marshmallow and newer versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check if camera permission is already available
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has not been granted
                updateMole();
            } else {
                // Request camera permission
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        } else {
            updateMole();
        }
    }

    // Handle request permissions result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            // Received permission result for camera
            // Check if it has been granted
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                // Camera permission was denied
                Toast.makeText(this, R.string.camera_permission_details, Toast.LENGTH_LONG).show();
            } else {
                updateMole();

            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Open a custom dialog which asks for the new mole name
    private void updateMole() {
        // Create a new camera intent
        Intent intent = new Intent(MoleHistoryActivity.this, CameraActivity.class);
        intent.putExtra("CONFIGURATION_FILE_PATH", configFilePath.getAbsolutePath());
        intent.putExtra("PATH", molePath);
        startActivity(intent);
    }

    // Update the activity when the user returns to it
    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }
}

