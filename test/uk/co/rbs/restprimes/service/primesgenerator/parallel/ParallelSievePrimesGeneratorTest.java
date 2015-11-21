package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.ActorSystem;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveGuiceModule.MasterActorFactory;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

public class ParallelSievePrimesGeneratorTest {

    private static final List<Integer> primesTo30 = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);

    private ParallelSievePrimesGenerator primesGenerator;

    @Before
    public void setup() {
        ActorSystem system = ActorSystem.create("TestSystem");
        MasterActorFactory mockMasterActorFactory = mock(MasterActorFactory.class);
        this.primesGenerator = new ParallelSievePrimesGenerator(system, mockMasterActorFactory);
    }

    @Test
    public void shouldReturnEmptyListForZeroAndOne() throws Exception {

        // when
        Object result = Await.result(primesGenerator.generatePrimes(0, 1, 10000), Duration.create("2 seconds"));

        // then
        assertThat(primeNumbersFrom((BitSet)result, 0), equalTo(emptyList()));

        // when
        result = Await.result(primesGenerator.generatePrimes(1, 1, 10000), Duration.create("2 seconds"));

        // then
        assertThat(primeNumbersFrom((BitSet)result, 0), equalTo(emptyList()));
    }

    @Test
    @Ignore("todo implement test to verify the the message sent to actor containe proper parameter values")
    public void shouldSendAMessageOfTypeGeneratePrimesToTheMasterActor() throws Exception {

        // when
        Object result = Await.result(primesGenerator.generatePrimes(30, 1, 10000), Duration.create("2 seconds"));

        // then
        // verify master actor received a message like GeneratePrimes(20, 1)

    }

    @Test
    @Ignore("this should be moved to a test that send a mesage to the master actor") // TODO move this test!!
    public void shouldGeneratePrimesWithDifferentWorkerConfigurations() throws Exception {

        final Duration _2sec = Duration.create("2 seconds");
        final int timeoutMillis = 10000;
        Object result;

        final int number = 30;

        result = Await.result(primesGenerator.generatePrimes(number, 1, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        result = Await.result(primesGenerator.generatePrimes(number, 2, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        result = Await.result(primesGenerator.generatePrimes(number, 4, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        result = Await.result(primesGenerator.generatePrimes(number, 5, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));
    }

}
