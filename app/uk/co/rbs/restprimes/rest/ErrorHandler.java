package uk.co.rbs.restprimes.rest;


import play.Configuration;
import play.Environment;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.Http.RequestHeader;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.xml.bind.JAXBException;

import static play.mvc.Results.status;
import static uk.co.rbs.restprimes.rest.Xml.toXml;

public class ErrorHandler extends DefaultHttpErrorHandler {

    @Inject
    public ErrorHandler(
            Configuration configuration,
            Environment environment,
            OptionalSourceMapper sourceMapper,
            Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
    }

    @Override
    protected Promise<Result> onDevServerError(RequestHeader request, UsefulException exception) {
        return allEnvServerError(request, exception);
    }

    @Override
    protected Promise<Result> onProdServerError(RequestHeader request, UsefulException exception) {
        return allEnvServerError(request, exception);
    }

    private Promise<Result> allEnvServerError(RequestHeader request, UsefulException exception) {

        if (exception.getCause()!= null && exception.getCause() instanceof UnsupportedMediaTypeException) {
            return Promise.pure(errorResponse(request, 406, exception.getCause().getMessage()));
        }

        if (exception.getCause()!= null && exception.getCause() instanceof InvalidParameterException) {
            return Promise.pure(errorResponse(request, 400, exception.getCause().getMessage()));
        }

        return Promise.pure(errorResponse(request, 500, exception.getMessage()));
    }

    @Override
    protected Promise<Result> onNotFound(RequestHeader request, String message) {
        return Promise.pure(errorResponse(request, 404, message));
    }

    @Override
    protected Promise<Result> onBadRequest(RequestHeader request, String message) {
        return Promise.pure(errorResponse(request, 400, message));
    }

    private Result errorResponse(RequestHeader request, int statusCode, String message) {

        final ErrorResponse errorResponse = new ErrorResponse(statusCode, message);

        if (request.accepts("application/json")) {
            // this handles Accept = */*
            return status(statusCode, Json.toJson(errorResponse)).as("application/json");

        } else if (request.accepts("application/xml") || request.accepts("text/xml")) {
            return status(statusCode, toXml(errorResponse)).as("application/xml");

        } else {
            return status(statusCode, Json.toJson(errorResponse)).as("application/json");
        }
    }

}