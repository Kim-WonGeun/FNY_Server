package com.mailservice.fny.mailbox.service;

class GmailSyncStats {

    private int fetched;
    private int inserted;
    private int skipped;
    private int analysisRequested;
    private int analysisCompleted;
    private int analysisSkipped;

    GmailSyncStats(int initialSkipped) {
        this.skipped = initialSkipped;
    }

    void recordFetched(int count) {
        fetched += count;
    }

    void recordInserted() {
        inserted++;
    }

    void recordSkipped() {
        skipped++;
    }

    void recordSkipped(int count) {
        skipped += count;
    }

    void recordAnalysisRequested() {
        analysisRequested++;
    }

    void recordAnalysisSkipped() {
        analysisSkipped++;
    }

    int fetched() {
        return fetched;
    }

    int inserted() {
        return inserted;
    }

    int skipped() {
        return skipped;
    }

    int analysisRequested() {
        return analysisRequested;
    }

    int analysisCompleted() {
        return analysisCompleted;
    }

    int analysisSkipped() {
        return analysisSkipped;
    }
}
