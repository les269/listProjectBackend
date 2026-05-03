package com.lsb.listProjectBackend.mapper.share;

import com.lsb.listProjectBackend.domain.share.ShareTagValueTO;
import com.lsb.listProjectBackend.entity.dynamic.share.ShareTagValue;

import com.lsb.listProjectBackend.mapper.SpringAndIgnoreUnmappedMapperConfig;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = SpringAndIgnoreUnmappedMapperConfig.class)
public interface ShareTagValueMapper {

    ShareTagValue toEntity(ShareTagValueTO to);

    List<ShareTagValue> toEntity(List<ShareTagValueTO> list);

    ShareTagValueTO toDomain(ShareTagValue entity);

    List<ShareTagValueTO> toDomain(List<ShareTagValue> list);
}
