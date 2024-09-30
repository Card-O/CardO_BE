package com.example.testserver.aiimage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeepAiResponse {

    @JsonProperty("output_url")
    private String outputUrl;

    public String getOutputUrl() {
        return outputUrl;
    }

    public void setOutputUrl(String outputUrl) {
        this.outputUrl = outputUrl;
    }
}