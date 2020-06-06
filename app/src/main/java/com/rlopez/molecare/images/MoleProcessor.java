package com.rlopez.molecare.images;

import com.rlopez.molecare.models.ImageModel;
import com.rlopez.molecare.models.MoleModel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
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
        //Imgcodecs.imwrite(savingPath + File.separator + "hsv.jpg", auxMat);
        // Raise the contrast to highlight the mole
        auxMat.convertTo(auxMat , -1, 2, 0);
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

    // Calculate average % of size changes
    public static double compareMolesSize(List<Mat> srcMats) {
        // TODO
        int molePixels = Core.countNonZero(srcMats.get(0));
        return 0.0;
    }

    // Calculate average % of color changes
    public static double compareMolesColor(List<Mat> srcMats) {
        // TODO
        return 0.0;
    }

    // Calculate an average % of shape changes
    public static double compareMolesShape(List<Mat> srcMats) {
        // TODO
        return 0.0;
    }

}
