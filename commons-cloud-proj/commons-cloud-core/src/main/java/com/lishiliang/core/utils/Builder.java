package com.lishiliang.core.utils;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class Builder<T> {

    private final T instance;

    public Builder(T instance) {
        Objects.requireNonNull(instance);
        this.instance = instance;
    }

    public static <T> Builder<T> of(T instance) {

        return new Builder<T>(instance);
    }

    public static <T> Builder<T> of(Supplier<T> instantiator) {
        return new Builder<>(instantiator.get());
    }

    public static <T> Builder<T> ofDefault(T instance, T defaultInstance) {
        if (instance == null) {
            instance = defaultInstance;
        }
        return new Builder<T>(instance);
    }

    public static <T> Builder<T> ofDefault(T instance, Supplier<T> defaultInstantiator) {
        if (instance == null) {
            Objects.requireNonNull(defaultInstantiator);
            instance = defaultInstantiator.get();
        }
        return new Builder<T>(instance);
    }

    public static <P, T> Builder<T> of(Function<P, T> instantiator, P param) {

        return new Builder<T>(instantiator.apply(param));
    }


    public <U> Builder<T> with(BiConsumer<T, U> biConsumer, U parameter) {
        biConsumer.accept(this.instance, parameter);
        return this;
    }


    /**
     * 多参数方法  例 ：with(instance -> instance.method(param1, param2,...))
     * @param consumer
     * @return
     */
    public Builder<T> with(Consumer<T> consumer) {
        consumer.accept(this.instance);
        return this;
    }

    /**
     * 满足条件内部完成某个操作 如 :whenDo(true, instance->instance.method(param1, param2,...))
     * @param condition 条件
     * @param action 操作
     * @return
     */
    public Builder<T> whenDo(boolean condition, Consumer<T> action) {
        action.accept(this.instance);
        return this;
    }

    /**
     * 满足条件外部完成某个操作 如 :whenDo(true, (String data)->data.method(param1, param2,...))
     * @param condition 条件
     * @param action 操作
     * @return
     */
    public <P> Builder<T>  whenDo(boolean condition, Consumer<P> action, P data) {
        action.accept(data);
        return this;
    }

    //TODO
    public <P1, P2, R> Builder<T> with(BiFunctionExpand1<T, P1, P2, R> biFunctionEx, P1 p1, P2 p2, R r) {
        biFunctionEx.apply(this.instance, p1, p2);
        return this;
    }
    public <P1, P2> Builder<T> with(BiFunctionExpand2<T, P1, P2> biFunctionEx, P1 p1, P2 p2) {
        biFunctionEx.apply(this.instance, p1, p2);
        return this;
    }



    public T build() {
        return this.instance;
    }

    
    @FunctionalInterface
    public interface BiFunctionExpand1<T, P1, P2, R> {

        R apply(T t, P1 p1, P2 p2);
    }

    @FunctionalInterface
    public interface BiFunctionExpand2<T, P1, P2> {

        void apply(T t, P1 p1, P2 p2);
    }
    
}