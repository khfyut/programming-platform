package com.programming.service.ai;

public record AiLlmConfig(
        String provider,
        String model,
        String ollamaUrl,
        String apiUrl,
        String apiKey
) {
    public String normalizedProvider() {
        return provider == null || provider.isBlank() ? "openai-compatible" : provider.trim().toLowerCase();
    }

    public String normalizedModel() {
        return model == null || model.isBlank() ? "deepseek-v4-flash" : model.trim();
    }
}
