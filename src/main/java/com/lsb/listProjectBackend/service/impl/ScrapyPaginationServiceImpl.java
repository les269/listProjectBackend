package com.lsb.listProjectBackend.service.impl;

import com.lsb.listProjectBackend.aop.UseDynamic;
import com.lsb.listProjectBackend.domain.ScrapyPaginationTO;
import com.lsb.listProjectBackend.domain.ScrapyPaginationTestTO;
import com.lsb.listProjectBackend.entity.dynamic.Cookie;
import com.lsb.listProjectBackend.mapper.ScrapyPaginationMapper;
import com.lsb.listProjectBackend.repository.dynamic.ScrapyPaginationRepository;
import com.lsb.listProjectBackend.service.ScrapyPaginationService;
import com.lsb.listProjectBackend.utils.Utils;

import lombok.extern.slf4j.Slf4j;

import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@UseDynamic
@Service
public class ScrapyPaginationServiceImpl extends ScrapyBase implements ScrapyPaginationService {

    @Autowired
    private ScrapyPaginationRepository scrapyPaginationRepository;

    private final ScrapyPaginationMapper scrapyPaginationMapper = ScrapyPaginationMapper.INSTANCE;

    @Override
    public ScrapyPaginationTO get(String name) {
        return scrapyPaginationMapper.toDomain(scrapyPaginationRepository.findById(name).orElse(null));
    }

    @Override
    public void update(ScrapyPaginationTO req) {
        var entity = scrapyPaginationMapper.toEntity(req);
        scrapyPaginationRepository.save(entity);
    }

    @Override
    public void delete(String name) {
        scrapyPaginationRepository.deleteById(name);
    }

    @Override
    public boolean exist(String name) {
        return scrapyPaginationRepository.existsById(name);
    }

    @Override
    public List<ScrapyPaginationTO> getAll() {
        return scrapyPaginationMapper.toDomainList(scrapyPaginationRepository.findAll());
    }

    @Override
    public Map<String, Object> testHtml(ScrapyPaginationTestTO to) {
        Map<String, Object> result = new HashMap<>();
        useCssSelect(to.getHtml(), to.getConfig().getCssSelectList(), result);
        return result;
    }

    @Override
    public ScrapyPaginationTO updateRedirectData(String name) {
        var entityOp = scrapyPaginationRepository.findById(name);
        if (entityOp.isPresent()) {
            Map<String, Object> result = new HashMap<>();
            var entity = entityOp.get();
            var config = entityOp.get().getConfig();
            var redirectUrl = config.getStartUrl();
            Map<String, String> cookies = config.getCookie().stream()
                    .collect(Collectors.toMap(Cookie::getName, Cookie::getValue));
            try {
                while (Utils.isNotBlank(redirectUrl)) {
                    Document document = getConnection(redirectUrl)
                            .cookies(cookies)
                            .postDataCharset("UTF-8")
                            .get();
                    useCssSelect(document.html(), config.getCssSelectList(), result);
                    redirectUrl = (String) result.get("__next_page");
                }

                List<String> keys = (List<String>) result.computeIfAbsent("__item_key", k -> new ArrayList<>());
                List<String> urls = (List<String>) result.computeIfAbsent("__item_url", k -> new ArrayList<>());
                var keyRedirectUrlMap = new HashMap<String, String>();
                for (int i = 0; i < keys.size(); i++) {
                    keyRedirectUrlMap.put(keys.get(i), urls.get(i));
                }
                config.setKeyRedirectUrlMap(keyRedirectUrlMap);
                config.setCurrentUpdateDate(new Date());
                entity.setConfig(config);
                scrapyPaginationRepository.save(entity);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.get(name);
    }

    @Override
    public boolean checkForUpdate(String name) {
        var to = this.get(name);
        if (to == null || to.getConfig() == null) {
            return false;
        }
        var config = to.getConfig();
        if (config.getCurrentUpdateDate() == null || config.getLastUpdateDate() == null) {
            return false;
        }
        if (config.getCurrentUpdateDate().after(config.getLastUpdateDate())) {
            return false;
        }
        LocalDate localDate = config.getCurrentUpdateDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        var localDatePlus = switch (config.getUpdateIntervalType()) {
            case day -> localDate.plusDays(config.getUpdateInterval());
            case month -> localDate.plusMonths(config.getUpdateInterval());
            case year -> localDate.plusYears(config.getUpdateInterval());
        };
        return !localDatePlus.isAfter(LocalDate.now());
    }
}
