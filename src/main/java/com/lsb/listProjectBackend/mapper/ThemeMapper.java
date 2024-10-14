package com.lsb.listProjectBackend.mapper;


import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.entity.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ThemeMapper {
    ThemeMapper INSTANCE = Mappers.getMapper(ThemeMapper.class);

    ThemeHeader headerToEntity(ThemeHeaderTO to);

    @Mappings({
            @Mapping(target = "themeImage", ignore = true),
            @Mapping(target = "themeLabelList", ignore = true),
            @Mapping(target = "themeDBList", ignore = true)
    })
    ThemeHeader headerToEntity(ThemeHeader entity);
    ThemeHeaderTO headerToDomain(ThemeHeader entity);
    List<ThemeHeaderTO> headerToDomainList(List<ThemeHeader> entity);


    @Mappings({
            @Mapping(target = "themeHeader", ignore = true),
    })
    ThemeImage imageToEntity(ThemeImage entity);
    ThemeImage imageToEntity(ThemeImageTO to);
    ThemeImageTO imageToDomain(ThemeImage entity);

    @Mappings({
            @Mapping(target = "themeHeader", ignore = true),
    })
    ThemeDB dbToEntity(ThemeDB entity);
    List<ThemeDB> dbToEntity(List<ThemeDB> entity);
    ThemeDB dbToEntity(ThemeDBTO to);
    List<ThemeDB> dbListToEntity(List<ThemeDBTO> to);
    ThemeDBTO dbToDomain(ThemeDB entity);
    List<ThemeDBTO> dbListToDomain(List<ThemeDB> entity);

    @Mappings({
            @Mapping(target = "themeHeader", ignore = true),
    })
    ThemeLabel labelToEntity(ThemeLabel entity);
    List<ThemeLabel> labelToEntity(List<ThemeLabel> entity);
    ThemeLabel labelToEntity(ThemeLabelTO to);
    List<ThemeLabel> labelListToEntity(List<ThemeLabelTO> to);
    ThemeLabelTO labelToDomain(ThemeLabel entity);
    List<ThemeLabelTO> labelListToDomain(List<ThemeLabel> entity);

    @Mappings({
            @Mapping(target = "themeHeader", ignore = true),
    })
    ThemeCustom customToEntity(ThemeCustom entity);
    List<ThemeCustom> customToEntity(List<ThemeCustom> entity);
    ThemeCustom customToEntity(ThemeCustomTO to);
    List<ThemeCustom> customListToEntity(List<ThemeCustomTO> to);
    ThemeCustomTO customToDomain(ThemeCustom entity);
    List<ThemeCustomTO> customListToDomain(List<ThemeCustom> entity);
    @BeforeMapping
    default void handleNulls(@MappingTarget ThemeCustom target) {
        if (target.getApiArray() == null) {
            target.setApiArray("");
        }
    }

    ThemeCustomValue customValueToEntity(ThemeCustomValueTO to);
    List<ThemeCustomValue> customValueListToEntity(List<ThemeCustomValueTO> to);
    ThemeCustomValueTO customValueToDomain(ThemeCustomValue entity);
    List<ThemeCustomValueTO> customValueListToDomain(List<ThemeCustomValue> entity);
}
