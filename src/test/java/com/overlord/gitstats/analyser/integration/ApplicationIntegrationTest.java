package com.overlord.gitstats.analyser.integration;

import com.overlord.gitstats.analyser.core.ReportGenerator;
import com.overlord.gitstats.analyser.git.GitClient;
import com.overlord.gitstats.analyser.model.ChangedFile;
import com.overlord.gitstats.analyser.model.ReportRow;
import com.overlord.gitstats.analyser.parser.JavaSourceParser;
import com.overlord.gitstats.analyser.writer.ReportWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ApplicationIntegrationTest {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * End-to-end test after which main() would be modelled after
     */
    @Test
    public void shouldAnalyseGitRepo() throws GitAPIException, IOException {
        String remoteUrl = "https://github.com/cabaletta/baritone.git";
        String localStoragePath = "test-output";
        GitClient gitClient = new GitClient(remoteUrl, localStoragePath);
        Git git = gitClient.cloneRepository();
        List<ChangedFile> changedFiles = gitClient.getChangesModifyingJavaMethodDeclarations(git);

        JavaSourceParser parser = new JavaSourceParser();
        List<ReportRow> report = new ReportGenerator(gitClient, parser, git.getRepository())
                .generateReport(changedFiles);
        LOGGER.info("Found {} commits where method parameters were removed.", report.size());

        ReportWriter reportWriter = new ReportWriter(localStoragePath, ReportWriter.extractRepoName(remoteUrl));
        String reportName = reportWriter.writeCsv(report);
        if (reportName != null)
            LOGGER.info("Wrote report {}", reportName);
        git.close();
    }

}
