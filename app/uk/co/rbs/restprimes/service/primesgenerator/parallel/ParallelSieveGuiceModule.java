package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import com.google.inject.AbstractModule;
import play.libs.akka.AkkaGuiceSupport;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveMasterActor;

public class ParallelSieveGuiceModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        bindActorFactory(ParallelSieveMasterActor.class, MasterActorFactory.class);
    }

    public interface MasterActorFactory {
        ParallelSieveMasterActor create();
    }

}