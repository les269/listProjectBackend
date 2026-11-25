package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.aop.UseDynamicTx;
import com.lsb.listProjectBackend.domain.spider.SpiderMappingTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderMappingPK;
import com.lsb.listProjectBackend.mapper.spider.SpiderMappingMapper;
import com.lsb.listProjectBackend.repository.dynamic.spider.SpiderMappingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
                .filter(m -> m.spiderId().equals(spiderId))
                .sorted((a, b) -> a.executionOrder().compareTo(b.executionOrder()))
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
    @UseDynamicTx
    public void delete(String spiderId, Integer executionOrder, String spiderItemId) {
        List<SpiderMappingTO> mappings = spiderMappingMapper.toDomainList(
                spiderMappingRepository.findBySpiderIdOrderByExecutionOrderAsc(spiderId));
        boolean exists = mappings.stream().anyMatch(mapping ->
                Objects.equals(mapping.executionOrder(), executionOrder)
                        && Objects.equals(mapping.spiderItemId(), spiderItemId));
        if (!exists) {
            return;
        }

        List<SpiderMappingTO> remaining = new ArrayList<>();
        for (SpiderMappingTO mapping : mappings) {
            if (Objects.equals(mapping.executionOrder(), executionOrder)
                    && Objects.equals(mapping.spiderItemId(), spiderItemId)) {
                continue;
            }
            remaining.add(new SpiderMappingTO(spiderId, remaining.size(),
                    mapping.spiderItemId(), mapping.updatedTime()));
        }

        // Clear managed entities before reusing their composite primary keys.
        spiderMappingRepository.deleteAllBySpiderId(spiderId);
        spiderMappingRepository.saveAll(spiderMappingMapper.toEntityList(remaining));
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
