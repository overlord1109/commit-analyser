package com.overlord.gitstats.analyser.git;

import com.overlord.gitstats.analyser.model.Change;
import com.overlord.gitstats.analyser.regex.JavaMethodPatternMatcher;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
                .setCloneAllBranches(true)
                .call();
        LOGGER.info("Repository cloned");
        return git;
    }

    /**
     * This method walks commits and filters for the commits that have modified a "*.java" file,
     * and whose diff contents match something resembling a Java method declaration
     */
    public List<Change> getChangesModifyingJavaMethodDeclarations(Git git) throws IOException, GitAPIException {
        List<Change> changes = new ArrayList<>();
        Pattern pattern = JavaMethodPatternMatcher.instance().getPattern();

        Repository repo = git.getRepository();
        RevWalk walk = new RevWalk(repo);
        walk.markStart(walk.parseCommit(repo.resolve("HEAD")));

        for (RevCommit cmit : walk) {
            RevCommit[] parents = cmit.getParents();
            for (RevCommit parent : parents) {
                AbstractTreeIterator oldTreeParser = prepareTreeParser(repo, parent.getName());
                AbstractTreeIterator newTreeParser = prepareTreeParser(repo, cmit.getName());
                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .setPathFilter(PathSuffixFilter.create(".java"))
                        .call();
                for (DiffEntry diff : diffs) {
                    //Only consider diffs which modified the file
                    if (diff.getChangeType() != DiffEntry.ChangeType.MODIFY)
                        continue;

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (DiffFormatter formatter = new DiffFormatter(out)) {
                        formatter.setRepository(repo);
                        formatter.format(diff);
                        String diffLines = out.toString();
                        //Only include those diffs which modified something resembling a Java method declaration
                        boolean matches = pattern.matcher(diffLines).find();
                        if (matches) {
                            changes.add(new Change(diff.getNewPath(), parent.getName(), cmit.getName()));
                        }
                    }
                }
            }
        }
        return changes;
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
