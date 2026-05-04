package com.lsb.listProjectBackend.service.common;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.CookieListMapTO;
import com.lsb.listProjectBackend.entity.dynamic.common.CookieListMapPK;
import com.lsb.listProjectBackend.mapper.common.CookieListMapMapper;
import com.lsb.listProjectBackend.repository.dynamic.common.CookieListMapRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Global.CookieListMapType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@UseDynamic
@Service
@RequiredArgsConstructor
public class CookieListMapServiceImpl implements CookieListMapService {
    private final CookieListMapRepository cookieListMapRepository;

    private final CookieListMapMapper cookieListMapMapper;

    @Override
    public CookieListMapTO getMapByIdAndType(String refId, Global.CookieListMapType type) {
        return cookieListMapRepository.findById(new CookieListMapPK(refId, type))
                .map(cookieListMapMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<CookieListMapTO> getMapByRefId(String refId) {
        return cookieListMapMapper.toDomainList(cookieListMapRepository.findAllByRefId(refId));
    }

    @Override
    public List<CookieListMapTO> getMapByType(Global.CookieListMapType type) {
        return cookieListMapMapper.toDomainList(cookieListMapRepository.findAllByType(type.name()));
    }

    @Override
    public void updateMap(CookieListMapTO req) {
        cookieListMapRepository.save(cookieListMapMapper.toEntity(req));
    }

    @Override
    public void deleteMap(String refId, Global.CookieListMapType type) {
        cookieListMapRepository.deleteById(new CookieListMapPK(refId, type));
    }

    @Override
    public boolean isInUseMap(String cookieId, Global.CookieListMapType type) {
        return cookieListMapRepository.existsById(new CookieListMapPK(cookieId, type));
    }

    @Override
    public boolean cookieIsInUse(String cookieId) {
        return cookieListMapRepository.cookieIsInUse(cookieId) > 0;
    }

    @Override
    public List<CookieListMapTO> getByRefIdsAndType(List<String> refIds, CookieListMapType type) {
        return cookieListMapMapper.toDomainList(cookieListMapRepository.findAllByRefIdsAndType(refIds, type.name()));
    }

}
