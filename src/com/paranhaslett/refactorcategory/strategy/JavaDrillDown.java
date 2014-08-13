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
import com.paranhaslett.refactorcategory.ast.CommentAst;
import com.paranhaslett.refactorcategory.compare.CodeBlockAstComparitor;
import com.paranhaslett.refactorcategory.compare.CodeBlockAstSequence;

public class JavaDrillDown extends DrillDown {

  @Override
  public List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {

   
    return null;
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

  public List<Difference> drilldown(Difference difference,
      List<CodeBlock> oldChildren, List<CodeBlock> newChildren) throws IOException, GitAPIException {
    
    /* Get all the children differences */

    CodeBlock oldCb = difference.getOldCb();
    CodeBlock newCb = difference.getNewCb();

    // match up the AST children using longest common subsequence

    CodeBlockAstSequence oldSeq = new CodeBlockAstSequence(oldChildren);
    CodeBlockAstSequence newSeq = new CodeBlockAstSequence(newChildren);

    EditList editList = DiffAlgorithm
        .getAlgorithm(SupportedAlgorithm.HISTOGRAM).diff(
            CodeBlockAstComparitor.DEFAULT, oldSeq, newSeq);

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

        if (!editA.isEmpty() && !editB.isEmpty() && editA.contains(oldindex)
            && editB.contains(newindex) && newCmp.getAst() != null
            && oldCmp.getAst() != null) {
          Difference childDiff;
          if (newCmp.getAst().equalTypes(oldCmp.getAst())) {
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
          
          
          if (!editA.isEmpty() && editA.contains(oldindex) ) {
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

          Range<Long> oldCmpBlock = oldCmp.getAst().getRange();
          Range<Long> newCmpBlock = newCmp.getAst().getRange();
          Range<Long> oldCbBlock = oldCb.getBlock();
          Range<Long> newCbBlock = newCb.getBlock();

          Difference childDiff = createDiff(difference, oldCmp, newCmp,
              Type.EQUIVALENT, 0.0);

          if (oldCbBlock.contains(oldCmpBlock)
              && newCbBlock.contains(newCmpBlock)) {
           
            List<Difference> uncol = new AstDrillDown().drilldown(childDiff);
            List<Difference> collated = collate(childDiff, uncol);
            results.addAll(collated);

          } else {   
            List<Difference> uncol = new CodeBlockDrillDown().drilldown(childDiff);
            if(uncol.size()==0){
              uncol = new CodeBlockDrillDown().drilldown(childDiff);
            }
            List<Difference> collated = collate(childDiff, uncol);
            collated.addAll(modifies);
            return collated;
          }
        }
      
    }

    results.addAll(matchup(inserts, deletes));
    
    results.addAll(modifies);
    
    return results;
  }
}
