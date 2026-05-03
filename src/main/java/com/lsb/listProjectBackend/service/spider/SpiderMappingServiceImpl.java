package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.spider.SpiderMappingTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderMapping;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderMappingPK;
import com.lsb.listProjectBackend.mapper.spider.SpiderMappingMapper;
import com.lsb.listProjectBackend.repository.dynamic.spider.SpiderMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class SpiderMappingServiceImpl implements SpiderMappingService {

    private final SpiderMappingRepository spiderMappingRepository;

    private final SpiderMappingMapper spiderMappingMapper;

    @Override
    public List<SpiderMappingTO> getAll() {
        return spiderMappingMapper.toDomainList(spiderMappingRepository.findAll());
    }

    @Override
    public SpiderMappingTO getById(String spiderId, Integer executionOrder) {
        SpiderMappingPK pk = new SpiderMappingPK(spiderId, executionOrder);
        return spiderMappingMapper.toDomain(spiderMappingRepository.findById(pk).orElse(null));
    }

    @Override
    public List<SpiderMappingTO> getBySpiderId(String spiderId) {
        return spiderMappingMapper.toDomainList(spiderMappingRepository.findAll())
                .stream()
                .filter(m -> m.getSpiderId().equals(spiderId))
                .sorted((a, b) -> a.getExecutionOrder().compareTo(b.getExecutionOrder()))
                .toList();
    }

    @Override
    public void update(SpiderMappingTO req) {
        spiderMappingRepository.save(spiderMappingMapper.toEntity(req));
    }

    @Override
    public void updateList(List<SpiderMappingTO> req) {
        spiderMappingRepository.saveAll(spiderMappingMapper.toEntityList(req));
    }

    @Override
    public void delete(String spiderId, Integer executionOrder, String spiderItemId) {

        SpiderMapping example = new SpiderMapping();
        example.setSpiderId(spiderId);
        example.setExecutionOrder(executionOrder);
        example.setSpiderItemId(spiderItemId);
        spiderMappingRepository.findOne(Example.of(example)).ifPresent(spiderMappingRepository::delete);
    }

    @Override
    public void deleteBySpiderId(String spiderId) {
        spiderMappingRepository.findAll().stream()
                .filter(m -> m.getSpiderId().equals(spiderId))
                .forEach(m -> spiderMappingRepository.delete(m));
    }

    @Override
    public boolean inUseBySpiderItemId(String spiderItemId) {
        return spiderMappingRepository.countBySpiderItemId(spiderItemId) > 0;
    }
}
