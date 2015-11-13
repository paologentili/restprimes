package uk.co.rbs.restprimes.service.primesgenerator.parallel;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveProtocol.PrimeMultiplesMarkedOff;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveProtocol.SegmentResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveProtocol.SendResults;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveProtocol.SievingPrimeFound;

import static akka.pattern.Patterns.ask;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class ParallelSieveWorkerActorTest {

    private static ActorSystem actorSystem;

    @BeforeClass
    public static void setup() {
        actorSystem = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        actorSystem.shutdown();
        actorSystem.awaitTermination(Duration.create("10 seconds"));
    }

    @Test
    public void workerActorMarksOffMultipleOfSievingPrimesAndReturnsTheSegmentBitMap() throws Exception {

        // given
        int start = 10;
        int end = 19;
        int sievingPrime = 3;
        int sievingPrime5 = 5;

        final Props props = Props.create(ParallelSieveWorkerActor.class, start, end);
        final TestActorRef<ParallelSieveWorkerActor> actorRef = TestActorRef.create(actorSystem, props, "testA");

        // ----- when
        Future<Object> future = ask(actorRef, new SievingPrimeFound(sievingPrime), 1000);

        // then
        Object responseMessage = Await.result(future, Duration.Zero());
        assertThat(((PrimeMultiplesMarkedOff)responseMessage).prime, equalTo(sievingPrime));

        // ----- when
        ask(actorRef, new SievingPrimeFound(sievingPrime5), 1000);

        // ----- and
        future = ask(actorRef, new SendResults(), 1000);

        // then
        responseMessage = Await.result(future, Duration.Zero());
        assertTrue(responseMessage instanceof SegmentResults);

        // and
        SegmentResults segmentResults = (SegmentResults) responseMessage;
        assertThat(segmentResults.start, equalTo(start));
        assertThat(segmentResults.end, equalTo(end));

        // and: only multiple of of the sieving prime sent in the first message (3) are set to true
        assertThat(segmentResults.result.get(0), equalTo(true));     // 10
        assertThat(segmentResults.result.get(1), equalTo(false));    // 11
        assertThat(segmentResults.result.get(2), equalTo(true));     // 12
        assertThat(segmentResults.result.get(3), equalTo(false));    // 13
        assertThat(segmentResults.result.get(4), equalTo(false));    // 14
        assertThat(segmentResults.result.get(5), equalTo(true));     // 15
        assertThat(segmentResults.result.get(6), equalTo(false));    // 16
        assertThat(segmentResults.result.get(7), equalTo(false));    // 17
        assertThat(segmentResults.result.get(8), equalTo(true));     // 18
        assertThat(segmentResults.result.get(9), equalTo(false));    // 19

    }

}
