/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.views.CustomArrayAdapter;
import com.rlopez.molecare.views.NewMoleDialog;
import com.rlopez.molecare.views.RowItem;
import com.rlopez.molecare.utils.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    CustomArrayAdapter customAdapter;

    ListView foldersView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moles);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get configuration file and read it
        configFilePath = new File(Objects.requireNonNull(getIntent().getStringExtra("CONFIGURATION_FILE_PATH")));
        configuration = Configuration.readConfigurationJSON(configFilePath, getApplicationContext());

        // Get elements from view and selected body part
        foldersView = findViewById(R.id.foldersList);
        FloatingActionButton fab = findViewById(R.id.fab);

        // Get current path and folder
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        bodyPartPath = extras.getString("BODY_PART_PATH");
        assert bodyPartPath != null;
        currentFolder = new File(bodyPartPath);

        // Set activity title
        setTitle(getCustomTitle(currentFolder.getName()));

        // Fill list with corresponding folders
        getMoles();

        customAdapter = new CustomArrayAdapter(this, R.layout.list_item, folders);
        foldersView.setAdapter(customAdapter);
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
            public boolean onItemLongClick(AdapterView<?> adapter, View v, final int position, long id) {
                final RowItem dataModel = (RowItem) adapter.getItemAtPosition(position);
                final String molePath = bodyPartPath + File.separator + dataModel.getName();

                // Ask if the user wants to delete the mole
                AlertDialog alertDialog = new AlertDialog.Builder(MolesActivity.this).create();
                alertDialog.setTitle(R.string.delete);
                alertDialog.setMessage(getString(R.string.delete_first_part) + " " + dataModel.getName() + " " + getString(R.string.delete_mole_second_part));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                File moleFileDelete = new File(molePath);
                                FileManager.deleteFolderAndChildren(moleFileDelete);
                                folders.remove(position);
                                customAdapter.notifyDataSetChanged();
                                Toast.makeText(getApplicationContext(), dataModel.getName() + " " + getString(R.string.deleted), Toast.LENGTH_SHORT).show();
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

        // Handle click on floating action button
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkCameraPermission();
            }
        });
    }

    // Get the title translated if necessary
    private String getCustomTitle(String name) {
        String res = "";
        switch (name) {
            case "Head moles":
                res = getString(R.string.head_moles);
                break;
            case "Torso moles":
                res = getString(R.string.torso_moles);
                break;
            case "Left arm moles":
                res = getString(R.string.left_arm_moles);
                break;
            case "Right arm moles":
                res = getString(R.string.right_arm_moles);
                break;
            case "Left leg moles":
                res = getString(R.string.left_leg_moles);
                break;
            case "Right leg moles":
                res = getString(R.string.right_leg_moles);
                break;
        }
        return res;
    }

    private void getMoles() {
        // Fill list with corresponding folders
        folders.removeAll(folders);
        File[] subFiles = currentFolder.listFiles();
        assert subFiles != null;
        if (subFiles.length > 0) {
            for (File f : subFiles) {
                File imgFile = new File(Objects.requireNonNull(f.listFiles())[0].getAbsolutePath());
                Bitmap imgBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                folders.add(new RowItem(f.getName(), imgBitmap));
            }
        }
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
        getMoles();
        customAdapter.notifyDataSetChanged();
    }


}

