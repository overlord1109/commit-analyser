package com.overlord.gitstats.analyser.integration;

import com.overlord.gitstats.analyser.git.DiffPatternRevFilter;
import com.overlord.gitstats.analyser.git.GitClient;
import com.overlord.gitstats.analyser.regex.JavaMethodPatternMatcher;
import com.overlord.gitstats.analyser.util.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

public class DiffPatternRevFilterIntegrationTest {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Test
    public void shouldListCommitsWithRelevantDiffs() throws GitAPIException, IOException {
        Git git = new GitClient("https://github.com/redisson/redisson.git", "test-output")
                .cloneRepository();
        Repository repo = git.getRepository();
        RevWalk walk = new RevWalk(repo);
        walk.markStart(walk.parseCommit(repo.resolve("HEAD")));
        walk.setRevFilter(new DiffPatternRevFilter(JavaMethodPatternMatcher.instance().getPattern(), repo));
        for (RevCommit commit : walk) {
            LOGGER.info("Commit time: " + Instant.ofEpochSecond(commit.getCommitTime()) + ", SHA: " + commit.getId().getName() + ", Author: " + commit.getAuthorIdent().getName());
        }

        git.close();
        Utils.deleteRecursively(new File("test-output"));
    }
}
