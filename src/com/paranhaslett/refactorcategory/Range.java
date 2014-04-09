package com.paranhaslett.refactorcategory;

public class Range <T extends Comparable<T>> implements Cloneable{

  @Override
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  private T end;
  private T start;
  
  
  public Range(Range<T> range){
     this(range.start, range.end);
  }

  public Range(T start, T end) {
    this.start=start;
    this.end=end;
  }

  public boolean contains(Range<T> range) {
    return (range.start.compareTo(start)>-1 && range.end.compareTo(end)<1);
  }

  public boolean contains(T value) {
    return (value.compareTo(start)>-1 && value.compareTo(end)<1);
  }

  public T getEnd() {
    return end;
  }
  
  public Range<T> getIntersection(Range<T> range) {
    Range<T> result = new Range<T>(range);
    if (start.compareTo(range.start)>0){
      result.start = start;
    } 
    
    if (end.compareTo(range.end)<0){
      result.end = end;
    } 
    return result;
  }

  public T getStart() {
    return start;
  }

  public boolean intersects(Range<T> range) {
    return (contains(range.start)||range.contains(start));
  }

  @Override
  public String toString() {
    return "(" + start + ", " + end + ")";
  }

}
