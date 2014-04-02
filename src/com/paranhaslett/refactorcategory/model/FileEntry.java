package com.paranhaslett.refactorcategory.model;

import com.paranhaslett.refactorcategory.Range;

public interface FileEntry {
  
  String getRawText(Range<Long> range);
  
  String getPath();

}
