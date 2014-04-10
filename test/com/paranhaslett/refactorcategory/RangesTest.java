package com.paranhaslett.refactorcategory;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RangesTest {
  Ranges<Integer> ranges;

  @Before
  public void setUp() throws Exception {
    ranges = new Ranges<Integer>();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testAddRanges() {
    ranges.add(7, 10);
    assertFalse(ranges.contains(6));
    assertTrue(ranges.contains(7));
    assertTrue(ranges.contains(10));
    assertFalse(ranges.contains(11));
    ranges.add(4, 5);
    assertFalse(ranges.contains(3));
    assertTrue(ranges.contains(4));
    assertTrue(ranges.contains(5));
    assertFalse(ranges.contains(6));
    assertTrue(ranges.contains(7));
    assertTrue(ranges.contains(10));
    assertFalse(ranges.contains(11));
    ranges.add(5, 9);
    assertFalse(ranges.contains(3));
    assertTrue(ranges.contains(4));
    assertTrue(ranges.contains(5));
    assertTrue(ranges.contains(6));
    assertTrue(ranges.contains(7));
    assertTrue(ranges.contains(9));
    assertTrue(ranges.contains(10));
    assertFalse(ranges.contains(11));
  }
  
  @Test
  public void testRemoveRanges() {
    ranges.add(1, 10);
    assertFalse(ranges.contains(0));
    assertTrue(ranges.contains(1));
    assertTrue(ranges.contains(10));
    assertFalse(ranges.contains(11));
    ranges.remove(4, 5);
    assertFalse(ranges.contains(0));
    assertTrue(ranges.contains(1));
    assertTrue(ranges.contains(3));
    assertFalse(ranges.contains(4));
    assertFalse(ranges.contains(5));
    assertTrue(ranges.contains(6));
    assertTrue(ranges.contains(10));
    assertFalse(ranges.contains(11));    
    ranges.remove(5, 9);
    assertFalse(ranges.contains(0));
    assertTrue(ranges.contains(1));
    assertTrue(ranges.contains(3));
    assertFalse(ranges.contains(4));
    assertFalse(ranges.contains(5));
    assertFalse(ranges.contains(6));
    assertFalse(ranges.contains(9));
    assertTrue(ranges.contains(10));
    assertFalse(ranges.contains(11));    
  }

  @Test
  public void testContainsRanges() {
    ranges.add(1, 10);
    ranges.remove(4, 5);
    assertFalse(ranges.contains(1, 10));
    assertTrue(ranges.contains(1, 3));
    assertFalse(ranges.contains(1, 4));
    assertFalse(ranges.contains(4, 5));
    assertFalse(ranges.contains(3, 5));
    assertFalse(ranges.contains(4, 6));
    assertFalse(ranges.contains(5, 6));
    assertTrue(ranges.contains(6,10));
    assertFalse(ranges.contains(6,11));
    assertFalse(ranges.contains(10,11));
  }
  
  @Test
  public void testIncludesRanges(){
    ranges.add(1,5);
    Ranges<Integer> ranges2 = new Ranges<Integer>();
    ranges2.add(6,10);
    assertFalse(ranges.intersects(ranges2));
    assertFalse(ranges2.intersects(ranges));
    Ranges<Integer> ranges3 = new Ranges<Integer>();
    ranges3.add(5,6);
    assertTrue(ranges.intersects(ranges3));
    assertTrue(ranges2.intersects(ranges3));
    ranges2.add(3,4);
    assertTrue(ranges.intersects(ranges2));
    assertTrue(ranges2.intersects(ranges));
  }
}
