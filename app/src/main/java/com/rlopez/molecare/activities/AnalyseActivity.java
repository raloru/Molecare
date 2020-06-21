/*
* @author   Raúl López
* @version  1.0
* @year     2020
*/

package com.rlopez.molecare.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
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

    // To get current configuration
    File configFilePath;
    Configuration configuration;

    List<Mat> images;
    List<MoleModel> moles;

    GraphView diameterGraph;
    GraphView hueGraph;
    GraphView shapeGraph;
    Button diameterButton;
    Button hueButton;
    Button shapeButton;
    ImageButton infoButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyse);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show a progress dialog
        ProgressDialog progressDialog = ProgressDialog.show(AnalyseActivity.this, "",
                getString(R.string.analysing), true);

        // Get elements from view
        diameterGraph = findViewById(R.id.diameter_graph);
        hueGraph = findViewById(R.id.hue_graph);
        shapeGraph = findViewById(R.id.shape_graph);
        diameterButton = findViewById(R.id.btn_diameter);
        hueButton = findViewById(R.id.btn_hue);
        shapeButton = findViewById(R.id.btn_shape);
        infoButton = findViewById(R.id.btn_info);

        // Init OpenCV
        OpenCVLoader.initDebug();

        // Set buttons listener
        diameterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diameterButton.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                hueButton.setBackgroundColor(getResources().getColor(R.color.gray));
                shapeButton.setBackgroundColor(getResources().getColor(R.color.gray));
                diameterGraph.setVisibility(View.VISIBLE);
                hueGraph.setVisibility(View.INVISIBLE);
                shapeGraph.setVisibility(View.INVISIBLE);
            }
        });
        hueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diameterButton.setBackgroundColor(getResources().getColor(R.color.gray));
                hueButton.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                shapeButton.setBackgroundColor(getResources().getColor(R.color.gray));
                diameterGraph.setVisibility(View.INVISIBLE);
                hueGraph.setVisibility(View.VISIBLE);
                shapeGraph.setVisibility(View.INVISIBLE);
            }
        });
        shapeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diameterButton.setBackgroundColor(getResources().getColor(R.color.gray));
                hueButton.setBackgroundColor(getResources().getColor(R.color.gray));
                shapeButton.setBackgroundColor(getResources().getColor(R.color.colorSecondary));
                diameterGraph.setVisibility(View.INVISIBLE);
                hueGraph.setVisibility(View.INVISIBLE);
                shapeGraph.setVisibility(View.VISIBLE);
            }
        });
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title;
                String message;
                if (diameterGraph.getVisibility() == View.VISIBLE) {
                    title = getString(R.string.diameter_lbl);
                    message = getString(R.string.diameter_message_first) + "\n\n" + getString(R.string.diameter_message_second);
                } else if (hueGraph.getVisibility() == View.VISIBLE) {
                    title = getString(R.string.hue_lbl);
                    message = getString(R.string.hue_message_first) + "\n\n" + getString(R.string.hue_message_second);
                } else {
                    title = getString(R.string.shape_lbl);
                    message = getString(R.string.shape_message_first) + "\n\n" + getString(R.string.shape_message_second);
                }
                new AlertDialog.Builder(AnalyseActivity.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });

        // Get configuration file and read it
        configFilePath = new File(Objects.requireNonNull(getIntent().getStringExtra("CONFIGURATION_FILE_PATH")));
        configuration = Configuration.readConfigurationJSON(configFilePath, getApplicationContext());

        // Initialize lists
        images = new ArrayList<>();
        moles = new ArrayList<>();

        // Set parent path and current folder
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        // Mole path and folder
        String molePath = extras.getString("MOLE_PATH");
        assert molePath != null;
        File moleFolder = new File(molePath);

        // Read images information
        // To get images information
        ImagesInformation imagesInformation = ImagesInformation.readImagesInformationJSON(new File(moleFolder, "ImagesInformation.json"), getApplicationContext());
        List<ImageModel> imageModels = imagesInformation.getImageModels();

        setTitle(getString(R.string.analysis) + ": " + moleFolder.getName());

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
            for (ImageModel imageModel : imageModels) {
                for (File f : originalImages) {
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

            // Create (date, diameter), (date, hue), (date, shape correlation) data points
            DataPoint[] diameterDataPoints = new DataPoint[moles.size()];
            DataPoint[] hueDataPoints = new DataPoint[moles.size()];
            DataPoint[] shapeCorrelationPoints = new DataPoint[moles.size()];
            double maxDiameter = 0.0;
            double minHue = Double.MAX_VALUE;
            double maxHue = 0.0;
            double maxShapeCorrelation = 0.0;
            for (int i = 0; i < moles.size(); i++) {
                diameterDataPoints[i] = new DataPoint(moles.get(i).getDate().getTime(), moles.get(i).getDiameter());
                hueDataPoints[i] = new DataPoint(moles.get(i).getDate().getTime(), moles.get(i).getHue());
                shapeCorrelationPoints[i] = new DataPoint(moles.get(i).getDate().getTime(), moles.get(i).getShapeCorrelation());
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
            }

            // Fill diameters and hues graph
            fillGraph(diameterDataPoints, diameterGraph, 0.0, maxDiameter, 0);
            fillGraph(hueDataPoints, hueGraph, minHue, maxHue, 1);
            fillGraph(shapeCorrelationPoints, shapeGraph, 0.0, maxShapeCorrelation, 2);

        }

        progressDialog.dismiss();
    }

    // Fill and configure graphs
    private void fillGraph(DataPoint[] data, GraphView graph, double minValue, double maxValue, int type) {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        LineGraphSeries<DataPoint> maxLimitsSeries;
        LineGraphSeries<DataPoint> minLimitsSeries;
        series.setDrawDataPoints(true);
        series.setThickness(8);
        series.setDataPointsRadius(14);
        series.setColor(getResources().getColor(R.color.colorSecondary));
        double minY;
        double maxY;
        DataPoint[] maxValues = new DataPoint[data.length];
        DataPoint[] minValues = new DataPoint[data.length];

        graph.addSeries(series);

        // Create limit lines. Set max and min axis values
        switch (type) {
            case 0:
                minY = data[0].getY() - 1;
                maxY = data[0].getY() + 1;
                for (int i = 0; i < data.length; i++) {
                    minValues[i] = new DataPoint(data[i].getX(), minY);
                    maxValues[i] = new DataPoint(data[i].getX(), maxY);
                }
                minLimitsSeries = new LineGraphSeries<>(minValues);
                maxLimitsSeries = new LineGraphSeries<>(maxValues);
                graph.addSeries(minLimitsSeries);
                graph.addSeries(maxLimitsSeries);
                maxLimitsSeries.setDrawDataPoints(false);
                maxLimitsSeries.setThickness(3);
                maxLimitsSeries.setColor(getResources().getColor(R.color.colorPrimary));
                minLimitsSeries.setDrawDataPoints(false);
                minLimitsSeries.setThickness(3);
                minLimitsSeries.setColor(getResources().getColor(R.color.colorPrimary));
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(Math.round(maxValue) + 5);
                break;
            case 1:
                minY = data[0].getY() - 8;
                maxY = data[0].getY() + 8;
                for (int i = 0; i < data.length; i++) {
                    minValues[i] = new DataPoint(data[i].getX(), minY);
                    maxValues[i] = new DataPoint(data[i].getX(), maxY);
                }
                minLimitsSeries = new LineGraphSeries<>(minValues);
                maxLimitsSeries = new LineGraphSeries<>(maxValues);
                graph.addSeries(minLimitsSeries);
                graph.addSeries(maxLimitsSeries);
                maxLimitsSeries.setDrawDataPoints(false);
                maxLimitsSeries.setThickness(3);
                maxLimitsSeries.setColor(getResources().getColor(R.color.colorPrimary));
                minLimitsSeries.setDrawDataPoints(false);
                minLimitsSeries.setThickness(3);
                minLimitsSeries.setColor(getResources().getColor(R.color.colorPrimary));
                graph.getViewport().setMinY(Math.round(minValue) - 30);
                graph.getViewport().setMaxY(Math.round(maxValue) + 30);
                break;
            case 2:
                maxY = 4;
                maxValues = new DataPoint[data.length];
                for (int i = 0; i < data.length; i++) {
                    maxValues[i] = new DataPoint(data[i].getX(), maxY);
                }
                maxLimitsSeries = new LineGraphSeries<>(maxValues);
                graph.addSeries(maxLimitsSeries);
                maxLimitsSeries.setDrawDataPoints(false);
                maxLimitsSeries.setThickness(3);
                maxLimitsSeries.setColor(getResources().getColor(R.color.colorPrimary));
                graph.getViewport().setMinY(0);
                graph.getViewport().setMaxY(Math.max(10, maxValue));
                break;
        }

        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
        graph.getGridLabelRenderer().setNumHorizontalLabels(5);

        graph.getViewport().setMinX(moles.get(0).getDate().getTime());
        graph.getViewport().setMaxX(moles.get(moles.size() - 1).getDate().getTime());

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);

        graph.getGridLabelRenderer().setHumanRounding(false, true);

    }

}
