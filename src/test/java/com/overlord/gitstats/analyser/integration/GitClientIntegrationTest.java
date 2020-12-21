package com.overlord.gitstats.analyser.integration;

import com.overlord.gitstats.analyser.git.GitClient;
import com.overlord.gitstats.analyser.util.Utils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class GitClientIntegrationTest {

    @Test
    public void shouldBeAbleToCloneRepository() throws GitAPIException, IOException {
        Git repo = new GitClient("https://github.com/overlord1109/ctci.git", "test-output")
                .cloneRepository();
        Assert.assertTrue(repo.status().call().isClean());
        repo.close();
        Utils.deleteRecursively(new File("test-output"));
    }
}
