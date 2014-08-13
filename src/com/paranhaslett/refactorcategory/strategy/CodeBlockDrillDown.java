package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.paranhaslett.refactorcategory.CodeBlock;
import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.Range;
import com.paranhaslett.refactorcategory.ast.Ast;

public class CodeBlockDrillDown extends DrillDown {

  @Override
  List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {
    List<Difference> results = new ArrayList<Difference>();
    
    /* Get all the children differences */

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    Ast oldAst = oldCb.getAst();
    Ast newAst = newCb.getAst();

    if (oldAst == null && newAst == null) {
      // do the text difference
      results.addAll(new TextDrillDown().drilldown(difference));
    }

    if (oldAst != null && newAst != null) {
      // do the java difference
      List<CodeBlock> newJavaChildren = new ArrayList<CodeBlock>();
      List<CodeBlock> oldJavaChildren = new ArrayList<CodeBlock>();
      List<CodeBlock> newTextChildren = new ArrayList<CodeBlock>();
      List<CodeBlock> oldTextChildren = new ArrayList<CodeBlock>();
      
      getChildren(newCb, newJavaChildren, newTextChildren);
      getChildren(oldCb, oldJavaChildren, oldTextChildren);
      
      results.addAll(new JavaDrillDown().drilldown(difference, oldJavaChildren, newJavaChildren));  
      results.addAll(new TextDrillDown().drilldown(difference, oldTextChildren, newTextChildren));
    }
    
    
    return results;
  }
  
  private void getChildren(CodeBlock cb, List<CodeBlock> javaChildren,
      List<CodeBlock> textChildren) {
    
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
          javaChildren.add(javaCb);

        }
        if (minStart < child.getRange().getStart() - 1) {
          Range<Long> commentRange = new Range<Long>(minStart, (long) (child
              .getRange().getStart() - 1));

          if (commentRange.intersects(cb.getBlock())) {
            CodeBlock commentCb = (CodeBlock) cb.clone();
            commentRange = commentRange.getIntersection(cb.getBlock());
            commentCb.setBlock(commentRange);
            commentCb.setAst(null);
            if(!commentRange.isEmpty()){
              textChildren.add(commentCb);
            }

          }
        }

      } catch (CloneNotSupportedException e) {
        // This should not happen as clone of Difference and CodeBlock are valid
        e.printStackTrace();
      }
      minStart = child.getRange().getEnd() + 1;
    }
    
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
