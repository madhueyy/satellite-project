package unsw.blackout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import unsw.response.models.FileInfoResponse;
import unsw.utils.Angle;

public abstract class Entity {
    private String id;
    private String type;
    private Angle position;
    private ArrayList<File> fileList;
    private double range;

    public Entity(String id, Angle position, String type, double range) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.fileList = new ArrayList<File>();
        this.range = range;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Angle getPosition() {
        return position;
    }

    public void setPosition(Angle position) {
        this.position = position;
    }

    public double getRange() {
        return range;
    }

    public File getFile(String fileName) {
        for (File file : fileList) {
            if (file.getFileName().equals(fileName)) {
                return file;
            }
        }
        return null;
    }

    public ArrayList<File> getFileList() {
        return fileList;
    }

    public void setFileList(ArrayList<File> fileList) {
        this.fileList = fileList;
    }

    public void addFile(File newFile) {
        fileList.add(newFile);
    }

    public Map<String, FileInfoResponse> fileMap() {
        Map<String, FileInfoResponse> map = new HashMap<String, FileInfoResponse>();

        for (File file : fileList) {
            map.put(file.getFileName(), new FileInfoResponse(file.getFileName(), file.getContentComplete(),
                    file.getFileSize(), file.isTransferStatus()));
        }

        return map;
    }

    /*
     * Method to get the content of the file that
     * needs to be sent
     * @params fileName
     * @returns fileContent
     * @returns ""
     */
    public String getFileToSend(String fileName) {
        for (File file : fileList) {
            if (file.getFileName() == fileName) {
                return file.getFileContent();
            }
        }

        return "";
    }

    /*
     * Checks if given entity (through it's id) is in range
     * of the source entity
     * @params communicableEntitiesOfSource, toId
     * @returns boolean
     */
    public boolean checkIfInRange(List<String> communicableEntitiesOfSource, String toId) {
        for (String id : communicableEntitiesOfSource) {
            if (id == toId) {
                return true;
            }
        }
        return false;
    }

    /*
     * Updating transfer of all of the files in this entity
     */
    public void updateEntityFiles() {
        for (File file : getFileList()) {
            if (!file.isTransferStatus()) {
                file.decrementMinsRequired();
                file.setFileTransferComplete();
            }
        }
    }
}
