package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import com.google.inject.Singleton;
import scala.concurrent.Future;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.GeneratePrimes;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.BitSet;

import static akka.pattern.Patterns.ask;
import static uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.MASTER_ACTOR;

@Singleton
public class ParallelSievePrimesGenerator {

    @Inject
    @Named(MASTER_ACTOR)
    private ActorRef masterActor;

    public Future<Object> generate(Integer n, int numberOfWorkers, int timeoutMillis) {

        if (n < 2) {
            final BitSet result = new BitSet();
            result.set(0);
            return Futures.successful(result);
        }

        return ask(masterActor, new GeneratePrimes(n, numberOfWorkers), timeoutMillis);

    }

}
