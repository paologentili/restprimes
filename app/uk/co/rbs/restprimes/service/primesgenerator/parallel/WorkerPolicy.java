package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import com.google.inject.Singleton;
import play.Configuration;

import javax.inject.Inject;

@Singleton
public class WorkerPolicy {

    private final Configuration configuration;

    @Inject
    public WorkerPolicy(Configuration configuration) {
        this.configuration = configuration;
    }

    public Integer numberOfWorkers() {
        // TODO read from configuration
        return Runtime.getRuntime().availableProcessors() * 3 - 1;
    }

}
