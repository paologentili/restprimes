package uk.co.rbs.restprimes.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

/**
 {
     "Initial": "10",
     "Primes": [
         2,
         3,
         5,
         7
     ]
 }
 */
public class PrimesResponse {

    private Integer initial;
    private List<Integer> primes;

    @JsonCreator(mode = PROPERTIES)
    public PrimesResponse(@JsonProperty("initial") Integer n, @JsonProperty("primes") List<Integer> primes) {
        this.initial = n;
        this.primes = primes;
    }

    public Integer getInitial() {
        return initial;
    }

    public List<Integer> getPrimes() {
        return primes;
    }

//    public PrimesResponse() {
//    }

}
