/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

package com.rlopez.molecare.configuration;

import java.util.ArrayList;
import java.util.List;

// Contains save paths
public class Paths {

    private String imagesPath;
    private String headImagesPath;
    private String torsoImagesPath;
    private String leftArmImagesPath;
    private String rightArmImagesPath;
    private String leftLegImagesPath;
    private String rightLegImagesPath;

    public String getImagesPath() {
        return imagesPath;
    }

    public void setImagesPath(String imagesPath) {
        this.imagesPath = imagesPath;
    }

    public String getHeadImagesPath() {
        return headImagesPath;
    }

    public void setHeadImagesPath(String headImagesPath) {
        this.headImagesPath = headImagesPath;
    }

    public String getTorsoImagesPath() {
        return torsoImagesPath;
    }

    public void setTorsoImagesPath(String torsoImagesPath) {
        this.torsoImagesPath = torsoImagesPath;
    }

    public String getLeftArmImagesPath() {
        return leftArmImagesPath;
    }

    public void setLeftArmImagesPath(String leftArmImagesPath) {
        this.leftArmImagesPath = leftArmImagesPath;
    }

    public String getRightArmImagesPath() {
        return rightArmImagesPath;
    }

    public void setRightArmImagesPath(String rightArmImagesPath) {
        this.rightArmImagesPath = rightArmImagesPath;
    }

    public String getLeftLegImagesPath() {
        return leftLegImagesPath;
    }

    public void setLeftLegImagesPath(String leftLegImagesPath) {
        this.leftLegImagesPath = leftLegImagesPath;
    }

    public String getRightLegImagesPath() {
        return rightLegImagesPath;
    }

    public void setRightLegImagesPath(String rightLegImagesPath) {
        this.rightLegImagesPath = rightLegImagesPath;
    }

    // Return all body parts paths as a list
    public List<String> getBodyPartsList() {
        List<String> bodyPartsList = new ArrayList<>();
        bodyPartsList.add(getHeadImagesPath());
        bodyPartsList.add(getTorsoImagesPath());
        bodyPartsList.add(getLeftArmImagesPath());
        bodyPartsList.add(getRightArmImagesPath());
        bodyPartsList.add(getLeftLegImagesPath());
        bodyPartsList.add(getRightLegImagesPath());
        return bodyPartsList;
    }

}
