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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author andy coates
 *         created 24/12/15.
 */
public class EitherTest {

    public final Either<Integer, String> left = Either.left(23);
    public final Either<Integer, String> right = Either.right("23");

    @Mock
    private Consumer<? super Integer> leftConsumer;
    @Mock
    private Consumer<? super String> rightConsumer;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldKnowWhichSideItIs() throws Exception {
        assertThat(left.isLeft(), is(true));
        assertThat(left.isRight(), is(false));
        assertThat(right.isLeft(), is(false));
        assertThat(right.isRight(), is(true));
    }

    @Test
    public void shouldGetLeftValueFromLeft() throws Exception {
        assertThat(left.left(), is(23));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowOnGetLeftValueFromRight() throws Exception {
        right.left();
    }

    @Test
    public void shouldGetRightValueFromRight() throws Exception {
        assertThat(right.right(), is("23"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowOnGetRightValueFromLeft() throws Exception {
        left.right();
    }

    @Test
    public void shouldGetValue() throws Exception {
        assertThat(left.get(), is(23));
        assertThat(right.get(), is("23"));
    }

    @Test
    public void shouldFoldLeft() throws Exception {
        assertThat(left.fold(i -> i * 2L, s -> (long)s.length()), is(46L));
    }


    @Test
    public void shouldFoldRight() throws Exception {
        assertThat(right.fold(i -> i * 2L, s -> (long)s.length()), is(2L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionsFromFoldLeft() throws Exception {
        left.fold(i -> {throw new IllegalArgumentException("boo");}, s -> (long)s.length());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionsFromFoldRight() throws Exception {
        right.fold(i -> i * 2L, s -> {throw new IllegalArgumentException("boo");});
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileFoldWithSuperExtendedTypes() throws Exception {
        final Either<Number, Number> either = Either.left(24L);

        final Function<Object, Long> onLeft = o -> 2L;
        final Function<Object, Long> onRight = o -> 2L;
        final Number result = either.fold(onLeft, onRight);
    }

    @Test
    public void shouldConsumeLeftOnForEachOfLeft() throws Exception {
        // When:
        left.forEach(leftConsumer, rightConsumer);

        // Then:
        verify(leftConsumer).accept(23);
        verify(rightConsumer, never()).accept(anyString());
    }

    @Test
    public void shouldConsumeRightOnForEachOnRight() throws Exception {
        // When:
        right.forEach(leftConsumer, rightConsumer);

        // Then:
        verify(rightConsumer).accept("23");
        verify(leftConsumer, never()).accept(anyInt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionOnForEachOfLeft() throws Exception {
        left.forEach(v -> {throw new IllegalArgumentException("boo");}, rightConsumer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionOnForEachOfRight() throws Exception {
        right.forEach(leftConsumer, v -> {throw new IllegalArgumentException("boo");});
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileForEachWithSuperTypes() throws Exception {
        final Either<Number, Number> either = Either.left(24L);

        final Consumer<Object> onLeft = o -> {};
        final Consumer<Object> onRight = o -> {};
        either.forEach(onLeft, onRight);
    }
}