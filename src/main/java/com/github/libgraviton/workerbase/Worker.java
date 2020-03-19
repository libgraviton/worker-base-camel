package com.github.libgraviton.workerbase;

import org.apache.camel.main.Main;

/**
 * A Camel Application
 */
public class Worker {

    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        Main main = new Main();
        main.addRoutesBuilder(new WorkerRabbitRouteBuilder());
        main.run(args);
    }

}

