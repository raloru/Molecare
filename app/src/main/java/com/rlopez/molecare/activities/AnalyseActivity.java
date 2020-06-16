package com.rlopez.molecare.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.rlopez.molecare.R;
import com.rlopez.molecare.configuration.Configuration;
import com.rlopez.molecare.images.ImagesInformation;
import com.rlopez.molecare.analysis.MoleProcessor;
import com.rlopez.molecare.images.ImageModel;
import com.rlopez.molecare.analysis.MoleModel;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
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
        GraphView diametersGraph = findViewById(R.id.diameters_graph);
        GraphView huesGraph = findViewById(R.id.hues_graph);
        GraphView shapesGraph = findViewById(R.id.shapes_graph);

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

        // Get the saved mole images and process them
        File[] originalImages = moleFolder.listFiles(fileNameFilter);
        assert originalImages != null;
        if (originalImages.length > 0) {
            for (File f : originalImages) {
                for (ImageModel imageModel : imageModels) {
                    if (imageModel.getName().equals(f.getName())) {
                        // New mole model
                        MoleModel moleModel = new MoleModel(imageModel.getName(), imageModel.getFocusDistance(), imageModel.getFocalLength(),
                                imageModel.getSensorHeight(), imageModel.getOriginalImageHeight(), 96.0,
                                Imgcodecs.imread(f.getAbsolutePath(), Imgcodecs.IMREAD_COLOR), f.getAbsolutePath());
                        // Get the binary and coloured segmented images and save them
                        MoleProcessor.segmentMole(moleModel);
                        // Add the new mole to the moles list
                        moles.add(moleModel);
                    }
                }
            }

            // Get moles characteristics
            MoleProcessor.getMolesCharacteristics(moles);

            // Create (date, diameter) data points and fill the corresponding graph
            DataPoint[] diameterDataPoints = new DataPoint[moles.size()];
            DataPoint[] hueDataPoints = new DataPoint[moles.size()];
            DataPoint[] shapeCorrelationPoints = new DataPoint[moles.size()];
            double maxDiameter = 0.0;
            double minHue = Double.MAX_VALUE;
            double maxHue = 0.0;
            double maxShapeCorrelation = 0.0;
            for (int i = 0; i < moles.size(); i++) {
                diameterDataPoints[i] = new DataPoint(i, moles.get(i).getDiameter());
                hueDataPoints[i] = new DataPoint(i, moles.get(i).getHue());
                shapeCorrelationPoints[i] = new DataPoint(i, moles.get(i).getShapeCorrelation());
                if (moles.get(i).getDiameter() > maxDiameter) {
                    maxDiameter = moles.get(i).getDiameter();
                }
                if (moles.get(i).getHue() > maxHue) {
                    maxHue = moles.get(i).getHue();
                }
                if (moles.get(i).getHue() < minHue) {
                    minHue = moles.get(i).getHue();
                }
                if (moles.get(i).getShapeCorrelation() > maxShapeCorrelation) {
                    maxShapeCorrelation = moles.get(i).getShapeCorrelation();
                }
                Log.d("Debug", String.valueOf(moles.get(i).getShapeCorrelation()));
            }

            // Fill diameters and hues graph
            fillGraph(diameterDataPoints, diametersGraph, 0.0, maxDiameter, 0);
            fillGraph(hueDataPoints, huesGraph, minHue, maxHue, 1);
            fillGraph(shapeCorrelationPoints, shapesGraph, 0.0, maxShapeCorrelation, 2);

        }

        progressDialog.dismiss();
    }

    private void fillGraph(DataPoint[] data, GraphView graph, double minValue, double maxValue, int type) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        series.setDrawDataPoints(true);
        series.setThickness(6);

        graph.addSeries(series);
        // Set max and min axis values
        switch (type) {
            case 0:
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(Math.round(maxValue) + 5);
                break;
            case 1:
                graph.getViewport().setMinY(Math.round(minValue) - 30);
                graph.getViewport().setMaxY(Math.round(maxValue) + 30);
                break;
            case 2:
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(Math.max(1, maxValue));
                break;
        }
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(moles.size());
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);

    }

}
