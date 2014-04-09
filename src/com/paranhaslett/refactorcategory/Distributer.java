package com.paranhaslett.refactorcategory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.paranhaslett.refactorcategory.Difference.Language;
import com.paranhaslett.refactorcategory.strategy.RepoDrillDown;

public class Distributer {

  /**
   * @param args
   */
  public static void main(String[] args) {
    RepoDrillDown rdd = new RepoDrillDown();
    List<Difference> results = new ArrayList<Difference>();
    try {
      results.addAll(rdd.drilldown(null));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (GitAPIException e) {
      e.printStackTrace();
    }
    
    for (Difference diff:results){
        System.out.println(diff);
        if (diff.language == Language.VALID_JAVA){
        System.out.println(diff.getOldCb().getRawText());
        System.out.println(diff.getNewCb().getRawText());
      }
    }
    
  }
}
