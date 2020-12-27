package com.overlord.gitstats.analyser;

import com.overlord.gitstats.analyser.core.ReportGenerator;
import com.overlord.gitstats.analyser.git.GitClient;
import com.overlord.gitstats.analyser.model.ChangedFile;
import com.overlord.gitstats.analyser.model.ReportRow;
import com.overlord.gitstats.analyser.parser.JavaSourceParser;
import com.overlord.gitstats.analyser.writer.ReportWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.overlord.gitstats.analyser.writer.ReportWriter.extractRepoName;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) throws GitAPIException, IOException {

        if (args.length != 2) {
            LOGGER.error("Usage: java -jar [remote-url] [output-path]");
            return;
        }

        String remoteUrl = args[0];
        String localStoragePath = args[1];

        if (!remoteUrl.startsWith("https://") || !remoteUrl.endsWith(".git")) {
            LOGGER.error("Remote-url should be valid HTTPS Git url");
            return;
        }

        GitClient gitClient = new GitClient(remoteUrl, localStoragePath);
        Git git = gitClient.cloneRepository();
        List<ChangedFile> changedFiles = gitClient.getChangesModifyingJavaMethodDeclarations(git);

        JavaSourceParser parser = new JavaSourceParser();
        List<ReportRow> report = new ReportGenerator(gitClient, parser, git.getRepository())
                .generateReport(changedFiles);
        LOGGER.info("Found {} commits where method parameters were removed", report.size());

        ReportWriter reportWriter = new ReportWriter(localStoragePath, extractRepoName(remoteUrl));
        String reportName = reportWriter.writeCsv(report);
        if (reportName != null)
            LOGGER.info("Wrote report to {}", reportName);
        git.close();
    }
}
