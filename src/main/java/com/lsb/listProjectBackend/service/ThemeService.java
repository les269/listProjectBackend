package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.entity.*;
import com.lsb.listProjectBackend.mapper.ThemeMapper;
import com.lsb.listProjectBackend.repository.*;
import com.lsb.listProjectBackend.utils.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
public class ThemeService {
    @Autowired
    private ThemeHeaderRepository themeHeaderRepository;
    @Autowired
    private ThemeCustomValueRepository themeCustomValueRepository;

    private final ThemeMapper themeMapper = ThemeMapper.INSTANCE;

    public List<ThemeHeaderTO> getAllTheme() {
        return themeMapper.headerToDomainList(themeHeaderRepository.findAll());
    }

    public ThemeHeaderTO getByHeaderId(String headerId) {
        Optional<ThemeHeader> themeHeader = themeHeaderRepository.findById(headerId);

        return themeMapper.headerToDomain(themeHeaderRepository.findById(headerId).orElse(null));
    }

    public ThemeHeaderTO findTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = themeMapper.headerToEntity(headerTO);
        themeHeaderRepository.findById(header.getId());

        return themeMapper.headerToDomain(themeHeaderRepository.findById(header.getId()).orElse(null));
    }

    public boolean existTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = themeMapper.headerToEntity(headerTO);
        return themeHeaderRepository.existsById(header.getId());
    }

    @Transactional
    public void updateTheme(@RequestBody ThemeHeaderTO theme) throws Exception {
        ThemeHeader themeHeader = themeMapper.headerToEntity(theme);
        String headerId = themeHeader.getId();
        themeHeader.setHeaderId(headerId);
        themeHeader.setUpdateTime(new Date().getTime());
        themeHeaderRepository.deleteById(headerId);
        themeHeaderRepository.save(themeHeader);
    }

    @Transactional
    public void deleteTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = themeMapper.headerToEntity(headerTO);
        Optional<ThemeHeader> themeHeaderOptional = themeHeaderRepository.findById(header.getId());
        if (themeHeaderOptional.isPresent()) {
            themeHeaderRepository.deleteById(header.getId());
        } else {
            throw new LsbException("主題不存在無法刪除");
        }
    }

    @Transactional
    public void copyTheme(CopyThemeRequest request) {
        ThemeHeader source = themeMapper.headerToEntity(request.getSource());
        ThemeHeader target = themeMapper.headerToEntity(request.getTarget());
        Optional<ThemeHeader> sourceOptional = themeHeaderRepository.findById(source.getId());
        boolean existTarget = themeHeaderRepository.existsById(target.getId());
        if (sourceOptional.isPresent() && !existTarget) {
            String headerId = target.getId();
            target.setHeaderId(headerId);
            target.setUpdateTime(new Date().getTime());
            themeHeaderRepository.save(target);
        } else {
            throw new LsbException("主題不存在無法複製");
        }
    }

    public Map<String, Map<String, String>> findCustomValue(ThemeCustomValueRequest request) {
        List<ThemeCustomValue> list = themeCustomValueRepository.findByHeaderIdAndInCorrDataValue(request.getHeaderId(), request.getValueList());
        //先用data來groupBy,toMap成自定義的byKey跟value
        return list.stream().collect(
                groupingBy(ThemeCustomValue::getCorrespondDataValue,
                        toMap(ThemeCustomValue::getByKey, ThemeCustomValue::getCustomValue)
                ));

    }

    public void updateCustomValue(ThemeCustomValueTO customValueTO){
        themeCustomValueRepository.save(themeMapper.customValueToEntity(customValueTO));
    }
}
