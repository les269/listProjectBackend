package com.lsb.listProjectBackend.mapper.share;

import com.lsb.listProjectBackend.domain.share.ShareTagValueTO;
import com.lsb.listProjectBackend.entity.dynamic.share.ShareTagValue;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShareTagValueMapper {
    ShareTagValueMapper INSTANCE = Mappers.getMapper(ShareTagValueMapper.class);

    ShareTagValue toEntity(ShareTagValueTO to);

    List<ShareTagValue> toEntity(List<ShareTagValueTO> list);

    ShareTagValueTO toDomain(ShareTagValue entity);

    List<ShareTagValueTO> toDomain(List<ShareTagValue> list);
}
