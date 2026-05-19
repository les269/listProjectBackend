package com.lsb.listProjectBackend.service.common;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.HeadersMapTO;
import com.lsb.listProjectBackend.entity.dynamic.common.HeadersMapPK;
import com.lsb.listProjectBackend.mapper.common.HeadersMapMapper;
import com.lsb.listProjectBackend.repository.dynamic.common.HeadersMapRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Global.HeadersMapType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@UseDynamic
@Service
@RequiredArgsConstructor
public class HeadersMapServiceImpl implements HeadersMapService {
    private final HeadersMapRepository headersMapRepository;

    private final HeadersMapMapper headersMapMapper;

    @Override
    public HeadersMapTO getMapByIdAndType(String refId, Global.HeadersMapType type) {
        return headersMapRepository.findById(new HeadersMapPK(refId, type))
                .map(headersMapMapper::toDomain)
                .orElse(null);
    }

    @Override
    public List<HeadersMapTO> getMapByRefId(String refId) {
        return headersMapMapper.toDomainList(headersMapRepository.findAllByRefId(refId));
    }

    @Override
    public List<HeadersMapTO> getMapByType(Global.HeadersMapType type) {
        return headersMapMapper.toDomainList(headersMapRepository.findAllByType(type.name()));
    }

    @Override
    public void updateMap(HeadersMapTO req) {
        headersMapRepository.save(headersMapMapper.toEntity(req));
    }

    @Override
    public void deleteMap(String refId, Global.HeadersMapType type) {
        headersMapRepository.deleteById(new HeadersMapPK(refId, type));
    }

    @Override
    public boolean isInUseMap(String headersId, Global.HeadersMapType type) {
        return headersMapRepository.existsById(new HeadersMapPK(headersId, type));
    }

    @Override
    public boolean headersIsInUse(String headersId) {
        return headersMapRepository.headersIsInUse(headersId) > 0;
    }

    @Override
    public List<HeadersMapTO> getByRefIdsAndType(List<String> refIds, HeadersMapType type) {
        return headersMapMapper.toDomainList(headersMapRepository.findAllByRefIdsAndType(refIds, type.name()));
    }
}
