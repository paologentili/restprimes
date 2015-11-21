package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.UntypedActor;
import com.google.inject.assistedinject.Assisted;
import play.Logger;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.PrimeMultiplesMarkedOff;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SegmentResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SendResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SievingPrimeFound;

import javax.inject.Inject;
import java.util.BitSet;

import static java.util.stream.Collectors.toList;
import static uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.WORKER_ACTOR;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

public class ParallelSieveWorkerActor extends UntypedActor {

    private static final Logger.ALogger LOGGER = Logger.of(WORKER_ACTOR);

    private final Integer segmentStart;
    private final Integer segmentEnd;

    private final BitSet primes;

    @Inject
    public ParallelSieveWorkerActor(@Assisted("segmentStart") Integer segmentStart, @Assisted("segmentEnd") Integer segmentEnd) {

        this.segmentStart = segmentStart;
        this.segmentEnd = segmentEnd;

        this.primes = new BitSet();
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof SievingPrimeFound) {
            log(message);

            SievingPrimeFound primeNumberFound = (SievingPrimeFound) message;

            removeMultiples(primeNumberFound.prime);

            sender().tell(new PrimeMultiplesMarkedOff(primeNumberFound.prime), self());
        }

        if (message instanceof SendResults) {
            log(message);

            LOGGER.trace("sending results of ("+segmentStart+", "+segmentEnd+"): " + primeNumbersFrom(primes, segmentEnd-segmentStart).stream().map(i -> i + segmentStart).collect(toList()));

            sender().tell(new SegmentResults(segmentStart, segmentEnd, primes), self());
        }
    }

    private void removeMultiples(Integer prime) {

        Integer firstMultipleOfPrime;

        if (segmentStart % prime == 0) {
            firstMultipleOfPrime = segmentStart;
        } else {
            firstMultipleOfPrime = segmentStart + (prime - (segmentStart % prime));
        }

        for (int i=firstMultipleOfPrime; i<=segmentEnd; i+=prime) {
            LOGGER.trace("("+segmentStart+", "+segmentEnd+") - eliminating " + i + " at pos " + (i - segmentStart) + " for prime " + prime);
            primes.set(i - segmentStart);
        }
    }

    private void log(Object message) {
        LOGGER.debug(Thread.currentThread().getName() + ": received message " + message);
    }
}
