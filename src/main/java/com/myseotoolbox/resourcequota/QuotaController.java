package com.myseotoolbox.resourcequota;

import com.myseotoolbox.quota4j.QuotaManagerNotRegisteredException;
import com.myseotoolbox.quota4j.QuotaService;
import com.myseotoolbox.quota4j.ResourceQuotaNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class QuotaController {

    private final QuotaService quotaService;

    @GetMapping("/resources/{resourceId}/owners/{ownerId}")
    public long getAvailable(@PathVariable String resourceId, @PathVariable String ownerId) {
        return quotaService.getRemaining(ownerId, resourceId);
    }

    @PostMapping("/resources/{resourceId}/owners/{ownerId}/acquire")
    public ResponseEntity<String> acquireResource(@PathVariable String resourceId,
                                                  @PathVariable String ownerId,
                                                  @RequestParam(defaultValue = "1") int quantity) {
        try {
            if (!quotaService.tryAcquire(ownerId, resourceId, quantity))
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        } catch (ResourceQuotaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (QuotaManagerNotRegisteredException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        }

        return ResponseEntity.ok().build();
    }
}
