package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.ThemeHiddenTO;
import com.lsb.listProjectBackend.mapper.ThemeHiddenMapper;
import com.lsb.listProjectBackend.repository.dynamic.ThemeHiddenRepository;
import com.lsb.listProjectBackend.service.ThemeHiddenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@UseDynamic
@Service
public class ThemeHiddenServiceImpl implements ThemeHiddenService {

    @Autowired
    private ThemeHiddenRepository themeHiddenRepository;

    private final ThemeHiddenMapper themeHiddenMapper = ThemeHiddenMapper.INSTANCE;

    @Override
    public List<ThemeHiddenTO> getAll() {
        return themeHiddenMapper.toDomainList(themeHiddenRepository.findAll());
    }

    @Override
    public void save(ThemeHiddenTO req) {
        themeHiddenRepository.save(themeHiddenMapper.toEntity(req));
    }

    @Override
    public void delete(String headerId) {
        themeHiddenRepository.deleteById(headerId);
    }
}
