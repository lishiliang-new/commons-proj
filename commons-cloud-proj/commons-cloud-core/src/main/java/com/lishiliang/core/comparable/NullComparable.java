package com.lishiliang.core.comparable;

import java.util.Comparator;
import java.util.function.Function;

/**
 * @author lisl
 * @version 1.0
 * @desc : null排序器 默认降序并且null排前面
 */
public class NullComparable<T extends Comparable, R extends Comparable> implements Comparator<T> {

    private Function<T, R> action;
    //是否降序
    private boolean desc = true;
    //是否null排第一 false则排最后
    private boolean nullTofirst = true;

    @Override
    public int compare(T o1, T o2) {
        if (o1 == null) {
            return nullTofirst ? -1 : 1;
        }
        if (o2 == null) {
            return nullTofirst ? 1 : -1;
        }
        if (action != null) {
            return desc ? action.apply(o1).compareTo(action.apply(o2)) : action.apply(o2).compareTo(action.apply(o1));
        }
        return desc ? o1.compareTo(o2) : o2.compareTo(o1);
    }

    public NullComparable() {
    }

    public NullComparable(boolean desc) {
        this.desc = desc;
    }

    public NullComparable(boolean desc, boolean nullTofirst) {
        this.desc = desc;
        this.nullTofirst = nullTofirst;
    }

    public NullComparable(Function<T, R> action) {
        this.action = action;
    }

    public NullComparable(Function<T, R> action, boolean desc) {
        this.action = action;
        this.desc = desc;
    }

    public NullComparable(Function<T, R> action, boolean desc, boolean nullTofirst) {
        this.action = action;
        this.desc = desc;
        this.nullTofirst = nullTofirst;
    }


   
}
