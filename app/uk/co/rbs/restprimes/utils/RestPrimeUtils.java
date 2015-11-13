package uk.co.rbs.restprimes.utils;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class RestPrimeUtils {

    /**
     * returns a List of Integer containing the indexes of the zero bits in the BitSet until lastIndex
     * @param bitSet
     * @param lastIndex
     * @return
     */
    public static List<Integer> primeNumbersFrom(BitSet bitSet, int lastIndex) {
        List<Integer> result = new ArrayList<>();

        int nextPrimeIdx = 0;
        do {
            nextPrimeIdx = bitSet.nextClearBit(nextPrimeIdx);
            if (nextPrimeIdx<=lastIndex) {
                result.add(nextPrimeIdx);
            }
            nextPrimeIdx++;
        } while (nextPrimeIdx < lastIndex);

        return result;
    }

}
