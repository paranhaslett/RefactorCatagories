package com.paranhaslett.refactorcategory.strategy;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.paranhaslett.refactorcategory.Difference;

public interface DrillDown {

  List<Difference> drilldown(Difference difference) throws IOException, GitAPIException;
}
