package com.lishiliang.core.comparable;


import java.io.Serializable;
import java.util.Comparator;
import java.util.function.Function;

/**
 * @author lisl
 * @version 1.0
 * @asc : 复杂对象 null排序处理器
 */
public class ObjectsNullComparable<T, R extends Comparable> implements Comparator<T>, Serializable {

    private static final long serialVersionUID = -58207725754835049L;

    private Function<T, R> action;
    //是否升序
    private boolean asc = true;
    //null是否排高位
    private boolean nullsAreHigh = true;

    @Override
    public int compare(T o1, T o2) {

        if (o1 == o2)
            return 0;
        if (o1 == null)
            return nullsAreHigh ? -1 : 1;
        if (o2 == null)
            return nullsAreHigh ? 1 : -1;
        if (action == null) {
            if (o1.getClass().isAssignableFrom(Comparable.class)) {
                Comparable c1 = (Comparable)o1;
                Comparable c2 = (Comparable)o2;
                return asc ? c1.compareTo(c2) : c2.compareTo(c1);
            }
            return 0;
        }

        R pre = action.apply(o1);
        R next = action.apply(o2);

        if (pre == next)
            return 0;
        if (pre == null)
            return nullsAreHigh ? -1 : 1;
        if (next == null)
            return nullsAreHigh ? 1 : -1;

        return asc ? pre.compareTo(next) : next.compareTo(pre);
    }

    /**
     * use org.apache.commons.collections4.comparators.NullComparator
     */
    @Deprecated
    public ObjectsNullComparable() {
    }

    @Deprecated
    public ObjectsNullComparable(boolean asc) {
        this.asc = asc;
    }

    @Deprecated
    public ObjectsNullComparable(boolean asc, boolean nullsAreHigh) {
        this.asc = asc;
        this.nullsAreHigh = nullsAreHigh;
    }


    public ObjectsNullComparable(Function<T, R> action) {
        this.action = action;
    }

    public ObjectsNullComparable(Function<T, R> action, boolean asc) {
        this.action = action;
        this.asc = asc;
    }

    public ObjectsNullComparable(Function<T, R> action, boolean asc, boolean nullsAreHigh) {
        this.action = action;
        this.asc = asc;
        this.nullsAreHigh = nullsAreHigh;
    }



}
