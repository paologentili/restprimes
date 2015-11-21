package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.Props;
import akka.actor.UntypedActor;
import play.Logger;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.PrimeMultiplesMarkedOff;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SegmentResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SendResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.SievingPrimeFound;

import java.util.BitSet;

import static java.util.stream.Collectors.toList;
import static uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.WORKER_ACTOR;
import static uk.co.rbs.restprimes.utils.RestPrimeUtils.primeNumbersFrom;

/**
 * Worker Actor that find prime numbers between the start and the end number.
 * This is to implement the segmented Sieve of Eratosthenes
 * https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes#Segmented_sieve
 */
public class ParallelSieveWorkerActor extends UntypedActor {

    public static final Logger.ALogger LOGGER = Logger.of(WORKER_ACTOR);

    private int start;
    private int end;
    private BitSet primes;

    public static Props props(Integer start, Integer end) {
        return Props.create(ParallelSieveWorkerActor.class, start, end);
    }

    private ParallelSieveWorkerActor(int start, int end) {
        this.start = start;
        this.end = end;
        this.primes = new BitSet();
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof SievingPrimeFound) {

            SievingPrimeFound primeNumberFound = (SievingPrimeFound) message;

            removeMultiples(primeNumberFound.prime);

            sender().tell(new PrimeMultiplesMarkedOff(primeNumberFound.prime), self());
        }

        if (message instanceof SendResults) {
            LOGGER.trace("sending results of ("+start+", "+end+"): " + primeNumbersFrom(primes, end-start).stream().map(i -> i + start).collect(toList()));
            sender().tell(new SegmentResults(start, end, primes), self());
        }
    }

    private void removeMultiples(Integer prime) {

        Integer firstMultipleOfPrime;

        if (start % prime == 0) {
            firstMultipleOfPrime = start;
        } else {
            firstMultipleOfPrime = start + (prime - (start % prime));
        }

        for (int i=firstMultipleOfPrime; i<=end; i+=prime) {
            LOGGER.trace("("+start+", "+end+") - eliminating " + i + " at pos " + (i - start) + " for prime " + prime);
            primes.set(i - start);
        }
    }

}
