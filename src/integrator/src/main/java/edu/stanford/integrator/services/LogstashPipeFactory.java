package edu.stanford.integrator.services;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class LogstashPipeFactory implements PooledObjectFactory<LogstashPipe> {

    private PooledObject<LogstashPipe> wrap(LogstashPipe pipe) {
        return new DefaultPooledObject<>(pipe);
    }

    private LogstashPipe unwrap(PooledObject<LogstashPipe> poPipe) {
        return poPipe.getObject();
    }

    public PooledObject<LogstashPipe> makeObject() throws Exception {
        // is called whenever a new instance is needed.
        return wrap(new LogstashPipe());
    }

    public void destroyObject(PooledObject<LogstashPipe> poPipe) throws Exception {
        // is invoked on every instance when it is being "dropped" from the pool,
        // or for reasons specific to the pool implementation.) There is no guarantee
        // that the instance being destroyed will be considered active, passive or in
        // a generally consistent state.
        LogstashPipe pipe = unwrap(poPipe);
        pipe.setIsActive(false);
    }

    public boolean validateObject(PooledObject<LogstashPipe> poPipe) {
        // may be invoked on activated instances to make sure they can be borrowed from the pool.
        // validateObject may also be used to test an instance being returned to the pool before
        // it is passivated. It will only be invoked on an activated instance.
        LogstashPipe pipe = unwrap(poPipe);
        assert(pipe.getIsActive());
        return true;
    }

    public void activateObject(PooledObject<LogstashPipe> poPipe) throws Exception {
        // is invoked on every instance that has been passivated before it is borrowed from the pool.

        LogstashPipe pipe = unwrap(poPipe);
        pipe.setIsActive(true);
    }

    public void passivateObject(PooledObject<LogstashPipe> poPipe) throws Exception {
        // is invoked on every instance when it is returned to the pool
        LogstashPipe pipe = unwrap(poPipe);
        pipe.setIsActive(false);
    }

}