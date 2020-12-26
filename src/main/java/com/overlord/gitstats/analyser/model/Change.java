package com.overlord.gitstats.analyser.model;

public class Change {

    public Change(String filePath, String oldCommitId, String newCommitId) {
        this.filePath = filePath;
        this.oldCommitId = oldCommitId;
        this.newCommitId = newCommitId;
    }

    private String filePath;
    private String oldCommitId;
    private String newCommitId;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOldCommitId() {
        return oldCommitId;
    }

    public void setOldCommitId(String oldCommitId) {
        this.oldCommitId = oldCommitId;
    }

    public String getNewCommitId() {
        return newCommitId;
    }

    public void setNewCommitId(String newCommitId) {
        this.newCommitId = newCommitId;
    }
}
