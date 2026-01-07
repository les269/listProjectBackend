package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.domain.ShareTagTO;
import com.lsb.listProjectBackend.domain.ShareTagValueTO;
import com.lsb.listProjectBackend.entity.ShareTagValuePK;
import com.lsb.listProjectBackend.mapper.ShareTagMapper;
import com.lsb.listProjectBackend.mapper.ShareTagValueMapper;
import com.lsb.listProjectBackend.repository.ShareTagMapRepository;
import com.lsb.listProjectBackend.repository.ShareTagRepository;
import com.lsb.listProjectBackend.repository.ShareTagValueRepository;
import com.lsb.listProjectBackend.service.ShareTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

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
    public boolean hasThemeReference(String shareTagId) {
        return shareTagMapRepository.existsByShareTagId(shareTagId) > 0;
    }

    @Override
    public List<ShareTagValueTO> findShareTagValues(List<String> shareTagIds) {
        if (shareTagIds == null || shareTagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return shareTagMapper.toDomainValueList(shareTagValueRepository.findByShareTagIdIn(shareTagIds));
    }

    @Override
    public void addShareTagValue(ShareTagValueTO to) {
        shareTagValueRepository.save(shareTagValueMapper.toEntity(to));
    }

    @Override
    public void deleteShareTagValue(String shareTagId, String value) {
        shareTagValueRepository.deleteById(new ShareTagValuePK(shareTagId, value));
    }
}
