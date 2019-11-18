package edu.stanford.integrator.services;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class LogstashJobQueueImpl implements LogstashJobQueueService {
    private static final Logger LOGGER = Logger.getLogger(LogstashJobQueueImpl.class.getName());

    private GenericObjectPool pool;
    LogstashPipe pipe1 = new LogstashPipe();
    LogstashPipe pipe2 = new LogstashPipe();
    LogstashPipe pipe3 = new LogstashPipe();

    public LogstashJobQueueImpl() {
        LogstashPipeFactory factory = new LogstashPipeFactory();
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMinIdle(0);
        config.setMaxIdle(3);
        config.setMaxTotal(3);
        this.pool = new GenericObjectPool<>(factory, config);
    }

    /* Queue methods */
    public void requestJob(String configId) throws Exception {
        LogstashPipe currentWorker = borrowPipe();
        currentWorker.setConfigId(configId);
    }


    /* Pool methods */
    public void setupPool(int members) throws Exception {
        // create the pool with empty LogstashPipe objects
        for (int i = 0; i < members; i++) {
            this.pool.addObject();
        }

        // borrow each object to set its attributes
        LogstashPipe[] pipes = new LogstashPipe[members];
        for (int i = 0; i < members; i++) {
            pipes[i] = (LogstashPipe) this.pool.borrowObject();
            pipes[i].setPipeName("pipe" + (i+1));
            pipes[i].setPipeNumber(Integer.toString(i+1));
            pipes[i].setPipeFilesystemPath("/tmp/pipelines/pipeline" + (i+1) + "/");  // TODO: Ideally this is a property and configurable

        }

        // return all objects to the pool
        for (int i = 0; i < members; i++) {
            this.pool.returnObject(pipes[i]);
        }
    }

    public void tearDownPool() throws Exception {
        // clear, but don't close the pool
        // used for tests
        this.pool.clear();
        this.pool.close();
        this.pool = null;
    }

    public LogstashPipe borrowPipe() throws Exception {
        // borrow an pipe from the pool which will "activate" it
        this.LOGGER.log(Level.INFO,"Borrowing Pipe");
        return (LogstashPipe) this.pool.borrowObject();
    }

    public void returnPipe(LogstashPipe pipe) throws Exception {
        // return pipe back to the pool and "passivate" it
        this.LOGGER.log(Level.INFO,"Returning Pipe");
        this.pool.returnObject(pipe);
    }

    public int getPoolNumIdle() {
        return this.pool.getNumIdle();
    }

    // used for testing only
    public void setMaxWait(long waitTime) {
        this.pool.setMaxWaitMillis(waitTime);
    }

}