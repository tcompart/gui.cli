package de.compart.gui.cli.test;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.compart.gui.cli.Parameter;
import de.compart.gui.cli.ParameterFactory;
import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;
import de.compart.gui.cli.ParameterResult;
import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public class IntegrationTest {

	private static final Logger log = LoggerFactory.getLogger(IntegrationTest.class);
	
	@Test
	public void create() {
		
		final String argumentString = "--help --optional -o 'output.txt' -v SOME_VALUE";
		final String[] args = argumentString.split(" ");
		
		assertThat(argumentString, notNullValue());
		assertThat(args, notNullValue());
		assertThat(args.length, not(0));
		
		Parameter<?> verbose = ParameterFactory.createParameter('v', "verbose", ParameterConfiguration.NO_ARGUMENTS, ParameterConfiguration.OPTIONAL);
		Parameter<?> debug = ParameterFactory.createParameter(ParameterFactory.NO_SHORT_OPTION, "debug", ParameterConfiguration.NO_ARGUMENTS, ParameterConfiguration.OPTIONAL);
		Parameter<?> help = ParameterFactory.createParameter('h', "help", ParameterConfiguration.NO_ARGUMENTS, ParameterConfiguration.OPTIONAL);
		Parameter<String> output = ParameterFactory.createParameter('o', "output", ParameterConfiguration.ONE_ARGUMENT, String.class, ParameterConfiguration.REQUIRED);
		Parameter<?> defaultParameter = ParameterFactory.DEFAULT_PARAMETER;
		
		ParameterResult result = ParameterFactory.parse(args, 
				verbose,
				debug,
				help,
				output
			);
		
		assertThat(result.hasParameter(verbose), is(true));
		assertThat(result.hasParameter(debug), is(false));
		assertThat(result.hasParameter(help), is(true));
		assertThat(result.hasParameter(output), is(true));
		assertThat(result.hasParameter(defaultParameter), is(true));
		
		assertThat(result.getValue(output), notNullValue());
		assertThat(result.getValue(output).isJust(), is(true));
		assertThat(result.getValue(output).getValue(), is("'output.txt'"));
		
		final Maybe<Collection> maybeCollection = result.getValue(defaultParameter, Collection.class );
		
		assertThat(maybeCollection.isJust(), is(true));
		assertThat(maybeCollection.getValue(), notNullValue());
		
		final Iterator<Object> itr = maybeCollection.getValue().iterator();
		assertThat((String) itr.next(), is("--optional"));
		assertThat((String) itr.next(), is("SOME_VALUE"));
		
	}

	@Test(expected=IllegalArgumentException.class)
	public void expectIllegalArgument() {
		ParameterFactory.createParameter('v', "verbose", ParameterConfiguration.NO_ARGUMENTS, Boolean.class, ParameterConfiguration.OPTIONAL);		
	}
	
}
