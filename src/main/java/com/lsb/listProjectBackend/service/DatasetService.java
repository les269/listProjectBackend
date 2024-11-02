package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.DatasetTO;
import com.lsb.listProjectBackend.entity.Dataset;
import com.lsb.listProjectBackend.mapper.DatasetMapper;
import com.lsb.listProjectBackend.repository.DatasetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DatasetService {
    @Autowired
    private DatasetRepository datasetRepository;
    private final DatasetMapper datasetMapper = DatasetMapper.INSTANCE;

    public List<DatasetTO> getAllDataset() {
        return datasetMapper.toDomainList(datasetRepository.findAll());
    }

    public void updateDataset(DatasetTO req) {
        Dataset datasetData = datasetMapper.toEntity(req);
        datasetRepository.save(datasetData);
    }

    public void deleteDataset(String name) {
        datasetRepository.deleteById(name);
    }

    public DatasetTO getDataset(String name) {
        return datasetMapper.toDomain(datasetRepository.findById(name).orElse(null));
    }

    public boolean existDataset(String name) {
        return datasetRepository.existsById(name);
    }

}
