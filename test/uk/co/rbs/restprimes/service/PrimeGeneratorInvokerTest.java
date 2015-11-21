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

    private PrimeGeneratorInvoker invoker;

    @Before
    public void setupMocks() {
        naivePrimesGenerator = mock(NaivePrimesGenerator.class);
        sequentialPrimesGenerator = mock(SequentialPrimesGenerator.class);
        parallelSievePrimesGenerator = mock(ParallelSievePrimesGenerator.class);
    }

    @Before
    public void setup() {
        invoker = new PrimeGeneratorInvoker(
                naivePrimesGenerator,
                sequentialPrimesGenerator,
                parallelSievePrimesGenerator
        );
    }

    @Test(expected= InvalidParameterException.class)
    public void shouldThrowErrorForUnsupportedAlgorithm() {
        invoker.invoke(10, "unsupported");
    }

    @Test(expected= InvalidParameterException.class)
    public void shouldThrowErrorForInvalidNumber() {
        invoker.invoke(-10, ALGO_NAIVE);
    }

    @Test
    public void shouldInvokeNaiveGenerator() {
        invoker.invoke(10, ALGO_NAIVE);
        verify(naivePrimesGenerator, times(1)).generate(10);
    }

    @Test
    public void shouldInvokeSequentialGenerator() {
        invoker.invoke(10, ALGO_SEQUENTIAL);
        verify(sequentialPrimesGenerator, times(1)).generate(10);
    }

    @Test
    public void shouldInvokeParallelGenerator() {
        when(parallelSievePrimesGenerator.generatePrimes(any(Integer.class), anyInt(), anyInt()))
                .thenReturn(Futures.successful(new BitSet()));

        invoker.invoke(10, ALGO_PARALLEL);

        verify(parallelSievePrimesGenerator, times(1)).generatePrimes(any(Integer.class), anyInt(), anyInt());
    }
}
