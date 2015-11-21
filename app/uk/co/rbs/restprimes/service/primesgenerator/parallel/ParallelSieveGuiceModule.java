package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveMasterActor;

import static uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.MASTER_ACTOR;

public class ParallelSieveGuiceModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        bindActor(ParallelSieveMasterActor.class, MASTER_ACTOR);
    }

}