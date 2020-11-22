package com.db.dataplatform.techtest.server.service.impl;

import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.persistence.repository.DataHeaderRepository;
import com.db.dataplatform.techtest.server.persistence.repository.DataStoreRepository;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataBodyServiceImpl implements DataBodyService {

    private final DataStoreRepository dataStoreRepository;

    private final DataHeaderRepository dataHeaderRepository;
    @Override
    public void saveDataBody(DataBodyEntity dataBody) {
        dataStoreRepository.save(dataBody);
    }

    @Override
    public List<DataBodyEntity> getDataByBlockType(BlockTypeEnum blockType) {
        DataHeaderEntity entity = new DataHeaderEntity();
        entity.setBlocktype(blockType);
        Example<DataHeaderEntity> example = Example.of(entity);

        List<DataHeaderEntity> headers = dataHeaderRepository.findAll(example);
        return headers.stream()
                .flatMap(
                        dataHeaderEntity -> {
                            DataBodyEntity dataBodyProbe = new DataBodyEntity();
                            dataBodyProbe.setDataHeaderEntity(dataHeaderEntity);
                            Example<DataBodyEntity> exampleBody = Example.of(dataBodyProbe);
                            return dataStoreRepository.findAll(exampleBody).stream();
                        }
                ).collect(Collectors.toList());
    }

    @Override
    public Optional<DataBodyEntity> getDataByBlockName(String blockName) {
        DataHeaderEntity entity = new DataHeaderEntity();
        entity.setName(blockName);
        Example<DataHeaderEntity> example = Example.of(entity);
        return dataHeaderRepository.findOne(example)
                .flatMap(
                        dataHeaderEntity -> {
                            DataBodyEntity dataBodyProbe = new DataBodyEntity();
                            dataBodyProbe.setDataHeaderEntity(dataHeaderEntity);
                            Example<DataBodyEntity> exampleBody = Example.of(dataBodyProbe);
                            return dataStoreRepository.findOne(exampleBody);
                        }

                );
    }
}
