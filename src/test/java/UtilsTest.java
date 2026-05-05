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

    // ---------- moveChar ----------

    @Test
    public void moveChar_nullInput() {
        // null 輸入應原樣回傳
        assertNull(Utils.moveChar(null, 0, 1));
    }

    @Test
    public void moveChar_emptyInput() {
        // 空字串應原樣回傳
        assertEquals("", Utils.moveChar("", 0, 1));
    }

    @Test
    public void moveChar_fromIndexNegative() {
        // fromIndex 為負數 → 無效，原樣回傳
        assertEquals("abcde", Utils.moveChar("abcde", -1, 2));
    }

    @Test
    public void moveChar_fromIndexOutOfBounds() {
        // fromIndex >= length → 無效，原樣回傳
        assertEquals("abcde", Utils.moveChar("abcde", 5, 2));
    }

    @Test
    public void moveChar_toIndexNegative() {
        // toIndex 為負數 → 無效，原樣回傳（不 clamp）
        assertEquals("abcde", Utils.moveChar("abcde", 1, -1));
    }

    @Test
    public void moveChar_toIndexOutOfBounds() {
        // toIndex > len → 無效，原樣回傳
        assertEquals("abcde", Utils.moveChar("abcde", 1, 6));
    }

    @Test
    public void moveChar_moveToEnd() {
        // toIndex=len(5) 代表「字串最末尾」插入點
        // from=0(a) to=5：delete(0)→"bcde", toIndex-- →4, insert(4,'a') → "bcdea"
        assertEquals("bcdea", Utils.moveChar("abcde", 0, 5));
    }

    @Test
    public void moveChar_sameIndex() {
        // fromIndex == toIndex → 無需移動，原樣回傳
        assertEquals("abcde", Utils.moveChar("abcde", 2, 2));
    }

    @Test
    public void moveChar_moveForward() {
        // 向後移動：from=1(b), toIndex=3（插入點「before d」）
        // delete(1)→"acde", toIndex-- →2, insert(2,'b') → "acbde"
        // 'b' 落在 'c' 與 'd' 之間，即原本 toIndex=3 插入點之前
        assertEquals("acbde", Utils.moveChar("abcde", 1, 3));
    }

    @Test
    public void moveChar_moveBackward() {
        // 向前移動：from=3(d) to=1(b)
        // delete(3)→"abce", no adjust, insert(1,'d') → "adbce"
        assertEquals("adbce", Utils.moveChar("abcde", 3, 1));
    }

    @Test
    public void moveChar_moveToStart() {
        // 移到最前面：from=2(c) to=0
        assertEquals("cabde", Utils.moveChar("abcde", 2, 0));
    }

    @Test
    public void moveChar_moveToLastIndex() {
        // toIndex=4 代表插入點「before e」
        // from=0(a) to=4：delete(0)→"bcde", toIndex-- →3, insert(3,'a') → "bcdae"
        // 'a' 落在 'd' 與 'e' 之間
        assertEquals("bcdae", Utils.moveChar("abcde", 0, 4));
    }

    @Test
    public void moveChar_swapTwoChars() {
        // 兩字元交換：from=1(b) to=0(a) → "ba"
        assertEquals("ba", Utils.moveChar("ab", 1, 0));
    }

    @Test
    public void moveChar_adjacentForward() {
        // 相鄰往後：from=0(a), toIndex=1（插入點「before b」= 'a' 原本位置右側）
        // delete(0)→"b", toIndex-- →0, insert(0,'a') → "ab"（等同未動）
        assertEquals("ab", Utils.moveChar("ab", 0, 1));
    }

    @Test
    public void moveChar_singleChar() {
        // 單字元字串：from=0 to=0 → 同索引，回傳原字串
        assertEquals("x", Utils.moveChar("x", 0, 0));
    }
}
