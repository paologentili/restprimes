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

    @Inject
    public ParallelSievePrimesGenerator(ActorSystem actorSystem, MasterActorFactory masterActorFactory) {
        this.actorSystem = actorSystem;
        this.masterActorFactory = masterActorFactory;
    }

    public Future<Object> generatePrimes(Integer n, int numberOfWorkers, int timeoutMillis) {

        if (n < 2) {
            final BitSet result = new BitSet();
            result.set(0);
            return Futures.successful(result);
        }

        return ask(newMasterActor(), new GeneratePrimes(n, numberOfWorkers), timeoutMillis);

    }

    public void streamPrimes(Integer n, Out<String> out) {
        newMasterActor().tell(new StreamPrimes(n, 8, out), null);
    }

    private ActorRef newMasterActor() {
        final Supplier<Actor> actorSupplier = masterActorFactory::create;
        return actorSystem.actorOf(Props.create(Actor.class, actorSupplier::get));
    }

}
