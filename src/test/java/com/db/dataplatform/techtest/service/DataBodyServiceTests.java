package com.db.dataplatform.techtest.service;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.persistence.repository.DataHeaderRepository;
import com.db.dataplatform.techtest.server.persistence.repository.DataStoreRepository;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.service.impl.DataBodyServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Example;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.db.dataplatform.techtest.TestDataHelper.createTestDataBodyEntity;
import static com.db.dataplatform.techtest.TestDataHelper.createTestDataHeaderEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DataBodyServiceTests {

    public static final String TEST_NAME_NO_RESULT = "TestNoResult";

    @Mock
    private DataStoreRepository dataStoreRepositoryMock;

    @Mock
    private DataHeaderRepository dataHeaderRepositoryMock;

    private DataBodyService dataBodyService;
    private DataBodyEntity expectedDataBodyEntity;

    @Before
    public void setup() {
        DataHeaderEntity testDataHeaderEntity = createTestDataHeaderEntity(Instant.now());
        expectedDataBodyEntity = createTestDataBodyEntity(testDataHeaderEntity);

        dataBodyService = new DataBodyServiceImpl(dataStoreRepositoryMock, dataHeaderRepositoryMock);
    }

    @Test
    public void shouldSaveDataBodyEntityAsExpected(){
        dataBodyService.saveDataBody(expectedDataBodyEntity);

        verify(dataStoreRepositoryMock, times(1))
                .save(eq(expectedDataBodyEntity));
    }

    @Test
    public void shouldGetDataByBlockNameWhenExist(){
        DataBodyEntity dataBodyEntity = mock(DataBodyEntity.class);
        DataHeaderEntity dataHeaderEntity = new DataHeaderEntity();
        dataHeaderEntity.setName("dummyBlock");
        Example<DataHeaderEntity> example = Example.of(dataHeaderEntity);
        when(dataHeaderRepositoryMock.findOne(example))
                .thenReturn(Optional.of(dataHeaderEntity));
        when(dataStoreRepositoryMock.findOne(any(Example.class)))
                .thenReturn(Optional.of(dataBodyEntity));

        Optional<DataBodyEntity> f =  dataBodyService.getDataByBlockName("dummyBlock");

        assertThat(f.isPresent()).isTrue();
        verify(dataHeaderRepositoryMock).findOne(example);
    }

    @Test
    public void shouldReturnEmptyWhenBlockForNameDoesNotExist(){
        DataHeaderEntity dataHeaderEntity = new DataHeaderEntity();
        dataHeaderEntity.setName("dummyBlock");
        DataBodyEntity dataBodyProbe = new DataBodyEntity();
        dataBodyProbe.setDataHeaderEntity(dataHeaderEntity);
        Example<DataBodyEntity> exampleBody = Example.of(dataBodyProbe);
        Example<DataHeaderEntity> example = Example.of(dataHeaderEntity);
        when(dataHeaderRepositoryMock.findOne(example))
                .thenReturn(Optional.empty());

        Optional<DataBodyEntity> f =  dataBodyService.getDataByBlockName("dummyBlock");

        assertThat(f.isPresent()).isFalse();
        verify(dataHeaderRepositoryMock).findOne(example);
        verify(dataStoreRepositoryMock, never()).findOne(exampleBody);

    }

    @Test
    public void shouldReturnBlocksForAGivenType(){
        DataHeaderEntity dataHeaderEntity = new DataHeaderEntity();
        dataHeaderEntity.setBlocktype(BlockTypeEnum.BLOCKTYPEA);
        DataBodyEntity dataBodyEntity = new DataBodyEntity();
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);
        Example<DataBodyEntity> exampleBody = Example.of(dataBodyEntity);
        Example<DataHeaderEntity> example = Example.of(dataHeaderEntity);
        when(dataHeaderRepositoryMock.findAll(example))
                .thenReturn(Arrays.asList(dataHeaderEntity));
        when(dataStoreRepositoryMock.findAll(exampleBody))
                .thenReturn(Arrays.asList(dataBodyEntity));

        List<DataBodyEntity> ls =  dataBodyService.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA);

        assertThat(ls.size()).isEqualTo(1);
        assertThat(ls.get(0)).isEqualTo(dataBodyEntity);

    }

    @Test
    public void shouldReturnEmptyListWhenBlockDoesNotExistForGivenType(){
        DataHeaderEntity dataHeaderEntity = new DataHeaderEntity();
        dataHeaderEntity.setBlocktype(BlockTypeEnum.BLOCKTYPEA);
        DataBodyEntity dataBodyEntity = new DataBodyEntity();
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);
        Example<DataBodyEntity> exampleBody = Example.of(dataBodyEntity);
        Example<DataHeaderEntity> example = Example.of(dataHeaderEntity);
        when(dataHeaderRepositoryMock.findAll(example))
                .thenReturn(Collections.emptyList());


        List<DataBodyEntity> ls =  dataBodyService.getDataByBlockType(BlockTypeEnum.BLOCKTYPEA);

        assertThat(ls.isEmpty()).isEqualTo(true);
        verify(dataStoreRepositoryMock, never()).findAll(exampleBody);

    }



}
