
package com.sos.inventory.model.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class JobReturnCodeHelper {
    
    public enum TYPE {
        SUCCESS, WARNING, FAILURE;
    }

    @JsonIgnore
    private Map<TYPE, SortedSet<Integer>> singles = new HashMap<>();
    @JsonIgnore
    private Map<TYPE, List<SortedSet<Integer>>> intervals = new HashMap<>();
    @JsonIgnore
    private final static Predicate<String> pred = Pattern.compile("^-?[0-9]+(\\.\\.-?[0-9]+)?(, *-?[0-9]+(\\.\\.-?[0-9]+)?)*$").asPredicate();

    public JobReturnCodeHelper() {
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    protected String getCodes(Object codes, TYPE t) {
        if (codes == null || codes instanceof String) {
            return parseCodes((String) codes, t);
        } else if (codes instanceof List<?>) {
            this.singles.put(t, ((List<Object>) codes).stream().filter(Objects::nonNull).filter(i -> i instanceof Integer).map(i -> (Integer) i)
                    .collect(Collectors.toCollection(TreeSet::new)));
            return this.singles.get(t).stream().map(Objects::toString).collect(Collectors.joining(","));
        } else {
            return (String) null;
        }
    }

    @JsonIgnore
    protected String getCodes(List<Integer> codes, TYPE t) {
        if (codes == null) {
            return null;
        } else {
            this.singles.put(t, new TreeSet<>(codes));
            return codes.stream().filter(Objects::nonNull).map(Objects::toString).collect(Collectors.joining(","));
        }
    }
    
    @JsonIgnore
    protected String getCodes(String codes, TYPE t) {
        return parseCodes(codes, t);
    }

    private String parseCodes(String codes, TYPE t) {
        if (codes != null && !codes.isEmpty() && pred.test(codes)) {
            SortedSet<Integer> single = singles.getOrDefault(t, new TreeSet<>());
            List<SortedSet<Integer>> ival = intervals.getOrDefault(t, new ArrayList<>());
            codes = codes.replaceAll("\\s", "");
            Arrays.asList(codes.split(",")).stream().forEach(s -> {
                if (s.contains("..")) {
                    SortedSet<Integer> sSet = Arrays.asList(s.split("\\.\\.")).stream().map(i -> Integer.valueOf(i)).collect(Collectors.toCollection(
                            TreeSet::new));
                    if (sSet.size() == 1) {
                        single.add(sSet.iterator().next());
                    } else if (sSet.size() == 2) {
                        ival.add(sSet);
                    }
                } else {
                    single.add(Integer.valueOf(s));
                }
            });
            intervals.put(t, createUnionsOfIntervals(ival));
            single.removeIf(s -> isInIntervals(s, intervals.get(t)));
            singles.put(t, single);
        }
        return codes;
    }

    @JsonIgnore
    protected boolean isInReturnCodes(Integer i, TYPE t) {
        if (i == null) {
            return false;
        }
        if (singles.getOrDefault(t, Collections.emptySortedSet()).contains(i) || isInIntervals(i, intervals.getOrDefault(t, Collections
                .emptyList()))) {
            return true;
        }
        return false;
    }
    
    @JsonIgnore
    private boolean isInReturnCodes(Integer i, SortedSet<Integer> single, List<SortedSet<Integer>> interval) {
        if (single.contains(i) || isInIntervals(i, interval)) {
            return true;
        }
        return false;
    }

    @JsonIgnore
    protected String normalized(TYPE t) {
        SortedSet<Integer> single = singles.getOrDefault(t, Collections.emptySortedSet());
        List<SortedSet<Integer>> interval = intervals.getOrDefault(t, Collections.emptyList());
        if (single.isEmpty() && interval.isEmpty()) {
            return null;
        }
        return Stream.concat(single.stream().map(i -> i.toString()), interval.stream().map(i -> i.first() + ".." + i.last())).sorted().collect(
                Collectors.joining(","));
    }

    private boolean isInIntervals(Integer i, List<SortedSet<Integer>> ival) {
        for (SortedSet<Integer> interval : ival) {
            if (interval.first() <= i && i <= interval.last()) {
                return true;
            }
        }
        return false;
    }

    private List<SortedSet<Integer>> createUnionsOfIntervals(List<SortedSet<Integer>> ival) {
        Collections.sort(ival, Comparator.comparingInt(SortedSet::first));
        List<SortedSet<Integer>> union = new ArrayList<>();
        int left = Integer.MAX_VALUE;
        int right = Integer.MIN_VALUE;
        if (!ival.isEmpty()) {
            for (SortedSet<Integer> interval : ival) {
                int currentRight = interval.last();
                int currentLeft = interval.first();
                if (currentLeft <= right && currentRight > right) {
                    right = currentRight;
                }
                if (currentLeft < left && currentRight >= right) {
                    left = currentLeft;
                    right = currentRight;
                }
                if (currentLeft > right) {
                    union.add(constructSortedSet(left, right));
                    left = currentLeft;
                    right = currentRight;
                }
            }
            union.add(constructSortedSet(left, right));
        }
        return union;
    }
    
    private boolean isOverlapping(Integer ivalFirst, Integer ivalLast, SortedSet<Integer> otherIval) {
        // overlap happens ONLY when this's end is on the right of other's start
        // AND this's start is on the left of other's end.
        return ivalLast >= otherIval.first() && ivalFirst <= otherIval.last();
    }
    
    protected String deleteIntersectionByOtherIntervals(TYPE t, Object other, TYPE tOther) {
        getCodes(other, tOther);
        
        SortedSet<Integer> otherSingle = singles.getOrDefault(tOther, Collections.emptySortedSet());
        List<SortedSet<Integer>> otherIvals = intervals.getOrDefault(tOther, Collections.emptyList());
        
        SortedSet<Integer> single = singles.getOrDefault(t, Collections.emptySortedSet());
        single.removeIf(s -> isInReturnCodes(s, otherSingle, otherIvals));
        List<SortedSet<Integer>> ivals = intervals.getOrDefault(t, Collections.emptyList());
        
        otherSingle.forEach(s -> otherIvals.add(constructSortedSet(s)));
        
        List<SortedSet<Integer>> intersections = new ArrayList<>();
        for (SortedSet<Integer> ival : ivals) {
            intersections.addAll(deleteIntersectionByOtherIntervals(ival, otherIvals));
        }
        
        ivals.clear();
        for (SortedSet<Integer> intersection : intersections) {
            if (intersection.size() == 1) {
                single.add(intersection.iterator().next());
            } else if (intersection.size() == 2) {
                ivals.add(intersection);
            }
        }
        
        singles.put(t, single);
        intervals.put(t, ivals);
        return normalized(t);
    }
    
    private List<SortedSet<Integer>> deleteIntersectionByOtherIntervals(SortedSet<Integer> ival, List<SortedSet<Integer>> otherIvals) {
        if (otherIvals == null || otherIvals.isEmpty()) {
            return Collections.singletonList(ival);
        }
        
        List<SortedSet<Integer>> intersections = new ArrayList<>();
        List<SortedSet<Integer>> oIvals = otherIvals.stream().sorted(Comparator.comparingInt(SortedSet::first)).collect(Collectors.toList());
        
        Integer curFirst = ival.first();
        Integer curLast = ival.last();
        boolean recursive = false;
        for (SortedSet<Integer> otherIval : oIvals) {
            if (isOverlapping(curFirst, curLast, otherIval)) {
                // case [  (  ]  ) : round brackets = other  
                if (curLast >= otherIval.first() && curLast <= otherIval.last()) {
                    curLast = otherIval.first() - 1;
                    if (curLast < curFirst) {
                        break; 
                    }
                }
                // case (  [  )  ] : round brackets = other  
                if (curFirst >= otherIval.first() && curFirst <= otherIval.last()) {
                    curFirst = otherIval.last() + 1;
                    if (curLast < curFirst) {
                        break; 
                    }
                }
            }
        }
        for (SortedSet<Integer> otherIval : oIvals) {
            if (isOverlapping(curFirst, curLast, otherIval)) {
                // case [  (  )  ] : round brackets = other  
                if (curFirst <= otherIval.first() && curLast >= otherIval.last()) {
                    // return 2 sortedSets
                    intersections.addAll(deleteIntersectionByOtherIntervals(constructSortedSet(curFirst, otherIval.first() - 1), otherIvals));
                    intersections.addAll(deleteIntersectionByOtherIntervals(constructSortedSet(otherIval.last() + 1, curLast), otherIvals));
                    recursive = true;
                    break;
                }
            }
        }
        if (!recursive) {
            intersections.add(constructSortedSet(curFirst, curLast));
        }
        
        return intersections;
    }
    
    private SortedSet<Integer> constructSortedSet(Integer ...integers) {
        TreeSet<Integer> ts = new TreeSet<>();
        for (Integer integer : integers) {
            ts.add(integer); 
        }
        return ts;
    }

}
