package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ShareTagTO;
import com.lsb.listProjectBackend.domain.ShareTagValueDeleteListTO;
import com.lsb.listProjectBackend.domain.ShareTagValueTO;

import java.util.List;

public interface ShareTagService {
    List<ShareTagTO> getAllTag();

    void addTag(ShareTagTO to);

    void deleteTag(String shareTagId);

    List<String> getThemeHeaderIdsByShareTagId(String shareTagId);

    List<ShareTagValueTO> findShareTagValues(List<String> shareTagIds);

    List<ShareTagValueTO> findShareTagValueById(String shareTagId);

    void addShareTagValue(ShareTagValueTO to);

    void deleteShareTagValue(String shareTagId, String value);

    void deleteShareTagValueList(ShareTagValueDeleteListTO to);

}
