package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import play.Logger;

import javax.inject.Inject;

public class WorkerActorFactory {

    private static final Logger.ALogger LOGGER = Logger.of(WorkerActorFactory.class.getSimpleName());

    private final ActorSystem actorSystem;

    @Inject
    public WorkerActorFactory(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public ActorRef createWorkerActor(int i, Integer sqrt, Integer n, Integer numberOfWorkers) {

        int segmentSize = (n - sqrt) / numberOfWorkers;

        int segmentStart = sqrt + ((i-1) * segmentSize) + 1;

        int segmentEnd = i == numberOfWorkers ? n : segmentStart + segmentSize - 1;

        ActorRef workerActor = actorSystem.actorOf(Props.create(ParallelSieveWorkerActor.class, segmentStart, segmentEnd));

        LOGGER.debug("Created worker for segment ["+segmentStart+", "+segmentEnd+"] " + workerActor.path().uid());

        //getContext().watch(workerActor); TODO add code in master actor to watch workers

        return workerActor;
    }

}
