package com.paranhaslett.refactorcategory;

import java.util.SortedSet;
import java.util.TreeSet;

public class Ranges<T extends Comparable<T>> {
  private class Golt<S extends T> implements Comparable<Golt<S>> {
    private Type type;
    private S value;

    Golt(S value, Type type) {
      this.value = value;
      this.type = type;
    }

    @Override
    public int compareTo(Golt<S> golt) {
      if (golt.value != value) {
        return value.compareTo(golt.value);
      }

      return type.compareTo(golt.type);
    }

    @Override
    public String toString() {
      switch (type) {
      case LESS_THAN:
        return "<" + value;
      case LT_EQUAL:
        return ("<=" + value);
      case GT_EQUAL:
        return (value + ">=");
      case GREATER:
        return (value + ">");
      default:
        return value + "";
      }
    }
  }

  private static enum Type {
    LESS_THAN, GT_EQUAL, EXACTLY,  LT_EQUAL, GREATER
  }

  private SortedSet<Golt<T>> greaterOrLessThans = new TreeSet<Golt<T>>();

  public void add(Ranges<T> ranges) {
    //TODO
  }

  public void add(T value) {
    greaterOrLessThans.add(new Golt<T>(value, Type.EXACTLY));
  }

  public void add(T start, T end) {
    SortedSet<Golt<T>> result = new TreeSet<Golt<T>>(greaterOrLessThans);

    // remove any values between
    Golt<T> startAndOne = new Golt<T>(start, Type.LESS_THAN);
    Golt<T> endButOne = new Golt<T>(end, Type.GREATER);
    SortedSet<Golt<T>> subset = greaterOrLessThans.subSet(startAndOne,
        endButOne);
    for (Golt<T> removal : subset) {
      result.remove(removal);
    }

    // defaults to inclusive
    Golt<T> startGolt = new Golt<T>(start, Type.GT_EQUAL);

    Golt<T> endGolt = new Golt<T>(end, Type.LT_EQUAL);
    // is the start not with an existing range
    if (!contains(startGolt)) {
      result.add(startGolt);
    }
    // is the end not within an existing range
    if (!contains(endGolt)) {
      result.add(endGolt);
    }

    greaterOrLessThans = result;

  }

  public void addGreaterThan(T value) {
    // defaults to exclusive
    greaterOrLessThans.add(new Golt<T>(value, Type.GREATER));
  }

  public void addLessThan(T value) {
    // defaults to exclusive
    greaterOrLessThans.add(new Golt<T>(value, Type.LESS_THAN));
  }

  private boolean contains(Golt<T> value) {
    // if previous value is the beginning of a range

    Golt<T> prevVal = getPrevious(value);
    Golt<T> nextVal = getNext(value);

    if (prevVal != null
        && ((prevVal.value == value.value && prevVal.type != Type.LESS_THAN)
            || prevVal.type == Type.GREATER || prevVal.type == Type.GT_EQUAL)) {
      return true;
    }
    // or if following value is the end of a range

    if (nextVal != null
        && ((nextVal.value == value.value && nextVal.type != Type.GREATER)
            || nextVal.type == Type.LESS_THAN || nextVal.type == Type.LT_EQUAL)) {
      return true;
    }
    return false;
  }

  public boolean contains(T value) {
    return contains(new Golt<T>(value, Type.EXACTLY));
  }

  public boolean contains(T start, T end) {
    // if starts next value is after the end and ends previous value is before
    // the start
    Golt<T> startGolt = new Golt<T>(start, Type.GREATER);
    Golt<T> endGolt = new Golt<T>(end, Type.LESS_THAN);
    Golt<T> startNext = getNext(startGolt);
    Golt<T> endPrev = getPrevious(endGolt);

    if (endPrev != null && startNext != null) {
      // if end prev is before the start golt and end Prev is a less than type
      if ((startGolt.value.compareTo(endPrev.value) > 0 && (endPrev.type == Type.GREATER || endPrev.type == Type.GT_EQUAL))
          || startGolt.value.compareTo(endPrev.value) == 0
          && (endPrev.type == Type.EXACTLY || endPrev.type == Type.GT_EQUAL)) {

        if ((endGolt.value.compareTo(startNext.value) < 0 && (startNext.type == Type.LESS_THAN || startNext.type == Type.LT_EQUAL))
            || endGolt.value.compareTo(startNext.value) == 0
            && (startNext.type == Type.EXACTLY || startNext.type == Type.LT_EQUAL)) {
          return true;
        }
      }
    }
    return false;
  }

  private Golt<T> getNext(Golt<T> value) {
    SortedSet<Golt<T>> subset = (greaterOrLessThans.tailSet(value));
    Golt<T> tgolt = null;
    if (subset.size() > 0) {
      tgolt = (subset.first());
    }
    return tgolt;
  }

  private Golt<T> getPrevious(Golt<T> value) {
    SortedSet<Golt<T>> subset = greaterOrLessThans.headSet(value);
    Golt<T> tgolt = null;
    if (subset.size() > 0) {
      tgolt = (subset.last());
    }
    return tgolt;

  }

  public T getStart() {
    Golt<T> result = greaterOrLessThans.first();
    return result.value;
  }

  public Ranges<T> intersection(Ranges<T> blocks) {
    Ranges<T> result = new Ranges<T>();
    boolean on = false;
    for (Golt<T> endA : blocks.greaterOrLessThans) {
      Golt<T> startA = getPrevious(endA);
      if (on) {
        Golt<T> prev = endA;
        for (Golt<T> goltB : greaterOrLessThans.subSet(startA, endA)) {
          if (startA.compareTo(goltB) < 1) {

            result.greaterOrLessThans.add(startA);
            switch (startA.type) {
            case GREATER:
            case GT_EQUAL:
              // result.
            }
          }
        }
      }
      on = !on;
    }
    return null;
  }
  
  

  public boolean intersects(Ranges<T> ranges) {
    boolean on = false;
 
    for (Golt<T> endA : greaterOrLessThans) {
      Golt<T> startA = getPrevious(endA);
      if (on) {
       
        Golt<T> beforeStart = new Golt<T>(startA.value, Type.LESS_THAN);
        Golt<T> afterEnd = new Golt<T>(endA.value, Type.GREATER);
        System.out.println(startA + " " + endA);
        System.out.println(ranges.greaterOrLessThans.subSet(beforeStart, afterEnd));
        
        if(ranges.greaterOrLessThans.subSet(beforeStart, afterEnd).size()>0) {
          return true;
        }
      }
      on = !on;
    }
    
    on = false;
    for (Golt<T> endA : ranges.greaterOrLessThans) {
      Golt<T> startA = ranges.getPrevious(endA);
      if (on) {
       
        Golt<T> beforeStart = new Golt<T>(startA.value, Type.LESS_THAN);
        Golt<T> afterEnd = new Golt<T>(endA.value, Type.GREATER);
        System.out.println(startA + " " + endA);
        System.out.println(greaterOrLessThans.subSet(beforeStart, afterEnd));
        
        if(greaterOrLessThans.subSet(beforeStart, afterEnd).size()>0) {
          return true;
        }
      }
      on = !on;
    }
    return false;
  }

  public void remove(T start, T end) {
    SortedSet<Golt<T>> result = new TreeSet<Golt<T>>(greaterOrLessThans);

    // remove any values between
    Golt<T> startAndOne = new Golt<T>(start, Type.LESS_THAN);
    Golt<T> endButOne = new Golt<T>(end, Type.GREATER);
    SortedSet<Golt<T>> subset = greaterOrLessThans.subSet(startAndOne,
        endButOne);
    for (Golt<T> removal : subset) {
      result.remove(removal);
    }

    // defaults to exclusive
    Golt<T> startGolt = new Golt<T>(start, Type.LESS_THAN);

    Golt<T> endGolt = new Golt<T>(end, Type.GREATER);
    // is the start with an existing range
    if (contains(startGolt)) {
      result.add(startGolt);
    }
    // is the end within an existing range
    if (contains(endGolt)) {
      result.add(endGolt);
    }

    greaterOrLessThans = result;

  }

  @Override
  public String toString() {
    return greaterOrLessThans.toString();
  }

  
}
