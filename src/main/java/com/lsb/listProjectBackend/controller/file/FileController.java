package com.lsb.listProjectBackend.controller.file;

import com.lsb.listProjectBackend.domain.file.FileExistReqTO;
import com.lsb.listProjectBackend.domain.file.FileReqTO;
import com.lsb.listProjectBackend.domain.common.LsbException;
import com.lsb.listProjectBackend.utils.Utils;
import com.sun.jna.platform.FileUtils;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class FileController {

    @PostMapping("/file/delete")
    public boolean delete(@RequestBody FileReqTO fileRequest) throws Exception {
        File file = new File(fileRequest.path());
        if (fileRequest.path().startsWith("\\\\")) {
            throw new LsbException("文件位於非本地磁碟，無法移動到垃圾桶，可能導致直接刪除");
        }
        if (file.exists()) {
            FileUtils.getInstance().moveToTrash(file);
            return !file.exists();
        } else {
            throw new LsbException("檔案不存在", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/file/move-to")
    public boolean moveTo(@RequestBody FileReqTO fileRequest) {
        File file = new File(fileRequest.path());
        File moveTo = new File(fileRequest.moveTo());
        if (Utils.isBlank(
                fileRequest.path(), fileRequest.moveTo()) ||
                !file.exists() ||
                !moveTo.exists()) {
            return false;
        }
        return file.renameTo(new File(fileRequest.moveTo() + "\\" + file.getName()));
    }

    @PostMapping("/file/file-exist")
    public Map<String, Boolean> fileExist(@RequestBody List<FileExistReqTO> fileExistRequestList) {
        if (fileExistRequestList == null || fileExistRequestList.isEmpty()) {
            return Collections.emptyMap();
        }
        return fileExistRequestList.stream()
                .distinct() // 去除重複的 FileExistRequest（假設 equals/hashCode 已正確實現）
                .collect(Collectors.toMap(
                        FileExistReqTO::name,
                        req -> Paths.get(req.path()).toFile().exists(),
                        (a, b) -> a));
    }

    @PostMapping("/file/open-file")
    public boolean openHGame(@RequestBody FileReqTO request) throws Exception {
        File directory = new File(request.path());
        if (directory.isDirectory()) {
            Runtime.getRuntime().exec(new String[] { "explorer", "/n,", directory.getAbsolutePath() });
            return true;
        }
        return false;
    }
}
