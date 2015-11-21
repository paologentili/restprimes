package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.Assisted;
import play.libs.akka.AkkaGuiceSupport;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveMasterActor;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveWorkerActor;

public class ParallelSieveGuiceModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {

        bindActorFactory(ParallelSieveMasterActor.class, MasterActorFactory.class);

        bindActorFactory(ParallelSieveWorkerActor.class, WorkerActorFactory.class);

    }

    public interface WorkerActorFactory {
        ParallelSieveWorkerActor create(@Assisted("segmentStart") Integer segmentStart, @Assisted("segmentEnd") Integer segmentEnd);
    }

    public interface MasterActorFactory {
        ParallelSieveMasterActor create();
    }

}