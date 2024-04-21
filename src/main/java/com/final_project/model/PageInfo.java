package com.final_project.model;

public class PageInfo {
    private long totalResults;
    private long resultsPerPage;

    public long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(long value) {
        this.totalResults = value;
    }

    public long getResultsPerPage() {
        return resultsPerPage;
    }

    public void setResultsPerPage(long value) {
        this.resultsPerPage = value;
    }
}
