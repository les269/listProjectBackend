package com.lsb.listProjectBackend.service;

import com.lsb.listProjectBackend.domain.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface ThemeService {
    List<ThemeHeaderTO> getAllTheme();
    ThemeHeaderTO getByHeaderId(String headerId);
    ThemeHeaderTO findTheme(ThemeHeaderTO headerTO);
    boolean existTheme(ThemeHeaderTO headerTO);
    void updateTheme(@RequestBody ThemeHeaderTO theme);
    void deleteTheme(ThemeHeaderTO headerTO);
    void copyTheme(CopyThemeRequest request);
    Map<String, Map<String, String>> findCustomValue(ThemeCustomValueRequest request);
    void updateCustomValue(ThemeCustomValueTO customValueTO);
    void updateTagValue(List<ThemeTagValueTO> req);
    List<ThemeTagValueTO> getTagValueList(String headerId);
    Map<String, String> findTopCustomValue(String headerId);
    void updateTopCustomValue(ThemeTopCustomValueTO topCustomValueTO);
}
