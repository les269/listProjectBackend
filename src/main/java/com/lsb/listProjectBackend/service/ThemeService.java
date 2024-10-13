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

    public ThemeResponse getByHeaderId(String headerId) {
        ThemeResponse res = new ThemeResponse();
        Optional<ThemeHeader> themeHeader = themeHeaderRepository.findById(headerId);
        themeHeader.ifPresent(value -> {
            res.setThemeHeader(themeMapper.headerToDomain(value));
            // 為圖片模式時找圖片設定
            if (Global.ThemeHeaderType.imageList.equals(value.getType())) {
                res.setThemeImage(themeMapper.imageToDomain(value.getThemeImage()));
            }
            //設定標籤清單
            res.setThemeLabelList(themeMapper.labelListToDomain(value.getThemeLabelList()));
            // 設定DB清單
            res.setThemeDBList(themeMapper.dbListToDomain(value.getThemeDBList()));
            //設定客自化內容
            res.setThemeCustomList(themeMapper.customListToDomain(value.getThemeCustomList()));
        });
        return res;
    }

    public ThemeResponse findTheme(ThemeHeaderTO headerTO) {
        ThemeResponse res = new ThemeResponse();

        ThemeHeader header = themeMapper.headerToEntity(headerTO);
        Optional<ThemeHeader> themeHeader = themeHeaderRepository.findById(header.getId());
        //header存在則往下找
        themeHeader.ifPresent(value -> {
            res.setThemeHeader(themeMapper.headerToDomain(value));
            // 為圖片模式時找圖片設定
            if (Global.ThemeHeaderType.imageList.equals(value.getType())) {
                res.setThemeImage(themeMapper.imageToDomain(value.getThemeImage()));
            }
            //設定標籤清單
            res.setThemeLabelList(themeMapper.labelListToDomain(value.getThemeLabelList()));
            // 設定DB清單
            res.setThemeDBList(themeMapper.dbListToDomain(value.getThemeDBList()));
            //設定客自化內容
            res.setThemeCustomList(themeMapper.customListToDomain(value.getThemeCustomList()));
        });
        return res;
    }

    public boolean existTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = themeMapper.headerToEntity(headerTO);
        return themeHeaderRepository.existsById(header.getId());
    }

    @Transactional
    public void updateTheme(@RequestBody ThemeRequest theme) throws Exception {
        ThemeHeader themeHeader = themeMapper.headerToEntity(theme.getThemeHeader());
        String headerId = themeHeader.getId();
        themeHeader.setHeaderId(headerId);
        themeHeader.setUpdateTime(new Date().getTime());
        //設定image
        if (Global.ThemeHeaderType.imageList.equals(themeHeader.getType())) {
            ThemeImage themeImage = themeMapper.imageToEntity(theme.getThemeImage());
            themeImage.setHeaderId(headerId);
            themeHeader.setThemeImage(themeImage);
        }
        //創建db設定
        AtomicInteger i = new AtomicInteger(1);
        List<ThemeDB> themeDBList = themeMapper.dbListToEntity(theme.getThemeDBList()).stream()
                .peek(x -> {
                    x.setId(i.getAndIncrement());
                    x.setHeaderId(headerId);
                    x.setThemeHeader(themeHeader);
                })
                .toList();
        themeHeader.setThemeDBList(themeDBList);
        //創建label設定
        i.set(1);
        List<ThemeLabel> themeLabelList = themeMapper.labelListToEntity(theme.getThemeLabelList()).stream()
                .peek(x -> {
                    x.setId(i.getAndIncrement());
                    x.setHeaderId(headerId);
                    x.setThemeHeader(themeHeader);
                })
                .toList();
        themeHeader.setThemeLabelList(themeLabelList);

        //創建客自化
        i.set(1);
        List<ThemeCustom> themeCustomList = themeMapper.customListToEntity(theme.getThemeCustomList()).stream()
                .peek(x -> {
                    x.setId(i.getAndIncrement());
                    x.setHeaderId(headerId);
                    x.setThemeHeader(themeHeader);
                })
                .toList();
        themeHeader.setThemeCustomList(themeCustomList);

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

            if (Global.ThemeHeaderType.imageList.equals(target.getType())) {
                ThemeImage themeImage = themeMapper.imageToEntity(sourceOptional.get().getThemeImage());
                themeImage.setHeaderId(headerId);
                target.setThemeImage(themeImage);
            }


            List<ThemeLabel> themeLabelList = themeMapper.labelToEntity(sourceOptional.get().getThemeLabelList())
                    .stream().peek(x -> {
                        x.setHeaderId(headerId);
                        x.setThemeHeader(target);
                    }).toList();
            target.setThemeLabelList(themeLabelList);

            List<ThemeDB> themeDBList = themeMapper.dbToEntity(sourceOptional.get().getThemeDBList())
                    .stream().peek(x -> {
                        x.setHeaderId(headerId);
                        x.setThemeHeader(target);
                    }).toList();
            target.setThemeDBList(themeDBList);

            List<ThemeCustom> themeCustomList = themeMapper.customToEntity(sourceOptional.get().getThemeCustomList())
                    .stream().peek(x -> {
                        x.setHeaderId(headerId);
                        x.setThemeHeader(target);
                    }).toList();
            target.setThemeCustomList(themeCustomList);

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
