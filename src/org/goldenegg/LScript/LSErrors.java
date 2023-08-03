package org.goldenegg.LScript;

public class LSErrors {
    public static class LSError extends Exception {
        private String message;

        public LSError(String msg) {
            message = msg;
        }

        public void setMessage(String msg) {
            message = msg;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class InvalidTypeException extends LSError {
        public InvalidTypeException() {
            super("");
        }

        public InvalidTypeException(String msg) {
            super(msg);
        }
    };

    public static class InvalidOperationException extends LSError {
        public InvalidOperationException() {
            super("");
        }

        public InvalidOperationException(String msg) {
            super(msg);
        }
    };

    public static class InvalidTokenException extends LSError {
        public InvalidTokenException() {
            super("");
        }

        public InvalidTokenException(String msg) {
            super(msg);
        }
    };

    public static class InvalidEndOfStatementException extends LSError {
        public InvalidEndOfStatementException() {
            super("");
        }

        public InvalidEndOfStatementException(String msg) {
            super(msg);
        }
    };

    public static class InvalidArgumentLengthException extends LSError {
        public InvalidArgumentLengthException() {
            super("");
        }

        public InvalidArgumentLengthException(String msg) {
            super(msg);
        }
    };

    public static class ImportNotFoundException extends LSError {
        public ImportNotFoundException() {
            super("");
        }

        public ImportNotFoundException(String msg) {
            super(msg);
        }
    };

    public static class VariableNotFound extends LSError {
        public VariableNotFound() {
            super("");
        }

        public VariableNotFound(String msg) {
            super(msg);
        }
    };

    public static class ValueIsNull extends LSError {
        public ValueIsNull() {
            super("");
        }

        public ValueIsNull(String msg) {
            super(msg);
        }
    };
}
