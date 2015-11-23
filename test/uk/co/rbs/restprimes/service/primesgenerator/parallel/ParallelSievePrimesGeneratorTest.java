package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.ActorSystem;
import org.junit.*;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveGuiceModule.MasterActorFactory;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveMasterActor;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.GeneratePrimes;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.WorkerActorRouterFactory;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

public class ParallelSievePrimesGeneratorTest {

    private static final List<Integer> primesTo30 = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);

    private static ActorSystem testActorSystem;

    private WorkerPolicy mockWorkerPolicy;
    private MasterActorFactory mockMasterActorFactory;

    private ParallelSievePrimesGenerator primesGenerator;

    @BeforeClass
    public static void beforeClass() {
        testActorSystem = ActorSystem.create("TestSystem_" + ParallelSievePrimesGeneratorTest.class.getSimpleName());
    }

    @AfterClass
    public static void afterClass() {
        testActorSystem.shutdown();
    }

    @Before
    public void setup() {

        // test subject's mocked dependencies
        this.mockMasterActorFactory = mock(MasterActorFactory.class);
        this.mockWorkerPolicy = mock(WorkerPolicy.class);

        // test subject
        this.primesGenerator = new ParallelSievePrimesGenerator(testActorSystem, mockMasterActorFactory, mockWorkerPolicy);

    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowErrorForNegativeNumbers() throws Exception {

        // given
        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(-1);

        // when
        primesGenerator.generatePrimes(10, 1000);

        // then
        // throw exception
    }

    @Test
    public void shouldReturnEmptyListForZeroAndOne() throws Exception {

        // when
        Object result = Await.result(primesGenerator.generatePrimes(0, 1000), Duration.create("2 seconds"));

        // then
        assertThat(primeNumbersFrom((BitSet)result, 0), equalTo(emptyList()));

        // when
        result = Await.result(primesGenerator.generatePrimes(1, 1000), Duration.create("2 seconds"));

        // then
        assertThat(primeNumbersFrom((BitSet)result, 0), equalTo(emptyList()));
    }

    public static class MockMasterActor extends ParallelSieveMasterActor {
        public MockMasterActor(WorkerActorRouterFactory workerActorRouter) {
            super(workerActorRouter);
        }
        @Override
        public void onReceive(Object message) throws Exception {
            sender().tell(message, self());
        }
    }

    @Test
    public void shouldSendAMessageToTheMasterActorToTriggerPrimeGeneration() throws Exception {

        // given
        final int inputNumber = 30;
        final int numberOfWorkers = 1;

        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(numberOfWorkers);

        when(mockMasterActorFactory.create()).then(invocation -> new MockMasterActor(null));

        // when
        final Object result = Await.result(primesGenerator.generatePrimes(inputNumber, 1000), Duration.create("1s"));

        // then
        assertThat(((GeneratePrimes)result).n, equalTo(inputNumber));
        assertThat(((GeneratePrimes)result).numberOfWorkers, equalTo(numberOfWorkers));

    }

    @Test
    @Ignore("not implemeneted") // TODO implement!
    public void shouldGeneratePrimesWithDifferentWorkerConfigurations() throws Exception {

        final Duration _2sec = Duration.create("2 seconds");
        final int timeoutMillis = 10000;
        Object result;

        final int number = 30;

        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(1);
        result = Await.result(primesGenerator.generatePrimes(number, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(2);
        result = Await.result(primesGenerator.generatePrimes(number, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(3);
        result = Await.result(primesGenerator.generatePrimes(number, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(4);
        result = Await.result(primesGenerator.generatePrimes(number, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));


        when(mockWorkerPolicy.numberOfWorkers()).thenReturn(100); // TODO what happens if the number of workers is too big...
        // ....
    }

}
