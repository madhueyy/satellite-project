package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.utils.Angle;
import unsw.utils.MathsHelper;

public abstract class Device extends Entity {
    public Device(String deviceId, String type, Angle position, double range) {
        super(deviceId, position, type, range);
    }

    /*
     * Method to get all entities in range for a device
     * @param satelliteList
     * @returns satellitesInRange
     */
    public List<String> getInRangeForDevices(List<Satellite> satelliteList) {
        List<String> satellitesInRange = new ArrayList<String>();

        for (Satellite satellite : satelliteList) {
            if (inRangeDevandSat(satellite)
                    && !(satellite instanceof StandardSatellite && getType().equals("DesktopDevice"))) {
                satellitesInRange.add(satellite.getId());
            }
        }

        return satellitesInRange;
    }

    /*
     * Helper to check the range between a device and a satellite
     */
    private boolean inRangeDevandSat(Satellite satellite) {
        double satelliteDistance = MathsHelper.getDistance(satellite.getHeight(), satellite.getPosition(),
                getPosition());
        boolean satelliteVisible = MathsHelper.isVisible(satellite.getHeight(), satellite.getPosition(), getPosition());

        if (satelliteDistance <= getRange() && satelliteVisible) {
            return true;
        }
        return false;
    }
}
