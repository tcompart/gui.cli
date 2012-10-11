/**
 *
 */
package de.compart.gui.cli;

import de.compart.common.Maybe;

public interface ParameterResult {

	boolean hasParameter( final Parameter parameter );

	<T> Maybe<T> getValue( final Parameter<T> parameter );

	<T> Maybe<T> getValue( final Parameter<? super T> parameter, Class<T> valueClasses );
}
