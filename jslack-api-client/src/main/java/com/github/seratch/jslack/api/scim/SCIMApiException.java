package com.github.seratch.jslack.api.scim;

import com.github.seratch.jslack.SlackConfig;
import com.github.seratch.jslack.api.methods.SlackApiErrorResponse;
import com.github.seratch.jslack.common.json.GsonFactory;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

@Data
@Slf4j
public class SCIMApiException extends Exception {

    private final Response response;
    private final String responseBody;
    private final SCIMApiErrorResponse error;

    public SCIMApiException(Response response, String responseBody) {
        this(SlackConfig.DEFAULT, response, responseBody);
    }

    public SCIMApiException(SlackConfig config, Response response, String responseBody) {
        this(response, responseBody, parse(config, responseBody));
    }

    public SCIMApiException(Response response, String responseBody, SCIMApiErrorResponse error) {
        super(buildErrorMessage(response, error));
        this.response = response;
        this.responseBody = responseBody;
        this.error = error;
    }

    private static String buildErrorMessage(Response response, SCIMApiErrorResponse error) {
        String message = "status: " + response.code();
        if (error != null) {
            return message + ", description: " + error.getErrors().getDescription();
        } else {
            return message + ", no response body";
        }
    }

    private static SCIMApiErrorResponse parse(SlackConfig config, String responseBody) {
        SCIMApiErrorResponse parsedErrorResponse = null;
        try {
            parsedErrorResponse = GsonFactory.createCamelCase(config).fromJson(responseBody, SCIMApiErrorResponse.class);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                String responseToPrint = responseBody.length() > 1000 ? responseBody.subSequence(0, 1000) + " ..." : responseBody;
                log.debug("Failed to parse the error response body: {}", responseToPrint);
            }
        }
        return parsedErrorResponse;
    }

}
