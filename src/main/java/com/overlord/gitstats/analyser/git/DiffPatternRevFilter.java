package com.overlord.gitstats.analyser.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This RevFilter filters for the commits that have modified a .java file,
 * and whose diff contents match a simple regex of a Java method declaration
 */
public class DiffPatternRevFilter extends RevFilter {

    private final Pattern pattern;
    private final Repository repo;
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public DiffPatternRevFilter(Pattern pattern, Repository repo) {
        this.pattern = pattern;
        this.repo = repo;
    }

    @Override
    public boolean include(RevWalk walker, RevCommit cmit) throws StopWalkException, MissingObjectException, IncorrectObjectTypeException, IOException {
        RevCommit[] parents = cmit.getParents();
        for (RevCommit parent : parents) {
            AbstractTreeIterator oldTreeParser = prepareTreeParser(repo, parent.getName());
            AbstractTreeIterator newTreeParser = prepareTreeParser(repo, cmit.getName());
            try (Git git = new Git(repo)) {
                List<DiffEntry> diffs = git.diff()
                        .setOldTree(oldTreeParser)
                        .setNewTree(newTreeParser)
                        .setPathFilter(PathSuffixFilter.create(".java"))
                        .call();
                for (DiffEntry diff : diffs) {
                    //Only consider diffs which modified the file
                    if(diff.getChangeType() != DiffEntry.ChangeType.MODIFY)
                        return false;

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (DiffFormatter formatter = new DiffFormatter(out)) {
                        formatter.setRepository(repo);
                        formatter.format(diff);
                        String diffLines = out.toString();
                        //Only include those diffs which modified something resembling a Java method declaration
                        return matches(diffLines);
                    }
                }
            } catch (GitAPIException e) {
                LOGGER.error("Exception while filtering commits", e);
            }
        }
        return false;
    }

    @Override
    public RevFilter clone() {
        return null;
    }

    /**
     * Calculating diffs in JGit requires a iterable Tree, this method converts commit SHA into the snapshots TreeIterator
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

    private boolean matches(String input) {
        return pattern.matcher(input).find();
    }
}
