package com.overlord.gitstats.analyser.git;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;

import static com.overlord.gitstats.analyser.util.Utils.deleteRecursively;

public class GitClient {

    private final String httpsUrl;
    private final String localStoragePath;

    public GitClient(String httpsUrl, String localStoragePath) {
        this.httpsUrl = httpsUrl;
        this.localStoragePath = localStoragePath;
    }

    public Git cloneRepository() throws GitAPIException, IOException {
        File parentDir = new File(localStoragePath);

        //Clean directory before-hand
        if(parentDir.exists())
            deleteRecursively(parentDir);

        return new CloneCommand()
                .setURI(this.httpsUrl)
                .setDirectory(new File(localStoragePath + File.separator + "remote"))
                .setCloneAllBranches(true)
                .call();
    }


}
