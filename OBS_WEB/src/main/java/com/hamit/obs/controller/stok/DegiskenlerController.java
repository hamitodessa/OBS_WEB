package com.hamit.obs.controller.stok;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DegiskenlerController {

    @GetMapping("/stok/degiskenler")
    public String degiskenler() {
        return "stok/degiskenler";
    }
}
