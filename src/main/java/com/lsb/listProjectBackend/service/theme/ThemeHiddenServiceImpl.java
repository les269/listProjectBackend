package com.lsb.listProjectBackend.service.theme;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.theme.ThemeHiddenTO;
import com.lsb.listProjectBackend.mapper.theme.ThemeHiddenMapper;
import com.lsb.listProjectBackend.repository.dynamic.theme.ThemeHiddenRepository;
import com.lsb.listProjectBackend.service.theme.ThemeHiddenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class ThemeHiddenServiceImpl implements ThemeHiddenService {
    private final ThemeHiddenRepository themeHiddenRepository;

    private final ThemeHiddenMapper themeHiddenMapper;

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
