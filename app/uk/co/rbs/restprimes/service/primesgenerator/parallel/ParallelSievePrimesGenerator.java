package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import com.google.inject.Singleton;
import play.libs.Akka;
import scala.concurrent.Future;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveProtocol.GeneratePrimes;

import javax.inject.Inject;

import java.util.BitSet;

import static akka.pattern.Patterns.ask;
import static java.util.Collections.emptyList;

@Singleton
public class ParallelSievePrimesGenerator {

    private ActorSystem actorSystem;

    @Inject
    public ParallelSievePrimesGenerator(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    public Future<Object> generate(Integer n, int numberOfWorkers, int timeoutMillis) {

        if (n < 2) {
            final BitSet result = new BitSet();
            result.set(0);
            return Futures.successful(result);
        }

        ActorRef parallelSieveActor = actorSystem.actorOf(ParallelSieveMasterActor.props(n, numberOfWorkers));

        return ask(parallelSieveActor, new GeneratePrimes(), timeoutMillis);

    }

}
