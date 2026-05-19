package com.lsb.listProjectBackend.service.common;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.HeadersMapTO;
import com.lsb.listProjectBackend.domain.common.HeadersTO;
import com.lsb.listProjectBackend.mapper.common.HeadersMapper;
import com.lsb.listProjectBackend.repository.dynamic.common.HeadersRepository;
import com.lsb.listProjectBackend.utils.Global;
import com.lsb.listProjectBackend.utils.Global.HeadersMapType;

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
public class HeadersServiceImpl implements HeadersService {
    private final HeadersRepository headersRepository;
    private final HeadersMapService headersMapService;

    private final HeadersMapper headersMapper;

    @Override
    public HeadersTO getByRefIdAndType(String refId, Global.HeadersMapType type) {
        return headersMapper.toDomain(headersRepository.findByRefIdAndType(refId, type.name()));
    }

    @Override
    public List<HeadersTO> getAll() {
        return headersMapper.toDomainList(headersRepository.findAll());
    }

    @Override
    public HeadersTO getByHeadersId(String headersId) {
        return headersMapper.toDomain(headersRepository.findById(headersId).orElse(null));
    }

    @Override
    public void update(HeadersTO req) {
        headersRepository.save(headersMapper.toEntity(req));
    }

    @Override
    public void delete(String headersId) {
        headersRepository.deleteById(headersId);
    }

    @Override
    public Map<String, HeadersTO> getMapByRefIdsAndType(List<String> refIds, HeadersMapType type) {
        if (refIds == null || refIds.isEmpty() || type == null) {
            return Map.of();
        }
        refIds = refIds.stream().distinct().toList();
        List<HeadersMapTO> headersMaps = headersMapService.getByRefIdsAndType(refIds, type);

        if (headersMaps.isEmpty()) {
            return Map.of();
        }
        List<String> headerIds = headersMaps.stream().map(HeadersMapTO::headersId).distinct().toList();
        List<HeadersTO> headers = headersMapper.toDomainList(headersRepository.findAllById(headerIds));
        Map<String, HeadersTO> headersMap = headers.stream()
                .collect(Collectors.toMap(HeadersTO::headersId, Function.identity()));
        return headersMaps.stream()
                .collect(Collectors.toMap(HeadersMapTO::refId, map -> headersMap.get(map.headersId())));
    }
}
