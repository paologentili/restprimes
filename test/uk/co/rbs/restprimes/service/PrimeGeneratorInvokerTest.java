package uk.co.rbs.restprimes.service;

import akka.dispatch.Futures;
import org.junit.Before;
import org.junit.Test;
import uk.co.rbs.restprimes.rest.InvalidParameterException;
import uk.co.rbs.restprimes.service.primesgenerator.naive.NaivePrimesGenerator;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSievePrimesGenerator;
import uk.co.rbs.restprimes.service.primesgenerator.sequential.SequentialPrimesGenerator;

import java.util.BitSet;

import static org.mockito.Mockito.*;
import static uk.co.rbs.restprimes.service.PrimeGeneratorInvoker.ALGO_NAIVE;
import static uk.co.rbs.restprimes.service.PrimeGeneratorInvoker.ALGO_PARALLEL;
import static uk.co.rbs.restprimes.service.PrimeGeneratorInvoker.ALGO_SEQUENTIAL;

public class PrimeGeneratorInvokerTest {

    private NaivePrimesGenerator naivePrimesGenerator;
    private SequentialPrimesGenerator sequentialPrimesGenerator;
    private ParallelSievePrimesGenerator parallelSievePrimesGenerator;

    private PrimeGeneratorInvoker primeGeneratorInvoker;

    @Before
    public void setupMocks() {
        naivePrimesGenerator = mock(NaivePrimesGenerator.class);
        sequentialPrimesGenerator = mock(SequentialPrimesGenerator.class);
        parallelSievePrimesGenerator = mock(ParallelSievePrimesGenerator.class);
    }

    @Before
    public void setup() {
        primeGeneratorInvoker = new PrimeGeneratorInvoker(
                naivePrimesGenerator,
                sequentialPrimesGenerator,
                parallelSievePrimesGenerator
        );
    }

    @Test(expected=InvalidParameterException.class)
    public void shouldThrowErrorForUnsupportedAlgorithm() {
        primeGeneratorInvoker.invoke(10, "unsupported");
    }

    @Test(expected=InvalidParameterException.class)
    public void shouldThrowErrorForInvalidNumber() {
        primeGeneratorInvoker.invoke(-10, ALGO_NAIVE);
    }

    @Test
    public void shouldInvokeNaiveGenerator() {
        primeGeneratorInvoker.invoke(10, ALGO_NAIVE);
        verify(naivePrimesGenerator, timeout(1000).times(1)).generate(10);
    }

    @Test
    public void shouldInvokeSequentialGenerator() {
        primeGeneratorInvoker.invoke(10, ALGO_SEQUENTIAL);
        verify(sequentialPrimesGenerator, timeout(1000).times(1)).generate(10);
    }

    @Test
    public void shouldInvokeParallelGenerator() {
        when(parallelSievePrimesGenerator.generatePrimes(any(Integer.class), any(Integer.class)))
                .thenReturn(Futures.successful(new BitSet()));

        primeGeneratorInvoker.invoke(10, ALGO_PARALLEL);

        verify(parallelSievePrimesGenerator, timeout(1000).times(1)).generatePrimes(any(Integer.class), any(Integer.class));
    }
}
