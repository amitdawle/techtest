package com.db.dataplatform.techtest.client.component.impl;

import com.db.dataplatform.techtest.client.RestTemplateConfiguration;
import com.db.dataplatform.techtest.client.api.model.DataEnvelope;
import com.db.dataplatform.techtest.client.component.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.Response;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyList;

/**
 * Client code does not require any test coverage
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class ClientImpl implements Client {

    public static final String URI_PUSHDATA = "http://localhost:8090/dataserver/pushdata";
    public static final UriTemplate URI_GETDATA = new UriTemplate("http://localhost:8090/dataserver/data/{blockType}");
    public static final UriTemplate URI_PATCHDATA = new UriTemplate("http://localhost:8090/dataserver/update/{name}/{newBlockType}");
    @Autowired
    RestTemplateConfiguration restTemplateConfiguration;

    @Override
    public void pushData(DataEnvelope dataEnvelope) {
        Objects.requireNonNull(dataEnvelope);
        log.info("Pushing data {} to {}", dataEnvelope.getDataHeader().getName(), URI_PUSHDATA);
        RestTemplate restTemplate = restTemplateConfiguration.createRestTemplate( new MappingJackson2HttpMessageConverter(),
                new StringHttpMessageConverter());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DataEnvelope> request = new HttpEntity<>(dataEnvelope, headers);
        ResponseEntity<String> response2 = restTemplate.postForEntity(URI_PUSHDATA, request , String.class );
        log.info("Status code {} for {}", response2.getStatusCode(), URI_PUSHDATA);
    }

    @Override
    public List<DataEnvelope> getData(String blockType) {
        Objects.requireNonNull(blockType);
        log.info("Query for data with header block type {}", blockType);
        RestTemplate restTemplate = restTemplateConfiguration.createRestTemplate( new MappingJackson2HttpMessageConverter(),
                new StringHttpMessageConverter());
        try {
            URI getDataUri = URI_GETDATA.expand(blockType);
            ResponseEntity<DataEnvelope[]> response2 = restTemplate.getForEntity(getDataUri,
                    DataEnvelope[].class);
            log.info("Status code {} for {}. Number of records returned is {}", response2.getStatusCode(),
                    getDataUri.toURL(), response2.getBody().length);
        } catch (Exception e){
            log.error(String.format("Error while getting data for %s", blockType), e );
        }
        return emptyList();
    }

    @Override
    public boolean updateData(String blockName, String newBlockType) {
        log.info("Updating blocktype to {} for block with name {}", newBlockType, blockName);
        RestTemplate restTemplate = restTemplateConfiguration.createRestTemplate( new MappingJackson2HttpMessageConverter(),
                new StringHttpMessageConverter());
        try {

            URI patchDataUri = URI_PATCHDATA.expand(blockName, newBlockType);
            RequestEntity<Void> request = RequestEntity
                    .patch(patchDataUri)
                    .accept(MediaType.APPLICATION_JSON).build();
            ResponseEntity<Boolean> response = restTemplate.exchange(request,
                    Boolean.class);
            log.info("Status code is {} for patch request.", response.getStatusCode());
        } catch (Exception e){
            log.error(String.format("Error while getting patching data for %s", blockName), e );
        }
        return false;
    }


}
