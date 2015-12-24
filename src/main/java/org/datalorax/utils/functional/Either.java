/*
 * Copyright (c) 2015 Andrew Coates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.datalorax.utils.functional;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a value of one of two possible types (a disjoint union.)
 * <p>
 * It was inspired by Scala's {@code Either} type.
 * <p>
 * It is immutable.
 * <p>
 * Instances of {@link Either} are created via either {@link #left(Object)} or {@link #right(Object)}
 *
 * @author datalorax
 *         created 05/05/2015.
 */
public interface Either<L, R> {
    /**
     * Build an left-sided instance of {@link Either}
     *
     * @param value the left-side value
     * @param <L>   The left-side type
     * @param <R>   The right-side type
     * @return An instance of {@link Either} that represents a left-sided value.
     */
    static <L, R> Either<L, R> left(final L value) {
        return new Left<>(value);
    }

    /**
     * Build an right-sided instance of {@link Either}
     *
     * @param value the right-side value
     * @param <L>   The left-side type
     * @param <R>   The right-side type
     * @return An instance of {@link Either} that represents a right-sided value.
     */
    static <L, R> Either<L, R> right(final R value) {
        return new Right<>(value);
    }

    /**
     * @return true if the instance holds a value of the left hand side type {@code T}
     */
    boolean isLeft();

    /**
     * @return true if the instance holds a value of the right hand side type {@code R}
     */
    boolean isRight();

    /**
     * @return the left value
     * @throws UnsupportedOperationException if the instance holds a right hand side type
     */
    L left();

    /**
     * @return the right value
     * @throws UnsupportedOperationException if the instance holds a left hand side type
     */
    R right();

    /**
     * @return the value contained within the instance, regardless of which type it is.
     */
    Object get();

    /**
     * Apply either the supplied {@code onLeft} or {@code onRight} functions to transform the instance,
     * depending on whether the instance is a left or right handed instance.
     * <p>
     * Any exception thrown by the executed function is left to propagate out.
     *
     * @param onLeft  the transformation function to a left handed instance.
     * @param onRight the transformation function to call on a right handed instance.
     * @param <T>     the resulting type of the transformation
     * @return the result of the transformation.
     * @throws RuntimeException if the executed function throws.
     */
    <T> T fold(final Function<? super L, ? extends T> onLeft, final Function<? super R, ? extends T> onRight);

    /**
     * Call either the supplied {@code onLeft} or {@code onRight} {@link Consumer},
     * depending on whether the instance is a left or right handed instance.
     *
     * Any exception thrown by the executed function is left to propagate out.
     *
     * @param onLeft the consumer to call on a left handed instance.
     * @param onRight the consumer to call on a right handed instance.
     * @throws RuntimeException if the executed consumer throws.
     */
    void forEach(final Consumer<? super L> onLeft, final Consumer<? super R> onRight);

    class Left<L, R> implements Either<L, R> {
        private final L value;

        public Left(final L value) {
            this.value = value;
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

        @Override
        public L left() {
            return value;
        }

        @Override
        public R right() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public <T> T fold(final Function<? super L, ? extends T> onLeft, final Function<? super R, ? extends T> onRight) {
            return onLeft.apply(value);
        }

        @Override
        public void forEach(final Consumer<? super L> onLeft, final Consumer<? super R> onRight) {
            onLeft.accept(value);
        }
    }

    class Right<L, R> implements Either<L, R> {
        private final R value;

        public Right(final R value) {
            this.value = value;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

        @Override
        public L left() {
            throw new UnsupportedOperationException();
        }

        @Override
        public R right() {
            return value;
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public <T> T fold(final Function<? super L, ? extends T> onLeft, final Function<? super R, ? extends T> onRight) {
            return onRight.apply(value);
        }

        @Override
        public void forEach(final Consumer<? super L> onLeft, final Consumer<? super R> onRight) {
            onRight.accept(value);
        }
    }
}
