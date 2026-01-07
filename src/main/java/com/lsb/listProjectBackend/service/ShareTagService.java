package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ShareTagTO;
import com.lsb.listProjectBackend.domain.ShareTagValueTO;

import java.util.List;

public interface ShareTagService {
    List<ShareTagTO> getAllTag();

    void addTag(ShareTagTO to);

    void deleteTag(String shareTagId);

    boolean hasThemeReference(String shareTagId);

    List<ShareTagValueTO> findShareTagValues(List<String> shareTagIds);

    void addShareTagValue(ShareTagValueTO to);

    void deleteShareTagValue(String shareTagId, String value);

}
