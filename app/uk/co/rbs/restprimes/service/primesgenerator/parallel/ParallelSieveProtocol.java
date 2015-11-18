package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import play.mvc.Results.Chunks.Out;

import java.util.BitSet;

public class ParallelSieveProtocol {

    public static class GeneratePrimes {
    }

    public static class SendResults {
    }

    public static class SegmentResults {
        public int start;
        public int end;
        public BitSet result;
        public SegmentResults(int start, int end, BitSet result) {
            this.start = start;
            this.end = end;
            this.result = result;
        }
    }

    public static class SievingPrimeFound {
        public Integer prime;
        public SievingPrimeFound(Integer prime) {
            this.prime = prime;
        }
    }

    public static class PrimeMultiplesMarkedOff {
        public Integer prime;
        public PrimeMultiplesMarkedOff(Integer prime) {
            this.prime = prime;
        }
    }

    public static class StreamPrimes {
        public Out<String> chunks;
        public StreamPrimes(Out<String> chunks) {
            this.chunks = chunks;
        }
    }

}
