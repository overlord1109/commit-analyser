package com.overlord.gitstats.analyser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"Commit SHA", "Java File", "Old function signature", "New function signature"})
public class ReportRow {

    private ReportRow() {

    }

    private ReportRow(String commitSHA, String filePath, String oldFnSign, String newFnSign) {
        this.commitSHA = commitSHA;
        this.filePath = filePath;
        this.oldFnSign = oldFnSign;
        this.newFnSign = newFnSign;
    }

    @JsonProperty("Commit SHA")
    private String commitSHA;
    @JsonProperty("Java File")
    private String filePath;
    @JsonProperty("Old function signature")
    private String oldFnSign;
    @JsonProperty("New function signature")
    private String newFnSign;

    public String getCommitSHA() {
        return commitSHA;
    }

    public void setCommitSHA(String commitSHA) {
        this.commitSHA = commitSHA;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOldFnSign() {
        return oldFnSign;
    }

    public void setOldFnSign(String oldFnSign) {
        this.oldFnSign = oldFnSign;
    }

    public String getNewFnSign() {
        return newFnSign;
    }

    public void setNewFnSign(String newFnSign) {
        this.newFnSign = newFnSign;
    }

    public static class Builder {
        private String commitSHA;
        private String filePath;
        private String oldFnSign;
        private String newFnSign;

        public static Builder instance() {
            return new Builder();
        }

        public Builder withCommitSHA(String commitSHA) {
            this.commitSHA = commitSHA;
            return this;
        }

        public Builder withFilePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder withOldFnSign(String oldFnSign) {
            this.oldFnSign = oldFnSign;
            return this;
        }

        public Builder withNewFnSign(String newFnSign) {
            this.newFnSign = newFnSign;
            return this;
        }

        public ReportRow build() {
            return new ReportRow(commitSHA, filePath, oldFnSign, newFnSign);
        }
    }
}
