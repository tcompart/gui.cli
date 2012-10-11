/**
 *
 */
package de.compart.gui.cli;

import de.compart.common.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

public class ParameterFactory {

	public static final char NO_SHORT_OPTION = '\\';
	public static final String NO_LONG_OPTION = "\\//\\NO_LONG_OPTION\\//\\";

	public static final Parameter<Collection> DEFAULT_PARAMETER = ParameterFactory.<Collection>createParameter( NO_SHORT_OPTION, NO_LONG_OPTION ).setNeedsArgument( ParameterConfiguration.MANY_ARGUMENTS );

	public static final String LONG_PARAMETER_BEGIN = "--";
	public static final String SHORT_PARAMETER_BEGIN = "-";

	public enum ParameterConfiguration {
		NO_ARGUMENTS,
		ONE_ARGUMENT,
		MANY_ARGUMENTS
	}

	private static final Logger log = LoggerFactory.getLogger( ParameterFactory.class );

	public static ParameterResult parse( final String[] parameterValueStringArray, final Parameter<?>... parameters ) {
		return parse( parameterValueStringArray, Arrays.asList( parameters ) );
	}

	public static ParameterResult parse( final String[] parameterValueStringArray, final Collection<Parameter<?>> inputParameters ) {

		final LinkedList<String> args = new LinkedList<String>( Arrays.asList( parameterValueStringArray ) );
		final ParameterResultLinking result = new ParameterResultLinking();

		Maybe<Parameter<?>> maybeParameter;

		for ( String currentString : args ) {
			if ( currentString.startsWith( LONG_PARAMETER_BEGIN ) || currentString.startsWith( SHORT_PARAMETER_BEGIN ) ) {
				maybeParameter = getParameter( inputParameters, currentString );
				if ( maybeParameter.isJust() ) {
					final Parameter<?> parameter = maybeParameter.get();
					log.info( "Adding parameter '{}' to instance of class {}", parameter, ParameterResult.class.getSimpleName() );
					result.addParameter( parameter );

					assert result.hasParameter( parameter );

					continue;
				} else {
					log.warn( "Parameter '{}' seems to be no parameter, and will be treated as value for an already recognized parameter '{}'.", currentString, result.getCurrentParameter() );
				}
			}
			log.info( "Adding value '{}' to {}. Value will be added to last recognized parameter '{}'.", new Object[]{currentString, ParameterResult.class.getSimpleName(), result.getCurrentParameter()} );
			result.addValue( currentString );
		}

		return result;
	}


	private static Maybe<Parameter<?>> getParameter( final Collection<Parameter<?>> possibleParameterList, final String inputParameterString ) {
		if ( possibleParameterList == null || inputParameterString == null ) {
			throw new NullPointerException();
		}

		final String parameterString;
		if ( inputParameterString.startsWith( LONG_PARAMETER_BEGIN ) ) {
			parameterString = inputParameterString.replaceFirst( LONG_PARAMETER_BEGIN, "" );
		} else if ( inputParameterString.startsWith( SHORT_PARAMETER_BEGIN ) ) {
			parameterString = inputParameterString.replaceFirst( SHORT_PARAMETER_BEGIN, "" );
		} else {
			parameterString = inputParameterString;
		}

		for ( Parameter<?> allowedParameter : possibleParameterList ) {
			if ( equals( allowedParameter, parameterString ) ) {
				assert allowedParameter != null;
				log.info( "Mapping parameter '{}' to resulting parameter: '{}'", parameterString, allowedParameter.toString() );
				return Maybe.<Parameter<?>>just( allowedParameter );
			}
		}
		log.warn( "'{}' is not a known parameter.", parameterString );
		return Maybe.nothing();
	}

	private static boolean equals( final Parameter allowedParameter, final String possibleParameter ) {
		if ( !( possibleParameter == null || possibleParameter.isEmpty() ) ) {
			if ( allowedParameter != null ) {
				if ( ( allowedParameter.getShortForm() == null && allowedParameter.getLongForm() == null || allowedParameter.getShortForm() != null && allowedParameter.getShortForm().equals( possibleParameter ) || allowedParameter.getLongForm() != null && allowedParameter.getLongForm().equals( possibleParameter ) ) ) {
					return true;
				}
			}
		}
		return false;
	}

	public static <TYPE> DefaultParameter<TYPE> createParameter( final char shortParameterName, final String longParameterName ) {
		return new DefaultParameter<TYPE>( String.valueOf( shortParameterName ), longParameterName );
	}

	public static void printHelp( final OutputStream out, final OutputStream err, final ParameterResult given, final Collection<Parameter<?>> definedParameters ) throws IOException {
		try {
			writeLine( out, "\nFound parameters:" );
			for ( Parameter foundParameter : definedParameters ) {
				if ( given.hasParameter( foundParameter ) ) {
					writeLine( out, String.format( "\t: %s", foundParameter ) );
				}
			}
			writeLine( out, "\nPossible parameters:" );
			for ( Parameter foundParameter : definedParameters ) {
				writeLine( out, String.format( "\t: %s", foundParameter ) );
			}
			writeLine( err, "\nMissing parameters:" );
			for ( Parameter foundParameter : definedParameters ) {
				if ( foundParameter.isRequired() && !given.hasParameter( foundParameter ) ) {
					writeLine( err, String.format( "\t: %s", foundParameter ) );
				}
			}
		} finally {
			out.flush();
			out.close();
			err.flush();
			err.close();
		}

	}

	private static void writeLine( final OutputStream out, final String lineToBeWritten ) throws IOException {
		out.write( String.format( "%s%n", lineToBeWritten ).getBytes() );
	}

	public static class DefaultParameter<TYPE> implements Parameter<TYPE> {

		private final String shortForm;
		private final String longForm;

		private String description;
		private boolean required;
		private ParameterConfiguration needsArgument;

		public DefaultParameter( final String shortForm, final String longForm ) {

			assert shortForm != null;
			assert longForm != null;

			this.shortForm = shortForm;
			this.longForm = longForm;
		}

		@Override
		public String getShortForm() {
			return this.shortForm;
		}

		@Override
		public String getLongForm() {
			return this.longForm;
		}

		@Override
		public Maybe<String> getDescription() {
			return Maybe.just( this.description );
		}

		@Override
		public boolean isRequired() {
			return this.required;
		}

		@Override
		public ParameterConfiguration isArgumentRequired() {
			return this.needsArgument;
		}

		public DefaultParameter<TYPE> setDescription( final String inputDescription ) {
			this.description = inputDescription;
			return this;
		}

		public DefaultParameter<TYPE> setRequired( final boolean inputRequired ) {
			this.required = inputRequired;
			return this;
		}

		public DefaultParameter<TYPE> setNeedsArgument( final ParameterConfiguration inputNeedsArgument ) {
			if ( inputNeedsArgument == null ) {
				throw new NullPointerException();
			}
			if ( inputNeedsArgument != ParameterConfiguration.MANY_ARGUMENTS && inputNeedsArgument != ParameterConfiguration.ONE_ARGUMENT && inputNeedsArgument != ParameterConfiguration.NO_ARGUMENTS ) {
				throw new IllegalArgumentException( String.format( "Expected value are '%s', '%s' or '%s'. ", ParameterConfiguration.NO_ARGUMENTS, ParameterConfiguration.ONE_ARGUMENT, ParameterConfiguration.MANY_ARGUMENTS ) );
			}
			this.needsArgument = inputNeedsArgument;
			return this;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			sb.append( DefaultParameter.class.getSimpleName() );
			sb.append( " [" );
			sb.append( "'longForm': " );
			sb.append( longForm );
			sb.append( ", 'shortForm': " );
			sb.append( shortForm );
			sb.append( ", 'required': " );
			sb.append( isRequired() );
			if ( isArgumentRequired() != null ) {
				sb.append( ", 'isArgumentRequired': " );
				sb.append( isArgumentRequired().toString() );
			}
			sb.append( ", 'expectedClassType': " );
			// sb.append( ( getExpectedValueClass() != null ? getExpectedValueClass().getSimpleName() : "none" ) );
			if ( getDescription().isJust() ) {
				sb.append( ", 'description': " );
				sb.append( getDescription().get() );
			}
			sb.append( "]" );

			return sb.toString();
		}

		public Parameter<TYPE> build() {
			return this;
		}
	}
}
