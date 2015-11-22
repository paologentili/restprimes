package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import play.mvc.Results.Chunks.Out;

import java.util.BitSet;

public class ParallelSieveProtocol {

    public static final String MASTER_ACTOR_NAME = "master-actor";
    public static final String WORKER_ACTOR_NAME = "worker-actor";

    // ------------------------------------------------------------------------

    // client --> main
    public static class GeneratePrimes {
        public final int n;
        public final int numberOfWorkers;
        public GeneratePrimes(int n, int numberOfWorkers) {
            this.n = n;
            this.numberOfWorkers = numberOfWorkers;
        }
        public String toString() {
            return "GeneratePrimes{n=" + n + ", numberOfWorkers=" + numberOfWorkers + "}";
        }
    }

    // client --> main
    public static class StreamPrimes {
        public final int n;
        public final int numberOfWorkers;
        public final Out<String> chunks;
        public StreamPrimes(int n, int numberOfWorkers, Out<String> chunks) {
            this.n = n;
            this.numberOfWorkers = numberOfWorkers;
            this.chunks = chunks;
        }
        public String toString() {
            return "StreamPrimes{n=" + n + ", numberOfWorkers=" + numberOfWorkers + "}";
        }
    }

    // ------------------------------------------------------------------------

    // main --> worker
    public static class SievingPrimeFound {
        public final Integer prime;
        public SievingPrimeFound(Integer prime) {
            this.prime = prime;
        }
        public String toString() {
            return "SievingPrimeFound{prime=" + prime + "}";
        }
    }

    // worker --> main
    public static class PrimeMultiplesMarkedOff {
        public final Integer prime;
        public PrimeMultiplesMarkedOff(Integer prime) {
            this.prime = prime;
        }
        public String toString() {
            return "PrimeMultiplesMarkedOff{prime=" + prime + "}";
        }
    }

    // ------------------------------------------------------------------------

    // main --> worker
    public static class SendResults {}

    // worker --> main
    public static class SegmentResults {
        public final int start;
        public final int end;
        public final BitSet result;
        public SegmentResults(int start, int end, BitSet result) {
            this.start = start;
            this.end = end;
            this.result = result;
        }
        public String toString() {
            return "SegmentResults{start=" + start + ", end=" + end + "}";
        }
    }

}
