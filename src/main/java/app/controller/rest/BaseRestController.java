package app.controller.rest;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import app.enums.AuditEventTypes;
import app.enums.OperationTypes;
import app.service.AuditService;

public abstract class BaseRestController {
    @Autowired
    private AuditService auditService;

    protected static class SimpleResult {
        public static final SimpleResult Ok = new SimpleResult(Status.Ok, null);
        public static final SimpleResult UnknownError = new SimpleResult(Status.Ok, "Unknown error");

        private SimpleResult(Status status, Object info) {
            this.status = status;
            if (Status.Ok.equals(status)) {
                this.response = info;
            } else {
                this.error = info;
            }
        }

        Status status;
        Object error;
        Object response;

        public Status getStatus() {
            return status;
        }

        public Object getError() {
            return error;
        }

        public Object getResponse() {
            return response;
        }

        public static SimpleResult createOk(Object info) {
            SimpleResult result = new SimpleResult(Status.Ok, info);
            return result;
        }

        public static SimpleResult createError(Object info) {
            SimpleResult result = new SimpleResult(Status.Error, info);
            return result;
        }

        private static enum Status {Ok, Error}
    }

    protected Map<String, String> toMap(String... args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
    }

    protected SimpleResult processMissingParameter(Logger logger, String parameter, OperationTypes operation) {
        String message = "Error due to publish filePlaceHolder with empty parameter '" + parameter + "'";
        logger.error(message);
        AuditEventTypes event = AuditEventTypes.SUSPICIOUS_ACTIVITY;
        auditService.log(operation, event, parameter +  " is null or empty");

        message = "Required parameter '" + parameter + "' is missing";
        SimpleResult result = SimpleResult.createError(message);
        return result;
    }
}
