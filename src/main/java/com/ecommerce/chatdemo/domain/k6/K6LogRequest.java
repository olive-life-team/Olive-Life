package com.ecommerce.chatdemo.domain.k6;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class K6LogRequest {
    private String apiVersion;
    private int vu;
    private String iter;
    private String msg;
    private int status;
}
