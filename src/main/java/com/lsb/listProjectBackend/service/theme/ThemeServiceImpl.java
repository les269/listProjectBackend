package com.lsb.listProjectBackend.service.theme;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.theme.*;
import com.lsb.listProjectBackend.entity.dynamic.share.ShareTagMap;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeCustomValue;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeHeader;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTag;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTagValue;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeTopCustomValue;
import com.lsb.listProjectBackend.mapper.theme.ThemeMapper;
import com.lsb.listProjectBackend.mapper.theme.ThemeTagValueMapper;
import com.lsb.listProjectBackend.repository.dynamic.share.ShareTagMapRepository;
import com.lsb.listProjectBackend.repository.dynamic.theme.ThemeCustomValueRepository;
import com.lsb.listProjectBackend.repository.dynamic.theme.ThemeHeaderRepository;
import com.lsb.listProjectBackend.repository.dynamic.theme.ThemeTagValueRepository;
import com.lsb.listProjectBackend.repository.dynamic.theme.ThemeTopCustomValueRepository;
import com.lsb.listProjectBackend.service.theme.ThemeItemService;
import com.lsb.listProjectBackend.service.theme.ThemeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class ThemeServiceImpl implements ThemeService {
    private final ThemeHeaderRepository themeHeaderRepository;
    private final ThemeCustomValueRepository themeCustomValueRepository;
    private final ThemeTopCustomValueRepository themeTopCustomValueRepository;
    private final ThemeTagValueRepository themeTagValueRepository;
    private final ShareTagMapRepository shareTagMapRepository;
    private final ThemeItemService themeItemService;

    private final ThemeMapper themeMapper;
    private final ThemeTagValueMapper themeTagValueMapper;

    public List<ThemeHeaderTO> getAllTheme() {
        return themeHeaderRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    public ThemeHeaderTO getByHeaderId(String headerId) {
        Optional<ThemeHeader> themeHeader = themeHeaderRepository.findById(headerId);

        return themeMapper.headerToDomain(themeHeader.orElse(null));
    }

    public ThemeHeaderTO findTheme(ThemeHeaderTO headerTO) {
        ThemeHeader themeHeader = themeMapper.headerToEntity(headerTO);

        return themeMapper.headerToDomain(themeHeaderRepository.findById(themeHeader.getId()).orElse(null));
    }

    public boolean existTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = themeMapper.headerToEntity(headerTO);
        return themeHeaderRepository.existsById(header.getId());
    }

    public void updateTheme(@RequestBody ThemeHeaderTO theme) {
        ThemeHeader themeHeader = themeMapper.headerToEntity(theme);
        String headerId = themeHeader.getId();
        themeHeader.setHeaderId(headerId);
        themeHeader.setUpdateTime(new Date().getTime());
        themeHeaderRepository.deleteById(headerId);
        themeHeaderRepository.save(themeHeader);
        ThemeHeaderTO latest = getByHeaderId(headerId);
        syncShareTagMappings(headerId, latest == null ? null : latest.getThemeTagList());
    }

    public void deleteTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = themeMapper.headerToEntity(headerTO);
        Optional<ThemeHeader> themeHeaderOptional = themeHeaderRepository.findById(header.getId());
        if (themeHeaderOptional.isPresent()) {
            themeHeaderRepository.deleteById(header.getId());
            themeItemService.deleteThemeItemMapByHeaderId(header.getId());
        } else {
            throw new LsbException("主題不存在無法刪除", org.springframework.http.HttpStatus.NOT_FOUND);
        }
    }

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
            themeItemService.copyMapping(source.getId(), headerId);
            ThemeHeaderTO sourceTheme = themeMapper.headerToDomain(sourceOptional.get());
            syncShareTagMappings(headerId, sourceTheme.getThemeTagList());
        } else {
            throw new LsbException("主題不存在無法複製", org.springframework.http.HttpStatus.NOT_FOUND);
        }
    }

    public Map<String, Map<String, String>> findCustomValue(ThemeCustomValueRequest request) {
        List<ThemeCustomValue> list = themeCustomValueRepository.findByHeaderIdAndInCorrDataValue(request.getHeaderId(),
                request.getValueList());
        // 先用data來groupBy,toMap成自定義的byKey跟value
        return list.stream().collect(
                groupingBy(ThemeCustomValue::getCorrespondDataValue,
                        toMap(ThemeCustomValue::getByKey, ThemeCustomValue::getCustomValue)));

    }

    public void updateCustomValue(ThemeCustomValueTO customValueTO) {
        themeCustomValueRepository.save(themeMapper.customValueToEntity(customValueTO));
    }

    public void updateTagValue(List<ThemeTagValueTO> req) {
        List<ThemeTagValue> list = themeTagValueMapper.toEntityList(req);
        themeTagValueRepository.saveAll(list);
    }

    public void updateSingleTagValue(ThemeTagValueTO req) {
        ThemeTagValue entity = themeTagValueMapper.toEntity(req);
        themeTagValueRepository.save(entity);
    }

    public List<ThemeTagValueTO> getTagValueList(String headerId) {
        var result = themeTagValueMapper.toDomainList(themeTagValueRepository.findByHeaderId(headerId));
        themeHeaderRepository.findById(headerId).ifPresent(theme -> {
            ThemeHeaderTO current = themeMapper.headerToDomain(theme);
            if (current == null || current.getThemeTagList() == null) {
                return;
            }
            for (var tag : current.getThemeTagList()) {
                String shareTagId = tag.getShareTagId();
                if (!StringUtils.hasText(shareTagId)) {
                    continue;
                }
                var tagValue = result.stream().filter(x -> shareTagId.equals(x.getTag())).findFirst();
                if (tagValue.isEmpty()) {
                    ThemeTagValueTO themeTagValueTO = new ThemeTagValueTO();
                    themeTagValueTO.setHeaderId(headerId);
                    themeTagValueTO.setTag(shareTagId);
                    themeTagValueTO.setValueList(new ArrayList<>());
                    result.add(themeTagValueTO);
                }
            }
        });
        return result;
    }

    public Map<String, String> findTopCustomValue(String headerId) {
        List<ThemeTopCustomValue> list = themeTopCustomValueRepository.findByHeaderId(headerId);
        // 先用data來groupBy,toMap成自定義的byKey跟value
        return list.stream().collect(
                Collectors.toMap(ThemeTopCustomValue::getByKey, ThemeTopCustomValue::getCustomValue));
    }

    public void updateTopCustomValue(ThemeTopCustomValueTO topCustomValueTO) {
        themeTopCustomValueRepository.save(themeMapper.topCustomValueToEntity(topCustomValueTO));
    }

    private ThemeHeaderTO toSummary(ThemeHeader entity) {
        ThemeHeaderTO summary = new ThemeHeaderTO();
        summary.setName(entity.getName());
        summary.setVersion(entity.getVersion());
        summary.setTitle(entity.getTitle());
        summary.setType(entity.getType());
        summary.setSeq(entity.getSeq());
        summary.setThemeCustomList(entity.getThemeCustomList());
        summary.setThemeImage(entity.getThemeImage());
        summary.setThemeLabelList(entity.getThemeLabelList());
        summary.setThemeDatasetList(entity.getThemeDatasetList());
        summary.setThemeTagList(entity.getThemeTagList());
        summary.setThemeOtherSetting(entity.getThemeOtherSetting());
        return summary;
    }

    private void syncShareTagMappings(String headerId, List<ThemeTag> themeTagList) {
        List<ShareTagMap> existingMappings = shareTagMapRepository.findByThemeHeaderId(headerId);
        Set<String> desiredShareTagIds = themeTagList == null
                ? Collections.emptySet()
                : themeTagList.stream()
                        .map(ThemeTag::getShareTagId)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toSet());

        Set<String> currentShareTagIds = existingMappings.stream()
                .map(ShareTagMap::getShareTagId)
                .collect(Collectors.toSet());

        List<ShareTagMap> toRemove = existingMappings.stream()
                .filter(mapping -> !desiredShareTagIds.contains(mapping.getShareTagId()))
                .collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            shareTagMapRepository.deleteAll(toRemove);
        }

        List<ShareTagMap> toAdd = desiredShareTagIds.stream()
                .filter(shareTagId -> !currentShareTagIds.contains(shareTagId))
                .map(shareTagId -> {
                    ShareTagMap shareTagMap = new ShareTagMap();
                    shareTagMap.setShareTagId(shareTagId);
                    shareTagMap.setThemeHeaderId(headerId);
                    return shareTagMap;
                })
                .collect(Collectors.toList());
        if (!toAdd.isEmpty()) {
            shareTagMapRepository.saveAll(toAdd);
        }
    }
}
