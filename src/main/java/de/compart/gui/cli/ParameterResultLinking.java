/**
 * 
 */
package de.compart.gui.cli;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;
import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public class ParameterResultLinking implements ParameterResult {

	private static final Logger log = LoggerFactory.getLogger(ParameterResultLinking.class);
	
	private final HashMap<Parameter<?>,List<String>> resultSet = new HashMap<Parameter<?>, List<String>>();
	
	@Override
	public <TYPE> boolean hasParameter(final Parameter<TYPE> parameter) {
		return (this.resultSet.containsKey(parameter));
	}

	@Override
	public <TYPE> Maybe<TYPE> getValue(final Parameter<TYPE> parameter) {
		if (!this.hasParameter(parameter)) {
			log.info("This instance of '{}' does not contain parameter '{}'", ParameterResultLinking.class.getSimpleName(), parameter);
			return Maybe.nothing();
		}
		return this.getValue(parameter, parameter.getExpectedValueClass().getValue());
	}
	
	public <TYPE> Maybe<TYPE> getValue(final Parameter<?> parameter, final Class<TYPE> expectedValueClass) {
		if (!this.hasParameter(parameter)) {
			log.info("This instance of '{}' does not contain parameter '{}'", ParameterResultLinking.class.getSimpleName(), parameter);
			return Maybe.nothing();
		}
		
		final List<String> a = this.resultSet.get(parameter);
		
		log.debug("Parameter {} has '{}' {} stored.", new Object[]{parameter, a.size(), (a.size() == 1 ? "element" : "elements" )});
		
		if (a.isEmpty()) {
			return Maybe.nothing();
		} else if (a.size() == 1) {
			return ClassCastHelperFactory.castObject(expectedValueClass, a.get(0));
		} else if (!expectedValueClass.isAssignableFrom(Collection.class)) {
			return Maybe.nothing();
		}
		
		return (Maybe<TYPE>) Maybe.just(expectedValueClass.cast(a));			
	}
	
	private Parameter<?> currentParameter = null;
	
	public Parameter<?> getCurrentParameter() {
		if (this.currentParameter == null || currentParameter.needsArgument() == ParameterConfiguration.NO_ARGUMENTS) {
			return ParameterFactory.DEFAULT_PARAMETER;
		}
		return this.currentParameter;
	}
	
	public void addValue(final String value) {
		if (value == null) { throw new NullPointerException(); }
		
		Parameter<?> currentParameter = this.getCurrentParameter();
		if (!resultSet.containsKey(currentParameter)) {
			resultSet.put(currentParameter, new ArrayList<String>());
		}
		resultSet.get(currentParameter).add(value);
		
		assert resultSet.get(currentParameter).contains(value);
	}

	public void addParameter(final Parameter<?> value) {
		if (value == null) { throw new NullPointerException(); }
		this.currentParameter = value;
		if (!resultSet.containsKey(currentParameter)) {
			resultSet.put(currentParameter, new ArrayList<String>());
		}
		
		assert resultSet.containsKey(value);
		
	}

	
}
