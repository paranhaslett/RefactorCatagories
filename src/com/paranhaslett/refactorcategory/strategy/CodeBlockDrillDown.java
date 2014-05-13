package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.paranhaslett.refactorcategory.Difference;
import com.paranhaslett.refactorcategory.ast.Ast;

public class CodeBlockDrillDown extends DrillDown {

  @Override
  List<Difference> drilldown(Difference difference) throws IOException,
      GitAPIException {
    List<Difference> results = new ArrayList<Difference>();

    Ast oldAst = difference.getOldCb().getAst();
    Ast newAst = difference.getNewCb().getAst();

    if (oldAst == null && newAst == null) {
      // do the text difference
      results.addAll(new TextDrillDown().drilldown(difference));
    }

    if (oldAst != null && newAst != null) {
      // do the java difference
      results.addAll(new JavaDrillDown().drilldown(difference));
    }

    return results;
  }

}
