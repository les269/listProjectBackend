import com.lsb.listProjectBackend.utils.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UtilsTest {

    // ---------- capitalizeFirstLetterByFirstUpper ----------

    @Test
    public void capitalize_normalEnglish() {
        // 一般英文小寫開頭
        assertEquals("Hello", Utils.capitalizeFirstLetterByFirstUpper("hello"));
    }

    @Test
    public void capitalize_alreadyUpperCase() {
        // 已是大寫，應保持不變
        assertEquals("Hello", Utils.capitalizeFirstLetterByFirstUpper("Hello"));
    }

    @Test
    public void capitalize_chineseThenEnglish() {
        // 中文開頭 + 英文 → 找到第一個英文字母大寫
        assertEquals("測試Hello", Utils.capitalizeFirstLetterByFirstUpper("測試hello"));
    }

    @Test
    public void capitalize_chineseThenLowerEnglish() {
        // 中文 + 小寫英文 → 第一個英文字母應轉大寫
        assertEquals("測試Hello world", Utils.capitalizeFirstLetterByFirstUpper("測試hello world"));
    }

    @Test
    public void capitalize_onlyChinese() {
        // 全中文，沒有英文字母 → 原字串不變
        assertEquals("測試中文", Utils.capitalizeFirstLetterByFirstUpper("測試中文"));
    }

    @Test
    public void capitalize_chineseWithNumber() {
        // 數字 + 中文 + 英文 → 跳過數字與中文，找到第一個英文大寫
        assertEquals("123測試Hello", Utils.capitalizeFirstLetterByFirstUpper("123測試hello"));
    }

    @Test
    public void capitalize_emptyString() {
        assertEquals("", Utils.capitalizeFirstLetterByFirstUpper(""));
    }

    @Test
    public void capitalize_nullString() {
        assertNull(Utils.capitalizeFirstLetterByFirstUpper(null));
    }

    @Test
    public void capitalize_numberOnly() {
        // 純數字，沒有英文字母 → 原字串不變
        assertEquals("12345", Utils.capitalizeFirstLetterByFirstUpper("12345"));
    }

    // ---------- capitalizeFirstLetter ----------

    @Test
    public void capitalizeFirst_normalEnglish() {
        // 一般英文小寫開頭
        assertEquals("Hello", Utils.capitalizeFirstLetter("hello"));
    }

    @Test
    public void capitalizeFirst_alreadyUpperCase() {
        // 已是大寫，應保持不變
        assertEquals("Hello", Utils.capitalizeFirstLetter("Hello"));
    }

    @Test
    public void capitalizeFirst_chineseStart() {
        // 中文開頭 → 第一個字元 toUpperCase 對中文無影響，原字串不變
        assertEquals("測試hello", Utils.capitalizeFirstLetter("測試hello"));
    }

    @Test
    public void capitalizeFirst_singleChar() {
        // 單一小寫字母
        assertEquals("A", Utils.capitalizeFirstLetter("a"));
    }

    @Test
    public void capitalizeFirst_emptyString() {
        assertEquals("", Utils.capitalizeFirstLetter(""));
    }

    @Test
    public void capitalizeFirst_nullString() {
        assertNull(Utils.capitalizeFirstLetter(null));
    }
}
