package unsw.blackout;

import java.util.ArrayList;
import java.util.List;

import unsw.blackout.FileTransferException.VirtualFileAlreadyExistsException;
import unsw.blackout.FileTransferException.VirtualFileNoBandwidthException;
import unsw.blackout.FileTransferException.VirtualFileNotFoundException;
import unsw.response.models.EntityInfoResponse;
import unsw.utils.Angle;
import unsw.utils.MathsHelper;

/**
 * The controller for the Blackout system.
 *
 * WARNING: Do not move this file or modify any of the existing method
 * signatures
 */
public class BlackoutController {
    private List<Device> deviceList = new ArrayList<Device>();
    private List<Satellite> satelliteList = new ArrayList<Satellite>();

    /*
     * Method to create a device
     * @params deviceId, type, position
     */
    public void createDevice(String deviceId, String type, Angle position) {
        switch (type) {
        case "HandheldDevice":
            deviceList.add(new HandheldDevice(deviceId, type, position));
            break;
        case "LaptopDevice":
            deviceList.add(new LaptopDevice(deviceId, type, position));
            break;
        case "DesktopDevice":
            deviceList.add(new DesktopDevice(deviceId, type, position));
            break;
        default:
            break;
        }
    }

    /*
     * Method to remove a device given a deviceId
     * @params deviceId
     */
    public void removeDevice(String deviceId) {
        for (Device device : deviceList) {
            if (device.getId().equals(deviceId)) {
                deviceList.remove(device);
                return;
            }
        }
    }

    /*
     * Method to create a satellite based on the type given
     * @params satelliteId, type, height, position
     */
    public void createSatellite(String satelliteId, String type, double height, Angle position) {
        // TODO: Task 1c)
        switch (type) {
        case "StandardSatellite":
            satelliteList.add(new StandardSatellite(satelliteId, type, height, position));
            break;
        case "RelaySatellite":
            satelliteList.add(new RelaySatellite(satelliteId, type, height, position));
            break;
        case "TeleportingSatellite":
            satelliteList.add(new TeleportingSatellite(satelliteId, type, height, position));
            break;
        default:
            break;
        }
    }

    /*
     * Method to remove a satellite given a satelliteId
     * @params satelliteId, type, height, position
     */
    public void removeSatellite(String satelliteId) {
        for (Satellite satellite : satelliteList) {
            if (satellite.getId().equals(satelliteId)) {
                satelliteList.remove(satellite);
                return;
            }
        }
    }

    /*
     * Method to get a list of all deviceIds
     * @returns deviceIdList
     */
    public List<String> listDeviceIds() {
        List<String> deviceIdList = new ArrayList<String>();

        for (Device device : deviceList) {
            deviceIdList.add(device.getId());
        }

        return deviceIdList;
    }

    /*
     * Method to get a list of all satelliteIds
     * @returns satelliteIdList
     */
    public List<String> listSatelliteIds() {
        List<String> satelliteIdList = new ArrayList<String>();

        for (Satellite satellite : satelliteList) {
            satelliteIdList.add(satellite.getId());
        }

        return satelliteIdList;
    }

    /*
     * Method to add a file to a device
     * @params deviceId, filename, content
     */
    public void addFileToDevice(String deviceId, String filename, String content) {
        for (Device device : deviceList) {
            if (device.getId().equals(deviceId)) {
                File newFile = new File(filename, content);
                newFile.setContentComplete();
                device.addFile(newFile);
                return;
            }
        }
    }

    /*
     * Method to get info on a particular entity given it's id
     * @params id
     * @returns EntityInfoResponse
     * @returns null
     */
    public EntityInfoResponse getInfo(String id) {
        Device device = getDevice(id);
        if (device != null) {
            return new EntityInfoResponse(id, device.getPosition(), MathsHelper.RADIUS_OF_JUPITER, device.getType(),
                    device.fileMap());
        }

        Satellite satellite = getSatellite(id);
        if (satellite != null) {
            return new EntityInfoResponse(id, satellite.getPosition(), satellite.getHeight(), satellite.getType(),
                    satellite.fileMap());
        }

        return null;
    }

    /*
     * Method to simulate changing satellite position and
     * updating of entity file transfers
     */
    public void simulate() {
        for (Satellite satellite : satelliteList) {
            satellite.changePosition();
            satellite.updateEntityFiles();
        }
        for (Device device : deviceList) {
            device.updateEntityFiles();
        }
    }

    /**
     * Simulate for the specified number of minutes. You shouldn't need to modify
     * this function.
     */
    public void simulate(int numberOfMinutes) {
        for (int i = 0; i < numberOfMinutes; i++) {
            simulate();
        }
    }

    /*
     * Method to get a list of entities within range of
     * an entity given it's id
     * @params id
     * @returns entitiesInRange
     */
    public List<String> communicableEntitiesInRange(String id) {
        List<String> entitiesInRange = new ArrayList<String>();

        Satellite satellite = getSatellite(id);
        if (satellite != null) {
            entitiesInRange.addAll(satellite.getInRangeForSatellites(satelliteList, deviceList));
        }

        Device device = getDevice(id);
        if (device != null) {
            entitiesInRange.addAll(device.getInRangeForDevices(satelliteList));
        }

        return entitiesInRange;
    }

    /*
     * Method to send a file from a particular entity
     * to another particular entity given their id's
     * @params fileName, fromId, toId
     */
    public void sendFile(String fileName, String fromId, String toId) throws FileTransferException {
        Device deviceSending = getDevice(fromId);
        Satellite satelliteSending = getSatellite(fromId);

        // Sending from a device
        if (deviceSending != null) {
            List<String> communicableEntities = communicableEntitiesInRange(fromId);
            // Check in range, file exists/partial transfer
            if (!deviceSending.checkIfInRange(communicableEntities, toId)) {
                return;
            }
            if (deviceSending.getFile(fileName) == null || !deviceSending.getFile(fileName).isTransferStatus()) {
                throw new VirtualFileNotFoundException(fileName);
            }

            Satellite satelliteReceiving = getSatellite(toId);
            if (satelliteReceiving != null) {
                sendToSatellite(fileName, satelliteReceiving, deviceSending);
            }
            return;
        }

        // Sending from a satellite
        if (satelliteSending != null) {
            List<String> communicableEntities = communicableEntitiesInRange(fromId);
            // Check in range, file exists/partial transfer and bandwidth availability
            if (!satelliteSending.checkIfInRange(communicableEntities, toId)) {
                return;
            }
            if (satelliteSending.getFile(fileName) == null || !satelliteSending.getFile(fileName).isTransferStatus()) {
                throw new VirtualFileNotFoundException(fileName);
            }
            if (satelliteSending.getFilesUploading() >= satelliteSending.getSendBandwidth()) {
                throw new VirtualFileNoBandwidthException(satelliteSending.getId());
            }

            // Sending to satellite
            Satellite satelliteReceiving = getSatellite(toId);
            if (satelliteReceiving != null) {
                sendToSatellite(fileName, satelliteReceiving, satelliteSending);
            }
            // Sending to device
            Device deviceReceiving = getDevice(toId);
            if (deviceReceiving != null) {
                sendToDevice(fileName, deviceReceiving, satelliteSending);
            }
        }
    }

    // ------------------------------- HELPER METHODS --------------------------------------

    /*
     * Method to get a device given it's id
     * @params deviceId
     * @returns device
     */
    public Device getDevice(String deviceId) {
        for (Device device : deviceList) {
            if (device.getId().equals(deviceId)) {
                return device;
            }
        }

        return null;
    }

    /*
     * Method to get a satellite given it's id
     * @params satelliteId
     * @returns satellite
     */
    public Satellite getSatellite(String satelliteId) {
        for (Satellite satellite : satelliteList) {
            if (satellite.getId().equals(satelliteId)) {
                return satellite;
            }
        }

        return null;
    }

    /*
     * Method to send a file to a satellite, can be used by any sending entity type
     * @params fileName, satellite, entitySending
     */
    private void sendToSatellite(String fileName, Satellite satellite, Entity entitySending)
            throws FileTransferException {
        // Two exceptions in one method - file already exists and bandwidth
        catchSatelliteTransferExceptions(satellite, fileName);

        // Create new file
        File file = new File(fileName, entitySending.getFileToSend(fileName));
        file.setTransferStatus(false);

        // Exceeded storage exception
        satellite.isAllStorageUsed(file.getFileSize());

        // Calculate which bandwidth to use - min(sending, receiving)
        int bandwidth = satellite.getReceiveBandwidth();
        Satellite satelliteSending = getSatellite(entitySending.getId());
        if (satelliteSending != null) {
            if (satellite.getReceiveBandwidth() > satelliteSending.getSendBandwidth()) {
                bandwidth = satelliteSending.getSendBandwidth();
            }
        }

        // Set mins required to complete transfer
        int minsRequired = file.calculateMinsRequired(bandwidth);
        file.setMinsRequired(minsRequired);

        // Add file and update storage and downloading
        satellite.addFile(file);
        satellite.setCurrStorageUnused(satellite.getCurrStorageUnused() - file.getFileSize());
        satellite.setFilesDownloading(satellite.getFilesDownloading() + 1);
    }

    /*
     * Method to send a file to a device, can be used by any sending entity type
     * @params fileName, device, entitySending
     */
    private void sendToDevice(String fileName, Device device, Entity entitySending) throws FileTransferException {
        // File already exists in device's fileList
        if (device.getFile(fileName) != null) {
            throw new VirtualFileAlreadyExistsException(fileName);
        }

        // Create new file
        File file = new File(fileName, entitySending.getFileToSend(fileName));
        file.setTransferStatus(false);

        // Calculate which bandwidth to use - sending
        Satellite satelliteSending = getSatellite(entitySending.getId());
        int minsRequired = file.calculateMinsRequired(satelliteSending.getSendBandwidth());
        file.setMinsRequired(minsRequired);

        // Add file and update uploading
        device.addFile(file);
        satelliteSending.setFilesUploading(satelliteSending.getFilesUploading() + 1);
    }

    /*
     * Method to catch transfer exceptions specific to a satellite receiving a file
     * @params satellite, fileName
     */
    private void catchSatelliteTransferExceptions(Satellite satellite, String fileName) throws FileTransferException {
        // File already exists in satellite's fileList
        if (satellite.getFile(fileName) != null) {
            throw new VirtualFileAlreadyExistsException(fileName);
        }

        // Bandwidth is full
        if (satellite.getFilesDownloading() >= satellite.getReceiveBandwidth()) {
            throw new VirtualFileNoBandwidthException(satellite.getId());
        }
    }

    public void createDevice(String deviceId, String type, Angle position, boolean isMoving) {
        createDevice(deviceId, type, position);
        // TODO: Task 3
    }

    public void createSlope(int startAngle, int endAngle, int gradient) {
        // TODO: Task 3
        // If you are not completing Task 3 you can leave this method blank :)
    }
}
