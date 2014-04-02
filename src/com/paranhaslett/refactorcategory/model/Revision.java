package com.paranhaslett.refactorcategory.model;

import java.util.List;

import com.paranhaslett.refactorcategory.Difference;

public interface Revision {
  List<Difference> getFileEntries(Difference difference);
  String getName();
}
