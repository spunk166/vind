/*
 * Copyright (c) 2018 Redlink GmbH.
 */
package com.rbmhtechnology.vind.monitoring.report;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * Created on 08.03.18.
 */
public class ReportConfiguration {

    private String applicationId;
    private Map<String, String> generalFilters = new HashMap<>();
    private Map<String, HashMap<String,String>> facetFieldsExtension = new HashMap<>();
    private Map<String, HashMap<String,String>> suggestionFieldsExtension = new HashMap<>();
    private Map<String, HashMap<String,String>> fulltextQueryExtension = new HashMap<>();
    private List<String> queryFilters = new ArrayList<>();

    public ReportConfiguration (String applicationId){
        if (StringUtils.isNotEmpty(applicationId)) {
            this.applicationId = applicationId;
        } else {
            throw new RuntimeException("Error creating report configuration: Application ID should not be null or blank");
        }
    }

    public String getApplicationId() {
        return applicationId;
    }

    public ReportConfiguration setApplicationId(String applicationId) {
        if (StringUtils.isNotEmpty(applicationId)) {
            this.applicationId = applicationId;
        } else {
            throw new RuntimeException("Error setting new application ID: Application ID should not be null or blank");
        }
        return this;
    }

    public Map<String, String> getGeneralFilters() {
        return generalFilters;
    }

    public ReportConfiguration addGeneralFilter(String fieldName, String filterValue) {
        if (Objects.nonNull(filterValue) & Objects.nonNull(fieldName)) {
            this.generalFilters.put(fieldName,filterValue);
        }
        return this;
    }

    public Map<String, HashMap<String, String>> getFacetFieldsExtension() {
        return Collections.unmodifiableMap(facetFieldsExtension);
    }


    public ReportConfiguration addFacetFieldExtension(String extensionName, HashMap<String, String> facetFieldsExtension) {
        if (Objects.nonNull(extensionName) ) {
            if (Objects.isNull(facetFieldsExtension)) {
                facetFieldsExtension = new HashMap<>();
            }
            this.facetFieldsExtension.put(extensionName, facetFieldsExtension);
        }
        return this;
    }

    public Map<String, HashMap<String, String>> getSuggestionFieldsExtension() {
        return suggestionFieldsExtension;
    }

    public ReportConfiguration addSuggestionFieldExtension(String extensionName, HashMap<String, String> suggestionFieldsExtension) {
        if (Objects.nonNull(extensionName) ) {
            if (Objects.isNull(suggestionFieldsExtension)) {
                suggestionFieldsExtension = new HashMap<>();
            }
            this.suggestionFieldsExtension.put(extensionName, suggestionFieldsExtension);
        }
        return this;
    }

    public Map<String, HashMap<String, String>> getFulltextQueryExtension() {
        return fulltextQueryExtension;
    }

    public ReportConfiguration addFulltextQueryExtension(String extensionName, HashMap<String, String> fulltextQueryExtension) {
        if (Objects.nonNull(extensionName) ) {
            if (Objects.isNull(fulltextQueryExtension)) {
                fulltextQueryExtension = new HashMap<>();
            }
            this.fulltextQueryExtension.put(extensionName, fulltextQueryExtension);
        }
        return this;
    }

    public List<String> getQueryFilters() {
        return queryFilters;
    }

    public ReportConfiguration setQueryFilters(List<String> queryFilters) {
        if (Objects.nonNull(queryFilters)) {
            this.queryFilters = queryFilters;
        }
        return this;
    }

    public ReportConfiguration setQueryFilters(String... queryFilters) {
        if (Objects.nonNull(queryFilters)) {
            this.queryFilters = Arrays.asList(queryFilters);
        }
        return this;
    }

    public ReportConfiguration addQueryFilters(String... queryFilters) {
        if (Objects.nonNull(queryFilters)) {
            this.queryFilters.addAll(Arrays.asList(queryFilters));
        }
        return this;
    }
}
