package com.paranhaslett.refactorcategory.strategy;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.CodeBlockComparitor;
import com.paranhaslett.refactorcategory.CodeBlockSequence;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Language;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.Ranges;
import com.paranhaslett.refactorcategory.ast.Ast;

public class JavaDrillDown implements DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) {

    /* Get all the children differences */

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    List<CodeBlock> newChildren = getChildren(newCb);
    List<CodeBlock> oldChildren = getChildren(oldCb);

    // match up the AST children using longest common subsequence

    CodeBlockSequence oldSeq = new CodeBlockSequence(oldChildren);
    CodeBlockSequence newSeq = new CodeBlockSequence(newChildren);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            CodeBlockComparitor.DEFAULT, oldSeq, newSeq);

    int oldindex = 0;
    int newindex = 0;

    List<Difference> others = new ArrayList<Difference>();
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();

    while (oldindex < oldChildren.size() && newindex < newChildren.size()) {

      CodeBlock oldCmp = oldChildren.get(oldindex);
      CodeBlock newCmp = newChildren.get(newindex);

      boolean isInEditList = false;

      for (Edit edit : editList) {
        Range<Integer> editA = new Range<Integer>(edit.getBeginA(),
            edit.getEndA());
        Range<Integer> editB = new Range<Integer>(edit.getBeginB(),
            edit.getEndB());

        if (!editA.isEmpty() && editA.contains(oldindex)) {
          Difference childDiff = createChild(difference, oldCmp, newCmp,
              Type.DELETE);
          System.out.println("DELETE:" + oldCmp.getRawText());
          oldindex++;
          deletes.add(childDiff);
          isInEditList = true;
        }
        if (!editB.isEmpty() && editB.contains(newindex)) {
          Difference childDiff = createChild(difference, oldCmp, newCmp,
              Type.INSERT);
          System.out.println("INSERT:" + newCmp.getRawText());
          inserts.add(childDiff);
          newindex++;
          isInEditList = true;
        }
        if (isInEditList) {
          break;
        }
      }

      if (!isInEditList) {
        newindex++;
        oldindex++;

        if (areJavaBlocks(oldCmp, newCmp)) {

          Range<Long> oldCmpBlock = oldCmp.getAst().getRange();
          Range<Long> newCmpBlock = newCmp.getAst().getRange();
          Range<Long> oldCbBlock = oldCb.getBlock();
          Range<Long> newCbBlock = newCb.getBlock();
          
          Difference childDiff = createChild(difference, oldCmp, newCmp,
              Type.EQUIVALENT);

          if (oldCbBlock.contains(oldCmpBlock)
              && newCbBlock.contains(newCmpBlock)) {         
            others.addAll(new AstDrillDown().drilldown(childDiff));
          } else {
            others.addAll(drilldown(childDiff));
          }
        } 
      }
    }

    List<Difference> result = others;
    result.addAll(deletes);
    result.addAll(inserts);
    return result;
  }

  private Difference createChild(Difference difference, CodeBlock oldCmp,
      CodeBlock newCmp, Type type) {
    try {
      Difference childDiff = (Difference) difference.clone();
      childDiff.setOldCb((CodeBlock) oldCmp.clone());
      childDiff.setNewCb((CodeBlock) newCmp.clone());
      childDiff.setType(type);
      if (areJavaBlocks(oldCmp, newCmp)) {
        childDiff.setLanguage(Language.VALID_JAVA);
      } else {
        childDiff.setLanguage(Language.COMMENT);
      }
      return childDiff;
    } catch (CloneNotSupportedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  private boolean areJavaBlocks(CodeBlock oldCmp, CodeBlock newCmp) {
    return oldCmp.getAst() != null && newCmp.getAst() != null
        && !oldCmp.getAst().isEmpty() && !newCmp.getAst().isEmpty();
  }

  private List<Difference> isAstInBlocks(Difference difference) {
    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    Ast oldAst = oldCb.getAst();
    Ast newAst = newCb.getAst();

    /** is the Ast fully inside a block? */

    Range<Long> oldAstR = oldAst.getRange();
    Range<Long> newAstR = newAst.getRange();
    List<Difference> result = new ArrayList<Difference>();
    boolean oldPartInRange = false;
    boolean newPartInRange = false;

    boolean oldInRange = false;
    boolean newInRange = false;

    Range<Long> oldBlock = oldCb.getBlock();
    Range<Long> newBlock = newCb.getBlock();

    if (oldBlock.intersects(oldAstR)) {
      oldPartInRange = true;
      if (oldBlock.contains(oldAstR)) {
        oldInRange = true;
      }
    }

    if (newBlock.intersects(newAstR)) {
      newPartInRange = true;
      if (newBlock.contains(newAstR)) {
        newInRange = true;
      }
    }

    if (!oldPartInRange) {
      if (!newPartInRange) {
        // this one is not interesting
        // return an empty result
        return result;
      }
      difference.setType(Type.INSERT);
      if (newInRange) {
        // this one IS interesting
        // return insert difference
        result.add(difference);
        return result;
      }
    } else {
      if (!newPartInRange) {
        difference.setType(Type.DELETE);
        if (oldInRange) {
          // this one IS interesting
          // return delete difference
          result.add(difference);
          return result;
        }
      } else {
        difference.setType(Type.MODIFY);
        if (oldInRange && newInRange) {
          // this one IS interesting
          // return modify difference
          result.add(difference);
          return result;
        }
      }
    }
    // if we get here we need to drilldown some more
    // result = needtodd(difference);
    return result;
  }

  private List<Difference> isAstInBlocks(Difference parent,
      List<Difference> compareAgainst) {
    CodeBlock oldCb = parent.getOldCb();
    CodeBlock newCb = parent.getNewCb();

    Ast oldAst = oldCb.getAst();
    Ast newAst = newCb.getAst();

    /** is the Ast fully inside a block? */

    Range<Long> oldAstR = oldAst.getRange();
    Range<Long> newAstR = newAst.getRange();
    List<Difference> result = new ArrayList<Difference>();
    boolean oldPartInRange = false;
    boolean newPartInRange = false;

    boolean oldInRange = false;
    boolean newInRange = false;

    for (Difference range : compareAgainst) {
      Range<Long> oldBlock = range.getOldCb().getBlock();
      Range<Long> newBlock = range.getNewCb().getBlock();

      if (oldBlock.intersects(oldAstR)) {
        oldPartInRange = true;
        if (oldBlock.contains(oldAstR)) {
          oldInRange = true;
        }
      }

      if (newBlock.intersects(newAstR)) {
        newPartInRange = true;
        if (newBlock.contains(newAstR)) {
          newInRange = true;
        }
      }
    }

    if (!oldPartInRange) {
      if (!newPartInRange) {
        // this one is not interesting
        // return an empty result
        return result;
      }
      parent.setType(Type.INSERT);
      if (newInRange) {
        // this one IS interesting
        // return insert difference
        result.add(parent);
        return result;
      }
    } else {
      if (!newPartInRange) {
        parent.setType(Type.DELETE);
        if (oldInRange) {
          // this one IS interesting
          // return delete difference
          result.add(parent);
          return result;
        }
      } else {
        parent.setType(Type.MODIFY);
        if (oldInRange && newInRange) {
          // this one IS interesting
          // return modify difference
          result.add(parent);
          return result;
        }
      }
    }
    // if we get here we need to drilldown some more
    // result = needtodd(parent);
    return result;
  }

  private List<CodeBlock> getChildrens(CodeBlock cb) {
    List<CodeBlock> children = new ArrayList<CodeBlock>();

    resolveEmptyAsts(cb);

    long minStart = cb.getAst().getStart();

    for (Ast child : cb.getAst().getChildren()) {

      try {

        Ranges<Long> javaAst = child.getRanges();

        if (javaAst.intersects(cb.getBlocks())) {
          CodeBlock javaCb = (CodeBlock) cb.clone();
          javaAst = javaAst.intersection(cb.getBlocks());
          javaCb.setBlocks(javaAst);
          javaCb.setAst(child);
          children.add(javaCb);
        }
        if (minStart < child.getRange().getStart() - 1) {
          Ranges<Long> commentAst = new Ranges<Long>();
          commentAst.add(minStart, (long) (child.getStart() - 1));
          if (commentAst.intersects(cb.getBlocks())) {
            CodeBlock commentCb = (CodeBlock) cb.clone();
            commentAst = commentAst.intersection(cb.getBlocks());
            commentCb.setBlocks(commentAst);
            commentCb.setAst(null);
            children.add(commentCb);

          }
        }

      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      minStart = child.getRange().getEnd() + 1;
    }

    return children;
  }

  private List<CodeBlock> getChildren(CodeBlock cb) {
    List<CodeBlock> children = new ArrayList<CodeBlock>();

    resolveEmptyAsts(cb);

    long minStart = cb.getAst().getRange().getStart();

    for (Ast child : cb.getAst().getChildren()) {

      try {

        Range<Long> javaAst = child.getRange();

        if (javaAst.intersects(cb.getBlock())) {
          CodeBlock javaCb = (CodeBlock) cb.clone();
          javaAst = javaAst.getIntersection(cb.getBlock());
          javaCb.setBlock(javaAst);
          javaCb.setAst(child);
          children.add(javaCb);

        }
        if (minStart < child.getRange().getStart() - 1) {
          Range<Long> commentAst = new Range<Long>(minStart, (long) (child
              .getRange().getStart() - 1));

          if (commentAst.intersects(cb.getBlock())) {
            CodeBlock commentCb = (CodeBlock) cb.clone();
            commentAst = commentAst.getIntersection(cb.getBlock());
            commentCb.setBlock(commentAst);
            commentCb.setAst(null);
            children.add(commentCb);

          }
        }

      } catch (CloneNotSupportedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      minStart = child.getRange().getEnd() + 1;
    }

    return children;
  }

  private void resolveEmptyAsts(CodeBlock cb) {
    long minStart = cb.getAst().getRange().getStart();

    // sort out any JastAddJ numbering problems
    Ast previous = null;

    for (Ast child : cb.getAst().getChildren()) {
      if (child.getRange().getStart() == 0) {
        child.setStart(minStart);
      } else {
        if (previous != null && previous.getRange().getEnd() == 0) {
          previous.setEnd(minStart);
        }
        minStart = child.getRange().getEnd();
      }
      previous = child;

    }
    if (previous != null && previous.getRange().getEnd() == 0) {
      previous.setEnd(cb.getAst().getRange().getEnd());
    }
  }

  public List<Difference> matchup(List<Difference> differences) {

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

}
