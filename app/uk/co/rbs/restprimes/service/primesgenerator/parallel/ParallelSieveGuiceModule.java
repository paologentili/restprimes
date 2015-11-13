package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;

public class ParallelSieveGuiceModule extends AbstractModule implements AkkaGuiceSupport {

    public static final String PARALLEL_SIEVE_ACTOR_NAME = "parallel-sieve-actor";

    @Override
    protected void configure() {
        bindActor(ParallelSieveMasterActor.class, PARALLEL_SIEVE_ACTOR_NAME);
    }

}