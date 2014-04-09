package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.MissingObjectException;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;

public class TextDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference)
      throws MissingObjectException, IOException {

    List<Difference> results = new ArrayList<Difference>();

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    RawText oldRaw = oldCb.getEntry().getRawText(oldCb.getBlock());
    RawText newRaw = newCb.getEntry().getRawText(newCb.getBlock());

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            RawTextComparator.WS_IGNORE_ALL, oldRaw, newRaw);

    // Convert Edit List to a list of differences
    for (Edit edit : editList) {
      try {
        Difference diff = (Difference) difference.clone();

        Range<Long> oldRange = convertEditRange(edit.getBeginA(),
            edit.getEndA());
        Range<Long> newRange = convertEditRange(edit.getBeginB(),
            edit.getEndB());

        diff.getOldCb().setBlock(oldRange);
        diff.getNewCb().setBlock(newRange);
        results.add(diff);
      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return results;
  }

  private Range<Long> convertEditRange(int start, int end) {
    long rangeStart = (long) ASTNode.makePosition(start, 0);
    long rangeEnd = (long) ASTNode.makePosition(end, 0);
    return new Range<Long>(rangeStart, rangeEnd);
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

  public List<Difference> matchup(List<Difference> differences)
      throws MissingObjectException, IOException {

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

          List<Difference> scoreList = new ArrayList<Difference>();// drilldown(diff);

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
