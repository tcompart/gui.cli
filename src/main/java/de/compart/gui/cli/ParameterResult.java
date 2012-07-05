/**
 * 
 */
package de.compart.gui.cli;

import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public interface ParameterResult {

	<T> boolean hasParameter(final Parameter<T> parameter);

	<T> Maybe<T> getValue(final Parameter<T> parameter);
	
	<T> Maybe<T> getValue(final Parameter<?> parameter, Class<T> valueClass);
	
}
