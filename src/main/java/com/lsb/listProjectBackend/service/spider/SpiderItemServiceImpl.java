package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.spider.SpiderItemTO;
import com.lsb.listProjectBackend.mapper.spider.SpiderItemMapper;
import com.lsb.listProjectBackend.repository.dynamic.spider.SpiderItemRepository;
import com.lsb.listProjectBackend.service.spider.SpiderItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@UseDynamic
@Service
public class SpiderItemServiceImpl implements SpiderItemService {

    @Autowired
    private SpiderItemRepository spiderItemRepository;

    private final SpiderItemMapper spiderItemMapper = SpiderItemMapper.INSTANCE;

    @Override
    public List<SpiderItemTO> getAll() {
        return spiderItemMapper.toDomainList(spiderItemRepository.findAll());
    }

    @Override
    public SpiderItemTO getById(String spiderItemId) {
        return spiderItemMapper.toDomain(spiderItemRepository.findById(spiderItemId).orElse(null));
    }

    @Override
    public void update(SpiderItemTO req) {
        spiderItemRepository.save(spiderItemMapper.toEntity(req));
    }

    @Override
    public void delete(String spiderItemId) {
        spiderItemRepository.deleteById(spiderItemId);
    }

    @Override
    public boolean exists(String spiderItemId) {
        return spiderItemRepository.existsById(spiderItemId);
    }

    @Override
    public List<SpiderItemTO> getByIdList(List<String> spiderItemIdList) {
        return spiderItemMapper.toDomainList(spiderItemRepository.findAllById(spiderItemIdList));
    }

    @Override
    public List<SpiderItemTO> getBySpiderId(String spiderId) {
        return spiderItemMapper.toDomainList(spiderItemRepository.findAllBySpiderId(spiderId));
    }
}
