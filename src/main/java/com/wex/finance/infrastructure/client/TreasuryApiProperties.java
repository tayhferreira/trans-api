package com.wex.finance.infrastructure.client;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "wex.treasury")
public class TreasuryApiProperties {

    private Map<String, String> currencyNameMapping = new HashMap<>();

    public Map<String, String> getCurrencyNameMapping() {
        return currencyNameMapping;
    }

    public void setCurrencyNameMapping(Map<String, String> currencyNameMapping) {
        this.currencyNameMapping = currencyNameMapping;
    }
}
