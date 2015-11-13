package uk.co.rbs.restprimes.utils;

import org.junit.Test;

import java.util.BitSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

public class RestPrimesUtilsTest {

    @Test
    public void shouldConvertBitMapToListOfPrimeNumbers() {

        BitSet bitSet = new BitSet();

        bitSet.clear();
        assertThat(primeNumbersFrom(bitSet, 0), equalTo(singletonList(0)));
        assertThat(primeNumbersFrom(bitSet, 3), equalTo(asList(0, 1, 2)));

        bitSet.set(0);
        assertThat(primeNumbersFrom(bitSet, 3), equalTo(asList(1, 2)));

        bitSet.set(1);
        assertThat(primeNumbersFrom(bitSet, 3), equalTo(singletonList(2)));

        bitSet.set(5);
        assertThat(primeNumbersFrom(bitSet, 5), equalTo(asList(2, 3, 4)));
    }
}
