package uk.co.rbs.restprimes.rest;

import com.thoughtworks.xstream.XStream;

public class Xml {

    private static XStream xStream = new XStream();

    public static String toXml(PrimesResponse primesResponse) {
        xStream.alias("primes", PrimesResponse.class);
        return xStream.toXML(primesResponse);
    }

    public static String toXml(ErrorResponse errorResponse) {
        xStream.alias("error", ErrorResponse.class);
        return xStream.toXML(errorResponse);
    }

}
