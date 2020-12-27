package com.overlord.gitstats.analyser.git;

import com.overlord.gitstats.analyser.model.ChangedFile;
import com.overlord.gitstats.analyser.regex.JavaMethodPatternMatcher;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
                .setTimeout(60)
                .setURI(this.httpsUrl)
                .setDirectory(new File(localStoragePath + File.separator + "remote"))
                .call();
        LOGGER.info("Repository cloned");
        return git;
    }

    /**
     * This method walks commits and filters for the commits that have modified a "*.java" file,
     * and whose diff contents match something resembling a Java method declaration
     */
    public List<ChangedFile> getChangesModifyingJavaMethodDeclarations(Git git) throws IOException, GitAPIException {
        LOGGER.info("Obtaining commits modifying lines resembling Java method declarations");
        List<ChangedFile> changedFiles = new ArrayList<>();
        Pattern pattern = JavaMethodPatternMatcher.instance().getPattern();

        Repository repo = git.getRepository();
        ObjectId branchId = repo.resolve("HEAD");
        Iterable<RevCommit> commits = git.log().add(branchId).call();

        //Walk the commit-graph from the latest commit
        for (RevCommit commit : commits) {
            RevCommit[] parents = commit.getParents();
            for (RevCommit parent : parents) {
                AbstractTreeIterator oldTreeParser = prepareTreeParser(repo, parent.getName());
                AbstractTreeIterator newTreeParser = prepareTreeParser(repo, commit.getName());
                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .setPathFilter(PathSuffixFilter.create(".java"))
                        .call();
                for (DiffEntry diff : diffs) {
                    //Only consider diffs which modified the file
                    if (diff.getChangeType() != DiffEntry.ChangeType.MODIFY)
                        continue;

                    //Only include those diffs which modified something resembling a Java method declaration
                    boolean matches = pattern.matcher(getDiffContent(repo, diff)).find();
                    if (matches) {
                        changedFiles.add(new ChangedFile(diff.getNewPath(), parent.getName(), commit.getName()));
                    }
                }
            }
        }
        LOGGER.info("Found {} such commits", changedFiles.size());
        return changedFiles;
    }

    /**
     * Obtains an input stream for the specified file from the given revision (i.e. commit) of the repository
     */
    public InputStream getFileAtRevision(String revId, String filePath, Repository repo) throws IOException {
        RevCommit commit = new RevWalk(repo).parseCommit(repo.resolve(revId));          //resolve commit ID
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(commit.getTree());
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));                                //and set a TreeWalk to the required file

        if (!treeWalk.next()) {
            throw new RuntimeException("Specified file not found at given commit ID");
        }

        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objectId);
        return loader.openStream();
    }

    /**
     * Extract diff content from a DiffEntry as a String for subsequent pattern-matching
     */
    private String getDiffContent(Repository repo, DiffEntry diff) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (DiffFormatter formatter = new DiffFormatter(out)) {
            formatter.setRepository(repo);
            formatter.format(diff);
            return out.toString();
        }
    }

    /**
     * Calculating diffs in JGit requires a iterable Tree, this method converts commit SHA into the snapshot's TreeIterator
     */
    private AbstractTreeIterator prepareTreeParser(Repository repository, String objectId) throws IOException {
        // From the commit, we can build the tree which allows us to construct the TreeParser
        try (RevWalk walk = new RevWalk(repository)) {
            RevCommit commit = walk.parseCommit(ObjectId.fromString(objectId));
            RevTree tree = walk.parseTree(commit.getTree().getId());

            CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }

}
