package com.lsb.listProjectBackend.service.theme;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.theme.CopyThemeItemReqTO;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.domain.theme.ThemeItemMapTO;
import com.lsb.listProjectBackend.domain.theme.ThemeItemTO;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeItem;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeItemMap;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeItemMapPK;
import com.lsb.listProjectBackend.entity.dynamic.theme.ThemeItemPK;
import com.lsb.listProjectBackend.mapper.theme.ThemeItemMapMapper;
import com.lsb.listProjectBackend.mapper.theme.ThemeItemMapper;
import com.lsb.listProjectBackend.repository.dynamic.theme.ThemeItemMapRepository;
import com.lsb.listProjectBackend.repository.dynamic.theme.ThemeItemRepository;
import com.lsb.listProjectBackend.service.theme.ThemeItemService;
import com.lsb.listProjectBackend.utils.Global;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@UseDynamic
@Service
@RequiredArgsConstructor
public class ThemeItemServiceImpl implements ThemeItemService {
    private final ThemeItemRepository themeItemRepository;
    private final ThemeItemMapRepository themeItemMapRepository;

    private final ThemeItemMapper themeItemMapper;
    private final ThemeItemMapMapper themeItemMapMapper;

    public void copyThemeItem(CopyThemeItemReqTO req) {
        if (req == null || req.type() == null
                || !StringUtils.hasText(req.sourceItemId())
                || !StringUtils.hasText(req.targetItemId())) {
            throw new LsbException("sourceItemId、targetItemId、type 皆不可為空");
        }

        ThemeItemPK sourcePk = new ThemeItemPK(req.sourceItemId(), req.type());
        ThemeItem source = themeItemRepository.findById(sourcePk)
                .orElseThrow(() -> new LsbException("找不到來源 ThemeItem: " + req.sourceItemId(),
                        org.springframework.http.HttpStatus.NOT_FOUND));

        ThemeItemPK targetPk = new ThemeItemPK(req.targetItemId(), req.type());
        if (themeItemRepository.existsById(targetPk)) {
            throw new LsbException("目標 ThemeItem 已存在: " + req.targetItemId());
        }

        ThemeItem target = new ThemeItem();
        target.setItemId(req.targetItemId());
        target.setType(req.type());
        target.setJson(source.getJson());
        target.setDescription(source.getDescription());
        themeItemRepository.save(target);
    }

    public ThemeItemTO getThemeItemById(Global.ThemeItemType type, String itemId) {
        ThemeItem item = themeItemRepository.findById(new ThemeItemPK(itemId, type))
                .orElse(null);
        return themeItemMapper.toDomain(item);
    }

    public void updateThemeItem(ThemeItemTO req) {
        themeItemRepository.save(themeItemMapper.toEntity(req));
    }

    public void deleteThemeItem(Global.ThemeItemType type, String itemId) {
        themeItemRepository.deleteById(new ThemeItemPK(itemId, type));
    }

    public List<ThemeItemTO> getAllThemeItem(Global.ThemeItemType type) {
        return themeItemMapper.toDomain(themeItemRepository.findAllByType(type.name()));
    }

    public List<ThemeItemMapTO> getAllThemeItemMapByType(Global.ThemeItemType type) {
        return themeItemMapMapper.toDomain(themeItemMapRepository.findAllByType(type.name()));
    }

    public List<ThemeItemTO> getItemsByHeaderId(String headerId) {
        return themeItemMapper.toDomain(themeItemRepository.findAllByHeaderId(headerId));
    }

    public void updateThemeItemMap(ThemeItemMapTO req) {
        themeItemMapRepository.save(themeItemMapMapper.toEntity(req));
    }

    public void deleteThemeItemMap(Global.ThemeItemType type, String headerId) {
        themeItemMapRepository.deleteById(new ThemeItemMapPK(headerId, type));
    }

    public void deleteThemeItemMapByHeaderId(String headerId) {
        List<ThemeItemMap> maps = themeItemMapRepository.findAllByHeaderId(headerId);
        themeItemMapRepository.deleteAll(maps);
    }

    public boolean themeItemMapInUse(Global.ThemeItemType type, String itemId) {
        return themeItemMapRepository.existsByItemIdAndType(itemId, type.name()) > 0;
    }

    public void copyMapping(String sourceHeaderId, String targetHeaderId) {
        List<ThemeItemMap> maps = themeItemMapRepository.findAllByHeaderId(sourceHeaderId).stream().map(x -> {
            var newMap = new ThemeItemMap();
            newMap.setItemId(x.getItemId());
            newMap.setHeaderId(targetHeaderId);
            newMap.setType(x.getType());
            return newMap;
        }).toList();
        themeItemMapRepository.saveAll(maps);
    }
}
