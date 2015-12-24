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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author andy coates
 *         created 06/05/15.
 */
public class TryTest {
    @SuppressWarnings("ThrowableInstanceNeverThrown")
    public final IllegalArgumentException failureException = new IllegalArgumentException("Oops");
    public final Try<Integer> success = Try.success(42);
    public final Try<Integer> failure = Try.failure(failureException);

    @Mock
    public Function<Integer, Long> mapValue;

    @Mock
    public Function<Exception, Integer> mapException;

    @Mock
    public Function<Integer, Try<Long>> flatMapValue;

    @Mock
    public Function<Exception, Try<Integer>> flatMapException;

    @Mock
    private Consumer<Integer> successConsumer;

    @Mock
    private Consumer<? super Exception> failureConsumer;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldKnowIfTheySucceededOrFailed() throws Exception {
        assertThat(success.isSuccess(), is(true));
        assertThat(success.isFailure(), is(false));
        assertThat(failure.isSuccess(), is(false));
        assertThat(failure.isFailure(), is(true));
    }

    @Test
    public void shouldReturnSuccessFromExecIfNotExceptionThrown() throws Exception {
        assertThat(Try.exec(() -> 42), is(Try.success(42)));
        assertThat(Try.exec(source -> source * 2, 21), is(Try.success(42)));
        assertThat(Try.exec((s1, s2) -> (s1 * s2) + 2, 4, 10), is(Try.success(42)));
    }

    @Test
    public void shouldReturnFailureFromExecIfExceptionThrown() throws Exception {
        assertThat(Try.exec(() -> {throw failureException;}), is(Try.failure(failureException)));
        assertThat(Try.exec(source -> {throw failureException;}, 22), is(Try.failure(failureException)));
        assertThat(Try.exec((s1, s2) -> {throw failureException;}, 2, 10), is(Try.failure(failureException)));
    }

    @Test
    public void shouldReturnTryFromTryExecIfNotExceptionThrown() throws Exception {
        assertThat(Try.flatExec(() -> Try.success(42)), is(Try.success(42)));
        assertThat(Try.flatExec(() -> Try.failure(failureException)), is(Try.failure(failureException)));
        assertThat(Try.flatExec(source -> Try.success(source * 2), 21), is(Try.success(42)));
        assertThat(Try.flatExec(source -> Try.failure(failureException), 21), is(Try.failure(failureException)));
        assertThat(Try.flatExec((s1, s2) -> Try.success((s1 * s2) + 2), 4, 10), is(Try.success(42)));
        assertThat(Try.flatExec((s1, s2) -> Try.failure(failureException), 4, 10), is(Try.failure(failureException)));
    }

    @Test
    public void shouldReturnFailureFromTryExecIfExceptionThrown() throws Exception {
        assertThat(Try.flatExec(() -> {throw failureException;}), is(Try.failure(failureException)));
        assertThat(Try.flatExec(source -> {throw failureException;}, 22), is(Try.failure(failureException)));
        assertThat(Try.flatExec((s1, s2) -> {throw failureException;}, 2, 10), is(Try.failure(failureException)));
    }

    @Test
    public void shouldReturnResultOnGetIfSuccess() throws Exception {
        assertThat(success.get(), is(42));
    }

    @Test(expected = Try.FailureException.class)
    public void shouldThrowFailureExceptionOnGetIfFailure() throws Exception {
        failure.get();
    }

    @Test
    public void shouldHaveActualExceptionWithinFailureException() throws Exception {
        try {
            failure.get();
        } catch (final Exception e) {
            assertThat(e.getCause(), is(failureException));
        }
    }

    @Test
    public void shouldReturnResultOnGetOrThrowIfSuccess() throws Exception {
        assertThat(success.getOrThrow(), is(42));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExactExceptionOnGetOrThrowIfFailure() throws Exception {
        failure.getOrThrow();
    }

    @Test
    public void shouldNotUseDefaultOnGetOrElseOnSuccess() throws Exception {
        assertThat(success.getOrElse(90), is(42));
        assertThat(success.getOrElse(() -> 90), is(42));
    }

    @Test
    public void shouldUseDefaultOnGetOrElseOnFailure() throws Exception {
        assertThat(failure.getOrElse(90), is(90));
        assertThat(failure.getOrElse(() -> 90), is(90));
    }

     @Test
    public void shouldReturnExceptionOnGetExceptionIfFailure() throws Exception {
        assertThat(failure.exception(), is(not(Optional.empty())));
        assertThat(failure.exception().get(), is(failureException));
    }

    @Test
    public void shouldReturnEmptyOnGetExceptionIfSuccess() throws Exception {
        assertThat(success.exception(), is(Optional.empty()));
    }

    @Test
    public void shouldReturnValueOnToOptionalForSuccess() throws Exception {
        assertThat(success.toOptional(), is(Optional.of(42)));
    }

    @Test
    public void shouldReturnEmptyOnToOptionalForFailure() throws Exception {
        assertThat(failure.toOptional(), is(Optional.empty()));
    }

    @Test
    public void shouldMapSuccess() throws Exception {
        assertThat(success.map(v -> v * 2L), is(Try.success(84L)));
    }

    @Test
    public void shouldNotMapFailure() throws Exception {
        // When:
        final Try<Long> result = failure.map(mapValue);

        // Then:
        assertThat(result, is(failure));
        verify(mapValue, never()).apply(anyInt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromMap() throws Exception {
        success.map(v -> {throw failureException;});
    }

    @Test
    public void shouldTryMapSuccess() throws Exception {
        assertThat(success.tryMap(v -> v * 2L), is(Try.success(84L)));
    }

    @Test
    public void shouldNotTryMapFailure() throws Exception {
        // When:
        final Try<Long> result = failure.tryMap(mapValue);

        // Then:
        assertThat(result, is(failure));
        verify(mapValue, never()).apply(anyInt());
    }

    @Test
    public void shouldConvertExceptionToFailureOnTryMap() throws Exception {
        assertThat(success.tryMap(v -> {throw failureException;}), is(Try.failure(failureException)));
    }

    @Test
    public void shouldRecoverFailure() throws Exception {
        assertThat(failure.recover(e -> 15), is(Try.success(15)));
    }

    @Test
    public void shouldNotRecoverSuccess() throws Exception {
        // When:
        final Try<Integer> result = success.recover(mapException);

        // Then:
        assertThat(result, is(success));
        verify(mapException, never()).apply(any(Exception.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromTryMap() throws Exception {
        failure.recover(e -> {throw failureException;});
    }

    @Test
    public void shouldTryRecoverFailure() throws Exception {
        assertThat(failure.tryRecover(e -> 15), is(Try.success(15)));
    }

    @Test
    public void shouldNotTryRecoverSuccess() throws Exception {
        // When:
        final Try<Integer> result = success.tryRecover(mapException);

        // Then:
        assertThat(result, is(success));
        verify(mapException, never()).apply(any(Exception.class));
    }

    @Test
    public void shouldConvertExceptionToFailureOnTryRecover() throws Exception {
        assertThat(failure.tryRecover(e -> {throw failureException;}), is(Try.failure(failureException)));
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileMapWithSuperExtendedTypes() throws Exception {
        final Function<Object, Long> f = o -> 42L;
        final Try<Number> map = success.map(f);
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileTryMapWithSuperExtendedTypes() throws Exception {
        final Function<Object, Long> f = o -> 42L;
        final Try<Number> map = success.tryMap(f);
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileRecoverWithSuperExtendedTypes() throws Exception {
        final Try<Number> success = Try.success(42L);
        final Function<Object, Long> f = o -> 24L;
        final Try<Number> map = success.recover(f);
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileTryRecoverWithSuperExtendedTypes() throws Exception {
        final Try<Number> success = Try.success(42L);
        final Function<Object, Long> f = o -> 24L;
        final Try<Number> map = success.tryRecover(f);
    }

    @Test
    public void shouldFlatMapSuccess() throws Exception {
        assertThat(success.flatMap(v -> Try.success(v * 2L)), is(Try.success(84L)));
        assertThat(success.flatMap(v -> Try.failure(failureException)), is(Try.failure(failureException)));
    }

    @Test
    public void shouldNotFlatMapFailure() throws Exception {
        // When:
        final Try<Long> result = failure.flatMap(flatMapValue);

        // Then:
        assertThat(result, is(failure));
        verify(mapValue, never()).apply(anyInt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromFlatMap() throws Exception {
        success.flatMap(v -> {throw failureException;});
    }

    @Test
    public void shouldTryFlatMapSuccess() throws Exception {
        assertThat(success.tryFlatMap(v -> Try.success(v * 2L)), is(Try.success(84L)));
        assertThat(success.tryFlatMap(v -> Try.failure(failureException)), is(Try.failure(failureException)));
    }

    @Test
    public void shouldNotTryFlatMapFailure() throws Exception {
        // When:
        final Try<Long> result = failure.tryFlatMap(flatMapValue);

        // Then:
        assertThat(result, is(failure));
        verify(mapValue, never()).apply(anyInt());
    }

    @Test
    public void shouldConvertExceptionToFailureOnTryFlatMap() throws Exception {
        assertThat(success.tryFlatMap(v -> {throw failureException;}), is(Try.failure(failureException)));
    }

    @Test
    public void shouldFlatRecoverFailure() throws Exception {
        assertThat(failure.flatRecover(e -> Try.success(15)), is(Try.success(15)));
        assertThat(failure.flatRecover(e -> Try.failure(failureException)), is(Try.failure(failureException)));
    }

    @Test
    public void shouldNotFlatRecoverSuccess() throws Exception {
        // When:
        final Try<Integer> result = success.flatRecover(flatMapException);

        // Then:
        assertThat(result, is(success));
        verify(flatMapException, never()).apply(any(Exception.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromTryFlatMap() throws Exception {
        failure.flatRecover(e -> {throw failureException;});
    }

    @Test
    public void shouldTryFlatRecoverFailure() throws Exception {
        assertThat(failure.tryFlatRecover(e -> Try.success(15)), is(Try.success(15)));
        assertThat(failure.tryFlatRecover(e -> Try.failure(failureException)), is(Try.failure(failureException)));
    }

    @Test
    public void shouldNotTryFlatRecoverSuccess() throws Exception {
        // When:
        final Try<Integer> result = success.tryFlatRecover(flatMapException);

        // Then:
        assertThat(result, is(success));
        verify(flatMapException, never()).apply(any(Exception.class));
    }

    @Test
    public void shouldConvertExceptionToFailureOnTryFlatRecover() throws Exception {
        assertThat(failure.tryFlatRecover(e -> {throw failureException;}), is(Try.failure(failureException)));
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileFlatMapWithSuperExtendedTypes() throws Exception {
        final Function<Object, Try<Number>> f = o -> Try.success(42L);
        final Try<Number> map = success.flatMap(f);
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileTryFlatMapWithSuperExtendedTypes() throws Exception {
        final Function<Object, Try<Number>> f = o -> Try.success(42L);
        final Try<Number> map = success.tryFlatMap(f);
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileFlatRecoverWithSuperExtendedTypes() throws Exception {
        final Try<Number> success = Try.success(42L);
        final Function<Object, Try<Number>> f = o -> Try.success(42L);
        final Try<Number> map = success.flatRecover(f);
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileTryFlatRecoverWithSuperExtendedTypes() throws Exception {
        final Try<Number> success = Try.success(42L);
        final Function<Object, Try<Number>> f = o -> Try.success(42L);
        final Try<Number> map = success.tryFlatRecover(f);
    }

    @Test
    public void shouldFoldSuccess() throws Exception {
        assertThat(success.fold(i -> i * 2L, e -> 33L), is(84L));
    }

    @Test
    public void shouldFoldFailure() throws Exception {
        assertThat(failure.fold(i -> i * 2L, e -> 33L), is(33L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromFoldSuccess() throws Exception {
        success.fold(i -> {throw failureException;}, e -> 33L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromFoldFailure() throws Exception {
        failure.fold(i -> 23, e -> {throw failureException;});
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileFoldSuperExtendedTypes() throws Exception {
        final Try<Number> theTry = Try.success(24);

        final Function<Object, Long> onSuccess = o -> 24L;
        final Function<Object, Long> onFailure = e -> 24L;
        final Number result = theTry.fold(onSuccess, onFailure);
    }

    @Test
    public void shouldConsumeSuccessOnForEach() throws Exception {
        // When:
        success.forEach(successConsumer, failureConsumer);

        // Then:
        verify(successConsumer).accept(42);
        verify(failureConsumer, never()).accept(any(Exception.class));
    }

    @Test
    public void shouldConsumeFailureOnForEach() throws Exception {
        // When:
        failure.forEach(successConsumer, failureConsumer);

        // Then:
        verify(failureConsumer).accept(failureException);
        verify(successConsumer, never()).accept(anyInt());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromForEachOnSuccess() throws Exception {
        success.forEach(v -> {throw failureException;}, e -> {});
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldPropagateExceptionFromForEachOnFailure() throws Exception {
        failure.forEach(v -> {}, e -> {throw failureException;});
    }

    @SuppressWarnings("unused") // Not a test - just needs to compile
    public void shouldCompileForEachSuperExtendedTypes() throws Exception {
        final Try<Number> theTry = Try.success(24);

        final Consumer<Object> onSuccess = o -> {};
        final Consumer<Object> onFailure = e -> {};
        theTry.forEach(onSuccess, onFailure);
    }
}