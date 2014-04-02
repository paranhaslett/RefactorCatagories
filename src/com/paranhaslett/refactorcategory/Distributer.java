package com.paranhaslett.refactorcategory;

import java.io.IOException;

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
    }

  }

}
