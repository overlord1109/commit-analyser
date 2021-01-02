package com.overlord.gitstats.analyser.core;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ast.body.MethodDeclaration;
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
import java.util.*;
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
     * <p>
     * Changes where parameters were removed are added to the report. See wasParameterRemoved(...) for criteria used for
     * parameter removal
     */
    public List<ReportRow> generateReport(List<ChangedFile> changedFiles) throws IOException {
        LOGGER.info("Iterating over commits, parsing files if required and generating report..");
        List<ReportRow> report = new ArrayList<>();
        for (ChangedFile changedFile : changedFiles) {
            try {
                List<MethodDeclaration> oldDeclarations = extractMethodDeclarations(changedFile.getFilePath(), changedFile.getOldCommitId());
                List<MethodDeclaration> newDeclarations = extractMethodDeclarations(changedFile.getFilePath(), changedFile.getNewCommitId());

                //Only keep MethodDeclarations with names that are present in both old and new method declarations.
                //This filters out newly added methods from newMethodDeclarations and removed methods from oldMethodDeclarations.
                filterDeletedAndAddedMethods(oldDeclarations, newDeclarations);

                //Filter out all method declarations that were not changed
                filterOutUnchangedMethods(oldDeclarations, newDeclarations);

                for (MethodDeclaration oldDecl : oldDeclarations) {
                    //Find matching method declarations from new file with the same name
                    List<MethodDeclaration> matchingDeclarations = newDeclarations.stream()
                            .filter(decl -> decl.getNameAsString().equals(oldDecl.getNameAsString()))
                            .collect(Collectors.toList());

                    if(matchingDeclarations.size() == 0)
                        continue;

                    List<MethodDeclaration> removedDeclarations = matchingDeclarations.stream()
                            .filter(newDecl -> wasParameterRemoved(oldDecl, newDecl))
                            .collect(Collectors.toList());

                    if(removedDeclarations.size() == matchingDeclarations.size()) {
                        MethodDeclaration newDecl = removedDeclarations.stream().findAny().get();
                        newDeclarations.remove(newDecl);
                        report.add(constructReportRow(changedFile, newDecl, oldDecl));
                    } else {
                        MethodDeclaration updatedDeclaration = matchingDeclarations.stream()
                                .filter(matchingDecl -> !removedDeclarations.contains(matchingDecl))
                                .findAny()
                                .orElseThrow(RuntimeException::new);
                        newDeclarations.remove(updatedDeclaration);
                    }
                }

            } catch (ParseProblemException e) {
                LOGGER.warn("Method parsing failed, file from this commit will be silently ignored.");
            }
        }

        return report;
    }

    public void filterDeletedAndAddedMethods(List<MethodDeclaration> oldDeclarations, List<MethodDeclaration> newDeclarations) {
        oldDeclarations.removeIf(oldDecl -> (newDeclarations.stream()
                .noneMatch(newDecl -> newDecl.getNameAsString().equals(oldDecl.getNameAsString()))));

        newDeclarations.removeIf(newDecl -> oldDeclarations.stream()
                .noneMatch(oldDecl -> oldDecl.getNameAsString().equals(newDecl.getNameAsString())));
    }

    public void filterOutUnchangedMethods(List<MethodDeclaration> oldDeclarations, List<MethodDeclaration> newDeclarations) {
        List<MethodDeclaration> unchangedOld = oldDeclarations.stream()
                .filter(oldDecl -> newDeclarations.stream()
                        .anyMatch(newDecl -> isSameMethodDeclaration(oldDecl, newDecl)))
                .collect(Collectors.toList());

        List<MethodDeclaration> newUchanged = newDeclarations.stream()
                .filter(newDecl -> oldDeclarations.stream()
                        .anyMatch(oldDecl -> isSameMethodDeclaration(oldDecl, newDecl)))
                .collect(Collectors.toList());

        oldDeclarations.removeAll(unchangedOld);
        newDeclarations.removeAll(newUchanged);
    }

    //TODO: Use multiset check for parameter list
    public boolean isSameMethodDeclaration(MethodDeclaration one, MethodDeclaration other) {
        return one.getDeclarationAsString(false, false, false)
                .equals(other.getDeclarationAsString(false, false, false));
    }


    /**
     * This method compares old and new method declarations, and checks whether the parameters from the old method declaration
     * are present in the parameter list of new method declaration. Order of the parameters does not matter.
     *
     * The implementation considers the parameter list as a multiset i.e. a set where elements may be duplicated. Multiset is
     * implemented as a map of the element name to its count (Map<String, Integer>). The old parameter multiset is iterated upon,
     * and it's count is compared against count corresponding to the element in the new parameter list. If old count is lesser than
     * newer count, the parameter is considered to be removed, and 'true' is returned.
     *
     * e.g.
     * Consider a Java method declaration:
     *
     * int subtract(int a, int b, long c)
     *
     * which is changed to
     *
     * int subtract(int a, long c)
     *
     * Here the old parameter list (represented as a multiset) is : ["int": 2, "long": 1]
     * and the new parameter list (represented as a multiset) is : ["int": 1, "long": 1]
     * As there is one less "int" parameter in the new parameter list, the method returns "true".
     *
     * <p>
     * Note: Local parameter variable names are not considered, only their types.
     */
    public boolean wasParameterRemoved(MethodDeclaration oldDecl, MethodDeclaration newDecl) {
        Map<String, Integer> oldParams = new HashMap<>();
        oldDecl.getParameters().forEach(
                param -> oldParams.compute(param.getType().asString(), (k, v) -> v == null ? 1 : v + 1)
        );
        Map<String, Integer> newParams = new HashMap<>();
        newDecl.getParameters().forEach(
                param -> newParams.compute(param.getType().asString(), (k, v) -> v == null ? 1 : v + 1)
        );

        for (Map.Entry<String, Integer> old : oldParams.entrySet()) {
            if (newParams.getOrDefault(old.getKey(), 0) < old.getValue())
                return true;
        }

        return false;
    }

    /**
     * Parse method declarations from file at specified revision as a list of MethodDecalarations
     * <p>
     * e.g. If a Java file consists of following method declarations
     * <p>
     * int subtract(int a, int b) {
     * boolean compareLists(List<Integer> first, List<Integer> second) {
     * long getTime() {
     * <p>
     * Above methods would be converted into a list as below:
     * [
     *  {"methodName":"subtract", "paramList":["int", "int"]},
     *  {{"methodName":"compareLists", "paramList":["List<Integer>", "List<Integer>"]}},
     *  {{"methodName":"getTime, "paramList":[]}}
     * ]
     */
    private List<MethodDeclaration> extractMethodDeclarations(String filePath, String revisionId) throws IOException {
        InputStream oldFile = gitClient.getFileAtRevision(revisionId, filePath, repo);
        return parser.extractDeclarations(oldFile);
    }

    private ReportRow constructReportRow(ChangedFile changedFile, MethodDeclaration newDecl, MethodDeclaration oldDecl) {
        return ReportRow.Builder.instance()
                .withCommitSHA(changedFile.getNewCommitId())
                .withFilePath(changedFile.getFilePath())
                .withOldFnSign(functionSignatureString(oldDecl.getNameAsString(), oldDecl.getParameters()))
                .withNewFnSign(functionSignatureString(newDecl.getNameAsString(), newDecl.getParameters()))
                .build();
    }

    private String functionSignatureString(String methodName, List<Parameter> parameters) {
        String paramList = parameters.stream()
                .map(param -> param.getType().asString())
                .collect(Collectors.joining(", "));
        return methodName + '(' + paramList + ')';
    }
}
