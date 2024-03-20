package com.adamcalculator.dynamicpack.util;

public class FailedOpenPackFileSystemException extends RuntimeException {
    public FailedOpenPackFileSystemException() {
    }

    public FailedOpenPackFileSystemException(String message) {
        super(message);
    }

    public FailedOpenPackFileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailedOpenPackFileSystemException(Throwable cause) {
        super(cause);
    }

    public FailedOpenPackFileSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
