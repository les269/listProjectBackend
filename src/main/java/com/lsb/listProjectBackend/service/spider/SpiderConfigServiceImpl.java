package com.lsb.listProjectBackend.service.spider;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.spider.SpiderConfigTO;
import com.lsb.listProjectBackend.mapper.spider.SpiderConfigMapper;
import com.lsb.listProjectBackend.repository.dynamic.spider.SpiderConfigRepository;
import com.lsb.listProjectBackend.service.spider.SpiderConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class SpiderConfigServiceImpl implements SpiderConfigService {
    private final SpiderConfigRepository spiderConfigRepository;

    private final SpiderConfigMapper spiderConfigMapper;

    @Override
    public List<SpiderConfigTO> getAll() {
        return spiderConfigMapper.toDomainList(spiderConfigRepository.findAll());
    }

    @Override
    public SpiderConfigTO getById(String spiderId) {
        return spiderConfigMapper.toDomain(spiderConfigRepository.findById(spiderId).orElse(null));
    }

    @Override
    public void update(SpiderConfigTO req) {
        spiderConfigRepository.save(spiderConfigMapper.toEntity(req));
    }

    @Override
    public void delete(String spiderId) {
        spiderConfigRepository.deleteById(spiderId);
    }

    @Override
    public boolean exists(String spiderId) {
        return spiderConfigRepository.existsById(spiderId);
    }
}
