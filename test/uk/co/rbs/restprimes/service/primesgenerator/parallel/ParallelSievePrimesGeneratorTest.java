package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.ActorSystem;
import org.junit.Before;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import uk.co.rbs.restprimes.service.primesgenerator.naive.NaivePrimesGenerator;
import uk.co.rbs.restprimes.utils.RestPrimeUtils;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

public class ParallelSievePrimesGeneratorTest {

    private static final List<Integer> primesTo30 = Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29);

    private ParallelSievePrimesGenerator primesGenerator;

    @Before
    public void setup() {
        ActorSystem system = ActorSystem.create("PiSystem");
        this.primesGenerator = new ParallelSievePrimesGenerator(system);
    }

    @Test
    public void shouldReturnEmptyListForZeroAndOne() throws Exception {

        // when
        Object result = Await.result(primesGenerator.generate(0, 1, 10000), Duration.create("2 seconds"));

        // then
        assertThat(primeNumbersFrom((BitSet)result, 0), equalTo(emptyList()));

        // when
        result = Await.result(primesGenerator.generate(1, 1, 10000), Duration.create("2 seconds"));

        // then
        assertThat(primeNumbersFrom((BitSet)result, 0), equalTo(emptyList()));
    }


    @Test
    public void shouldGeneratePrimes() throws Exception {

        // when
        Object result = Await.result(primesGenerator.generate(30, 1, 10000), Duration.create("2 seconds"));

        // then
        assertThat(primeNumbersFrom((BitSet)result, 30), equalTo(primesTo30));

    }

    @Test
    public void shouldGeneratePrimesWithDifferentWorkerConfigurations() throws Exception {

        final Duration _2sec = Duration.create("2 seconds");
        final int timeoutMillis = 10000;
        Object result;

        final int number = 30;

        result = Await.result(primesGenerator.generate(number, 1, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        result = Await.result(primesGenerator.generate(number, 2, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        result = Await.result(primesGenerator.generate(number, 4, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));

        result = Await.result(primesGenerator.generate(number, 5, timeoutMillis), _2sec);
        assertThat(primeNumbersFrom((BitSet)result, number), equalTo(primesTo30));
    }
}
