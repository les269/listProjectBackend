package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.ShareTagTO;
import com.lsb.listProjectBackend.domain.ShareTagValueDeleteListTO;
import com.lsb.listProjectBackend.domain.ShareTagValueTO;
import com.lsb.listProjectBackend.entity.dynamic.ShareTagValuePK;
import com.lsb.listProjectBackend.mapper.ShareTagMapper;
import com.lsb.listProjectBackend.mapper.ShareTagValueMapper;
import com.lsb.listProjectBackend.repository.dynamic.ShareTagMapRepository;
import com.lsb.listProjectBackend.repository.dynamic.ShareTagRepository;
import com.lsb.listProjectBackend.repository.dynamic.ShareTagValueRepository;
import com.lsb.listProjectBackend.service.ShareTagService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@UseDynamic
@Service
public class ShareTagServiceImpl implements ShareTagService {

    @Autowired
    private ShareTagRepository shareTagRepository;
    @Autowired
    private ShareTagMapRepository shareTagMapRepository;
    @Autowired
    private ShareTagValueRepository shareTagValueRepository;

    private final ShareTagMapper shareTagMapper = ShareTagMapper.INSTANCE;
    private final ShareTagValueMapper shareTagValueMapper = ShareTagValueMapper.INSTANCE;

    @Override
    public List<ShareTagTO> getAllTag() {
        return shareTagMapper.toDomain(shareTagRepository.findAll());
    }

    @Override
    public void addTag(ShareTagTO to) {
        shareTagRepository.save(shareTagMapper.toEntity(to));
    }

    @Override
    public void deleteTag(String shareTagId) {
        shareTagRepository.deleteById(shareTagId);
    }

    @Override
    public List<String> getThemeHeaderIdsByShareTagId(String shareTagId) {
        if (shareTagId == null || shareTagId.isEmpty()) {
            return Collections.emptyList();
        }
        return shareTagMapRepository.findThemeHeaderIdsByShareTagId(shareTagId);
    }

    @Override
    public List<ShareTagValueTO> findShareTagValues(List<String> shareTagIds) {
        if (shareTagIds == null || shareTagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return shareTagMapper.toDomainValueList(shareTagValueRepository.findByShareTagIdIn(shareTagIds));
    }

    @Override
    public List<ShareTagValueTO> findShareTagValueById(String shareTagId) {
        if (shareTagId == null || shareTagId.isEmpty()) {
            return Collections.emptyList();
        }
        return shareTagMapper.toDomainValueList(shareTagValueRepository.findByShareTagId(shareTagId));
    }

    @Override
    public void addShareTagValue(ShareTagValueTO to) {
        shareTagValueRepository.save(shareTagValueMapper.toEntity(to));
    }

    @Override
    public void deleteShareTagValue(String shareTagId, String value) {
        shareTagValueRepository.deleteById(new ShareTagValuePK(shareTagId, value));
    }

    @Override
    public void deleteShareTagValueList(ShareTagValueDeleteListTO to) {
        if (to == null || to.getShareTagId() == null || to.getValues() == null || to.getValues().isEmpty()) {
            return;
        }
        String shareTagId = to.getShareTagId();
        var shareTagValuePKs = to.getValues().stream().map(value -> new ShareTagValuePK(shareTagId, value)).toList();
        shareTagValueRepository.deleteAllById(shareTagValuePKs);
    }
}
