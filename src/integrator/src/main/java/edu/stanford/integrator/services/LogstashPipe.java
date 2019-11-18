package edu.stanford.integrator.services;

public class LogstashPipe {
    // Available Logstash executor "pipe", defined as a logstash pipeline
    private String configId;
    private String pipeName;
    private String pipeNumber;
    private String filesystemPath;
    private boolean isActive;

    private enum CurrentStatus {
        READY, IN_QUEUE, PROCESSING, DONE_SUCCESS, DONE_ERROR
    }
    public LogstashPipe() {
        this.configId = null;
        this.pipeName = null;
        this.pipeNumber = null;
        this.filesystemPath = null;
        this.isActive = false;
    }

    public String getConfigId() { return this.configId; }
    public void setConfigId(String configId) {
        this.configId = configId;
    }
    public String getPipeName() {
        return this.pipeName;
    }
    public void setPipeName(String pipeName) {
        this.pipeName = pipeName;
    }
    public String getPipeNumber() { return this.pipeNumber; }
    public void setPipeNumber(String pipeNumber) { this.pipeNumber = pipeNumber; }
    public String getPipeFilesystemPath() {
        return this.filesystemPath;
    }
    public void setPipeFilesystemPath(String filesystemPath) {
        this.filesystemPath = filesystemPath;
    }
    public boolean getIsActive() {
        return this.isActive;
    }
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
