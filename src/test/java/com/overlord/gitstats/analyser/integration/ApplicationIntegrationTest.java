package com.overlord.gitstats.analyser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.Parameter;
import com.overlord.gitstats.analyser.core.MethodDeclarationComparer;
import com.overlord.gitstats.analyser.git.GitClient;
import com.overlord.gitstats.analyser.model.Change;
import com.overlord.gitstats.analyser.model.ReportRow;
import com.overlord.gitstats.analyser.parser.JavaSourceParser;
import com.overlord.gitstats.analyser.writer.ReportWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationTest {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    /**
     * End-to-end test after which main() would be modelled after
     */
    @Test
    public void shouldAnalyseGitRepo() throws GitAPIException, IOException {
        GitClient gitClient = new GitClient("https://github.com/cabaletta/baritone.git", "test-output");
        Git git = gitClient
                .cloneRepository();
        List<Change> changes = gitClient.getChangesModifyingJavaMethodDeclarations(git);

        JavaSourceParser parser = new JavaSourceParser();
        MethodDeclarationComparer comparer = new MethodDeclarationComparer();

        List<ReportRow> report = new ArrayList<>();
        for (Change change : changes) {
            try {
                InputStream oldFile = gitClient.getFileAtRevision(change.getOldCommitId(), change.getFilePath(), git.getRepository());
                Map<String, List<Parameter>> oldMethodDeclarations = parser.extractDeclarations(oldFile);
                InputStream newFile = gitClient.getFileAtRevision(change.getNewCommitId(), change.getFilePath(), git.getRepository());
                Map<String, List<Parameter>> newMethodDeclarations = parser.extractDeclarations(newFile);
                for (Map.Entry<String, List<Parameter>> oldEntry : oldMethodDeclarations.entrySet()) {
                    List<Parameter> oldParams = oldEntry.getValue();
                    if (newMethodDeclarations.containsKey(oldEntry.getKey())) {
                        List<Parameter> newParams = newMethodDeclarations.get(oldEntry.getKey());
                        if (comparer.wasParameterRemoved(oldParams, newParams)) {
                            report.add(ReportRow.Builder.instance()
                                    .withCommitSHA(change.getNewCommitId())
                                    .withFilePath(change.getFilePath())
                                    .withOldFnSign(oldEntry.getKey() + '(' + oldParams.stream()
                                            .map(param -> param.getType().asString()).collect(Collectors.joining(", ")) + ')')
                                    .withNewFnSign(oldEntry.getKey() + '(' + newParams.stream()
                                            .map(param -> param.getType().asString()).collect(Collectors.joining(", ")) + ')')
                                    .build());
                        }
                    }
                }
            } catch (ParseProblemException e) {
                continue;
            }
        }
        StoredConfig config = git.getRepository().getConfig();
        String url = config.getString("remote", "origin", "url");
        new ReportWriter("test-output", "placeholder").writeCsv(report);
        LOGGER.info("Found {} commits where method parameters were removed", report.size());
        git.close();
    }
}
