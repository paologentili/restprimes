package uk.co.rbs.restprimes.service.primesgenerator.naive;

import com.google.inject.Singleton;

import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Singleton
public class NaivePrimesGenerator {

    public List<Integer> generate(Integer n) {

        return IntStream.range(2, n)
                .parallel()
                .filter(this::isPrime).boxed()
                .collect(toList());

    }

    private boolean isPrime(int n) {
        return n > 1 && IntStream
                .rangeClosed(2, (int) Math.sqrt(n))
                .noneMatch(divisor -> n % divisor == 0);
    }

}
