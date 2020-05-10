package com.rlopez.molecare.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.lists.CustomArrayAdapter;
import com.rlopez.molecare.lists.RowItem;
import com.rlopez.molecare.utils.ImagesInformation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MoleHistoryActivity extends AppCompatActivity {

    // Code for the camera granted permission
    private static final int REQUEST_CAMERA = 1000;

    // Mole path and folder
    private String molePath;
    private File moleFolder;

    private ListView filesView;
    List<RowItem> photos;
    CustomArrayAdapter customAdapter;
            
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
        photos = new ArrayList<>();
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
        customAdapter = new CustomArrayAdapter(this, R.layout.list_item, photos);
        filesView.setAdapter(customAdapter);

        filesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View v, final int position, long id) {
                final RowItem dataModel = (RowItem) adapter.getItemAtPosition(position);
                final String imagePath = molePath + File.separator + dataModel.getName();

                // Ask if the user wants to delete the mole
                AlertDialog alertDialog = new AlertDialog.Builder(MoleHistoryActivity.this).create();
                alertDialog.setTitle(R.string.delete);
                alertDialog.setMessage(getString(R.string.delete_first_part) + " " + dataModel.getName() + "?");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File photoFileDelete = new File(imagePath);
                                deletePhoto(photoFileDelete);
                                photos.remove(position);
                                customAdapter.notifyDataSetChanged();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

                return true;
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
    }

    private void deletePhoto(File photoFileDelete) {
        File parent = photoFileDelete.getParentFile();
        String[] children = parent.list();
        if(children.length == 2) {
            for (int i = 0; i < children.length; i++)
            {
                new File(parent, children[i]).delete();
            }
            parent.delete();
            Toast.makeText(getApplicationContext(), parent.getName() + " " + getString(R.string.deleted), Toast.LENGTH_SHORT).show();
            MoleHistoryActivity.this.finish();
        } else {
            File imagesInformationFile = new File(moleFolder.getAbsolutePath(), "ImagesInformation.json");
            ImagesInformation imagesInformation = ImagesInformation.readImagesInformationJSON(imagesInformationFile, getApplicationContext());
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
            photoFileDelete.delete();
            Toast.makeText(getApplicationContext(), photoFileDelete.getName() + " " + getString(R.string.deleted), Toast.LENGTH_SHORT).show();
        }

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

