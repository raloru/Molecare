package com.rlopez.molecare.images;

import com.rlopez.molecare.models.MoleModel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.ListIterator;

public class MoleProcessor {

    private static final double INCH_TO_CM = 2.54;

    // Segment image to get only the mole zone, obtaining a binary mat
    public static void segmentMole(MoleModel mole) {
        Mat srcMat = mole.getOriginalImage();
        Mat auxMat = new Mat();
        Mat binarySegmentedMat = new Mat();
        Mat colouredSegmentedMat = new Mat();
        // Convert image to HSV to practically remove airs
        Imgproc.cvtColor(srcMat, auxMat, Imgproc.COLOR_RGB2HSV);
        //Imgcodecs.imwrite(savingPath + File.separator + "hsv.jpg", auxMat);
        // Raise the contrast to highlight the mole
        auxMat.convertTo(auxMat, -1, 2, 0);
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
        }
    }

    // Calculate the mole diameter
    private static void calculateMoleDiameter(MoleModel mole) {
        double realDiameter;
        double dpi = mole.getDpi();
        double distance = 1 / mole.getFocusDistance();
        // Diameter = Perimeter / PI
        double imageDiameterInPixels = Core.countNonZero(mole.getBinarySegmentedImage()) / Math.PI;
        // dpi = dots per inch, 1 inch = 2.54cm
        double imageDiameter = imageDiameterInPixels * INCH_TO_CM / dpi;
        // object size in image = focal length * object size / object distance
        // object size = object size in image * object distance / focal length
        realDiameter = imageDiameter * distance / mole.getFocalLength();
        mole.setDiameter(realDiameter);
    }

    // Calculate the mole hue
    private static double getMoleHue(Mat srcMat) {
        // TODO
        return 0.0;
    }

    // Calculate the mole shape
    private static double getMoleShape(Mat srcMat) {
        // TODO
        return 0.0;
    }

}
