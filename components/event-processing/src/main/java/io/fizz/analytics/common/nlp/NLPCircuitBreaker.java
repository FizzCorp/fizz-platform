package io.fizz.analytics.common.nlp;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import io.fizz.analytics.domain.TextAnalysisResult;
import io.fizz.common.infastructure.circuitBreaker.AbstractCircuitBreaker;
import io.fizz.common.infastructure.circuitBreaker.CircuitBreakerConfig;

import java.util.Objects;

public class NLPCircuitBreaker extends AbstractCircuitBreaker<TextAnalysisResult> {
    final private AbstractTextAnalysisService nlpService;
    private String text;

    public NLPCircuitBreaker(final CircuitBreakerConfig aConfig, final AbstractTextAnalysisService aNLPService) {
        super(aConfig);
        if (Objects.isNull(aNLPService)) {
            throw new IllegalArgumentException("invalid NLP service specified.");
        }
        nlpService = aNLPService;
    }

    public NLPCircuitBreaker text(final String aText) {
        text = aText;
        return this;
    }

    @Override
    protected TextAnalysisResult run() {
        try {
            return nlpService.analyze(text).get();
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    protected TextAnalysisResult fallback() {
        return null;
    }
}
