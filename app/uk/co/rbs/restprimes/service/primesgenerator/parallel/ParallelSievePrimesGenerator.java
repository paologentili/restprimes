package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
import com.google.inject.Singleton;
import play.mvc.Results.Chunks.Out;
import scala.concurrent.Future;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveGuiceModule.MasterActorFactory;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.GeneratePrimes;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.StreamPrimes;

import javax.inject.Inject;
import java.util.BitSet;
import java.util.function.Supplier;

import static akka.pattern.Patterns.ask;

@Singleton
public class ParallelSievePrimesGenerator {

    private final ActorSystem actorSystem;
    private final MasterActorFactory masterActorFactory;
    private final Integer numberOfWorkers;

    @Inject
    public ParallelSievePrimesGenerator(ActorSystem actorSystem, MasterActorFactory masterActorFactory, WorkerPolicy workerPolicy) {
        this.actorSystem = actorSystem;
        this.masterActorFactory = masterActorFactory;
        this.numberOfWorkers = workerPolicy.numberOfWorkers();
    }

    /**
     * Produces a list of integer containing the prime numbers using a parallel version of the segmented sieve of eratosthenes
     * @param n
     * @param timeoutMillis
     * @return
     */
    public Future<Object> generatePrimes(Integer n, Integer timeoutMillis) {
        if (n < 2) {
            final BitSet result = new BitSet();
            result.set(0);
            return Futures.successful(result);
        }
        if (numberOfWorkers < 1) {
            throw new IllegalArgumentException("at least one worker is required for this algorithm");
        }
        return ask(newMasterActor(), new GeneratePrimes(n, numberOfWorkers), timeoutMillis);
    }

    /**
     * Streaming version of the parallel version of the segmented sieve of eratosthenes.
     * Pushes primes to the out stream provided as a parameter so that the whole list of primes doesn't have to be contained in memory
     * @param n
     * @param out the output where to send data in chunks
     */
    public void streamPrimes(Integer n, Out<String> out) {
        newMasterActor().tell(new StreamPrimes(n, numberOfWorkers, out), null);
    }

    // ------------------------------------------------------------------------

    // TODO is there a better way to create a different master actor instance from DI, for every request?
    private ActorRef newMasterActor() {
        final Supplier<Actor> actorSupplier = masterActorFactory::create;
        return actorSystem.actorOf(Props.create(Actor.class, actorSupplier::get));
    }

}
