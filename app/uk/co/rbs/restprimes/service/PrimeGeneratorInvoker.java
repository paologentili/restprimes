package uk.co.rbs.restprimes.service;

import com.google.common.collect.Sets;
import com.google.inject.Singleton;
import play.Logger;
import play.libs.F.Promise;
import play.mvc.Results.Chunks.Out;
import uk.co.rbs.restprimes.rest.InvalidParameterException;
import uk.co.rbs.restprimes.service.primesgenerator.naive.NaivePrimesGenerator;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSievePrimesGenerator;
import uk.co.rbs.restprimes.service.primesgenerator.sequential.SequentialPrimesGenerator;

import javax.inject.Inject;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.lang.System.currentTimeMillis;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

@Singleton
public class PrimeGeneratorInvoker {

    public static final int TIMEOUT_MILLIS = 3000000;

    public static final String ALGO_NAIVE = "naive";
    public static final String ALGO_SEQUENTIAL = "sequential";
    public static final String ALGO_PARALLEL = "parallel";

    private NaivePrimesGenerator naivePrimesGenerator;
    private SequentialPrimesGenerator sequentialPrimesGenerator;
    private ParallelSievePrimesGenerator parallelSievePrimesGenerator;

    @Inject
    public PrimeGeneratorInvoker(NaivePrimesGenerator naivePrimesGenerator,
                                 SequentialPrimesGenerator sequentialPrimesGenerator,
                                 ParallelSievePrimesGenerator parallelSievePrimesGenerator)
    {
        this.naivePrimesGenerator = naivePrimesGenerator;
        this.sequentialPrimesGenerator = sequentialPrimesGenerator;
        this.parallelSievePrimesGenerator = parallelSievePrimesGenerator;
    }

    public Set<String> getSupportedAlgorithms() {
        return Sets.newHashSet(ALGO_NAIVE, ALGO_SEQUENTIAL, ALGO_PARALLEL);
    }

    public Promise<List<Integer>> invoke(Integer n, String algorithm) {

        if (n < 0) {
            throw new InvalidParameterException("parameter \"n\" is not valid");
        }

        if (!getSupportedAlgorithms().contains(algorithm)) {
            throw new InvalidParameterException("The algorithm " + algorithm + " is not supported");
        }

        if (n < 2) {
            return Promise.pure(Collections.emptyList());
        }

        Promise<List<Integer>> promise;

        if (ALGO_PARALLEL.equals(algorithm)) {

            final long start = currentTimeMillis();

            int numberOfWorkers = Runtime.getRuntime().availableProcessors() * 3 - 1;

            promise = Promise.wrap(parallelSievePrimesGenerator.generatePrimes(n, numberOfWorkers, TIMEOUT_MILLIS))
                    .map(resp -> {
                        Logger.info("time to calculate prime numbers: " + (currentTimeMillis() - start) + " millis");
                        return resp;
                    })
                    .map(bitSet -> primeNumbersFrom((BitSet)bitSet, n));


        } else if (ALGO_SEQUENTIAL.equals(algorithm)) {
            promise = Promise.promise(() -> sequentialPrimesGenerator.generate(n));

        } else {
            promise = Promise.promise(() -> naivePrimesGenerator.generate(n));
        }

        return promise;

    }

    public void invokeStream(Integer n, Out<String> out) {
        parallelSievePrimesGenerator.streamPrimes(n, out);
    }

}
