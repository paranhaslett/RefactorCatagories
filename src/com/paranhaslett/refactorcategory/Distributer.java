package com.paranhaslett.refactorcategory;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.paranhaslett.refactorcategory.strategy.RepoDrillDown;

public class Distributer {

  /**
   * @param args
   */
  public static void main(String[] args) {
    RepoDrillDown rdd = new RepoDrillDown();
    try {
      rdd.drilldown(null);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (GitAPIException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
