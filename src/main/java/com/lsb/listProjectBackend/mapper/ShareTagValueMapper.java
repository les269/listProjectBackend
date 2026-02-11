package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.ShareTagTO;
import com.lsb.listProjectBackend.domain.ShareTagValueTO;
import com.lsb.listProjectBackend.entity.dynamic.ShareTag;
import com.lsb.listProjectBackend.entity.dynamic.ShareTagValue;

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
