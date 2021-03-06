package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import play.Logger;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.PrimeMultiplesMarkedOff;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SegmentResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SendResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SievingPrimeFound;

import java.util.BitSet;

import static java.util.stream.Collectors.toList;
import static uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.WORKER_ACTOR_NAME;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

public class ParallelSieveWorkerActor extends AbstractActor {

    private static final Logger.ALogger LOGGER = Logger.of(WORKER_ACTOR_NAME);

    private final Integer segmentStart;
    private final Integer segmentEnd;

    private final BitSet primes;

    public ParallelSieveWorkerActor(Integer segmentStart, Integer segmentEnd) {

        this.segmentStart = segmentStart;
        this.segmentEnd = segmentEnd;

        this.primes = new BitSet();

        receive(ReceiveBuilder
                .match(SievingPrimeFound.class, this::sievingPrimeFoundMsgHandler)
                .match(SendResults.class, this::sendResultsMsgHandler)
                .build()

        );
    }

    private void sendResultsMsgHandler(SendResults message) {
        log(message);
        LOGGER.trace("sending results of ("+segmentStart+", "+segmentEnd+"): " + primeNumbersFrom(primes, segmentEnd-segmentStart).stream().map(i -> i + segmentStart).collect(toList()));
        sender().tell(new SegmentResults(segmentStart, segmentEnd, primes), self());
    }

    private void sievingPrimeFoundMsgHandler(SievingPrimeFound message) {
        log(message);
        removeMultiples(message.prime);
        sender().tell(new PrimeMultiplesMarkedOff(message.prime), self());
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
