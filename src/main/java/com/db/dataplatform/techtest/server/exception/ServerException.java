package com.db.dataplatform.techtest.server.exception;

import org.springframework.web.client.ResourceAccessException;

public class ServerException extends RuntimeException{

    public ServerException(String message) {
        super(message);
    }

    public ServerException(Exception e) {
        super(e);
    }
}
