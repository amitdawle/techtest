package com.db.dataplatform.techtest.server.exception;

public class DataLakeException extends RuntimeException {

    public DataLakeException(Exception e) {
        super(e);
    }
}
