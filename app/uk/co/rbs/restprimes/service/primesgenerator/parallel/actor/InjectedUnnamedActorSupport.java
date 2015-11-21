package uk.co.rbs.restprimes.service.primesgenerator.parallel.actor;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import play.libs.akka.InjectedActorSupport;

import java.util.function.Function;
import java.util.function.Supplier;

public interface InjectedUnnamedActorSupport extends InjectedActorSupport {

    default ActorRef injectedUnnamedChild(Supplier<Actor> create) {
        Function<Props, Props> props = Function.identity();
        return context().actorOf(props.apply(Props.create(Actor.class, create::get)));
    }

}
