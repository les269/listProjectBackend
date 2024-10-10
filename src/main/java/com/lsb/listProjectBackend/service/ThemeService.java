package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.*;
import com.lsb.listProjectBackend.entity.*;
import com.lsb.listProjectBackend.mapper.ThemeMapper;
import com.lsb.listProjectBackend.repository.ThemeDBRepository;
import com.lsb.listProjectBackend.repository.ThemeHeaderRepository;
import com.lsb.listProjectBackend.repository.ThemeImageRepository;
import com.lsb.listProjectBackend.repository.ThemeLabelRepository;
import com.lsb.listProjectBackend.utils.Global;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ThemeService {
    @Autowired
    private ThemeHeaderRepository themeHeaderRepository;
    @Autowired
    private ThemeImageRepository themeImageRepository;
    @Autowired
    private ThemeDBRepository themeDBRepository;
    @Autowired
    private ThemeLabelRepository themeLabelRepository;

    public List<ThemeHeaderTO> getAllTheme() {
        return ThemeMapper.INSTANCE.headerToDomainList(themeHeaderRepository.findAll());
    }
    public ThemeResponse getByHeaderId(String headerId) {
        ThemeResponse res = new ThemeResponse();
        Optional<ThemeHeader> themeHeader = themeHeaderRepository.findById(headerId);
        themeHeader.ifPresent(value -> {
            res.setThemeHeader(ThemeMapper.INSTANCE.headerToDomain(value));
            // 為圖片模式時找圖片設定
            if (Global.ThemeHeaderType.imageList.equals(value.getType())) {
                res.setThemeImage(ThemeMapper.INSTANCE.imageToDomain(value.getThemeImage()));
            }
            //設定標籤清單
            res.setThemeLabelList(ThemeMapper.INSTANCE.labelListToDomain(value.getThemeLabelList()));
            // 設定DB清單
            res.setThemeDBList(ThemeMapper.INSTANCE.dbListToDomain(value.getThemeDBList()));
        });
        return res;
    }
    public ThemeResponse findTheme(ThemeHeaderTO headerTO) {
        ThemeResponse res = new ThemeResponse();

        ThemeHeader header = ThemeMapper.INSTANCE.headerToEntity(headerTO);
        Optional<ThemeHeader> themeHeader = themeHeaderRepository.findById(header.getId());
        //header存在則往下找
        themeHeader.ifPresent(value -> {
            res.setThemeHeader(ThemeMapper.INSTANCE.headerToDomain(value));
            // 為圖片模式時找圖片設定
            if (Global.ThemeHeaderType.imageList.equals(value.getType())) {
                res.setThemeImage(ThemeMapper.INSTANCE.imageToDomain(value.getThemeImage()));
            }
            //設定標籤清單
            res.setThemeLabelList(ThemeMapper.INSTANCE.labelListToDomain(value.getThemeLabelList()));
            // 設定DB清單
            res.setThemeDBList(ThemeMapper.INSTANCE.dbListToDomain(value.getThemeDBList()));
        });
        return res;
    }

    public boolean existTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = ThemeMapper.INSTANCE.headerToEntity(headerTO);
        return themeHeaderRepository.existsById(header.getId());
    }

    @Transactional
    public void updateTheme(@RequestBody ThemeRequest theme) throws Exception {
        ThemeHeader themeHeader = ThemeMapper.INSTANCE.headerToEntity(theme.getThemeHeader());
        String headerId = themeHeader.getId();
        themeHeader.setHeaderId(headerId);
        themeHeader.setUpdateTime(new Date().getTime());
        //設定image
        if (Global.ThemeHeaderType.imageList.equals(themeHeader.getType())) {
            ThemeImage themeImage = ThemeMapper.INSTANCE.imageToEntity(theme.getThemeImage());
            themeImage.setHeaderId(headerId);
            themeHeader.setThemeImage(themeImage);
        }
        //創建db設定
        AtomicInteger i = new AtomicInteger(1);
        List<ThemeDB> themeDBList = ThemeMapper.INSTANCE.dbListToEntity(theme.getThemeDBList()).stream()
                .peek(x -> {
                    x.setId(i.getAndIncrement());
                    x.setHeaderId(headerId);
                    x.setThemeHeader(themeHeader);
                })
                .toList();
        themeHeader.setThemeDBList(themeDBList);
        //創建label設定
        i.set(1);
        List<ThemeLabel> themeLabelList = ThemeMapper.INSTANCE.labelListToEntity(theme.getThemeLabelList()).stream()
                .peek(x -> {
                    x.setId(i.getAndIncrement());
                    x.setHeaderId(headerId);
                    x.setThemeHeader(themeHeader);
                })
                .toList();
        themeHeader.setThemeLabelList(themeLabelList);
        themeHeaderRepository.deleteById(headerId);
        themeHeaderRepository.save(themeHeader);

    }

    @Transactional
    public void deleteTheme(ThemeHeaderTO headerTO) {
        ThemeHeader header = ThemeMapper.INSTANCE.headerToEntity(headerTO);
        Optional<ThemeHeader> themeHeaderOptional = themeHeaderRepository.findById(header.getId());
        if (themeHeaderOptional.isPresent()) {
            themeHeaderRepository.deleteById(header.getId());
        } else {
            throw new LsbException("主題不存在無法刪除");
        }
    }

    @Transactional
    public void copyTheme(CopyThemeRequest request) {
        ThemeHeader source = ThemeMapper.INSTANCE.headerToEntity(request.getSource());
        ThemeHeader target = ThemeMapper.INSTANCE.headerToEntity(request.getTarget());
        Optional<ThemeHeader> sourceOptional = themeHeaderRepository.findById(source.getId());
        boolean existTarget = themeHeaderRepository.existsById(target.getId());
        if (sourceOptional.isPresent() && !existTarget) {
            String headerId = target.getId();
            target.setHeaderId(headerId);
            target.setUpdateTime(new Date().getTime());

            if (Global.ThemeHeaderType.imageList.equals(target.getType())) {
                ThemeImage themeImage = ThemeMapper.INSTANCE.imageToEntity(sourceOptional.get().getThemeImage());
                themeImage.setHeaderId(headerId);
                target.setThemeImage(themeImage);
            }


            List<ThemeLabel> themeLabelList = ThemeMapper.INSTANCE.labelToEntity(sourceOptional.get().getThemeLabelList())
                    .stream().peek(x -> {
                        x.setHeaderId(headerId);
                        x.setThemeHeader(target);
                    }).toList();
            target.setThemeLabelList(themeLabelList);

            List<ThemeDB> themeDBList = ThemeMapper.INSTANCE.dbToEntity(sourceOptional.get().getThemeDBList())
                    .stream().peek(x -> {
                        x.setHeaderId(headerId);
                        x.setThemeHeader(target);
                    }).toList();
            target.setThemeDBList(themeDBList);

            themeHeaderRepository.save(target);
        } else {
            throw new LsbException("主題不存在無法複製");
        }
    }
}
