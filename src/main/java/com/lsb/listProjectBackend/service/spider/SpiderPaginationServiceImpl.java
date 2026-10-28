package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPagination;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationConfig;
import com.lsb.listProjectBackend.mapper.spider.SpiderPaginationMapper;
import com.lsb.listProjectBackend.repository.dynamic.spider.SpiderPaginationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class SpiderPaginationServiceImpl implements SpiderPaginationService {
    private final SpiderPaginationRepository spiderPaginationRepository;

    private final SpiderPaginationMapper spiderPaginationMapper;

    @Override
    public List<SpiderPaginationTO> getAll() {
        return spiderPaginationMapper.toDomainList(spiderPaginationRepository.findAll());
    }

    @Override
    public SpiderPaginationTO getById(String spiderPaginationId) {
        return spiderPaginationMapper.toDomain(spiderPaginationRepository.findById(spiderPaginationId).orElse(null));
    }

    @Override
    public void update(SpiderPaginationTO req) {
        SpiderPagination entity = spiderPaginationMapper.toEntity(req);
        if (entity.getPaginationConfig() == null) {
            entity.setPaginationConfig(new SpiderPaginationConfig());
        }
        spiderPaginationRepository.save(entity);
    }

    @Override
    public void delete(String spiderPaginationId) {
        spiderPaginationRepository.deleteById(spiderPaginationId);
    }

    @Override
    public boolean exists(String spiderPaginationId) {
        return spiderPaginationRepository.existsById(spiderPaginationId);
    }
}
