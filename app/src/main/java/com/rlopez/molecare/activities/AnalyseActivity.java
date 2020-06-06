package com.rlopez.molecare.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.images.ImagesInformation;
import com.rlopez.molecare.images.MoleProcessor;
import com.rlopez.molecare.models.ImageModel;
import com.rlopez.molecare.models.MoleModel;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class AnalyseActivity extends AppCompatActivity {

    // Mole path and folder
    private String molePath;
    private File moleFolder;

    // To get current configuration
    File configFilePath;
    Configuration configuration;

    // To get images information
    private ImagesInformation imagesInformation;
    private List<ImageModel> imageModels;

    List<Mat> images;
    List<MoleModel> moles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get elements from view
        GraphView graph = findViewById(R.id.size_graph);

        // Init OpenCV
        OpenCVLoader.initDebug();

        // Get configuration file and read it
        configFilePath = new File(Objects.requireNonNull(getIntent().getStringExtra("CONFIGURATION_FILE_PATH")));
        configuration = Configuration.readConfigurationJSON(configFilePath, getApplicationContext());

        // Initialize lists
        images = new ArrayList<>();
        moles = new ArrayList<>();
        imageModels = new ArrayList<>();

        // Set parent path and current folder
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        molePath = extras.getString("MOLE_PATH");
        assert molePath != null;
        moleFolder = new File(molePath);

        // Read images information
        imagesInformation = ImagesInformation.readImagesInformationJSON(new File(moleFolder, "ImagesInformation.json"), getApplicationContext());
        imageModels = imagesInformation.getImageModels();

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
                ImageModel imageModel = null;
                while (imageModels.iterator().hasNext()) {
                    imageModel = imageModels.iterator().next();
                    if (imageModel.getName().equals(f.getName())) {
                        break;
                    }
                }
                MoleModel moleModel = new MoleModel(imageModel.getName(), imageModel.getFocusDistance(), imageModel.getFocalLength(), 96.0,
                        Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.IMREAD_COLOR), f.getAbsolutePath());
                // Get the binary and coloured segmented images and save them
                MoleProcessor.segmentMole(moleModel);
                // Add the new mole to the moles list
                moles.add(moleModel);
            }
        }

        // Get moles characteristics
        MoleProcessor.getMolesCharacteristics(moles);

        ListIterator<MoleModel> iterator = moles.listIterator();

        // Fill the size graph
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(1, 0),
                new DataPoint(2, iterator.next().getDiameter()),
                new DataPoint(3, iterator.next().getDiameter())
        });
        graph.addSeries(series);

        progressDialog.dismiss();
    }

}
