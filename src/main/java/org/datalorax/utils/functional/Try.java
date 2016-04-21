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

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The Try type represents a computation that may either result in an exception, or return a successfully computed value.
 * It's similar to, but semantically different from the {@link Either} type.
 * <p>
 * It was inspired by Scala's Try type.
 * <p>
 * It is immutable.
 * <p>
 * Instances of Try&lt;T&gt; are created via either {@link #success(Object)} or {@link #failure(Exception)}.
 * <p>
 * {@code Try} can be used to allow methods to return an exception or a value, rather than trowing an exception.
 * This is useful in many places, but especially so with in introduction of streams in Java8, which don't support
 * checked exceptions from within their operations.
 * <p>
 * Many of the methods have both a standard and a 'try' variant e.g. {@link #map} and {@link #tryMap}. The non-try variant
 * will propagate any exceptions thrown by the functions passed in as parameters to the method, while the try variant
 * will catch any thrown exceptions and convert them to a {@link Try.Failure}.
 * <p>
 * The {@link Try.Failure} wraps an {@link Exception}, rather than the {@link Throwable} that the Scala {@code Try} type
 * wraps. This is because I believe {@link Error}s should not generally be caught.
 * <p>
 * The type allows you to more explicitly handle exceptions and chain together operations, where some may fail / throw:
 * <p>
 * <pre>
 * {@code
 *   long someFunctionThatMightThrow() {... whatever ...}
 *
 *   String foo() {
 *      // Call some function that may throw an exception:
 *      final Try<Long> result = Try.exec(someFunctionThatMightThrows(blah));
 *
 *      // Perform a transformation on the result, if it didn't throw:
 *      final Try<String> text  = result.map(l -> "Got result: " + l);
 *
 *      // Return the value or, if it failed, do something with the exception - in this case just convert to text message:
 *      return text.recover(e -> "Failed to get thing. Cause: " + e.getMessage());
 *   }
 *  </pre>
 * <p>
 * The above can be more succinctly written as:
 * <p>
 * <pre>
 * {@code
 *   String foo() {
 *      return Try.exec(someFunctionThatMightThrows(blah))
 *                .fold(l -> "Got result: " + l,
 *                      e -> "Failed to get thing. Cause: " + e.getMessage());
 *   }
 *  </pre>
 *
 * @author datalorax
 *         created 05/05/15.
 */
public interface Try<T> {

    /**
     * Create a successful result holding the value {@code value}
     *
     * @param value the actual value of the result.
     * @param <T>   the type, or super type, of the value contained within the instance.
     * @return a {@link Try} instance representing success.
     */
    static <T> Try<T> success(final T value) {
        return new Success<>(value);
    }

    /**
     * Create a failure result holding the cause for failure {@code e}
     *
     * @param e   the cause for failure.
     * @param <T> the type of the value had the operation been successful.
     * @return a {@link Try} instance representing failure.
     */
    static <T> Try<T> failure(final Exception e) {
        return new Failure<>(e);
    }

    /**
     * Execute the supplied function, wrapping the result in a successful {@link Try} instance, and any exception
     * thrown in a failure {@link Try} instance.
     *
     * @param f   the function to call
     * @param <T> the type of the resulting {@link Try} instance
     * @return If the function {@code f} returned, then a successful {@link Try} instance containing the returned value.
     * If the function {@code f} threw an exception, then a failure {@link Try} instance containing the thrown exception.
     */
    static <T> Try<T> exec(final Supplier<? extends T> f) {
        try {
            return Try.success(f.get());
        } catch (final Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Execute the supplied function, wrapping the result in a successful {@link Try} instance, and any exception
     * thrown in a failure {@link Try} instance.
     *
     * @param f   the function to call
     * @param arg the arg to the function {code f}
     * @param <T> the type of the resulting {@link Try} instance
     * @return If the function {@code f} returned, then a successful {@link Try} instance containing the returned value.
     * If the function {@code f} threw an exception, then a failure {@link Try} instance containing the thrown exception.
     */
    static <S, T> Try<T> exec(final Function<? super S, ? extends T> f, final S arg) {
        try {
            return Try.success(f.apply(arg));
        } catch (final Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Execute the supplied function, wrapping the result in a successful {@link Try} instance, and any exception
     * thrown in a failure {@link Try} instance.
     *
     * @param f    the function to call
     * @param arg1 the first arg to the function {code f}
     * @param arg2 the second arg to the function {code f}
     * @param <T>  the type of the resulting {@link Try} instance
     * @return If the function {@code f} returned, then a successful {@link Try} instance containing the returned value.
     * If the function {@code f} threw an exception, then a failure {@link Try} instance containing the thrown exception.
     */
    static <S1, S2, T> Try<T> exec(final BiFunction<? super S1, ? super S2, ? extends T> f, final S1 arg1, final S2 arg2) {
        try {
            return Try.success(f.apply(arg1, arg2));
        } catch (final Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Execute the supplied function returning the value, and any exception thrown in a failure {@link Try} instance.
     *
     * @param f   the function to call
     * @param <T> the type of the resulting {@link Try} instance
     * @return If the function {@code f} returned, then the returned {@link Try} instance.
     * If the function {@code f} threw an exception, then a failure {@link Try} instance containing the thrown exception.
     */
    static <T> Try<T> flatExec(final Supplier<Try<T>> f) {
        try {
            return f.get();
        } catch (final Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Execute the supplied function returning the value, and any exception thrown in a failure {@link Try} instance.
     *
     * @param f   the function to call
     * @param arg the arg to the function {code f}
     * @param <T> the type of the resulting {@link Try} instance
     * @return If the function {@code f} returned, then the returned {@link Try} instance.
     * If the function {@code f} threw an exception, then a failure {@link Try} instance containing the thrown exception.
     */
    static <S, T> Try<T> flatExec(final Function<? super S, Try<T>> f, final S arg) {
        try {
            return f.apply(arg);
        } catch (final Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * Execute the supplied function returning the value, and any exception thrown in a failure {@link Try} instance.
     *
     * @param f    the function to call
     * @param arg1 the first arg to the function {code f}
     * @param arg2 the second arg to the function {code f}
     * @param <T>  the type of the resulting {@link Try} instance
     * @return If the function {@code f} returned, then the returned {@link Try} instance.
     * If the function {@code f} threw an exception, then a failure {@link Try} instance containing the thrown exception.
     */
    static <S1, S2, T> Try<T> flatExec(final BiFunction<? super S1, ? super S2, Try<T>> f, final S1 arg1, final S2 arg2) {
        try {
            return f.apply(arg1, arg2);
        } catch (final Exception e) {
            return Try.failure(e);
        }
    }

    /**
     * @return true if the operation was successful, i.e. it has a value, false otherwise.
     */
    boolean isSuccess();

    /**
     * @return true if the operation was unsuccessful, i.e. it has an exception, false otherwise.
     */
    default boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Get the value associated with the instance if it is a success,
     * or else throw the associated failure exception, wrapped in a {@link FailureException}.
     * <p>
     * This method is similar to {@link #getOrThrow()}, except that it is guaranteed that only no checked exception will
     * be thrown, making it useful when detail with stream operations.
     *
     * @return the value on success
     * @throws FailureException on failure, that wraps the exception within the instance.
     */
    T get();

    /**
     * Get the value associated with the instance if it is a success,
     * or else return the supplied default {@code defaultValue}.
     * <p>
     *
     * @return the instance's value if the instance is a success, or the supplied {@code defaultValue} if its a failure.
     */
    T getOrElse(final T defaultValue);

    /**
     * Get the value associated with the instance if it is a success,
     * or else return the value returned by the supplied functions {@code f}.
     * <p>
     *
     * @return the instance's value if the instance is a success, or return the value returned by the supplied function
     * {@code f}, if its a failure.
     * @throws RuntimeException if the supplied function {@code f} throws.
     */
    T getOrElse(final Supplier<? extends T> f);

    /**
     * Get the value associated with the result, or else throw the associated failure exception.
     * <p>
     * This method is similar to {@link #get()}, except that this method throws the actual exception, if present, that the
     * instance contains.
     *
     * @return the value on success
     * @throws Exception on failure.
     */
    T getOrThrow() throws Exception;

    /**
     * Get the exception associated with the failure.
     *
     * @return An {@link Optional} containing the exception where the instance represents a failure, or else {@link Optional#empty()}
     */
    Optional<Exception> exception();

    /**
     * Convet the instance to an {@link Optional}.
     *
     * @return If the instance is a success then an {@link Optional} containing the instances value, else {@link Optional#empty()}
     */
    Optional<T> toOptional();

    /**
     * Applies the given function {@code f} to transform any successful result. Failures remain unchanged.
     * <p>
     * Any exception thrown by function {@code f} is left to propagate out. If you would prefer the exception to be
     * converted into a {@link Try.Failure}, then see {@link #tryMap}
     * <p>
     * The symmetrical method for failures is {@link #recover}
     *
     * @param f   the transformation function to call to transform the successful value to type {@code U}.
     * @param <U> the resulting type of the transformation
     * @return If the result is successful, then a successful result containing the result of the transformation returned
     * by the function {@code f}, otherwise the failure, unchanged.
     * @throws RuntimeException if function {@code f} throws.
     */
    <U> Try<U> map(final Function<? super T, ? extends U> f);

    /**
     * Applies the given function {@code f} to transform any successful result. Failures remain unchanged.
     * <p>
     * Any exception thrown by function {@code f} is converted to a {@link Try.Failure}. If you would prefer any
     * exception to propagate out, then see {@link #map}
     * <p>
     * The symmetrical method for failures is {@link #tryRecover}
     *
     * @param f   the transformation function to call to transform the successful value to type {@code U}.
     * @param <U> the resulting type of the transformation
     * @return If the result is successful, then a successful result containing the result of the transformation returned
     * by the function {@code f} or, if the function {@code f} threw an exception, then a failure containing the thrown
     * exception, otherwise the original failure, unchanged.
     */
    <U> Try<U> tryMap(final Function<? super T, ? extends U> f);

    /**
     * Applies the given function {@code f} to transform any failure. Successes remain unchanged.
     * <p>
     * Any exception thrown by function {@code f} is left to propagate out. If you would prefer the exception to be
     * converted into a {@link Try.Failure}, then see {@link #tryRecover}
     * <p>
     * The symmetrical method for successes is {@link #map}
     *
     * @param f the function to transform the exception into a value of type {@code T}.
     * @return If the instance is a failure, a successful result containing the result of the transformation returned by
     * the function {@code f}, otherwise the already successful instance, unchanged.
     * @throws RuntimeException if function {@code f} throws.
     */
    Try<T> recover(final Function<? super Exception, ? extends T> f);

    /**
     * Applies the given function {@code f} to transform any failure. Successes remain unchanged.
     * <p>
     * Any exception thrown by function {@code f} is converted to a {@link Try.Failure}. If you would prefer any
     * exception to propagate out, then see {@link #recover}
     * <p>
     * The symmetrical method for successes is {@link #tryMap}
     *
     * @param f the function to transform the exception into a value of type {@code T}.
     * @return If the instance is a failure, a successful result containing the result of the transformation returned by
     * the function {@code f}, or, if the function {@code f} threw an exception, then a failure containing the thrown
     * exception, otherwise the already successful instance, unchanged.
     */
    Try<T> tryRecover(final Function<? super Exception, ? extends T> f);

    /**
     * Applies the given function {@code f} to transform any successful result to a new {@link Try}.
     * <p>
     * Any exception thrown by function {@code f} is left to propagate out. If you would prefer the exception to be
     * converted into a {@link Try.Failure}, then see {@link #tryFlatMap}
     * <p>
     * The symmetrical method for failures is {@link #flatRecover}
     *
     * @param f   the transformation function to call to transform the successful value to type {@code U}.
     * @param <U> the resulting value type of the transformation
     * @return If the instance is successful, then the result of the transformation returned by the function {@code f},
     * otherwise the failure, unchanged.
     * @throws RuntimeException if function {@code f} throws.
     */
    <U> Try<U> flatMap(final Function<? super T, Try<U>> f);

    /**
     * Applies the given function {@code f} to transform any successful result to a new {@link Try}.
     * <p>
     * Any exception thrown by function {@code f} is converted to a {@link Try.Failure}. If you would prefer any
     * exception to propagate out, then see {@link #flatMap}
     * <p>
     * The symmetrical method for failures is {@link #tryFlatRecover}
     *
     * @param f   the transformation function to call to transform the successful value to type {@code U}.
     * @param <U> the resulting value type of the transformation
     * @return If the instance is a success, the the result of the transformation returned by the function {@code f},
     * or, if the function {@code f} threw an exception, then a failure containing the thrown exception,
     * otherwise the failure, unchanged.
     */
    <U> Try<U> tryFlatMap(final Function<? super T, Try<U>> f);

    /**
     * Applies the given function {@code f} to transform any failure result to a new {@link Try}.
     * <p>
     * Any exception thrown by function {@code f} is left to propagate out. If you would prefer the exception to be
     * converted into a {@link Try.Failure}, then see {@link #tryFlatRecover}
     * <p>
     * The symmetrical method for failures is {@link #flatMap}
     *
     * @param f the function to transform the exception into a value of type {@code T}.
     * @return If the instance is a failure, then the result of the transformation returned by the function {@code f},
     * otherwise the success instance, unchanged.
     * @throws RuntimeException if function {@code f} throws.
     */
    Try<T> flatRecover(final Function<? super Exception, Try<T>> f);

    /**
     * Applies the given function {@code f} to transform any failure result to a new {@link Try}.
     * <p>
     * Any exception thrown by function {@code f} is converted to a {@link Try.Failure}. If you would prefer any
     * exception to propagate out, then see {@link #flatRecover}
     * <p>
     * The symmetrical method for failures is {@link #tryFlatMap}
     *
     * @param f the function to transform the exception into a value of type {@code T}.
     * @return If the instance is a failure, the result of the transformation returned by the function {@code f},
     * or, if the function {@code f} threw an exception, then a failure containing the thrown exception,
     * otherwise the already successful instance, unchanged.
     */
    Try<T> tryFlatRecover(final Function<? super Exception, Try<T>> f);

    /**
     * Apply either the supplied {@code onSuccess} or {@code onFailure} functions to transform the instance,
     * depending on whether the instance is successful or a failure.
     * <p>
     * Any exception thrown by the executed function is left to propagate out.
     *
     * @param onSuccess the transformation function to call on success.
     * @param onFailure the transformation function to call on failure.
     * @param <U>       the resulting type of the transformation
     * @return the result of the transformation.
     * @throws RuntimeException if the executed function throws.
     */
    <U> U fold(final Function<? super T, ? extends U> onSuccess, final Function<? super Exception, ? extends U> onFailure);

    /**
     * Call either the supplied {@code onSuccess} or {@code onFailure} {@link Consumer 'consumers'},
     * depending on whether the instance is successful or a failure.
     * <p>
     * Any exception thrown by the executed function is left to propagate out.
     *
     * @param onSuccess the consumer to call on success.
     * @param onFailure the consumer to call on failure.
     * @throws RuntimeException if the executed consumer throws.
     */
    void forEach(final Consumer<? super T> onSuccess, final Consumer<? super Exception> onFailure);

    class Success<T> implements Try<T> {
        private final T value;

        public Success(final T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public T getOrElse(final T defaultValue) {
            return get();
        }

        @Override
        public T getOrElse(final Supplier<? extends T> f) {
            return get();
        }

        @Override
        public T getOrThrow() throws Exception {
            return value;
        }

        @Override
        public Optional<Exception> exception() {
            return Optional.empty();
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }

        @Override
        public <U> Try<U> map(final Function<? super T, ? extends U> f) {
            return Try.success(f.apply(value));
        }

        @Override
        public <U> Try<U> tryMap(final Function<? super T, ? extends U> f) {
            return Try.exec(f, value);
        }

        @Override
        public Try<T> recover(final Function<? super Exception, ? extends T> f) {
            return this;
        }

        @Override
        public Try<T> tryRecover(final Function<? super Exception, ? extends T> f) {
            return this;
        }

        @Override
        public <U> Try<U> flatMap(final Function<? super T, Try<U>> f) {
            return f.apply(value);
        }

        @Override
        public <U> Try<U> tryFlatMap(final Function<? super T, Try<U>> f) {
            return Try.flatExec(f, value);
        }

        @Override
        public Try<T> flatRecover(final Function<? super Exception, Try<T>> f) {
            return this;
        }

        @Override
        public Try<T> tryFlatRecover(final Function<? super Exception, Try<T>> f) {
            return this;
        }

        @Override
        public <U> U fold(final Function<? super T, ? extends U> onSuccess, final Function<? super Exception, ? extends U> onFailure) {
            return onSuccess.apply(value);
        }

        @Override
        public void forEach(final Consumer<? super T> onSuccess, final Consumer<? super Exception> onFailure) {
            onSuccess.accept(value);
        }

        @Override
        public String toString() {
            return "Success(" + value + ")";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Success<?> success = (Success<?>) o;
            return Objects.equals(value, success.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }
    }

    class Failure<T> implements Try<T> {
        private final Exception exception;

        @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
        public Failure(final Exception exception) {
            if (exception == null) {
                throw new NullPointerException("exception");
            }
            this.exception = exception;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T get() {
            throw new FailureException(exception);
        }

        @Override
        public T getOrElse(final T defaultValue) {
            return defaultValue;
        }

        @Override
        public T getOrElse(final Supplier<? extends T> f) {
            return f.get();
        }

        @Override
        public T getOrThrow() throws Exception {
            throw exception;
        }

        @Override
        public Optional<Exception> exception() {
            return Optional.of(exception);
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> map(final Function<? super T, ? extends U> f) {
            return (Try<U>) this;
        }

        @Override
        public <U> Try<U> tryMap(final Function<? super T, ? extends U> f) {
            return map(f);
        }

        @Override
        public Try<T> recover(final Function<? super Exception, ? extends T> f) {
            return Try.success(f.apply(exception));
        }

        @Override
        public Try<T> tryRecover(final Function<? super Exception, ? extends T> f) {
            return Try.exec(f, exception);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> Try<U> flatMap(final Function<? super T, Try<U>> f) {
            return (Try<U>) this;
        }

        @Override
        public <U> Try<U> tryFlatMap(final Function<? super T, Try<U>> f) {
            return flatMap(f);
        }

        @Override
        public Try<T> flatRecover(final Function<? super Exception, Try<T>> f) {
            return f.apply(exception);
        }

        @Override
        public Try<T> tryFlatRecover(final Function<? super Exception, Try<T>> f) {
            return Try.flatExec(f, exception);
        }

        @Override
        public <U> U fold(final Function<? super T, ? extends U> onSuccess, final Function<? super Exception, ? extends U> onFailure) {
            return onFailure.apply(exception);
        }

        @Override
        public void forEach(final Consumer<? super T> onSuccess, final Consumer<? super Exception> onFailure) {
            onFailure.accept(exception);
        }

        @Override
        public String toString() {
            return "Failure(" + exception + ")";
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            final Failure<?> failure = (Failure<?>) o;
            return Objects.equals(exception, failure.exception);
        }

        @Override
        public int hashCode() {
            return Objects.hash(exception);
        }
    }

    class FailureException extends RuntimeException {
        FailureException(final Throwable cause) {
            super(cause);
        }
    }
}

