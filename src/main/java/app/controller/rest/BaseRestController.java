package app.controller.rest;

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
}
