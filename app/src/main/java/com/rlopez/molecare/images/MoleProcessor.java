package com.rlopez.molecare.images;

import com.rlopez.molecare.models.MoleModel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class MoleProcessor {

    private static final double INCH_TO_MM = 25.4;

    // Segment image to get only the mole zone, obtaining a binary mat
    public static void segmentMole(MoleModel mole) {
        Mat srcMat = mole.getOriginalImage();
        Mat auxMat = new Mat();
        Mat binarySegmentedMat = new Mat();
        Mat colouredSegmentedMat = new Mat();
        // Convert image to HSV to practically remove airs
        //srcMat.convertTo(auxMat, -1, 3.5, 0);
        Imgproc.cvtColor(srcMat, auxMat, Imgproc.COLOR_RGB2HSV);
        //Imgcodecs.imwrite(savingPath + File.separator + "hsv.jpg", auxMat);
        // Raise the contrast to highlight the mole

        //Imgcodecs.imwrite(savingPath + File.separator + "contrast.jpg", auxMat);
        // Convert to GRAY scale to facilitate segmentation
        Imgproc.cvtColor(auxMat, auxMat, Imgproc.COLOR_RGB2GRAY);
        //Imgcodecs.imwrite(savingPath + File.separator + "gray.jpg", auxMat);
        // Apply a gaussian blur to eliminate the remaining hairs
        Imgproc.GaussianBlur(auxMat, auxMat, new Size(19, 19), 0);
        //Imgcodecs.imwrite(savingPath + File.separator + "blur.jpg", auxMat);
        // Apply threshold algorithm to segment, obtaining a binary image
        Imgproc.threshold(auxMat, binarySegmentedMat, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        // Get the coloured segmented image applying binary image as a mask to the original image
        srcMat.copyTo(colouredSegmentedMat, binarySegmentedMat);
        // Save both binary and coloured segmented images
        mole.setBinarySegmentedImage(binarySegmentedMat);
        mole.setColouredSegmentedImage(colouredSegmentedMat);
        // Save segmented mats
        mole.saveSegmentedImages();
    }

    // Get the moles characteristics (diameter, hue and shape)
    public static void getMolesCharacteristics(List<MoleModel> moles) {
        for (MoleModel currentMole : moles) {
            calculateMoleDiameter(currentMole);
            calculateMoleHue(currentMole);
        }
    }

    // Calculate the mole diameter
    private static void calculateMoleDiameter(MoleModel mole) {
        double realDiameterInPixels;
        // Focus distance is given in diopters. 1 diopter = 1/1m
        double distance = (1 / mole.getFocusDistance()) * 1000;
        // Area = PI * D^2 / 4
        double imageDiameterInPixels = Math.sqrt(Core.countNonZero(mole.getBinarySegmentedImage()) * 4 / Math.PI) ;
        // Real height (px) = distance (mm) * object height (px) * sensor height (mm) / (focal length (mm) * original image height (px))
        realDiameterInPixels = (distance * imageDiameterInPixels * mole.getSensorHeight()) / (mole.getFocalLength() * mole.getOriginalImageHeight());
        // Pixels to mm
        double realDiameterInMm = (realDiameterInPixels * INCH_TO_MM / mole.getDpi());
        // Set diameter in mm
        mole.setDiameter(realDiameterInPixels);
    }

    // Calculate the mole hue
    private static void calculateMoleHue(MoleModel mole) {
        // Convert RGB to HSV
        Mat hsvMat = new Mat();
        Imgproc.cvtColor(mole.getColouredSegmentedImage(), hsvMat, Imgproc.COLOR_RGB2HSV);
        // Get the mean of H channel (Hue) and set average mole hue
        double mean = Core.mean(hsvMat, mole.getBinarySegmentedImage()).val[0];
        mole.setHue(mean);
    }

    // Calculate the mole shape
    private static double getMoleShape(Mat srcMat) {
        // TODO
        return 0.0;
    }

}
