package app.controller.rest;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseRestController {

    protected static class SimpleResult {
        public SimpleResult(Status status, Object info) {
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

        enum Status {Ok, Error}
    }

    protected Map<String, String> toMap(String... args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
    }
}
