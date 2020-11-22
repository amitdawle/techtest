package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.component.DataLakeRestGateway;
import com.db.dataplatform.techtest.server.exception.DataLakeException;
import com.db.dataplatform.techtest.server.exception.ServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.time.Duration;
import java.util.Collections;

import static java.lang.String.format;

@Slf4j
@Service
public class HadoopDataLakeGateway implements DataLakeRestGateway {

    private static final UriTemplate URI_PUSHDATA = new UriTemplate("http://localhost:8090/hadoopserver/pushbigdata");

    private static final int MAX_WAIT_MILLIS = 5000;
    private final int RETRY_TIMES = 5;


    private final RestTemplate restTemplate;

    public HadoopDataLakeGateway(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofMillis(MAX_WAIT_MILLIS)).build();
    }

    @Override
    @Retryable(value = DataLakeException.class, maxAttempts = RETRY_TIMES, backoff = @Backoff(delay = 1000))
    public boolean pushData(String payload) {
        try {
            log.info("Attempting to push data to the data lake");

            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            ResponseEntity<Boolean> responseEntity
                    = restTemplate.postForEntity(URI_PUSHDATA.expand(Collections.emptyMap()),
                    request, Boolean.class);

            if(responseEntity.getStatusCode() != HttpStatus.OK){
                log.warn("Push returned {}", responseEntity.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error(format("Exception in accessing the data lake, " +
                    "will attempt to retry until max retries is reached. The error was %s.", e.getMessage()));
            throw new DataLakeException(e);
        }
        log.info("Push was successful.");
        return true;
    }
}
