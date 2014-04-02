package com.paranhaslett.refactorcategory.strategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.git.GitHelper;

public class RepoDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException {
    List<Difference> result = new ArrayList<Difference>();
    
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    Repository repo = builder.setGitDir(new File("/home/paran/Documents/Test/Jasm/.git"))
    .readEnvironment().findGitDir().build();
    

    RevWalk walk = new RevWalk(repo);
    walk.markStart(walk.parseCommit(repo.resolve("HEAD")));
    RevCommit last = null;
    for (Iterator<RevCommit> iterator = walk.iterator(); iterator.hasNext();) {
        RevCommit rev = iterator.next();
        if (last != null){
         RevTree lastRt = last.getTree();
         RevTree thisRt = rev.getTree();
         
         CanonicalTreeParser aParser = new CanonicalTreeParser();
         CanonicalTreeParser bParser = new CanonicalTreeParser();
        
         
         //GitHelper.scan(lastRt,thisRt);
    
        }
        last = rev;
    }
    return result;
  }

}
