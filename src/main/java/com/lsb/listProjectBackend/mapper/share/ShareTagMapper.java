package com.lsb.listProjectBackend.mapper.share;

import com.lsb.listProjectBackend.domain.share.ShareTagTO;
import com.lsb.listProjectBackend.domain.share.ShareTagValueTO;
import com.lsb.listProjectBackend.entity.dynamic.share.ShareTag;
import com.lsb.listProjectBackend.entity.dynamic.share.ShareTagValue;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShareTagMapper {
    ShareTagMapper INSTANCE = Mappers.getMapper(ShareTagMapper.class);

    ShareTag toEntity(ShareTagTO to);

    List<ShareTag> toEntity(List<ShareTagTO> list);

    ShareTagTO toDomain(ShareTag entity);

    List<ShareTagTO> toDomain(List<ShareTag> list);

    ShareTagValueTO toDomain(ShareTagValue entity);

    List<ShareTagValueTO> toDomainValueList(List<ShareTagValue> list);
}
