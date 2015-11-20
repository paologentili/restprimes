package uk.co.rbs.restprimes.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.xstream.XStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import play.test.TestServer;
import uk.co.rbs.restprimes.rest.ErrorResponse;
import uk.co.rbs.restprimes.rest.PrimesResponse;
import uk.co.rbs.restprimes.rest.Xml;
import uk.co.rbs.restprimes.service.PrimeGeneratorInvoker;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static play.test.Helpers.*;
import static uk.co.rbs.restprimes.service.PrimeGeneratorInvoker.ALGO_NAIVE;
import static uk.co.rbs.restprimes.service.PrimeGeneratorInvoker.ALGO_PARALLEL;
import static uk.co.rbs.restprimes.service.PrimeGeneratorInvoker.ALGO_SEQUENTIAL;

public class IntegrationTest {

    protected static final int RESPONSE_WAIT_TIMEOUT = 30000;

    protected static final int testPlayServerPort = 3333;
    protected static String URL_API = "http://localhost:" + testPlayServerPort;

    protected static final ObjectMapper om = new ObjectMapper();

    protected static TestServer testPlayServer;

    private static final String NOT_ACCEPTABLE_MEDIA_TYPE = "application/pdf";

    private static String primesResponseFor30;

    @BeforeClass
    public static void startupTestApp() throws Exception {
        testPlayServer = testServer(testPlayServerPort, fakeApplication());
        testPlayServer.start();

        primesResponseFor30 = om.writeValueAsString(new PrimesResponse(30, Arrays.asList(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)));
    }

    @AfterClass
    public static void stopTestApp() throws Exception {
        testPlayServer.stop();
    }

    // ------------------------------------------------------------------------

    @Test
    public void indexEndpointShouldReturnPrimeNumbersInJsonForNaiveAlgorithm() throws Exception {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/30")
                .setQueryParameter("algorithm", ALGO_NAIVE)
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));
        assertThat(wsResponse.getBody(), equalTo(primesResponseFor30));

    }

    @Test
    public void indexEndpointShouldReturnPrimeNumbersInJsonForSequentialAlgorithm() throws Exception {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/30")
                .setQueryParameter("algorithm", ALGO_SEQUENTIAL)
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));
        assertThat(wsResponse.getBody(), equalTo(primesResponseFor30));

    }

    @Test
    public void indexEndpointShouldReturnPrimeNumbersInJsonForParallelAlgorithm() throws Exception {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/30")
                .setQueryParameter("algorithm", ALGO_PARALLEL)
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));
        assertThat(wsResponse.getBody(), equalTo(primesResponseFor30));

    }

    @Test
    public void testStreaming() throws Exception {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/stream/30")
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));
        assertThat(wsResponse.getBody(), equalTo(primesResponseFor30));
    }

    @Test
    public void indexEndpointShouldReturnPrimeNumbersInJsonForDefaultAlgorithm() throws Exception {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/30")
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));
        assertThat(wsResponse.getBody(), equalTo(primesResponseFor30));

    }

    @Test
    public void indexEndpointShouldReturnPrimeNumbersInXml() {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/3")
                .setHeader("Accept", "application/xml")
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/xml"));

        assertThat(wsResponse.getBody().replaceAll(" ", "").replaceAll("\n", ""),
                equalTo("<primes><initial>3</initial><primes><int>2</int></primes></primes>"));
    }

    @Test
    public void algorithmParameterIsNotRequired() {
        final WSResponse wsResponse = WS.url(URL_API + "/primes/30")
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));
    }

    @Test
    public void shouldReturn400ForInvalidInitialNumberN() throws IOException {

        // when
        WSResponse wsResponse = WS.url(URL_API + "/primes/-30").get().get(RESPONSE_WAIT_TIMEOUT);

        // then
        assertThat(wsResponse.getStatus(), equalTo(BAD_REQUEST));
        assertThat(om.readValue(wsResponse.getBody(), ErrorResponse.class).getHttpStatus(), equalTo(400));

        // when
        wsResponse = WS.url(URL_API + "/primes/dwfshgew").get().get(RESPONSE_WAIT_TIMEOUT);

        // then
        assertThat(wsResponse.getStatus(), equalTo(BAD_REQUEST));
        assertThat(om.readValue(wsResponse.getBody(), ErrorResponse.class).getHttpStatus(), equalTo(400));
    }

    @Test
    public void shouldReturnErrorForUnsupportedContentTypes() {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/30")
                .setHeader("Accept", NOT_ACCEPTABLE_MEDIA_TYPE)
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(NOT_ACCEPTABLE));

        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));

    }

    @Test
    public void shouldSupportXmlErrorResponses() {

        final WSResponse wsResponse = WS.url(URL_API + "/invalid-endpoint")
                .setHeader("Accept", "text/xml")
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(NOT_FOUND));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/xml"));
        assertThat(wsResponse.getBody().replaceAll(" ", "").replaceAll("\n", ""),
                equalTo("<error><httpStatus>404</httpStatus><description></description></error>"));

    }

    @Test
    public void shouldReturnErrorForUnsupportedAlgorithmParameter() {

        final WSResponse wsResponse = WS.url(URL_API + "/primes/30")
                .setQueryParameter("algorithm", "atkin-sieve")
                .get()
                .get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(BAD_REQUEST));
        assertThat(wsResponse.getHeader(Http.HeaderNames.CONTENT_TYPE), containsString("application/json"));

    }

    @Test
    public void shouldExposeSwaggerResourcesEndpoint() throws IOException {
        final WSResponse wsResponse = WS.url(URL_API + "/api-docs").get().get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertTrue(om.readValue(wsResponse.getBody(), Map.class).containsKey("apis"));
    }

    @Test
    public void shouldExposeSwaggerPrimesResourceEndpoint() throws IOException {
        final WSResponse wsResponse = WS.url(URL_API + "/api-docs/primes").get().get(RESPONSE_WAIT_TIMEOUT);

        assertThat(wsResponse.getStatus(), equalTo(OK));
        assertThat(om.readValue(wsResponse.getBody(), Map.class).get("resourcePath"), equalTo("/primes"));
    }

}
