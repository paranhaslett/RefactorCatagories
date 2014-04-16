package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;

import AST.ASTNode;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Config;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;

public class TextDrillDown extends DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException, GitAPIException {

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    // Convert Edit List into lists of differences
    List<Difference> results = new ArrayList<Difference>();

    RawText oldRaw = new RawText(oldCb.getRawText().getBytes());
    RawText newRaw = new RawText(newCb.getRawText().getBytes());
    
    long oldPos = oldCb.getBlock().getStart();
    long newPos = newCb.getBlock().getStart();

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            RawTextComparator.WS_IGNORE_ALL, oldRaw, newRaw);

    // Convert Edit List into lists of differences
    for (Edit edit : editList) {
        Difference diff = createDiff(difference, Type.UNKNOWN, 0.0);

        Range<Long> oldRange = convertEditRange(oldPos, edit.getBeginA(),
            edit.getEndA());
        Range<Long> newRange = convertEditRange(newPos, edit.getBeginB(),
            edit.getEndB());

        diff.getOldCb().setBlock(oldRange);
        diff.getNewCb().setBlock(newRange);

        switch (edit.getType()) {
        case INSERT:
          diff.setType(Difference.Type.INSERT);
          diff.setScore(Config.scoreUnit);
          break;
        case DELETE:
          diff.setType(Difference.Type.DELETE);
          diff.setScore(Config.scoreUnit);
          break;
        case REPLACE:
          diff.setType(Difference.Type.MODIFY);
          diff.setScore(Config.scoreUnit * 2);
          break;
        default:
          break;
        }
        results.add(diff);
    }
    return results;
  }

  private Range<Long> convertEditRange(long prevStart, int start,int end) {
    long rangeStart = (long) ASTNode.makePosition(start, 0) + prevStart;
    long rangeEnd = (long) ASTNode.makePosition(end, 0) + prevStart;
    return new Range<Long>(rangeStart, rangeEnd);
  }

}
