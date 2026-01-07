package com.lsb.listProjectBackend.mapper;

import com.lsb.listProjectBackend.domain.ShareTagTO;
import com.lsb.listProjectBackend.domain.ShareTagValueTO;
import com.lsb.listProjectBackend.entity.ShareTag;
import com.lsb.listProjectBackend.entity.ShareTagValue;
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
