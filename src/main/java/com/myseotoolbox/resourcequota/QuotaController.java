package com.myseotoolbox.resourcequota;

import com.myseotoolbox.quota4j.QuotaManagerNotRegisteredException;
import com.myseotoolbox.quota4j.QuotaNotFoundException;
import com.myseotoolbox.quota4j.QuotaService;
import com.myseotoolbox.quota4j.quotamanager.AcquireResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class QuotaController {

    private final QuotaService quotaService;

    @GetMapping("/resource/{resourceId}/owner/{ownerId}")
    public Object getState(@PathVariable String resourceId, @PathVariable String ownerId) {
        return quotaService.getQuotaState(ownerId, resourceId);
    }

    @PostMapping(value = "/resource/{resourceId}/owner/{ownerId}/acquire", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> acquireResource(@PathVariable String resourceId,
                                             @PathVariable String ownerId,
                                             @RequestParam(defaultValue = "1") long quantity) {
        try {
            AcquireResponse<?> acquireResponse = quotaService.tryAcquire(ownerId, resourceId, quantity);
            return ResponseEntity.status(HttpStatus.OK).body(acquireResponse);
        } catch (QuotaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (QuotaManagerNotRegisteredException e) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(e.getMessage());
        }

    }
}