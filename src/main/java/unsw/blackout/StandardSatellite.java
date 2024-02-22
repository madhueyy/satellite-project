package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.blackout.FileTransferException.VirtualFileNoStorageSpaceException;
import unsw.utils.Angle;

public class StandardSatellite extends Satellite {
    public StandardSatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, "StandardSatellite", height, position, 2500, 150000, 1, 1, 80);
    }

    /*
     * Overridden method as standardSatellite
     * cannot send anything to DesktopDevices
     */
    @Override
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
            // Only add if device is not DesktopDevice and is in range
            if (inRangeSatandDev(device) && !(device instanceof DesktopDevice)) {
                satellitesInRange.add(device.getId());
            }
        }

        return satellitesInRange;
    }

    /*
     * Overridden method where standard satellites have to check for
     * number of files and bytes for throwing no storage exception
     */
    @Override
    public void isAllStorageUsed(int fileSize) throws FileTransferException {
        if (getFileList().size() > 3) {
            throw new VirtualFileNoStorageSpaceException("Max Files Reached");
        }
        if (getCurrStorageUnused() < fileSize) {
            throw new VirtualFileNoStorageSpaceException("Max Storage Reached");
        }

        return;
    }

    /*
     * Calculating position based on this particular satellite's behaviour
     */
    @Override
    public void changePosition() {
        Angle currPos = super.getPosition();
        double angularVelocity = super.getLinearVelocity() / super.getHeight();

        double newPos = currPos.toRadians() - angularVelocity;
        newPos %= 360;

        super.setPosition(Angle.fromRadians(newPos));
    }
}
