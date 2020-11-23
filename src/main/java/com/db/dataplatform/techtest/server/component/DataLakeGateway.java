package com.db.dataplatform.techtest.server.component;

public interface DataLakeGateway {

    // Instead of boolean, the api could return more meaningful result.
    boolean pushData(String payload);
}
