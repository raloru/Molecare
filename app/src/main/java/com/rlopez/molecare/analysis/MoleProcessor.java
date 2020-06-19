package com.rlopez.molecare.analysis;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class MoleProcessor {

    // Segment image to get only the mole zone, obtaining a binary mat
    public static void segmentMole(MoleModel mole) {
        Mat srcMat = mole.getOriginalImage();
        Mat auxMat = new Mat();
        Mat binarySegmentedMat = new Mat();
        Mat colouredSegmentedMat = new Mat();
        // Convert image to HSV to practically remove airs
        Imgproc.cvtColor(srcMat, auxMat, Imgproc.COLOR_RGB2HSV);
        // Convert to GRAY scale to facilitate segmentation
        Imgproc.cvtColor(auxMat, auxMat, Imgproc.COLOR_RGB2GRAY);
        // Apply a gaussian blur to eliminate the remaining hairs
        Imgproc.GaussianBlur(auxMat, auxMat, new Size(19, 19), 0);
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
        Mat referenceBinaryMat = moles.get(0).getBinarySegmentedImage();
        for (MoleModel currentMole : moles) {
            calculateMoleDiameter(currentMole);
            calculateMoleHue(currentMole);
            calculateShapeCorrelation(referenceBinaryMat, currentMole);
        }
    }

    // Calculate the mole diameter
    private static void calculateMoleDiameter(MoleModel mole) {
        double realDiameterInMm;
        double imageDiameterInPixels;
        double distance;
        // Corrects the excess pixels generated in the segmentation
        double correctionFactor = 0.6;
        // Focus distance is given in diopters. 1 diopter = 1/1m
        distance = (1 / mole.getFocusDistance()) * 1000;
        // Area = PI * D^2 / 4 and a correction factor of 0.7
        imageDiameterInPixels = Math.sqrt(Core.countNonZero(mole.getBinarySegmentedImage()) * correctionFactor * 4 / Math.PI) ;
        // Real height (px) = distance (mm) * object height (px) * sensor height (mm) / (focal length (mm) * original image height (px))
        realDiameterInMm = (distance * imageDiameterInPixels * mole.getSensorHeight()) / (mole.getFocalLength() * mole.getOriginalImageHeight());
        // Pixels to mm
        // Set diameter in mm
        mole.setDiameter(realDiameterInMm);
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

    private static void calculateShapeCorrelation(Mat referenceMat, MoleModel mole) {
        mole.setShapeCorrelation(Imgproc.matchShapes(referenceMat, mole.getBinarySegmentedImage(),
                Imgproc.CV_CONTOURS_MATCH_I1, 0) * 10000);
    }

}
