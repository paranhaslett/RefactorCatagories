package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.errors.MissingObjectException;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.git.GitHelper;

public class FileEntryDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws MissingObjectException, IOException {
    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getOldCb();
    
    
    byte[] oldBytes = GitHelper.getGitHelper().open(oldCb.getEntry());
    byte[] newBytes = GitHelper.getGitHelper().open(newCb.getEntry());
    
    RawText oldRt = new RawText(oldBytes);
    RawText newRt = new RawText(oldBytes);
    
    // do the text diff
    
    //check if it is java
    //setup ast 
    
    
    
    
    // TODO Auto-generated method stub
    return null;
  }

  private List<Difference> getBestScores(List<List<Difference>> grid) {
    List<Difference> result = new ArrayList<Difference>();
    Difference lowest = null;
    int lowestRow = 0;
    int lowestCol = 0;
    for (int ins = 0; ins < grid.size(); ins++) {
      for (int del = 0; del < grid.get(ins).size(); del++) {
        Difference score = grid.get(ins).get(del);
        if (lowest == null || lowest.getScore() > score.getScore()) {
          lowest = score;
          lowestRow = ins;
          lowestCol = del;
        }
      }
    }
    if (lowest != null) {

      grid.remove(lowestRow);
      for (int ins = 0; ins < grid.size(); ins++) {
        grid.get(ins).remove(lowestCol);
      }
      if (grid.size() > 0 && grid.get(0).size() > 0) {
        result.addAll(getBestScores(grid));
      }
      result.add(lowest);
    }
    return result;
  }

  int getScore(List<Difference> differences) {
    int score = 0;
    for (Difference difference : differences) {
      score += difference.getOldCb().getBlock().getEnd()
          - difference.getOldCb().getBlock().getStart();
      score += difference.getNewCb().getBlock().getEnd()
          - difference.getNewCb().getBlock().getStart();

    }
    return score;
  }

  public List<Difference> matchup(List<Difference> differences) throws MissingObjectException, IOException {

    // split up differences according to type
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();
    List<Difference> others = new ArrayList<Difference>();

    for (Difference diff : differences) {
      if (diff.getType() == Type.INSERT) {
        inserts.add(diff);
      } else {
        if (diff.getType() == Type.DELETE) {
          deletes.add(diff);
        } else {
          others.add(diff);
        }
      }
    }

    List<List<Difference>> grid = new ArrayList<List<Difference>>();

    // find all potential matches for deletes and inserts
    for (int ins = 0; ins < inserts.size(); ins++) {
      List<Difference> rows = new ArrayList<Difference>();
      for (int del = 0; del < deletes.size(); del++) {
        long score = 0;
        try {
          Difference diff = (Difference) inserts.get(ins).clone();

          diff.setOldCb(deletes.get(del).getOldCb());
          diff.setNewCb(inserts.get(ins).getNewCb());
          diff.setType(Type.MODIFY);

          List<Difference> scoreList = drilldown(diff);

          for (Difference scoreDiff : scoreList) {
            score += scoreDiff.getOldCb().getBlock().getEnd()
                - scoreDiff.getOldCb().getBlock().getStart();
            score += scoreDiff.getNewCb().getBlock().getEnd()
                - scoreDiff.getNewCb().getBlock().getStart();
          }

          long base = diff.getOldCb().getBlock().getEnd()
              - diff.getOldCb().getBlock().getStart();
          base += diff.getNewCb().getBlock().getEnd()
              - diff.getNewCb().getBlock().getStart();
          if (base != 0) {
            diff.setScore(score / base);

          } else {
            diff.setScore(score * 2);
          }
          rows.add(diff);
        } catch (CloneNotSupportedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      grid.add(rows);
    }

    others.addAll(getBestScores(grid));
    return others;
  }

  public Collection<? extends Difference> matchup(List<Difference> insMatchs,
      List<Difference> delMatchs) {
    // TODO Auto-generated method stub
    return null;
  }

  void splitRemainder(List<List<Difference>> grid) {

  }

}