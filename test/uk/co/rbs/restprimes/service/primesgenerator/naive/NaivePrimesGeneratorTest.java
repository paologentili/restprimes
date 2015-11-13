package uk.co.rbs.restprimes.service.primesgenerator.naive;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NaivePrimesGeneratorTest {

    final NaivePrimesGenerator primesGenerator = new NaivePrimesGenerator();

    @Test
    public void shouldReturnEmptyListForZeroAndOne() {

        // when
        List<Integer> primes = primesGenerator.generate(0);

        // then
        assertThat(primes, equalTo(emptyList()));

        // when
        primes = primesGenerator.generate(1);

        // then
        assertThat(primes, equalTo(emptyList()));
    }


    @Test
    public void shouldGeneratePrimes() {

        // when
        final List<Integer> primes = primesGenerator.generate(30);

        // then
        assertThat(primes, equalTo(Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)));

    }

}
