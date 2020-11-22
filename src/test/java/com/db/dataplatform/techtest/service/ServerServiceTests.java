package com.db.dataplatform.techtest.service;

import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.component.DataLakeRestGateway;
import com.db.dataplatform.techtest.server.component.Server;
import com.db.dataplatform.techtest.server.component.impl.ServerImpl;
import com.db.dataplatform.techtest.server.exception.DataLakeException;
import com.db.dataplatform.techtest.server.exception.ServerException;
import com.db.dataplatform.techtest.server.mapper.ServerMapperConfiguration;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static com.db.dataplatform.techtest.TestDataHelper.createTestDataEnvelopeApiObject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServerServiceTests {

    @Mock
    private DataBodyService dataBodyServiceImplMock;

    @Mock
    private DataLakeRestGateway dataLakeRestGateway;

    private ModelMapper modelMapper;

    private DataBodyEntity expectedDataBodyEntity;
    private DataEnvelope testDataEnvelope;

    private Server server;

    @Before
    public void setup() {
        ServerMapperConfiguration serverMapperConfiguration = new ServerMapperConfiguration();
        modelMapper = serverMapperConfiguration.createModelMapperBean();

        testDataEnvelope = createTestDataEnvelopeApiObject();
        expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));

        when(dataLakeRestGateway.pushData(anyString())).thenReturn(true);

        server = new ServerImpl(dataBodyServiceImplMock, modelMapper, dataLakeRestGateway);
    }

    @Test
    public void shouldSaveDataEnvelopeAsExpected() throws NoSuchAlgorithmException, IOException {
        boolean success = server.saveDataEnvelope(testDataEnvelope);

        assertThat(success).isTrue();

        verify(dataBodyServiceImplMock, times(1)).saveDataBody(eq(expectedDataBodyEntity));
        verify(dataLakeRestGateway, times(1)).pushData(expectedDataBodyEntity.getDataBody());
    }

    @Test
    public void shouldNoSaveDataEnvelopeWhenCheckSumFails() throws NoSuchAlgorithmException, IOException {
        DataEnvelope testDataEnvelope = createTestDataEnvelopeApiObject();
        testDataEnvelope.getDataBody().setMd5Checksum("FOO");
        DataBodyEntity expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));

        boolean success = server.saveDataEnvelope(testDataEnvelope);

        assertThat(success).isFalse();
        verify(dataBodyServiceImplMock, times(0)).saveDataBody(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldFailWhenDataLakeTimesout()  {
        DataEnvelope testDataEnvelope = createTestDataEnvelopeApiObject();
        testDataEnvelope.getDataBody().setMd5Checksum("CECFD3953783DF706878AAEC2C22AA70");
        DataBodyEntity expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));
        when(dataLakeRestGateway.pushData(anyString())).thenReturn(false);

        assertThatThrownBy(() -> server.saveDataEnvelope(testDataEnvelope)).isInstanceOf(ServerException.class);
    }

    @Test
    public void shouldFailWhenDataLakeInvocationFails()  {
        DataEnvelope testDataEnvelope = createTestDataEnvelopeApiObject();
        testDataEnvelope.getDataBody().setMd5Checksum("CECFD3953783DF706878AAEC2C22AA70");
        DataBodyEntity expectedDataBodyEntity = modelMapper.map(testDataEnvelope.getDataBody(), DataBodyEntity.class);
        expectedDataBodyEntity.setDataHeaderEntity(modelMapper.map(testDataEnvelope.getDataHeader(), DataHeaderEntity.class));
        when(dataLakeRestGateway.pushData(anyString())).thenThrow(new DataLakeException(new Exception("Unavailable")));

        assertThatThrownBy(() -> server.saveDataEnvelope(testDataEnvelope)).isInstanceOf(ServerException.class);
    }
}
