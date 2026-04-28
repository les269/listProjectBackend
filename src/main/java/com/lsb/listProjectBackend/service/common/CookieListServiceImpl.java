package com.lsb.listProjectBackend.service.common;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.CookieListMapTO;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.entity.dynamic.common.CookieListMap;
import com.lsb.listProjectBackend.mapper.common.CookieListMapper;
import com.lsb.listProjectBackend.repository.dynamic.common.CookieListMapRepository;
import com.lsb.listProjectBackend.repository.dynamic.common.CookieListRepository;
import com.lsb.listProjectBackend.service.common.CookieListMapService;
import com.lsb.listProjectBackend.service.common.CookieListService;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Global.CookieListMapType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.groupingBy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@UseDynamic
@Service
public class CookieListServiceImpl implements CookieListService {

    @Autowired
    private CookieListRepository cookieListRepository;

    @Autowired
    private CookieListMapService cookieListMapService;

    private final CookieListMapper cookieListMapper = CookieListMapper.INSTANCE;

    @Override
    public CookieListTO getByRefIdAndType(String refId, Global.CookieListMapType type) {
        return cookieListMapper.toDomain(cookieListRepository.findByRefIdAndType(refId, type.name()));
    }

    @Override
    public List<CookieListTO> getAll() {
        return cookieListMapper.toDomainList(cookieListRepository.findAll());
    }

    @Override
    public CookieListTO getByCookieId(String cookieId) {
        return cookieListMapper.toDomain(cookieListRepository.findById(cookieId).orElse(null));
    }

    @Override
    public void update(CookieListTO req) {
        cookieListRepository.save(cookieListMapper.toEntity(req));
    }

    @Override
    public void delete(String cookieId) {
        cookieListRepository.deleteById(cookieId);
    }

    @Override
    public Map<String, CookieListTO> getMapByRefIdsAndType(List<String> refIds, CookieListMapType type) {
        if (refIds == null || refIds.isEmpty() || type == null) {
            return Map.of();
        }
        refIds = refIds.stream().distinct().toList();
        List<CookieListMapTO> cookieListMaps = cookieListMapService.getByRefIdsAndType(refIds, type);

        if (cookieListMaps.isEmpty()) {
            return Map.of();
        }
        List<String> cookieIds = cookieListMaps.stream().map(CookieListMapTO::getCookieId).distinct().toList();
        List<CookieListTO> cookies = cookieListMapper.toDomainList(cookieListRepository.findAllById(cookieIds));
        Map<String, CookieListTO> cookieMap = cookies.stream()
                .collect(Collectors.toMap(CookieListTO::getCookieId, Function.identity()));
        return cookieListMaps.stream()
                .collect(Collectors.toMap(CookieListMapTO::getRefId, map -> cookieMap.get(map.getCookieId())));
    }

}
