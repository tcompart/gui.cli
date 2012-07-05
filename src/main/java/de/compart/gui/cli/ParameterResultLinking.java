/**
 * 
 */
package de.compart.gui.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;
import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public class ParameterResultLinking implements ParameterResult {

	private final HashMap<Parameter<?>,List<Object>> resultSet = new HashMap<Parameter<?>, List<Object>>();
	
	@Override
	public <TYPE> boolean hasParameter(final Parameter<TYPE> parameter) {
		return (this.resultSet.containsKey(parameter));
	}

	@Override
	public <TYPE> Maybe<TYPE> getValue(final Parameter<TYPE> parameter) {
		if (!this.hasParameter(parameter)) {
			return Maybe.nothing();
		}
		return this.getValue(parameter, parameter.getExpectedValueClass().getValue());
	}
	
	public <TYPE> Maybe<TYPE> getValue(final Parameter<?> parameter, final Class<TYPE> expectedValueClass) {
		if (!this.hasParameter(parameter)) {
			return Maybe.nothing();
		}
		
		final List<Object> a = this.resultSet.get(parameter);
		
		if (a.isEmpty()) {
			return Maybe.nothing();
		} else if (a.size() == 1) {
			return Maybe.just(expectedValueClass.cast(a.get(0)));
		} else if (!expectedValueClass.isAssignableFrom(Collection.class)) {
			return Maybe.nothing();
		}
		
		return (Maybe<TYPE>) Maybe.just(expectedValueClass.cast(a));			
	}
	
	private Parameter<?> currentParameter = null;
	
	public void addValue(Object value) {
		if (value == null) { throw new NullPointerException(); }
		
		if (currentParameter == null || currentParameter.needsArgument() == ParameterConfiguration.NO_ARGUMENTS) {
			currentParameter = ParameterFactory.DEFAULT_PARAMETER;
		}
		if (!resultSet.containsKey(currentParameter)) {
			resultSet.put(currentParameter, new ArrayList<Object>());
		}
		resultSet.get(currentParameter).add(value);
	}

	public void addParameter(Parameter<?> value) {
		if (value == null) { throw new NullPointerException(); }
		this.currentParameter = value;
		if (!resultSet.containsKey(currentParameter)) {
			resultSet.put(currentParameter, new ArrayList<Object>());
		}
		
		assert resultSet.containsKey(value);
		
	}

	
}
