package edu.stanford.integrator.services;

public interface LogstashJobQueueService {
    void requestJob(String configId) throws Exception;
    void setupPool(int members) throws Exception;
    void tearDownPool() throws Exception;
    LogstashPipe borrowPipe() throws Exception;
    void returnPipe(LogstashPipe pipe) throws Exception;
    int getPoolNumIdle();
    void setMaxWait(long waitTime);
}