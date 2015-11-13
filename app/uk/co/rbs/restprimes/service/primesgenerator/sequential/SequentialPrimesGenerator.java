package uk.co.rbs.restprimes.service.primesgenerator.sequential;

import java.util.BitSet;
import java.util.List;

import static java.util.Collections.emptyList;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

public class SequentialPrimesGenerator {

    public List<Integer> generate(Integer n) {

        if (n < 2) {
            return emptyList();
        }

        BitSet bitSet = new BitSet();
        bitSet.set(0);
        bitSet.set(1);

        for (int i = 2; i <= n; i++) {

            if (!bitSet.get(i)) {

                int multipleIndex = i + i;

                while (multipleIndex < n) {
                    bitSet.set(multipleIndex, true);
                    multipleIndex += i;
                }
            }
        }

        return primeNumbersFrom(bitSet, n);
    }


}
