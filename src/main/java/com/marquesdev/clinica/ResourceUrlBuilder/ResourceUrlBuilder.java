package com.marquesdev.clinica.ResourceUrlBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ResourceUrlBuilder {

    @Value("${app.base-url}")
    private String baseUrl;

    public String build(String endpoint, UUID id){
        return baseUrl + endpoint + "/" + id;
    }
}
