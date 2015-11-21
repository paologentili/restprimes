package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import play.Logger;
import play.libs.akka.InjectedActorSupport;
import play.mvc.Results.Chunks.Out;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.*;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.*;

public class ParallelSieveMasterActor extends UntypedActor implements InjectedActorSupport {

    private static final Logger.ALogger LOGGER = Logger.of(MASTER_ACTOR);

    private int n;
    private int sqrt;
    private int numberOfWorkers;

    private boolean noMorePrimes;
    private Map<Integer, Integer> pendingEliminations;
    private int segmentSize;

    private BitSet primes;

    private ActorRef webClient;
    private Out<String> chunks;

    private Router broadcastWorkers;

    private int remainingWorkersBeforeCompletion;

    private void init(Integer n, Integer numberOfWorkers) {

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

        this.broadcastWorkers = createWorkersRouter(sqrt, n, segmentSize, numberOfWorkers);

        LOGGER.debug("created: " + this);

    }

    private Router createWorkersRouter(int sqrt, int n, int segmentSize, int numberOfWorkers) {

        List<Routee> workers = IntStream
                .rangeClosed(1, numberOfWorkers)
                .mapToObj(i -> getActorRefRoutee(i, sqrt, n, segmentSize, numberOfWorkers))
                .collect(toList());

        return new Router(new BroadcastRoutingLogic(), workers);
    }

    private ActorRefRoutee getActorRefRoutee(int i, int sqrt, int n, int segmentSize, int numberOfWorkers) {

        int segmentStart = sqrt + ((i-1) * segmentSize) + 1;
        int segmentEnd = i == numberOfWorkers ? n : segmentStart + segmentSize - 1;

        LOGGER.debug(i + ": creating worker for ("+segmentStart+", "+segmentEnd+")");

        ActorRef workerActor = getContext().actorOf(ParallelSieveWorkerActor.props(segmentStart, segmentEnd));
        getContext().watch(workerActor);

        return new ActorRefRoutee(workerActor);
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

        if (message instanceof StreamPrimes) {

            LOGGER.debug("received message: StreamPrimes");

            final StreamPrimes streamPrimes = (StreamPrimes) message;
            this.chunks = streamPrimes.chunks;

            findPrimes(streamPrimes.n, streamPrimes.numberOfWorkers);
        }

        if (message instanceof GeneratePrimes) {

            LOGGER.debug("received message: GeneratePrimes");

            // storing the reference of the initial caller to terminate the web request
            this.webClient = sender();

            GeneratePrimes generatePrimes = (GeneratePrimes) message;

            findPrimes(generatePrimes.n, generatePrimes.numberOfWorkers);

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

    private void findPrimes(int n, int numberOfWorkers) {

        init(n, numberOfWorkers);

        // find all primes up to sqrt(n)
        findAndBroadcastSievingPrimes();

        // from now on as soon as the map of pending eliminations is emptied, i can terminate
        this.noMorePrimes = true;
    }

    private void findAndBroadcastSievingPrimes() {

        for (int i = 2; i <= sqrt; i++) {

            if (!primes.get(i)) {

                LOGGER.debug("found a sieving prime: " + i);

                pendingEliminations.put(i, numberOfWorkers);

                broadcastWorkers.route(new ParallelSieveProtocol.SievingPrimeFound(i), self());

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

        for (int i = 0; i < segmentSize; i++) {
            primes.set(start+i, primesFromSegment.get(i));
        }

        this.remainingWorkersBeforeCompletion--;

        if (this.remainingWorkersBeforeCompletion==0) {
            terminate();
        }

    }

    private void terminate() {

        if (this.webClient != null) {
            this.webClient.tell(primes, self());

        } else {

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

            LOGGER.info("closed stream");
        }
    }

}
