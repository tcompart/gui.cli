/**
 * 
 */
package de.compart.gui.cli;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.comparator.ComparableComparator;

import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public class ParameterFactory {

	public static final char NO_SHORT_OPTION = '\\';
	public static final String NO_LONG_OPTION = "\\//\\NO_LONG_OPTION\\//\\";
	
	public static final Parameter<Object> DEFAULT_PARAMETER = ParameterFactory.createParameter(NO_SHORT_OPTION, NO_LONG_OPTION, ParameterConfiguration.MANY_ARGUMENTS, Object.class, ParameterConfiguration.OPTIONAL);
	
	public static final String LONG_PARAMETER_BEGIN = "--";
	public static final String SHORT_PARAMETER_BEGIN = "-";
	
	public enum ParameterConfiguration {
		OPTIONAL,
		REQUIRED,
		
		NO_ARGUMENTS,
		ONE_ARGUMENT,
		MANY_ARGUMENTS
	}
	
	private static final Logger log = LoggerFactory.getLogger(ParameterFactory.class);
	
	public static ParameterResult parse(final String[] parameterValueStringArray, final Parameter<?>... parameters) {
		return parse(parameterValueStringArray, Arrays.asList(parameters));
	}
	
	public static ParameterResult parse(final String[] parameterValueStringArray, final Collection<Parameter<?>> inputParameters) {
		
		final LinkedList<String> args = new LinkedList<String>(Arrays.asList(parameterValueStringArray));
		final ParameterResultLinking result = new ParameterResultLinking();
		
		Maybe<Parameter<?>> maybeParameter;
		
		for (String currentString : args) {
			if (currentString.startsWith(LONG_PARAMETER_BEGIN) || currentString.startsWith(SHORT_PARAMETER_BEGIN)) {
				maybeParameter = getParameter(inputParameters, currentString);
				if (maybeParameter.isJust()) {
					final Parameter<?> parameter = maybeParameter.getValue();
					log.info("Adding parameter '{}' to {}", parameter, ParameterResult.class.getSimpleName());
					result.addParameter(parameter);
					
					assert result.hasParameter(parameter);
					
					continue;
				} else {
					log.warn("Parameter '{}' seems to be no parameter, and will be treated as value for another recognized parameter", currentString);
				}
			}
			log.info("Adding value '{}' to {}. Value will be added to last recognized parameter '{}'.", new Object[]{currentString, ParameterResult.class.getSimpleName(), result.getCurrentParameter()});
			result.addValue(currentString);
		}
		
		return result;
	}


	public static Maybe<Parameter<?>> getParameter(final Collection<Parameter<?>> allowedParameters, final String inputParameterString) {
		if (allowedParameters == null) { throw new NullPointerException(); }
		if (inputParameterString == null) { throw new NullPointerException(); }
		
		final String parameterString;
		if (inputParameterString.startsWith(LONG_PARAMETER_BEGIN)) {
			parameterString = inputParameterString.replaceFirst(LONG_PARAMETER_BEGIN, "");
		} else if (inputParameterString.startsWith(SHORT_PARAMETER_BEGIN)) {
			parameterString = inputParameterString.replaceFirst(SHORT_PARAMETER_BEGIN, "");
		} else {
			parameterString = inputParameterString;
		}

		for (Parameter<?> allowedParameter : allowedParameters) {
			if (equals(allowedParameter, parameterString)) {
				final Parameter<?> resultingParameter = allowedParameter;
				
				assert resultingParameter != null;
				log.info("Mapping parameter '{}' to resulting parameter: '{}'", parameterString, resultingParameter.toString());
				return Maybe.<Parameter<?>>just(resultingParameter);
			}
		}
		log.warn("'{}' is not a known parameter.", parameterString);
		return Maybe.nothing();
	}

	public static boolean equals(final Parameter<?> allowedParameter, final String possibleParameter) {
		if (possibleParameter == null || possibleParameter.isEmpty()) { return false; }
		if (allowedParameter == null) { return false; }
		if (allowedParameter.getShortForm().isNothing() && allowedParameter.getLongForm().isNothing()) { return true; }
		
		if (allowedParameter.getShortForm().isJust() && allowedParameter.getShortForm().getValue().equals(possibleParameter)) { return true; }
		if (allowedParameter.getLongForm().isJust() && allowedParameter.getLongForm().getValue().equals(possibleParameter)) { return true; }
		return false;
	}

	public static Parameter<?> createParameter(final Character shortParameterName, final String longParameterName,
			final ParameterConfiguration argument_option, final ParameterConfiguration optional_required_option) {
		if (shortParameterName == null || longParameterName == null || argument_option == null || optional_required_option == null) { throw new NullPointerException(); }
		if (optional_required_option != ParameterConfiguration.OPTIONAL && optional_required_option != ParameterConfiguration.REQUIRED) { throw new IllegalArgumentException(String.format("Expected values for second parameter should be '%s' or '%s'", ParameterConfiguration.OPTIONAL, ParameterConfiguration.REQUIRED)); }
		return createParameter(shortParameterName, longParameterName, argument_option, null, optional_required_option);
	}
	
	public static <TYPE> Parameter<TYPE> createParameter(final char shortParameterName, final String longParameterName,
			final ParameterConfiguration argument_option, final Class<TYPE> expectedValueClass, final ParameterConfiguration optional_required_option) {
		if (argument_option == null) {throw new NullPointerException(); }
		if (argument_option == ParameterConfiguration.NO_ARGUMENTS && expectedValueClass != null) { throw new IllegalArgumentException("Expected that no arguments are required, and therefore no value class would be assigned. This state of values is not allowed.");}
		
		final DefaultParameter<TYPE> parameter = new DefaultParameter<TYPE>(String.valueOf(shortParameterName), longParameterName, null);
		if (optional_required_option == ParameterConfiguration.OPTIONAL) {
			parameter.setRequired(false);
		} else if (optional_required_option == ParameterConfiguration.REQUIRED) {
			parameter.setRequired(true);
		}
		parameter.setNeedsArgument(argument_option);
		parameter.setExpectedValueType(expectedValueClass);
		
		return parameter;
	}
	
	public static void printHelp(final OutputStream out, final OutputStream err, final ParameterResult given, final Collection<Parameter<?>> definedParameters) throws IOException {
		try {
			writeLine(out, "\nFound parameters:");
			for (Parameter<?> foundParameter : definedParameters) {
				if (given.hasParameter(foundParameter)) {
					writeLine(out, String.format("\t: %s", foundParameter));
				}
			}
			writeLine(out, "\nPossible parameters:");
			for (Parameter<?> foundParameter : definedParameters) {
				writeLine(out, String.format("\t: %s", foundParameter));
			}
			writeLine(err, "\nMissing parameters:");
			for (Parameter<?> foundParameter : definedParameters) {
				if (foundParameter.isRequired() && !given.hasParameter(foundParameter)) {
					writeLine(err, String.format("\t: %s", foundParameter));
				}
			}			
		} finally {
			out.flush();
			out.close();
			err.flush();
			err.close();
		}
		
	}
	
	private static void writeLine(final OutputStream out, final String lineToBeWritten) throws IOException {
		out.write(String.format("%s%n", lineToBeWritten).getBytes());
	}

	public static class DefaultParameter<TYPE> implements Parameter<TYPE> {

		private final String shortForm;
		private final String longForm;
		private final String description;
		private boolean required;
		private ParameterConfiguration needsArgument;
		private Class<TYPE> expectedValueClass;
		
		public DefaultParameter(final String shortForm, final String longForm, final String description) {
			
			assert shortForm != null;
			assert longForm != null;
			
			this.shortForm = shortForm;
			this.longForm = longForm;
			this.description = description;
		}
		
		@Override
		public Maybe<String> getShortForm() {
			return Maybe.just(this.shortForm);
		}

		@Override
		public Maybe<String> getLongForm() {
			return Maybe.just(this.longForm);
		}

		@Override
		public Maybe<String> getDescription() {
			return Maybe.just(this.description);
		}
		
		@Override
		public boolean isRequired() {
			return this.required;
		}
		
		public void setRequired(final boolean inputRequired) {
			this.required = inputRequired;
		}
		
		@Override
		public ParameterConfiguration needsArgument() {
			return this.needsArgument;
		}
		
		public void setNeedsArgument(final ParameterConfiguration inputNeedsArgument) {
			if (inputNeedsArgument == null) { throw new NullPointerException(); }
			if (inputNeedsArgument != ParameterConfiguration.MANY_ARGUMENTS && inputNeedsArgument != ParameterConfiguration.ONE_ARGUMENT && inputNeedsArgument != ParameterConfiguration.NO_ARGUMENTS) {throw new IllegalArgumentException(String.format("Expected value are '%s', '%s' or '%s'. ", ParameterConfiguration.NO_ARGUMENTS, ParameterConfiguration.ONE_ARGUMENT, ParameterConfiguration.MANY_ARGUMENTS));}			
			this.needsArgument = inputNeedsArgument;
		}

		public void setExpectedValueType(Class<TYPE> inputExpectedValueClass) {
			this.expectedValueClass = inputExpectedValueClass;
		}

		@Override
		public Maybe<Class<TYPE>> getExpectedValueClass() {
			return Maybe.just(this.expectedValueClass);
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			
			sb.append(DefaultParameter.class.getSimpleName());
			sb.append(" [");
			sb.append("'longForm': ");
			sb.append(longForm);
			sb.append(", 'shortForm': ");
			sb.append(shortForm);
			sb.append(", 'required': ");
			sb.append(required);
			sb.append(", 'needsArgument': ");
			sb.append(needsArgument);
			sb.append(", 'expectedClassType': ");
			sb.append((expectedValueClass != null ? expectedValueClass.getSimpleName() : "none"));
			sb.append("]");
			
			
			return sb.toString();
		}
		
	}
}
