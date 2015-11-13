package uk.co.rbs.restprimes.rest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import play.mvc.Result;

import javax.xml.bind.annotation.XmlRootElement;

import static com.fasterxml.jackson.annotation.JsonCreator.Mode.PROPERTIES;

@XmlRootElement(name = "error")
public class ErrorResponse {

    private int httpStatus;
    private String description;

    @JsonCreator(mode = PROPERTIES)
    public ErrorResponse(@JsonProperty("status") int httpStatus, @JsonProperty("description") String description) {
        this.httpStatus = httpStatus;
        this.description = description;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getDescription() {
        return description;
    }

}
