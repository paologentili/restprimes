package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class WorkerActorFactoryTest {

    private WorkerActorFactory factory;
    private ActorSystem mockActorSystem;

    @Before
    public void before() {

        mockActorSystem = Mockito.mock(ActorSystem.class);

        when(mockActorSystem.actorOf(any())).thenReturn(new ActorRef() {
            @Override
            public boolean isTerminated() {
                return false;
            }

            @Override
            public ActorPath path() {
                return new RootActorPath(new Address("protocol", "system"), "name");
            }
        });

        factory = new WorkerActorFactory(mockActorSystem);
    }

    @Test
    public void shouldCreateWorkerActorWithEqualSplitBetweenWorkers() {

        int n = 30;
        int sqrt = 6;
        int numberOfWorkers = 4;

        assertTrue((n - sqrt) % numberOfWorkers == 0);

        factory.createWorkerActor(1, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 7, 12));

        factory.createWorkerActor(2, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 13, 18));

        factory.createWorkerActor(3, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 19, 24));

        factory.createWorkerActor(4, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 25, 30));
    }

    @Test
    public void shouldCreateWorkerActorWhenNotEqualSplitBetweenWorkers() {

        int n = 30;
        int sqrt = 6;
        int numberOfWorkers = 5;

        assertTrue((n - sqrt) % numberOfWorkers != 0);

        factory.createWorkerActor(1, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 7, 10));

        factory.createWorkerActor(2, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 11, 14));

        factory.createWorkerActor(3, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 15, 18));

        factory.createWorkerActor(4, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 19, 22));

        factory.createWorkerActor(5, sqrt, n, numberOfWorkers);
        verify(mockActorSystem, times(1)).actorOf(Props.create(ParallelSieveWorkerActor.class, 23, 30));

    }

}