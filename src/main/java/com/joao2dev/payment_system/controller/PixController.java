package com.joao2dev.payment_system.controller;

import com.joao2dev.payment_system.dto.PixChargeRequest;
import com.joao2dev.payment_system.service.PixService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pix")
public class PixController {
    @Autowired
    private PixService pixService;
    @GetMapping("/cod")
    public ResponseEntity pixCreateEVP(){
        JSONObject response = this.pixService.pixCreatedEVP();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.toString());
    }
    @PostMapping("/pagar")
    public ResponseEntity pixCreateCharge(@RequestBody PixChargeRequest pixChargeRequest){
        JSONObject response = this.pixService.pixCreateCharge(pixChargeRequest);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.toString());
    }
}
