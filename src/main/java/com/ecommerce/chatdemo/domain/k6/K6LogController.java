package com.ecommerce.chatdemo.domain.k6;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/performance")
public class K6LogController {

    private static final Logger perfLogger = LoggerFactory.getLogger("K6_PERFORMANCE");
    private static final String LOG_PATH = "logs/performance.log";

    // 1. K6로부터 로그 수집
    @PostMapping("/logs")
    public void collectLogs(@RequestBody K6LogRequest request) {
        perfLogger.info("[{}] VU:{} | Iter:{} | Status:{} | Msg:{}",
                request.getApiVersion(), request.getVu(), request.getIter(),
                request.getStatus(), request.getMsg());
    }

    // 2. 수집된 로그 파일 다운로드
    @GetMapping("/logs/download")
    public ResponseEntity<Resource> downloadLogs() {
        File file = new File(LOG_PATH);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"k6_log.log\"")
                .body(resource);
    }
}
