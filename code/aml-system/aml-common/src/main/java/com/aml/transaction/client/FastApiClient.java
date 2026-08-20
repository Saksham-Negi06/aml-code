package com.aml.transaction.client;

import com.aml.common.dto.request.FastApiRequest;
import com.aml.common.dto.response.FastApiResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FastApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public FastApiClient(
            @Value("${fastapi.base-url}") String baseUrl
    ) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public FastApiResponse predict(
            FastApiRequest request
    ) {

        FastApiResponse response =
                restTemplate.postForObject(
                        baseUrl + "/predict",
                        request,
                        FastApiResponse.class
                );

        if (response == null) {
            throw new IllegalStateException(
                    "FastAPI returned an empty response"
            );
        }

        return response;
    }
}