/**
 *
 */
package de.compart.gui.cli;

import de.compart.common.Maybe;
import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParameterResultLinking implements ParameterResult {

	private static final Logger log = LoggerFactory.getLogger( ParameterResultLinking.class );

	private final HashMap<Parameter, List<String>> resultSet = new HashMap<Parameter, List<String>>();

	@Override
	public boolean hasParameter( final Parameter parameter ) {
		return ( this.resultSet.containsKey( parameter ) );
	}

	@Override
	public <TYPE> Maybe<TYPE> getValue( final Parameter<TYPE> parameter ) {
		if ( !this.hasParameter( parameter ) ) {
			log.info( "This instance of '{}' does not contain parameter '{}'", ParameterResultLinking.class.getSimpleName(), parameter );
			return Maybe.nothing();
		}
		return ClassCastHelperFactory.getValueOutOfParameter( parameter, resultSet.get( parameter ).toArray( new String[ resultSet.get( parameter ).size() ] ) );
	}

	@Override
	public <TYPE> Maybe<TYPE> getValue( final Parameter<? super TYPE> parameter, final Class<TYPE> expectedValueClass ) {
		if ( !this.hasParameter( parameter ) ) {
			log.info( "This instance of '{}' does not contain parameter '{}'", ParameterResultLinking.class.getSimpleName(), parameter );
			return Maybe.nothing();
		}
		return ClassCastHelperFactory.getValueOutOfParameter( expectedValueClass, parameter.isArgumentRequired(), resultSet.get( parameter ).toArray( new String[ resultSet.get( parameter ).size() ] ) );
	}

	private Parameter currentParameter = null;

	public Parameter getCurrentParameter() {
		if ( currentParameter == null ) {
			return ParameterFactory.DEFAULT_PARAMETER;
		}
		return this.currentParameter;
	}

	public void addValue( final String value ) {
		if ( value == null ) {
			throw new NullPointerException();
		}

		Parameter parameter = this.getCurrentParameter();
		if ( !resultSet.containsKey( parameter ) ) {
			resultSet.put( parameter, new ArrayList<String>() );
		}

		final ParameterConfiguration configuration = parameter.isArgumentRequired();
		if ( configuration == ParameterConfiguration.NO_ARGUMENTS || ( configuration == ParameterConfiguration.ONE_ARGUMENT && resultSet.get( parameter ).size() == 1 ) ) {
			this.addValueToDefaultParameter( value );
		} else {
			resultSet.get( parameter ).add( value );
			assert resultSet.get( parameter ).contains( value );
		}
	}

	private void addValueToDefaultParameter( final String value ) {
		Parameter defaultParameter = ParameterFactory.DEFAULT_PARAMETER;
		if ( !resultSet.containsKey( defaultParameter ) ) {
			resultSet.put( defaultParameter, new ArrayList<String>() );
		}
		resultSet.get( defaultParameter ).add( value );
		assert resultSet.get( defaultParameter ).contains( value );
	}

	public void addParameter( final Parameter parameter ) {
		if ( parameter == null ) {
			throw new NullPointerException();
		}
		this.currentParameter = parameter;
		if ( !resultSet.containsKey( currentParameter ) ) {
			resultSet.put( currentParameter, new ArrayList<String>() );
		}

		assert resultSet.containsKey( parameter );

	}
}
