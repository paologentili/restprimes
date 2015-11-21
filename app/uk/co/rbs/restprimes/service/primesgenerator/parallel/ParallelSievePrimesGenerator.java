package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.Futures;
import com.google.inject.Singleton;
import scala.concurrent.Future;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveGuiceModule.MasterActorFactory;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.actor.ParallelSieveProtocol.GeneratePrimes;

import javax.inject.Inject;
import java.util.BitSet;
import java.util.function.Supplier;

import static akka.pattern.Patterns.ask;

@Singleton
public class ParallelSievePrimesGenerator {

    private final ActorSystem actorSystem;
    private final MasterActorFactory masterActorFactory;

    @Inject
    public ParallelSievePrimesGenerator(ActorSystem actorSystem, MasterActorFactory masterActorFactory) {
        this.actorSystem = actorSystem;
        this.masterActorFactory = masterActorFactory;
    }

    public Future<Object> generate(Integer n, int numberOfWorkers, int timeoutMillis) {

        if (n < 2) {
            final BitSet result = new BitSet();
            result.set(0);
            return Futures.successful(result);
        }

        final Supplier<Actor> actorSupplier = masterActorFactory::create;

        final ActorRef masterActor = actorSystem.actorOf(Props.create(Actor.class, actorSupplier::get));

        return ask(masterActor, new GeneratePrimes(n, numberOfWorkers), timeoutMillis);

    }

}
