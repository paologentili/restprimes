package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Router;
import org.junit.*;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveGuiceModule;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.WorkerPolicy;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParallelSieveMasterActorTest {

    private static ActorSystem testActorSystem;

    private WorkerPolicy mockWorkerPolicy;
    private ParallelSieveGuiceModule.MasterActorFactory mockMasterActorFactory;
    private WorkerActorRouterFactory mockRouterFactory;

    private static final int NUMBER_OF_WORKERS = 2;

    public static class DummyActor extends UntypedActor {
        @Override
        public void onReceive(Object message) throws Exception {
            // do nothing
        }
    }

    @BeforeClass
    public static void beforeClass() {
        testActorSystem = ActorSystem.create("TestSystem_" + ParallelSieveMasterActorTest.class.getSimpleName());
    }

    @AfterClass
    public static void afterClass() {
        testActorSystem.shutdown();
    }

    @Before
    public void setup() {

        // test subject's mocked dependencies
        this.mockMasterActorFactory = mock(ParallelSieveGuiceModule.MasterActorFactory.class);
        this.mockRouterFactory = mock(WorkerActorRouterFactory.class);
        this.mockWorkerPolicy = mock(WorkerPolicy.class);

        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(NUMBER_OF_WORKERS);

        when(mockMasterActorFactory.create()).then(invocation -> {
            return new ParallelSieveMasterActor(mockRouterFactory);
        });

    }

    @Test
    @Ignore("incomplete...")  // TODO complete this test
    public void shouldBroadCastSievingPrimesToAllWorkers() throws Exception {

        // given: n = 30 and numberOfWorkers = 2
        final int inputNumber = 30;

        when(mockRouterFactory.createBroadcastingRouter(6, inputNumber, NUMBER_OF_WORKERS))
                .thenReturn(new Router(new BroadcastRoutingLogic(), Arrays.asList(new ActorRefRoutee(testActorSystem.actorOf(Props.create(DummyActor.class))))));

        // when:
        // it receives either GeneratePrimes(n=30, numberOfWorkers=2) or StreamPrimes(n=30, numberOfWorkers=2) message

        // then:
        // it should broadcast SievingPrimeFound(2), SievingPrimeFound(3), SievingPrimeFound(5) to 2 workers

    }

}
