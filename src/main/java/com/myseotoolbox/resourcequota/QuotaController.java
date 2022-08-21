package com.myseotoolbox.resourcequota;

import io.github.quota4j.QuotaManagerNotRegisteredException;
import io.github.quota4j.ResourceQuotaNotFoundException;
import io.github.quota4j.QuotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class QuotaController {

    private final QuotaService quotaService;

    @GetMapping("/resources/{resourceId}/workspaces/{workspaceId}")
    public ResponseEntity<String> acquireResource(@PathVariable String resourceId, @PathVariable String workspaceId, @RequestParam(defaultValue = "1") int quantity) {
        try {
            if (!quotaService.tryAcquire(workspaceId, resourceId, quantity))
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (ResourceQuotaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (QuotaManagerNotRegisteredException e){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
