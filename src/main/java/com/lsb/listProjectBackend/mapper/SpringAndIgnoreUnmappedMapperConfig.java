package com.lsb.listProjectBackend.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SpringAndIgnoreUnmappedMapperConfig {
}

