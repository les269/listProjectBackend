package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.domain.ReplaceValueMapTO;
import com.lsb.listProjectBackend.entity.ReplaceValueMap;
import com.lsb.listProjectBackend.mapper.ReplaceValueMapMapper;
import com.lsb.listProjectBackend.repository.ReplaceValueMapRepository;
import com.lsb.listProjectBackend.service.ReplaceValueMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplaceValueMapServiceImpl implements ReplaceValueMapService {
    @Autowired
    private ReplaceValueMapRepository replaceValueMapRepository;

    private ReplaceValueMapMapper mapper = ReplaceValueMapMapper.INSTANCE;

    @Override
    public List<ReplaceValueMapTO> getNameList() {
        return mapper.toDomainList(replaceValueMapRepository.getNameList());
    }

    @Override
    public boolean existMap(String name) {
        return replaceValueMapRepository.existsById(name);
    }

    @Override
    public ReplaceValueMapTO getByName(String name) {
        return mapper.toDomain(replaceValueMapRepository.findById(name).orElse(null));
    }

    @Override
    public void update(ReplaceValueMapTO to) {
        replaceValueMapRepository.save(mapper.toEntity(to));
    }

    @Override
    public void deleteByName(String name) {
        replaceValueMapRepository.deleteById(name);
    }
}
