package uk.co.rbs.restprimes.rest;

import com.wordnik.swagger.annotations.*;
import play.Logger;
import play.libs.Akka;
import play.libs.F.Promise;
import play.mvc.Controller;
import play.mvc.Result;
import uk.co.rbs.restprimes.service.PrimeGeneratorInvoker;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveMasterActor;
import uk.co.rbs.restprimes.service.primesgenerator.parallel.ParallelSieveProtocol.StreamPrimes;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import static play.libs.Json.toJson;
import static play.mvc.Results.StringChunks.whenReady;
import static uk.co.rbs.restprimes.rest.Xml.toXml;
import static uk.co.rbs.restprimes.service.PrimeGeneratorInvoker.*;

@Singleton
@Api(value = "/primes", description = "All the prime numbers up to an including a number provided.")
public class Application extends Controller {

    @Inject
    private PrimeGeneratorInvoker primeGeneratorInvoker;

    @ApiOperation(
            nickname = "generatePrimes",
            value = "Returns the prime numbers up to an including a number provided.",
            notes = "Returns the prime numbers up to an including a number provided.",
            response = PrimesResponse.class,
            produces = "application/json, application/xml",
            httpMethod = "GET")
    @ApiResponses({
            @ApiResponse(code = 400, message = "Invalid input parameters supplied", response = ErrorResponse.class),
            @ApiResponse(code = 406, message = "Invalid Accept Header supplied", response = ErrorResponse.class)
    })
    public Promise<Result> primes(
            @PathParam("n") Integer n,
            @QueryParam("algorithm") @ApiParam(allowableValues = ALGO_NAIVE+","+ALGO_SEQUENTIAL+","+ ALGO_PARALLEL, defaultValue = ALGO_SEQUENTIAL) String algorithm) {

        Logger.info("Received call for " + n);

        if (request().accepts("application/json")) {
            return primeGeneratorInvoker.invoke(n, algorithm).map(primes -> ok(toJson(new PrimesResponse(n, primes))));

        } else if (request().accepts("text/xml") || request().accepts("application/xml")) {
            return primeGeneratorInvoker.invoke(n, algorithm).map(primes -> ok(toXml(new PrimesResponse(n, primes))).as("application/xml"));

        } else {
            throw new UnsupportedMediaTypeException("the media type " + request().getHeader("accept") + " specified in the accept header is not supported");
        }
    }

    public Result primesStream(Integer n) {
        Logger.info("primes stream called");
        return ok(whenReady(out -> {
            Akka.system()
                .actorOf(ParallelSieveMasterActor.props(n, 8))
                .tell(new StreamPrimes(out), null);
        })).as("application/json");
    }

}
