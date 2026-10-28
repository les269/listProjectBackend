package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.spider.SpiderPaginationDataTO;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationData;
import com.lsb.listProjectBackend.entity.dynamic.spider.SpiderPaginationDataPK;
import com.lsb.listProjectBackend.mapper.spider.SpiderPaginationDataMapper;
import com.lsb.listProjectBackend.repository.dynamic.spider.SpiderPaginationDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class SpiderPaginationDataServiceImpl implements SpiderPaginationDataService {
    private final SpiderPaginationDataRepository spiderPaginationDataRepository;

    private final SpiderPaginationDataMapper spiderPaginationDataMapper;

    @Override
    public List<SpiderPaginationDataTO> getAll() {
        return spiderPaginationDataMapper.toDomainList(spiderPaginationDataRepository.findAll());
    }

    @Override
    public List<SpiderPaginationDataTO> getBySpiderPaginationId(String spiderPaginationId) {
        return spiderPaginationDataMapper.toDomainList(
                spiderPaginationDataRepository.findAllBySpiderPaginationId(spiderPaginationId));
    }

    @Override
    public SpiderPaginationDataTO getById(String spiderPaginationId, String dataId) {
        SpiderPaginationDataPK pk = new SpiderPaginationDataPK(spiderPaginationId, dataId);
        return spiderPaginationDataMapper.toDomain(spiderPaginationDataRepository.findById(pk).orElse(null));
    }

    @Override
    public void update(SpiderPaginationDataTO req) {
        SpiderPaginationData entity = spiderPaginationDataMapper.toEntity(req);
        if (entity.getData() == null) {
            entity.setData(new HashMap<>());
        }
        spiderPaginationDataRepository.save(entity);
    }

    @Override
    public void delete(String spiderPaginationId, String dataId) {
        SpiderPaginationDataPK pk = new SpiderPaginationDataPK(spiderPaginationId, dataId);
        spiderPaginationDataRepository.deleteById(pk);
    }

    @Override
    public void deleteBySpiderPaginationId(String spiderPaginationId) {
        spiderPaginationDataRepository.findAllBySpiderPaginationId(spiderPaginationId)
                .forEach(spiderPaginationDataRepository::delete);
    }

    @Override
    public boolean exists(String spiderPaginationId, String dataId) {
        SpiderPaginationDataPK pk = new SpiderPaginationDataPK(spiderPaginationId, dataId);
        return spiderPaginationDataRepository.existsById(pk);
    }
}
