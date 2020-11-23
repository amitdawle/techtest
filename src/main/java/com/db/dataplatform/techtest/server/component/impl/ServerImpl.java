package com.db.dataplatform.techtest.server.component.impl;

import com.db.dataplatform.techtest.server.api.model.DataBody;
import com.db.dataplatform.techtest.server.api.model.DataEnvelope;
import com.db.dataplatform.techtest.server.api.model.DataHeader;
import com.db.dataplatform.techtest.server.component.DataLakeGateway;
import com.db.dataplatform.techtest.server.exception.DataLakeException;
import com.db.dataplatform.techtest.server.exception.ServerException;
import com.db.dataplatform.techtest.server.persistence.BlockTypeEnum;
import com.db.dataplatform.techtest.server.persistence.model.DataBodyEntity;
import com.db.dataplatform.techtest.server.persistence.model.DataHeaderEntity;
import com.db.dataplatform.techtest.server.service.DataBodyService;
import com.db.dataplatform.techtest.server.component.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ServerImpl implements Server {

    private final DataBodyService dataBodyServiceImpl;
    private final ModelMapper modelMapper;
    private final DataLakeGateway dataLakeGateway;
    /**
     * @param envelope
     * @return true if there is a match with the client provided checksum.
     */
    @Override
    public boolean saveDataEnvelope(DataEnvelope envelope) {

        try {
            String calculateMd5Checksum = null;
            try {
                calculateMd5Checksum = calculateMd5Checksum(envelope.getDataBody().getDataBody());
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Unable to get MD5 algorithm to calculate the checksum", e);
            }
            if (!calculateMd5Checksum.equalsIgnoreCase(envelope.getDataBody().getMd5Checksum())) {
                log.error("Checksum failed! Incoming {}, Expected {}", envelope.getDataBody().getMd5Checksum(),
                        calculateMd5Checksum);
                return false;
            }
            // Save to persistence.
            persist(envelope);

            if (!dataLakeGateway.pushData(envelope.getDataBody().getDataBody())) {
                log.warn("Failed to push data to the data lake");
                throw new ServerException("Failed to push data to datalake.");
            }
        } catch (DataLakeException e){
            log.error("Exception in accessing the data lake. ", e);
            throw new ServerException(e);
        }
        log.info("Data persisted successfully, data name: {}", envelope.getDataHeader().getName());
        return true;
    }

    @Override
    public List<DataEnvelope> findBy(BlockTypeEnum blockTypeEnum) {
        log.info("Finding records of type : {}", blockTypeEnum);
        List<DataBodyEntity> entities =  dataBodyServiceImpl.getDataByBlockType(blockTypeEnum);
        List<DataEnvelope> envelopes = entities.stream()
                .map(dbe -> asDataEnvelope(dbe)).collect(Collectors.toList());
        return envelopes;
    }

    private DataEnvelope asDataEnvelope(DataBodyEntity dbe) {
        DataHeader header = modelMapper.map(dbe.getDataHeaderEntity(), DataHeader.class);
        DataBody body = modelMapper.map(dbe.getDataBody(), DataBody.class);
        return new DataEnvelope(header, body);
    }

    @Override
    public boolean updateBlockType(String name, String newBlockType) {
        return dataBodyServiceImpl.getDataByBlockName(name)
                .map(dataBodyEntity -> {
                    log.info("Updating block type data body with name {}. Changing {} to {}", name,
                            dataBodyEntity.getDataHeaderEntity().getBlocktype(), newBlockType);
                    dataBodyEntity.getDataHeaderEntity().setBlocktype(BlockTypeEnum.valueOf(newBlockType));
                    this.saveData(dataBodyEntity);
                    return true;
                }).orElse(false);
    }

    private void persist(DataEnvelope envelope) {
        log.info("Persisting data with attribute name: {}", envelope.getDataHeader().getName());
        DataHeaderEntity dataHeaderEntity = modelMapper.map(envelope.getDataHeader(), DataHeaderEntity.class);

        DataBodyEntity dataBodyEntity = modelMapper.map(envelope.getDataBody(), DataBodyEntity.class);
        dataBodyEntity.setDataHeaderEntity(dataHeaderEntity);

        saveData(dataBodyEntity);
    }

    private void saveData(DataBodyEntity dataBodyEntity) {
        dataBodyServiceImpl.saveDataBody(dataBodyEntity);
    }


    private String calculateMd5Checksum(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(text.getBytes());
        byte[] digest = md.digest();
        String checkSum = DatatypeConverter
                .printHexBinary(digest).toUpperCase();
        return checkSum;
    }

}
