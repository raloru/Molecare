package com.rlopez.molecare.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.images.MoleProcessor;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class AnalyseActivity extends AppCompatActivity {

    // Mole path and folder
    private String molePath;
    private File moleFolder;
    private String binaryPath;
    private String segmentedPath;

    // To get current configuration
    File configFilePath;
    Configuration configuration;

    List<Mat> images;
    List<Mat> binarySegmentedMoles;
    List<Mat> colouredSegmentedMoles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Init OpenCV
        OpenCVLoader.initDebug();

        images = new ArrayList<>();
        binarySegmentedMoles = new ArrayList<>();
        colouredSegmentedMoles = new ArrayList<>();

        // Set parent path and current folder
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        molePath = extras.getString("MOLE_PATH");
        assert molePath != null;
        moleFolder = new File(molePath);
        binaryPath = moleFolder.getAbsolutePath() + File.separator + "binary";
        segmentedPath = moleFolder.getAbsolutePath() + File.separator + "coloured";

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

        // Get the original images in a mat list
        File[] originalImages = moleFolder.listFiles(fileNameFilter);
        assert originalImages != null;
        if (originalImages.length > 0) {
            for (File f : originalImages) {
                Mat originalMat;
                // Read the original image
                originalMat = Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
                // Get the binary and coloured segmented images and save them
                MoleProcessor.segmentMole(originalMat, binaryPath, segmentedPath, f.getName());
            }
        }

        progressDialog.dismiss();
    }

}
