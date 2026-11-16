package com.rockydrive.sync.controller;

import com.rockydrive.sync.dto.DeltaResponse;
import com.rockydrive.sync.security.JwtAuthFilter;
import com.rockydrive.sync.service.DeltaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeltaController {

    private final DeltaService deltaService;

    public DeltaController(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @GetMapping("/delta")
    public DeltaResponse getDelta(@RequestAttribute(JwtAuthFilter.USER_ID_ATTRIBUTE) String userId,
                                   @RequestParam(defaultValue = "0") long cursor) {
        return deltaService.getDelta(userId, cursor);
    }
}
