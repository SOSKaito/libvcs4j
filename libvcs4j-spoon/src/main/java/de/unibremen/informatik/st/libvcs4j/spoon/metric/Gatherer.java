package de.unibremen.informatik.st.libvcs4j.spoon.metric;

import de.unibremen.informatik.st.libvcs4j.Validate;
import de.unibremen.informatik.st.libvcs4j.spoon.Scanner;
import de.unibremen.informatik.st.libvcs4j.spoon.codesmell.Metric;
import lombok.NonNull;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This is the base class of all metric gatherers. By using a stack, metrics
 * may be gathered for nested AST nodes ({@link CtElement}). This class is
 * named "Gatherer" to avoid name collisions with {@link Metric}.
 *
 * @param <T>
 *     The type of the gathered metric value, e. g. {@link Integer}.
 */
public abstract class Gatherer<T extends Number> extends Scanner {

	/**
	 * Specifies how propagate the metric of an element to its parent.
	 */
	enum Propagation {
		/**
		 * Do nothing.
		 */
		NONE,

		/**
		 * Add the metric of an element to the metric of its parent.
		 *
		 * @see #sum(Number, Number)
		 */
		SUM

		/* Further strategies may be: SUBTRACT, MAX, MIN, ... */
	}

	/**
	 * Stacks the metric of nested elements.
	 */
	private final Deque<T> stack = new ArrayDeque<>();

	/**
	 * Maps an element to its metric.
	 */
	private final Map<CtElement, T> metrics = new IdentityHashMap<>();

	@Override
	public void visitRoot(final CtElement element) {
		stack.clear();
		metrics.clear();
		super.visitRoot(element);
	}

	/**
	 * Returns the sum of {@code a} and {@code b}.
	 *
	 * @param a
	 * 		The first operand.
	 * @param b
	 * 		The second operand.
	 * @return
	 * 		The sum of {@code a} and {@code b}.
	 * @throws NullPointerException
	 * 		If any of the given arguments is {@code null}.
	 */
	protected abstract T sum(final T a, final T b) throws NullPointerException;

	/**
	 * Returns the full name of this gatherer.
	 *
	 * @return
	 * 		The full name of this gatherer.
	 */
	public abstract String name();

	/**
	 * Returns the abbreviated name of this gatherer.
	 *
	 * @return
	 * 		The abbreviated name of this gatherer.
	 */
	public abstract String abbreviation();

	/**
	 * Increments the metric of the top element of {@link #stack} by
	 * {@code value}. Does nothing if {@link #stack} is empty.
	 *
	 * @param value
	 * 		The increment value.
	 * @throws NullPointerException
	 * 		If {@code value} is {@code null}.
	 */
	void inc(@NonNull final T value) throws NullPointerException {
		if (!stack.isEmpty()) {
			final T prev = stack.pop();
			stack.push(sum(prev, value));
		}
	}

	/**
	 * Visits the given element and gathers its metric. This method is intended
	 * to be used by one of the several visit methods of {@link CtScanner},
	 * e. g. {@link CtScanner#visitCtClass(CtClass)}. The consumer callback
	 * {@code superCall} is required to properly execute the recursive descent
	 * of {@code element} and, therefore, must point the super method of the
	 * visit function that calls this method. {@code superCall} should not be
	 * called elsewhere. Otherwise, it may be executed twice, once by this
	 * method and once again by the explicit call.
	 *
	 * @param element
	 * 		The element to visit.
	 * @param superCall
	 * 		The super function of the corresponding visit method.
	 * @param propagation
	 * 		Specifies how to propagate the metric of {@code element} to its
	 * 		parent.
	 * @param initValue
	 * 		The initial metric value of {@code element}.
	 * @param <E>
	 *     	The type of the element to visit.
	 * @throws NullPointerException
	 * 		If any of the given argument is {@code null}.
	 */
	<E extends CtElement> void visitNode(@NonNull final E element,
			@NonNull final Consumer<E> superCall,
			@NonNull final Propagation propagation,
			@NonNull final T initValue) throws NullPointerException {
		Validate.validateState(!metrics.containsKey(element),
				"Element '%s' has already been visited", element);
		stack.push(initValue);
		superCall.accept(element);
		final T metric = stack.pop();
		metrics.put(element, metric);
		if (!stack.isEmpty()) {
			switch (propagation) {
				case NONE:
					break;
				case SUM:
					inc(metric);
					break;
				default:
					Validate.fail("Unknown propagation '%s'", propagation);
			}
		}
	}

	/**
	 * Visits the given element and gathers its metric. This method extends
	 * {@link #visitNode(CtElement, Consumer, Propagation, Number)} and allows
	 * to register another callback which is called after {@code superCall},
	 * but right before the resulting metric is stored, therefore allowing to
	 * do some final calculations.
	 *
	 * @param element
	 * 		The element to visit.
	 * @param superCall
	 * 		The super function of the corresponding visit method.
	 * @param callBack
	 * 		The function to call right before the resulting metric is stored.
	 * @param propagation
	 * 		Specifies how to propagate the metric of {@code element} to its
	 * 		parent.
	 * @param initValue
	 * 		The initial metric value of {@code element}.
	 * @param <E>
	 *     	The type of the element to visit.
	 * @throws NullPointerException
	 * 		If any of the given argument is {@code null}.
	 */
	<E extends CtElement> void visitNode(@NonNull final E element,
			@NonNull final Consumer<E> superCall,
			@NonNull final Consumer<E> callBack,
			@NonNull final Propagation propagation, final T initValue) {
		visitNode(element, __ -> {
			superCall.accept(element);
			callBack.accept(element);
		}, propagation, initValue);
	}

	/**
	 * Returns the metric of {@code element}. Returns an empty {@link Optional}
	 * if {@code element} is {@code null}, or if {@code element} has no
	 * associated metric.
	 *
	 * @param element
	 * 		The element whose metric is requested.
	 * @return
	 * 		The metric of {@code element}.
	 */
	Optional<T> metricOf(final CtElement element) {
		return Optional.ofNullable(element).map(metrics::get);
	}
}
