package com.mailservice.fny.analysis.exception;

public class AnalysisJobNotFoundException extends RuntimeException {

    public AnalysisJobNotFoundException(String jobId) {
        super("분석 작업을 찾을 수 없습니다. id=" + jobId);
    }
}
