package com.lsb.listProjectBackend.service.share;

import com.lsb.listProjectBackend.domain.share.ShareTagTO;
import com.lsb.listProjectBackend.domain.share.ShareTagValueDeleteListTO;
import com.lsb.listProjectBackend.domain.share.ShareTagValueTO;

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
