package com.paranhaslett.refactorcategory.strategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.git.GitRepo;
import com.paranhaslett.refactorcategory.git.GitRevision;

public class RepoDrillDown extends DrillDown {
  private String repoFileName;

  public void setRepo(String filename) {
    this.repoFileName = filename;
  }

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {
    List<Difference> result = new ArrayList<Difference>();
    if (repoFileName == null) {
      throw new IOException("Repository or filename not set");
    } else {

      if (repoFileName.endsWith(".git")) {

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repo = builder.setGitDir(new File(repoFileName))
            .readEnvironment().findGitDir().build();

        ((GitRepo) GitRepo.getRepo()).setRepo(repo);

        RevWalk walk = new RevWalk(repo);
        
        ObjectId head = repo.resolve("HEAD");
        
        System.out.println(head);
        
        walk.markStart(walk.parseCommit(head));
        RevCommit newRc = null;
        for (Iterator<RevCommit> iterator = walk.iterator(); iterator.hasNext();) {
          RevCommit oldRc = iterator.next();
          if (newRc != null) {
            GitRevision newGr = new GitRevision(newRc);
            GitRevision oldGr = new GitRevision(oldRc);

            // GitRepo.getRepo().setCurrentRevision(oldGr, newGr);

            CodeBlock oldCb = new CodeBlock();
            CodeBlock newCb = new CodeBlock();

            oldCb.setRevision(oldGr);
            newCb.setRevision(newGr);

            Difference diff = new Difference(newCb, oldCb);
            result.addAll(new RevisionDrillDown().drilldown(diff));
          }
          newRc = oldRc;
        }  
      }
    }
    return result;
  }

}
