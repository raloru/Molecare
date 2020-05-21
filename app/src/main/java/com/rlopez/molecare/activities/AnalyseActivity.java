package com.rlopez.molecare.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import static com.rlopez.molecare.utils.FileManager.saveMatAsFile;

public class AnalyseActivity extends AppCompatActivity {

    // Mole path and folder
    private String molePath;
    private File moleFolder;
    private File processedImagesFolder;

    // To get current configuration
    File configFilePath;
    Configuration configuration;

    List<Mat> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Init OpenCV
        OpenCVLoader.initDebug();

        images = new ArrayList<>();

        // Set parent path and current folder
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        molePath = extras.getString("MOLE_PATH");
        assert molePath != null;
        moleFolder = new File(molePath);

        setTitle(getString(R.string.analysis) + ": " + moleFolder.getName());

        // Show a progress dialog
        ProgressDialog progressDialog = ProgressDialog.show(AnalyseActivity.this, "",
                getString(R.string.analysing), true);

        // Create new filename filter
        FilenameFilter fileNameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.lastIndexOf('.') > 0) {
                    // Get last index for '.' char
                    int lastIndex = name.lastIndexOf('.');
                    // Get extension
                    String str = name.substring(lastIndex);
                    // Keep jpg files
                    return str.equals(".jpg");
                }
                return false;
            }
        };

        // Create a folder for the processed images if it doesn't exist
        processedImagesFolder = new File(moleFolder, "processed");
        processedImagesFolder.mkdirs();

        // Get the original images in a mat list
        File[] originalImages = moleFolder.listFiles(fileNameFilter);
        assert originalImages != null;
        if (originalImages.length > 0) {
            for (File f : originalImages) {
                Mat mat;
                mat = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
                saveMatAsFile(mat, moleFolder.getAbsolutePath() + File.separator + "processed", f.getName(), getApplicationContext());
            }
        }

        progressDialog.dismiss();
    }

}
