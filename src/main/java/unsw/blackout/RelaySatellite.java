package unsw.blackout;

import unsw.utils.Angle;

public class RelaySatellite extends Satellite {
    private static final Angle MIN = Angle.fromDegrees(140);
    private static final Angle MAX = Angle.fromDegrees(190);
    private static final Angle THRESHOLD = Angle.fromDegrees(345);
    private static final int DUMMY_BANDWIDTH = 1000000000;

    public RelaySatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, "RelaySatellite", height, position, 1500, 300000, DUMMY_BANDWIDTH, DUMMY_BANDWIDTH, 0);
    }

    /*
     * Calculating position based on this particular satellite's behaviour
     */
    @Override
    public void changePosition() {
        Angle currPos = super.getPosition();
        double angularVelocity = super.getLinearVelocity() / super.getHeight();
        double newPos;

        // If < 140 or > 190 or if at 345 with negative velocity
        if (currPos.compareTo(MIN) == -1 || currPos.compareTo(MAX) == 1
                || (currPos.compareTo(THRESHOLD) == 0 && getLinearVelocity() < 0)) {

            newPos = currPos.toRadians() + angularVelocity;
            setLinearVelocity(-getLinearVelocity()); // Reverse linear velocity
        } else {
            newPos = currPos.toRadians() - angularVelocity;
        }

        super.setPosition(Angle.fromRadians(newPos));
    }
}
