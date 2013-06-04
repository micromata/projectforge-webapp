package org.projectforge.plugins.teamcal.externalsubscription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

/**
 * Holder for generic usage of the RangeMap, but with the feature of multiple elements per key.
 * 
 * @author Johannes Unterstein (j.unterstein@micromata.de)
 */
public class MultipleEntryRangeMapHolder<K extends Comparable, V> implements Serializable
{
  private RangeMap<K, List<V>> rangeMap;

  public MultipleEntryRangeMapHolder()
  {
    rangeMap = TreeRangeMap.create();
  }

  public void clear()
  {
    rangeMap.clear();
  }

  public void put(Range<K> range, V value)
  {
    // check precondition, no null keys!
    if (range == null) {
      return;
    }
    final Map<Range<K>, List<V>> rangeListMap = rangeMap.asMapOfRanges();
    final List<V> savedList = rangeListMap.get(range);
    if (savedList == null) {
      // range was not stored in RangeMap yet, so store it now
      List<V> listToAdd = new ArrayList<V>();
      listToAdd.add(value);
      rangeMap.put(range, listToAdd);
    } else {
      savedList.add(value);
    }
  }

  public List<V> getClosedResultList(K start, K end)
  {
    return getResultList(Range.closed(start, end));
  }

  public List<V> getResultList(Range<K> range)
  {
    // pick sublists
    final RangeMap<K, List<V>> listRangeMap = rangeMap.subRangeMap(range);
    // then gather
    List<V> result = new ArrayList<V>();
    for (List<V> subResult : rangeMap.asMapOfRanges().values()) {
      result.addAll(subResult);
    }
    // and return
    return result;
  }

}
