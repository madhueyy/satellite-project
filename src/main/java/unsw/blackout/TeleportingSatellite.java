package unsw.blackout;

import unsw.utils.Angle;

public class TeleportingSatellite extends Satellite {
    private static final Angle MID = Angle.fromDegrees(180);
    private boolean anticlockwise = true;

    public TeleportingSatellite(String satelliteId, String type, double height, Angle position) {
        super(satelliteId, "TeleportingSatellite", height, position, 1000, 200000, 10, 15, 200);
    }

    /*
     * Calculating position based on this particular satellite's behaviour
     */
    @Override
    public void changePosition() {
        Angle currPos = super.getPosition();
        double angularVelocity = super.getLinearVelocity() / super.getHeight();
        Angle newPos = new Angle();

        if (anticlockwise) {
            newPos = currPos.add(Angle.fromRadians(angularVelocity));
        } else {
            newPos = currPos.subtract(Angle.fromRadians(angularVelocity));
        }

        // Set new position depending on newPosition angle
        if (newPos.compareTo(MID) == 1) {
            super.setPosition(Angle.fromRadians(0));
            anticlockwise = false;
        } else {
            super.setPosition(newPos);
        }
    }
}
