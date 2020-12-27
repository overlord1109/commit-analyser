package com.overlord.gitstats.analyser.core;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.Parameter;
import com.overlord.gitstats.analyser.Application;
import com.overlord.gitstats.analyser.git.GitClient;
import com.overlord.gitstats.analyser.model.ChangedFile;
import com.overlord.gitstats.analyser.model.ReportRow;
import com.overlord.gitstats.analyser.parser.JavaSourceParser;
import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
    private GitClient gitClient;
    private JavaSourceParser parser;
    private Repository repo;

    public ReportGenerator() {

    }

    public ReportGenerator(GitClient gitClient, JavaSourceParser parser, Repository repo) {
        this.gitClient = gitClient;
        this.parser = parser;
        this.repo = repo;
    }

    /**
     * Iterates over each changed file, calculating the differences between function signatures from old and new revisions
     * of the changed file.
     *
     * Changes where parameters were removed are added to the report. See wasParameterRemoved(...) for criteria used for
     * parameter removal
     */
    public List<ReportRow> generateReport(List<ChangedFile> changedFiles) throws IOException {
        LOGGER.info("Iterating over commits, parsing files if required and generating report..");
        List<ReportRow> report = new ArrayList<>();
        for (ChangedFile changedFile : changedFiles) {
            try {
                Map<String, List<Parameter>> oldMethodDeclarations = extractMethodDeclarations(changedFile.getFilePath(), changedFile.getOldCommitId());
                Map<String, List<Parameter>> newMethodDeclarations = extractMethodDeclarations(changedFile.getFilePath(), changedFile.getNewCommitId());

                oldMethodDeclarations.entrySet()
                        .stream()
                        .filter(oldEntry -> newMethodDeclarations.containsKey(oldEntry.getKey()))
                        .filter(oldEntry -> wasParameterRemoved(oldEntry.getValue(), newMethodDeclarations.get(oldEntry.getKey())))
                        .map(oldEntry -> constructReportRow(changedFile, newMethodDeclarations, oldEntry))
                        .forEachOrdered(report::add);

            } catch (ParseProblemException e) {
                continue;
            }
        }

        return report;
    }

    /**
     * This method iterates over old parameters while performing element-wise comparison with new parameters.
     * If any parameter from old parameters list is not present at the same position in the new parameter list,
     * it is considered to be removed. Essentially, I check whether old parameter list is a prefix of the new
     * parameter list.
     * <p>
     * Note: Local parameter variable names are not considered, only their types.
     */
    public boolean wasParameterRemoved(List<Parameter> oldParams, List<Parameter> newParams) {
        if (oldParams.size() == 0 && newParams.size() == 0)
            return false;
        for (int i = 0; i < oldParams.size(); i++) {
            if (i >= newParams.size() || !oldParams.get(i).getType().equals(newParams.get(i).getType()))
                return true;
        }
        return false;
    }

    /**
     * Parse method declarations from file at specified revision into a map of (method name :: list of corresponding
     * parameter types)
     *
     * e.g. If a Java file consists of following method declarations
     *
     *      int subtract(int a, int b) {
     *      boolean compareLists(List<Integer> first, List<Integer> second) {
     *      long getTime() {
     *
     *      Above methods would be converted into a map as below:
     *      [
     *          "subtract" : ["int", "int"],
     *          "compareLists" : ["List<Integer>", "List<Integer>"],
     *          "getTime" : [],
     *      ]
     */
    private Map<String, List<Parameter>> extractMethodDeclarations(String filePath, String revisionId) throws IOException {
        InputStream oldFile = gitClient.getFileAtRevision(revisionId, filePath, repo);
        return parser.extractDeclarations(oldFile);
    }

    private ReportRow constructReportRow(ChangedFile changedFile, Map<String, List<Parameter>> newMethodDeclarations, Map.Entry<String, List<Parameter>> oldEntry) {
        return ReportRow.Builder.instance()
                .withCommitSHA(changedFile.getNewCommitId())
                .withFilePath(changedFile.getFilePath())
                .withOldFnSign(functionSignatureString(oldEntry.getKey(), oldEntry.getValue()))
                .withNewFnSign(functionSignatureString(oldEntry.getKey(), newMethodDeclarations.get(oldEntry.getKey())))
                .build();
    }

    private String functionSignatureString(String methodName, List<Parameter> parameters) {
        String paramList = parameters.stream()
                .map(param -> param.getType().asString())
                .collect(Collectors.joining(", "));
        return methodName + '(' + paramList + ')';
    }
}
