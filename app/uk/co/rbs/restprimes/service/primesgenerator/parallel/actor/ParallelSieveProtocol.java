package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import play.mvc.Results.Chunks.Out;

import java.util.BitSet;

public class ParallelSieveProtocol {

    public static final String MASTER_ACTOR = "master-actor";
    public static final String WORKER_ACTOR = "worker-actor";

    public static class GeneratePrimes {
        public int n;
        public int numberOfWorkers;
        public GeneratePrimes(int n, int numberOfWorkers) {
            this.n = n;
            this.numberOfWorkers = numberOfWorkers;
        }

        @Override
        public String toString() {
            return "GeneratePrimes{" +
                    "n=" + n +
                    ", numberOfWorkers=" + numberOfWorkers +
                    '}';
        }
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

        @Override
        public String toString() {
            return "SegmentResults{" +
                    "start=" + start +
                    ", end=" + end +
                    '}';
        }
    }

    public static class SievingPrimeFound {
        public Integer prime;
        public SievingPrimeFound(Integer prime) {
            this.prime = prime;
        }

        @Override
        public String toString() {
            return "SievingPrimeFound{" +
                    "prime=" + prime +
                    '}';
        }
    }

    public static class PrimeMultiplesMarkedOff {
        public Integer prime;
        public PrimeMultiplesMarkedOff(Integer prime) {
            this.prime = prime;
        }

        @Override
        public String toString() {
            return "PrimeMultiplesMarkedOff{" +
                    "prime=" + prime +
                    '}';
        }
    }

    public static class StreamPrimes {
        public int n;
        public int numberOfWorkers;
        public Out<String> chunks;
        public StreamPrimes(int n, int numberOfWorkers, Out<String> chunks) {
            this.n = n;
            this.numberOfWorkers = numberOfWorkers;
            this.chunks = chunks;
        }

        @Override
        public String toString() {
            return "StreamPrimes{" +
                    "n=" + n +
                    ", numberOfWorkers=" + numberOfWorkers +
                    '}';
        }
    }

}
