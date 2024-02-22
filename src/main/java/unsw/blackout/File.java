package unsw.blackout;

public class File {
    private String fileName;
    private String fileContent;
    private int fileSize;
    private boolean transferStatus;
    private String contentComplete;
    private int minsRequired;

    public File(String fileName, String fileContent) {
        this.fileName = fileName;
        this.fileContent = fileContent;
        this.fileSize = fileContent.length();
        this.transferStatus = true;
        this.contentComplete = "";
        this.minsRequired = 0;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileContent() {
        return fileContent;
    }

    public int getFileSize() {
        return fileSize;
    }

    public boolean isTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(boolean transferStatus) {
        this.transferStatus = transferStatus;
    }

    public String getContentComplete() {
        return contentComplete;
    }

    public void setContentComplete() {
        contentComplete = fileContent;
    }

    public void setMinsRequired(int minsRequired) {
        this.minsRequired = minsRequired;
    }

    public int getMinsRequired() {
        return minsRequired;
    }

    public void decrementMinsRequired() {
        this.minsRequired--;
    }

    /*
     * Method to calculate the amount of minutes required
     * to fully receive the content of the file based on the
     * particular satellite's receiving bandwidth and bytes of file
     * @param bandwidth
     * @returns int
     */
    public int calculateMinsRequired(int bandwidth) {
        int fileSize = getFileSize();

        return (int) Math.ceil((double) fileSize / bandwidth);
    }

    /*
     * Method to set the file transfer to be complete
     * once the mins required to complete has been decremented to 0
     */
    public void setFileTransferComplete() {
        if (getMinsRequired() == 0) {
            setContentComplete();
            setTransferStatus(true);
        }
    }
}
