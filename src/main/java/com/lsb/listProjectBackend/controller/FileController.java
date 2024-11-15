package com.lsb.listProjectBackend.controller;

import com.lsb.listProjectBackend.domain.FileExistRequest;
import com.lsb.listProjectBackend.domain.FileRequest;
import com.lsb.listProjectBackend.domain.LsbException;
import com.lsb.listProjectBackend.utils.Utils;
import com.sun.jna.platform.FileUtils;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin("*")
@RestController
@RequestMapping("api")
public class FileController {

    @PostMapping("/file/delete")
    public boolean delete(@RequestBody FileRequest fileRequest) throws Exception {
        File file = new File(fileRequest.getPath());
        if (fileRequest.getPath().startsWith("\\\\")) {
            throw new LsbException("文件位於非本地磁碟，無法移動到垃圾桶，可能導致直接刪除");
        }
        if (file.exists()) {
            FileUtils.getInstance().moveToTrash(file);
            return !file.exists();
        } else {
            throw new LsbException("File does not exist");
        }
    }

    @PostMapping("/file/move-to")
    public boolean moveTo(@RequestBody FileRequest fileRequest) {
        File file = new File(fileRequest.getPath());
        File moveTo = new File(fileRequest.getMoveTo());
        if (Utils.isBlank(
                fileRequest.getPath(), fileRequest.getMoveTo()) ||
                !file.exists() ||
                !moveTo.exists()
        ) {
            return false;
        }
        return file.renameTo(new File(fileRequest.getMoveTo() + "\\" + file.getName()));
    }

    @PostMapping("/file/file-exist")
    public Map<String, Boolean> fileExist(@RequestBody List<FileExistRequest> fileExistRequestList) {
        if (fileExistRequestList == null || fileExistRequestList.isEmpty()) {
            return Collections.emptyMap();
        }
        return fileExistRequestList.stream()
                .distinct() // 去除重複的 FileExistRequest（假設 equals/hashCode 已正確實現）
                .collect(Collectors.toMap(
                        FileExistRequest::getName,
                        req -> Paths.get(req.getPath()).toFile().exists(),
                        (a, b) -> a
                ));
    }
}
