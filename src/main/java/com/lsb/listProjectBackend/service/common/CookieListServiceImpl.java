package com.lsb.listProjectBackend.service.common;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.CookieListMapTO;
import com.lsb.listProjectBackend.domain.common.CookieListTO;
import com.lsb.listProjectBackend.mapper.common.CookieListMapper;
import com.lsb.listProjectBackend.repository.dynamic.common.CookieListRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Global.CookieListMapType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class CookieListServiceImpl implements CookieListService {
    private final CookieListRepository cookieListRepository;
    private final CookieListMapService cookieListMapService;

    private final CookieListMapper cookieListMapper;

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
        List<String> cookieIds = cookieListMaps.stream().map(CookieListMapTO::cookieId).distinct().toList();
        List<CookieListTO> cookies = cookieListMapper.toDomainList(cookieListRepository.findAllById(cookieIds));
        Map<String, CookieListTO> cookieMap = cookies.stream()
                .collect(Collectors.toMap(CookieListTO::cookieId, Function.identity()));
        return cookieListMaps.stream()
                .collect(Collectors.toMap(CookieListMapTO::refId, map -> cookieMap.get(map.cookieId())));
    }

}
