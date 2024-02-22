package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.blackout.FileTransferException.VirtualFileNoStorageSpaceException;
import unsw.utils.Angle;
import unsw.utils.MathsHelper;

public abstract class Satellite extends Entity {
    private double height;
    private double linearVelocity;
    private int sendBandwidth;
    private int receiveBandwidth;
    private int maxStorage;
    private int filesUploading;
    private int filesDownloading;
    private int currStorageUnused;

    public Satellite(String satelliteId, String type, double height, Angle position, double linearVelocity,
            double range, int sendBandwidth, int receiveBandwidth, int maxStorage) {
        super(satelliteId, position, type, range);
        this.height = height;
        this.linearVelocity = linearVelocity;
        this.sendBandwidth = sendBandwidth;
        this.receiveBandwidth = receiveBandwidth;
        this.maxStorage = maxStorage;
        this.filesDownloading = 0;
        this.filesUploading = 0;
        this.currStorageUnused = maxStorage;
    }

    public double getHeight() {
        return height;
    }

    public void setLinearVelocity(double linearVelocity) {
        this.linearVelocity = linearVelocity;
    }

    public double getLinearVelocity() {
        return linearVelocity;
    }

    public int getSendBandwidth() {
        return sendBandwidth;
    }

    public int getReceiveBandwidth() {
        return receiveBandwidth;
    }

    public int getFilesUploading() {
        return filesUploading;
    }

    public void setFilesUploading(int filesUploading) {
        this.filesUploading = filesUploading;
    }

    public int getFilesDownloading() {
        return filesDownloading;
    }

    public void setFilesDownloading(int filesDownloading) {
        this.filesDownloading = filesDownloading;
    }

    public int getCurrStorageUnused() {
        return currStorageUnused;
    }

    public void setCurrStorageUnused(int currStorageUnused) {
        this.currStorageUnused = currStorageUnused;
    }

    /*
     * Method to check if all storage in satellite is used
     * throws exception if true
     * @param fileSize
     */
    public void isAllStorageUsed(int fileSize) throws FileTransferException {
        if (currStorageUnused < fileSize) {
            throw new VirtualFileNoStorageSpaceException("Max Storage Reached");
        }

        return;
    }

    /*
     * Abstract method to change position of satellite
     * Different satellite types have different moving behaviours
     */
    public abstract void changePosition();

    /*
     * Method to get all entities in range for a satellite
     * @param satelliteList, deviceList
     * @returns satellitesInRange
     */
    public List<String> getInRangeForSatellites(List<Satellite> satelliteList, List<Device> deviceList) {
        List<String> satellitesInRange = new ArrayList<String>();

        for (Satellite satellite : satelliteList) {
            if (satellite.getId() != getId()) {
                if (inRangeSatandSat(satellite)) {
                    satellitesInRange.add(satellite.getId());
                }
            }
        }

        for (Device device : deviceList) {
            if (inRangeSatandDev(device)) {
                satellitesInRange.add(device.getId());
            }
        }

        return satellitesInRange;
    }

    /*
     * Helper to check the range between a satellite and a satellite
     */
    public boolean inRangeSatandSat(Satellite satellite) {
        double satelliteDistance = MathsHelper.getDistance(height, getPosition(), satellite.getHeight(),
                satellite.getPosition());
        boolean satelliteVisible = MathsHelper.isVisible(height, getPosition(), satellite.getHeight(),
                satellite.getPosition());

        if (satelliteDistance <= getRange() && satelliteVisible) {
            return true;
        }
        return false;
    }

    /*
     * Helper to check the range between a satellite and a device
     */
    public boolean inRangeSatandDev(Device device) {
        double deviceDistance = MathsHelper.getDistance(height, getPosition(), device.getPosition());
        boolean deviceVisible = MathsHelper.isVisible(height, getPosition(), device.getPosition());

        if (deviceDistance <= getRange() && deviceVisible) {
            return true;
        }
        return false;
    }

    /*
     * Overridden method from Entity super class
     * When to and from satellite, uploading and downloading
     * numbers must be updated
     */
    @Override
    public void updateEntityFiles() {
        for (File file : getFileList()) {
            if (!file.isTransferStatus()) {
                file.decrementMinsRequired();
                file.setFileTransferComplete();

                if (file.isTransferStatus()) {
                    setSatelliteFileComplete();
                }
            }

        }
    }

    private void setSatelliteFileComplete() {
        setFilesUploading(getFilesUploading() - 1);
        setFilesDownloading(getFilesDownloading() - 1);
    }
}
