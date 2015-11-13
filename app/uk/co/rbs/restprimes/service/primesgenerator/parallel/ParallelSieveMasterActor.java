package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import play.Logger;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveProtocol.*;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

public class ParallelSieveMasterActor extends UntypedActor {

    private static final Logger.ALogger LOGGER = Logger.of("masterActor");

    private int n;
    private int sqrt;
    private int numberOfWorkers;

    private boolean noMorePrimes;
    private Map<Integer, Integer> pendingEliminations;
    private int segmentSize;

    private BitSet primes;

    private ActorRef webClient;

    private Router broadcastWorkers;

    private int remainingWorkersBeforeCompletion;

    private ParallelSieveMasterActor(Integer n, Integer numberOfWorkers) {

        this.n = n;

        this.numberOfWorkers = numberOfWorkers;

        this.remainingWorkersBeforeCompletion = this.numberOfWorkers;

        this.noMorePrimes = false;
        this.pendingEliminations = new HashMap<>();

        this.sqrt = (int) Math.sqrt(this.n) + 1;

        this.segmentSize = (n - sqrt) / numberOfWorkers;

        this.primes = new BitSet(sqrt);
        this.primes.set(0);
        this.primes.set(1);

        this.broadcastWorkers = createWorkersRouter();

        LOGGER.debug("created: " + this);

    }

    public static Props props(Integer n, Integer numberOfWorkers) {
        return Props.create(ParallelSieveMasterActor.class, n, numberOfWorkers);
    }

    @Override
    public String toString() {
        return "ParallelSieveMasterActor{" +
                "n=" + n +
                ", sqrt=" + sqrt +
                ", numberOfWorkers=" + numberOfWorkers +
                ", segmentSize=" + segmentSize +
                '}';
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof GeneratePrimes) {

            LOGGER.debug("received message: GeneratePrimes");

            // storing the reference of the initial caller to terminate the web request
            this.webClient = sender();

            // find all primes up to sqrt(n)
            findAndBroadcastSievingPrimes();

            // from now on as soon as the map of pending eliminations is emptied, i can terminate
            this.noMorePrimes = true;

        }

        if (message instanceof PrimeMultiplesMarkedOff) {

            Integer prime = ((PrimeMultiplesMarkedOff) message).prime;

            pendingEliminations.put(prime, pendingEliminations.get(prime)-1);
            if (pendingEliminations.get(prime) <= 0) {
                pendingEliminations.remove(prime);
            }
            LOGGER.trace("remaining tasks for prime " + prime + ": " + pendingEliminations.getOrDefault(prime, 0));

            if (noMorePrimes && pendingEliminations.isEmpty()) {
                broadcastWorkers.route(new SendResults(), self());
            }
        }

        if (message instanceof SegmentResults) {

            mergeSegmentResultsAndTerminate((SegmentResults) message);
        }

    }

    private void findAndBroadcastSievingPrimes() {

        for (int i = 2; i <= sqrt; i++) {

            if (!primes.get(i)) {

                LOGGER.debug("found a sieving prime: " + i);

                pendingEliminations.put(i, numberOfWorkers);

                broadcastWorkers.route(new SievingPrimeFound(i), self());

                int multipleIndex = i + i;

                while (multipleIndex <= sqrt) {
                    primes.set(multipleIndex, true);
                    multipleIndex += i;
                }
            }
        }
    }

    private void mergeSegmentResultsAndTerminate(SegmentResults results) {

        BitSet primesFromSegment = results.result;
        int start = results.start;
        int end = results.end;
        int segmentSize = end - start + 1;

//        LOGGER.debug("merging " + primeNumbersFrom(primes, segmentSize) + " with" + primeNumbersFrom(primesFromSegment, segmentSize).stream().map(i -> i + start).collect(toList()));

        for (int i = 0; i < segmentSize; i++) {
//            LOGGER.debug("setting " + (start+i) + "th bit to " + primesFromSegment.get(i));
            primes.set(start+i, primesFromSegment.get(i));
        }

//        LOGGER.debug("merged result: " + primeNumbersFrom(primes, segmentSize));

        this.remainingWorkersBeforeCompletion--;

        if (this.remainingWorkersBeforeCompletion==0) {
            terminate();
        }

    }

    private void terminate() {
        if (this.webClient != null) {
            this.webClient.tell(primes, self());
        }
    }

    private Router createWorkersRouter() {

        List<Routee> workers = IntStream.rangeClosed(1, numberOfWorkers).mapToObj(i -> {

            int segmentStart = sqrt + ((i-1) * segmentSize) + 1;
            int segmentEnd = i == numberOfWorkers ? n : segmentStart + segmentSize - 1;

            LOGGER.debug(i + ": creating worker for ("+segmentStart+", "+segmentEnd+")");

            ActorRef workerActor = getContext().actorOf(ParallelSieveWorkerActor.props(segmentStart, segmentEnd));
            getContext().watch(workerActor);

            return new ActorRefRoutee(workerActor);

        }).collect(toList());

        return new Router(new BroadcastRoutingLogic(), workers);
    }

}
