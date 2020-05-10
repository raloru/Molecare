package com.rlopez.molecare.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.lists.CustomArrayAdapter;
import com.rlopez.molecare.dialogs.NewMoleDialog;
import com.rlopez.molecare.lists.RowItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MolesActivity extends AppCompatActivity implements NewMoleDialog.NewMoleDialogListener {

    // To get current configuration
    File configFilePath;
    Configuration configuration;

    // Code for the camera granted permission
    public final int REQUEST_CAMERA = 1000;
    // Current folder
    private File currentFolder;
    // Current path
    private String bodyPartPath;
    // Folders name and image
    private List<RowItem> folders = new ArrayList<>();

    ListView foldersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moles);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get configuration file and read it
        configFilePath = new File(getIntent().getStringExtra("CONFIGURATION_FILE_PATH"));
        configuration = Configuration.readConfigurationJSON(configFilePath, getApplicationContext());

        // Get elements from view and selected body part
        foldersView = findViewById(R.id.foldersList);
        FloatingActionButton fab = findViewById(R.id.fab);

        // Get current path and folder
        Bundle extras = getIntent().getExtras();
        bodyPartPath = extras.getString("BODY_PART_PATH");
        currentFolder = new File(bodyPartPath);

        // Set activity title
        setTitle(currentFolder.getName());

        // Fill list with corresponding folders
        File[] subFiles = currentFolder.listFiles();
        if(subFiles.length > 0) {
            for (File f : subFiles) {
                File imgFile = new File(f.listFiles()[0].getAbsolutePath());
                Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                folders.add(new RowItem(f.getName(), imgBitmap));
            }
        }

        CustomArrayAdapter adapter = new CustomArrayAdapter(this, R.layout.list_item, folders);
        foldersView.setAdapter(adapter);

        // Handle click on list items
        foldersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                // Get selected folder path
                RowItem dataModel = (RowItem) adapter.getItemAtPosition(position);
                String molePath = bodyPartPath + File.separator + dataModel.getName();
                // Create mole history activity
                Intent intent = new Intent(MolesActivity.this, MoleHistoryActivity.class);
                intent.putExtra("CONFIGURATION_FILE_PATH", configFilePath.getAbsolutePath());
                intent.putExtra("MOLE_PATH", molePath);
                startActivity(intent);
            }
        });

        foldersView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapter, View v, int position, long id) {
                RowItem dataModel = (RowItem) adapter.getItemAtPosition(position);
                String molePath = bodyPartPath + File.separator + dataModel.getName();
                return true;
            }
        });

        // Handle click on floating action button
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
                nameNewMole();
            } else {
                // Request camera permission
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        } else {
            nameNewMole();
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
                nameNewMole();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Open a custom dialog which asks for the new mole name
    private void nameNewMole() {
        // Create and show a dialog asking for the new mole name
        NewMoleDialog newMoleDialog = new NewMoleDialog();
        newMoleDialog.show(getSupportFragmentManager(), getString(R.string.title_new_mole));
    }

    // Implement dialog listener. Called when user press any dialog button
    @Override
    public void getMoleName(String moleName) {
        if (moleName.equals("")) {
            // Empty name. Notify user
            Toast.makeText(MolesActivity.this, R.string.error_empty_name, Toast.LENGTH_SHORT).show();
        } else {
            // Not empty name
            File newMoleFile = new File(bodyPartPath, moleName);
            if (newMoleFile.exists()) {
                // Mole name already in use. Notify user
                Toast.makeText(MolesActivity.this, R.string.error_mole_name_exists, Toast.LENGTH_SHORT).show();
            } else {
                // Valid name, create camera intent
                Intent intent = new Intent(MolesActivity.this, CameraActivity.class);
                intent.putExtra("CONFIGURATION_FILE_PATH", configFilePath.getAbsolutePath());
                intent.putExtra("PATH", bodyPartPath);
                intent.putExtra("MOLE_NAME", moleName);
                startActivity(intent);
            }
        }
    }

    // Update the activity when the user returns to it
    @Override
    public void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }


}

