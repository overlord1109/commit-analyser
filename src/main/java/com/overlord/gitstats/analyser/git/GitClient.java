package com.overlord.gitstats.analyser.git;

import com.overlord.gitstats.analyser.regex.JavaMethodPatternMatcher;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.overlord.gitstats.analyser.util.Utils.deleteRecursively;

public class GitClient {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final String httpsUrl;
    private final String localStoragePath;

    public GitClient(String httpsUrl, String localStoragePath) {
        this.httpsUrl = httpsUrl;
        this.localStoragePath = localStoragePath;
    }

    public Git cloneRepository() throws GitAPIException, IOException {
        LOGGER.info("Cloning remote repository present at {} at local path {}", httpsUrl, localStoragePath);
        File parentDir = new File(localStoragePath);

        //Clean directory before-hand
        if (parentDir.exists())
            deleteRecursively(parentDir);

        Git git = new CloneCommand()
                .setURI(this.httpsUrl)
                .setDirectory(new File(localStoragePath + File.separator + "remote"))
                .setCloneAllBranches(true)
                .call();
        LOGGER.info("Repository cloned");
        return git;
    }

    public List<RevCommit> getCommitsModifyingJavaMethodDeclarations(Git git) throws IOException {
        Repository repo = git.getRepository();
        RevWalk walk = new RevWalk(repo);
        walk.markStart(walk.parseCommit(repo.resolve("HEAD")));
        walk.setRevFilter(new DiffPatternRevFilter(JavaMethodPatternMatcher.instance().getPattern(), repo));
        List<RevCommit> commits = new ArrayList<>();
        walk.iterator().forEachRemaining(commits::add);
        return commits;
    }

}
