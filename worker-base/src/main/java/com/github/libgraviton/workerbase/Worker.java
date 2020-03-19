/**
 * connects to the queue and subscribes the WorkerConsumer on the queue
 */

package com.github.libgraviton.workerbase;

import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import com.github.libgraviton.workerbase.exception.WorkerException;
import com.github.libgraviton.workerbase.helper.PropertiesLoader;
import java.io.IOException;
import java.util.Properties;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Worker class.</p>
 *
 * @author List of contributors {@literal <https://github.com/libgraviton/graviton-worker-base-java/graphs/contributors>}
 * @see <a href="http://swisscom.ch">http://swisscom.ch</a>
 * @version $Id: $Id
 */
public class Worker {

    private static final Logger LOG = LoggerFactory.getLogger(Worker.class);

    /**
     * properties
     */
    private Properties properties;
    
    /**
     * worker
     */
    private WorkerAbstract worker;
    
    /**
     * constructor
     *
     * @param worker worker instance
     * @throws WorkerException if setup failed.
     * @throws GravitonCommunicationException whenever the worker is unable to communicate with Graviton.
     */
    public Worker(WorkerAbstract worker) throws GravitonCommunicationException, WorkerException, IOException {
        properties = PropertiesLoader.load();
        LOG.info("Starting " + properties.getProperty("application.name") + " " + properties.getProperty("application.version"));
        worker.initialize(properties);
        worker.onStartUp();
        this.worker = worker;
    }
    
    /**
     * setup worker
     *
     * @throws WorkerException If connecting to queue is impossible
     */
    public void run() throws WorkerException {
        // connect here..
        Main main = new Main();
        try {
            WorkerRabbitRouteBuilder routeBuilder = new WorkerRabbitRouteBuilder();
            routeBuilder.setWorker(worker);

            main.addRoutesBuilder(routeBuilder);
            main.run();
        } catch (Exception e) {
            throw new WorkerException("Exception during startup in run()", e);
        }
    }

    /**
     * Gets the properties
     *
     * @return properties
     */
    public Properties getProperties() {
        return properties;
    }
}
