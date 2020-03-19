package com.github.libgraviton.workerbase;


import java.util.Properties;
import org.apache.camel.builder.RouteBuilder;

/**
 * A Camel Java DSL Router
 */
public class WorkerRabbitRouteBuilder extends RouteBuilder {

    private WorkerAbstract worker;

    public WorkerAbstract getWorker() {
        return worker;
    }

    public void setWorker(WorkerAbstract worker) {
        this.worker = worker;
    }

    /**
     * Let's configure the Camel routing rules using Java code...
     */
    public void configure() {

        // here is a sample which processes the input files
        // (leaving them in place - see the 'noop' flag)
        // then performs content based routing on the message using XPath
        from("file:src/data?noop=true")
            .choice()
                .when(xpath("/person/city = 'London'"))
                    .log("UK message")
                    .to("file:target/messages/uk")
                .otherwise()
                    .log("Other message")
                    .to("file:target/messages/others");
    }

}
