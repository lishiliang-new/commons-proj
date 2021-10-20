package com.lishiliang.core.utils;

import java.util.Objects;
import java.util.function.BiConsumer;

/***
* @description:
* @param
* @return:
* @author: lisl
* @date:
*/
public class Iterables {

    /**
     * @desc 使用JAVA8的迭代方法，并获取下标
     * @param elements 需要迭代的对象
     * @param action 迭代器（第一个参数是下标，第二个参数是下标对应的迭代对象）
     */
    public static <E> void forEach(
            Iterable<? extends E> elements, BiConsumer<Integer, ? super E> action) {
        Objects.requireNonNull(elements);
        Objects.requireNonNull(action);

        int index = 0;
        for (E element : elements) {
            action.accept(index++, element);
        }
    }

}
