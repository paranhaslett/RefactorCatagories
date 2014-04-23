package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffAlgorithm.SupportedAlgorithm;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Config;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Difference.Language;
import com.paranhaslett.refactorcategory.Difference.Type;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;
import com.paranhaslett.refactorcategory.ast.CommentAst;
import com.paranhaslett.refactorcategory.compare.CodeBlockComparitor;
import com.paranhaslett.refactorcategory.compare.CodeBlockSequence;

public class JavaDrillDown extends DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {

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

    
    List<Difference> modifies = new ArrayList<Difference>();
    List<Difference> inserts = new ArrayList<Difference>();
    List<Difference> deletes = new ArrayList<Difference>();
    List<Difference> results = new ArrayList<Difference>();

    while (oldindex < oldChildren.size() && newindex < newChildren.size()) {

      CodeBlock oldCmp = oldChildren.get(oldindex);
      CodeBlock newCmp = newChildren.get(newindex);

      boolean isInEditList = false;

      for (Edit edit : editList) {
        Range<Integer> editA = new Range<Integer>(edit.getBeginA(),
            edit.getEndA());
        Range<Integer> editB = new Range<Integer>(edit.getBeginB(),
            edit.getEndB());

        if (!editA.isEmpty() && !editB.isEmpty()
            //&& editA.getEnd() - editA.getStart() == 1
            //&& editB.getEnd() - editB.getStart() == 1
            && editA.contains(oldindex) && editB.contains(newindex)) {
          Difference childDiff;
          if (newCmp.getAst() != null 
              && oldCmp.getAst() != null 
              && newCmp.getAst().equalTypes(oldCmp.getAst())) {
            childDiff = createDiff(difference, oldCmp, newCmp, Type.RENAMED,
                Config.scoreUnit);
          } else {
            childDiff = createDiff(difference, oldCmp, newCmp, Type.MODIFY,
                2 * Config.scoreUnit);
          }
          oldindex++;
          newindex++;
          modifies.add(childDiff);
          isInEditList = true;

        } else {

          if (!editA.isEmpty() && editA.contains(oldindex)) {
            Difference childDiff = createDiff(difference, oldCmp, newCmp,
                Type.DELETE, Config.scoreUnit);
            // System.out.println("DELETE:" + oldCmp.getRawText());
            oldindex++;
            deletes.add(childDiff);
            isInEditList = true;
          }
          if (!editB.isEmpty() && editB.contains(newindex)) {
            Difference childDiff = createDiff(difference, oldCmp, newCmp,
                Type.INSERT, Config.scoreUnit);
            // System.out.println("INSERT:" + newCmp.getRawText());
            inserts.add(childDiff);
            newindex++;
            isInEditList = true;
          }
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

          Difference childDiff = createDiff(difference, oldCmp, newCmp,
              Type.EQUIVALENT, 0.0);

          
          if (oldCbBlock.contains(oldCmpBlock)
              && newCbBlock.contains(newCmpBlock)) {

            List<Difference> collated = collate(childDiff,
                new AstDrillDown().drilldown(childDiff));
            results.addAll(collated);
          } else {
            List<Difference> collated = collate(childDiff,drilldown(childDiff));
            // no need to match up if it is simply a drilldown
            return collated;
          }
        }
      }
    }

    // split up comments and java differences and test separately
    List<Difference> textInserts = new ArrayList<Difference>();
    List<Difference> javaInserts = new ArrayList<Difference>();
    for (Difference diff : inserts) {
      if (diff.getOldCb().getAst() != null && diff.getNewCb().getAst() != null) {
        javaInserts.add(diff);
      }

      if (diff.getOldCb().getAst() == null && diff.getNewCb().getAst() == null) {
        textInserts.add(diff);
      }
    }

    List<Difference> textDeletes = new ArrayList<Difference>();
    List<Difference> javaDeletes = new ArrayList<Difference>();
    for (Difference diff : deletes) {
      if (diff.getOldCb().getAst() != null && diff.getNewCb().getAst() != null) {
        javaDeletes.add(diff);
      }

      if (diff.getOldCb().getAst() == null && diff.getNewCb().getAst() == null) {
        textDeletes.add(diff);
      }
    }

    results.addAll(new TextDrillDown().matchup(textInserts, textDeletes));
    results.addAll(matchup(javaInserts, javaDeletes));
    results.addAll(modifies);
    return results;
  }

  Difference createDiff(Difference difference, CodeBlock oldCmp,
      CodeBlock newCmp, Type type, double score) {
    Difference childDiff = super.createDiff(difference, type, score);
    childDiff.setOldCb(oldCmp);
    childDiff.setNewCb(newCmp);
    if (areJavaBlocks(oldCmp, newCmp)) {
      childDiff.setLanguage(Language.JAVA);
    } else {
      childDiff.setLanguage(Language.COMMENT);
    }
    return childDiff;
  }

  private boolean areJavaBlocks(CodeBlock oldCmp, CodeBlock newCmp) {
    return oldCmp.getAst() != null && newCmp.getAst() != null
        && !oldCmp.getAst().isEmpty() && !newCmp.getAst().isEmpty()
        && !(oldCmp.getAst() instanceof CommentAst)
        && !(newCmp.getAst() instanceof CommentAst);
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
          Range<Long> commentRange = new Range<Long>(minStart, (long) (child
              .getRange().getStart() - 1));

          if (commentRange.intersects(cb.getBlock())) {
            CodeBlock commentCb = (CodeBlock) cb.clone();
            commentRange = commentRange.getIntersection(cb.getBlock());
            commentCb.setBlock(commentRange);
            commentCb.setAst(null);// new CommentAst(commentCb.getRawText(),
                                   // commentRange.getStart(),
                                   // commentRange.getEnd()));
            children.add(commentCb);

          }
        }

      } catch (CloneNotSupportedException e) {
        // This should not happen as clone of Difference and CodeBlock are valid
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
}
