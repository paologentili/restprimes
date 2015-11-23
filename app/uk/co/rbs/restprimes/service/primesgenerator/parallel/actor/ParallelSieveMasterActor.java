package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.ActorRef;
import akka.actor.IllegalActorStateException;
import akka.actor.UntypedActor;
import akka.routing.Router;
import play.Logger;
import play.mvc.Results.Chunks.Out;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.*;

import javax.inject.Inject;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import static uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.*;

public class ParallelSieveMasterActor extends UntypedActor {

    private static final Logger.ALogger LOGGER = Logger.of(MASTER_ACTOR_NAME);

    private final WorkerActorRouterFactory workerActorRouterFactory;

    @Inject
    public ParallelSieveMasterActor(WorkerActorRouterFactory workerActorRouter) {
        this.workerActorRouterFactory = workerActorRouter;
    }

    private int n;
    private int sqrt;
    private int numberOfWorkers;

    private boolean noMorePrimes;
    private Map<Integer, Integer> pendingEliminations;

    private BitSet primes;

    private ActorRef webClient;
    private Out<String> chunks;

    private Router broadcastRouter;

    private int remainingWorkersBeforeCompletion;

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof StreamPrimes) {
            log(message);

            final StreamPrimes streamPrimes = (StreamPrimes) message;

            // storing the reference to the output stream
            this.chunks = streamPrimes.chunks;

            findPrimes(streamPrimes.n, streamPrimes.numberOfWorkers);
        }

        if (message instanceof GeneratePrimes) {
            log(message);

            final GeneratePrimes generatePrimes = (GeneratePrimes) message;

            // storing the reference of the initial caller to terminate the web request
            this.webClient = sender();

            findPrimes(generatePrimes.n, generatePrimes.numberOfWorkers);
        }

        if (message instanceof PrimeMultiplesMarkedOff) {
            log(message);

            Integer prime = ((PrimeMultiplesMarkedOff) message).prime;

            pendingEliminations.put(prime, pendingEliminations.get(prime)-1);
            if (pendingEliminations.get(prime) <= 0) {
                pendingEliminations.remove(prime);
            }
            LOGGER.trace("remaining tasks for prime " + prime + ": " + pendingEliminations.getOrDefault(prime, 0));

            if (noMorePrimes && pendingEliminations.isEmpty()) {
                broadcastRouter.route(new SendResults(), self());
            }
        }

        if (message instanceof SegmentResults) {
            log(message);

            mergeSegmentResultsAndTerminate((SegmentResults) message);
        }

    }

    private void findPrimes(int n, int numberOfWorkers) {

        init(n, numberOfWorkers);

        // find all primes up to sqrt(n)
        findAndBroadcastSievingPrimes();

        // from now on as soon as the map of pending eliminations is emptied, i can terminate
        this.noMorePrimes = true;
    }

    private void init(Integer n, Integer numberOfWorkers) {

        this.n = n;

        this.numberOfWorkers = numberOfWorkers;

        this.remainingWorkersBeforeCompletion = this.numberOfWorkers;

        this.noMorePrimes = false;
        this.pendingEliminations = new HashMap<>();

        this.sqrt = (int) Math.sqrt(this.n) + 1;

        this.primes = new BitSet(sqrt);

        this.primes.set(0); // 0 is not prime
        this.primes.set(1); // 1 is not prime

        this.broadcastRouter = workerActorRouterFactory.createBroadcastingRouter(sqrt, n, numberOfWorkers);

    }

    private void findAndBroadcastSievingPrimes() {

        for (int i = 2; i <= sqrt; i++) {

            if (!primes.get(i)) {

                LOGGER.debug(getSelf().path().uid() + ": Found a sieving prime: " + i);

                pendingEliminations.put(i, numberOfWorkers);

                broadcastRouter.route(new SievingPrimeFound(i), self());

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
        int segmentStart = results.start;
        int segmentEnd = results.end;
        int segmentSize = segmentEnd - segmentStart + 1;

        for (int i = 0; i < segmentSize; i++) {
            this.primes.set(segmentStart+i, primesFromSegment.get(i));
        }

        this.remainingWorkersBeforeCompletion--;

        if (this.remainingWorkersBeforeCompletion==0) {
            terminate();
        }

    }

    private void terminate() {

        LOGGER.debug(getSelf().path().uid() + ": Computation terminated, sending results to (webClient="+webClient+", chunks="+chunks+")");

        if (this.webClient != null) {
            this.webClient.tell(primes, self());

        } else if (this.chunks != null) {

            // TODO ugly. Use jackson as in http://stackoverflow.com/questions/29802060/java-play-framework-2-3-return-streamed-json-using-jackson and provide solution to stream xml as well

            chunks.write("{\"initial\":"+n+",\"primes\":[");

            boolean isFirst = true;

            for (int i = 0; i < n; i++) {

                if (!primes.get(i)) {

                    if (!isFirst) {
                        chunks.write(",");
                    }

                    chunks.write(String.valueOf(i));

                    isFirst = false;
                }
            }

            chunks.write("]");

            chunks.write("}");

            chunks.close();

            LOGGER.info(getSelf().path().uid() + ": Closed stream");

        } else {
            throw new IllegalActorStateException("no destination to send the results were configured.");
        }
    }

    private void log(Object message) {
        LOGGER.debug(getSelf().path().uid() + ": Received message " + message);
    }

    @Override
    public String toString() {
        return "ParallelSieveMasterActor{" +
                "uid= " + getSelf().path().uid() +
                "n=" + n +
                ", sqrt=" + sqrt +
                ", numberOfWorkers=" + numberOfWorkers +
                '}';
    }

}
