package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Singleton
public class WorkerActorRouterFactory {

    private final WorkerActorFactory workerActorFactory;

    @Inject
    public WorkerActorRouterFactory(WorkerActorFactory workerActorFactory) {
        this.workerActorFactory = workerActorFactory;
    }

    public Router createBroadcastingRouter(Integer sqrtN, Integer n, Integer numberOfWorkers) {
        return new Router(new BroadcastRoutingLogic(), IntStream.rangeClosed(1, numberOfWorkers)
                .mapToObj(i -> new ActorRefRoutee(workerActorFactory.createWorkerActor(i, sqrtN, n, numberOfWorkers)))
                .collect(toList())
        );
    }

}
