package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import org.junit.Test;
import org.mockito.Mockito;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WorkerActorRouterFactoryTest {

    @Test
    public void shouldCreateRouterWithRouteesAndRoutingLogic() {

        final WorkerActorFactory mockWorkerActorFactory = Mockito.mock(WorkerActorFactory.class);

        WorkerActorRouterFactory factory = new WorkerActorRouterFactory(mockWorkerActorFactory);

        final Router broadcastingRouter = factory.createBroadcastingRouter(6, 30, 4);

        assertTrue(broadcastingRouter.logic() instanceof BroadcastRoutingLogic);

        assertThat(broadcastingRouter.routees().size(), equalTo(4));

        verify(mockWorkerActorFactory, times(1)).createWorkerActor(1, 6, 30, 4);
        verify(mockWorkerActorFactory, times(1)).createWorkerActor(2, 6, 30, 4);
        verify(mockWorkerActorFactory, times(1)).createWorkerActor(3, 6, 30, 4);
        verify(mockWorkerActorFactory, times(1)).createWorkerActor(4, 6, 30, 4);

        verifyNoMoreInteractions(mockWorkerActorFactory);

    }

}