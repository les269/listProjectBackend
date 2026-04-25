package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.ReplaceValueMapTO;

import java.util.List;

public interface ReplaceValueMapService {
    List<ReplaceValueMapTO> getNameList();

    List<ReplaceValueMapTO> getAllByNameList(List<String> nameList);

    boolean existMap(String name);

    ReplaceValueMapTO getByName(String name);

    void update(ReplaceValueMapTO to);

    void deleteByName(String name);
}
