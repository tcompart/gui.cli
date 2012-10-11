package de.compart.gui.cli.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItems;

import de.compart.common.Maybe;
import de.compart.gui.cli.Parameter;
import de.compart.gui.cli.ParameterFactory;
import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;
import de.compart.gui.cli.ParameterResult;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

public class IntegrationTest {

	private final String argumentString = "--help --optional -o 'output.txt' -v SOME_VALUE";
	private final String[] args = argumentString.split( " " );

	private Parameter<Boolean> verbose;
	private Parameter<Boolean> debug;
	private Parameter<Boolean> help;
	private Parameter<String> output;
	private Parameter<Collection> defaultParameter;

	@Before
	public void setUp() {
		verbose = ParameterFactory.<Boolean>createParameter( 'v', "verbose" ).setNeedsArgument( ParameterConfiguration.NO_ARGUMENTS ).build();
		debug = ParameterFactory.<Boolean>createParameter( ParameterFactory.NO_SHORT_OPTION, "debug" ).build();
		help = ParameterFactory.<Boolean>createParameter( 'h', "help" ).setNeedsArgument( ParameterConfiguration.NO_ARGUMENTS ).build();
		output = ParameterFactory.<String>createParameter( 'o', "output" ).setNeedsArgument( ParameterConfiguration.ONE_ARGUMENT ).setRequired( true ).build();
		defaultParameter = ParameterFactory.DEFAULT_PARAMETER;
	}

	@Test
	public void assertPreInitializedDependencies() {
		assertThat( argumentString, notNullValue() );
		assertThat( args.length, not( 0 ) );
	}

	@Test
	public void assertVerboseInResult() {

		final ParameterResult result = ParameterFactory.parse( args,
																	 verbose
		);

		assertThat( result.hasParameter( verbose ), is( true ) );
	}

	@Test
	public void assertDebugInResult() {

		final ParameterResult result = ParameterFactory.parse( args,
																	 debug
		);
		assertThat( result.hasParameter( debug ), is( false ) );
	}

	@Test
	public void assertHelpInResult() {

		final ParameterResult result = ParameterFactory.parse( args,
																	 help
		);
		assertThat( result.hasParameter( help ), is( true ) );
	}

	@Test
	public void assertOutputInResult() {

		final ParameterResult result = ParameterFactory.parse( args,
																	 output
		);
		assertThat( result.getValue( output, String.class ).get(), is( "'output.txt'" ) );
	}

	@Test
	public void assertDefaultParameterWithCorrectType() {
		final ParameterResult result = ParameterFactory.parse( args, verbose, debug, help, output );

		assertThat( result.getValue( defaultParameter ) != null, is( true ) );
	}

	@SuppressWarnings( "unchecked" ) // see test assertDefaultParameterWithCorrectType
	@Test
	public void assertDefaultParameterInResult() {

		final ParameterResult result = ParameterFactory.parse( args, verbose, debug, help, output );

		final Maybe<Collection> maybeCollection = result.getValue( defaultParameter, Collection.class );

		assertThat( ( Collection<Object> ) maybeCollection.get(), hasItems( new Object[]{"--optional", "SOME_VALUE"} ) );
		assertThat( maybeCollection.get().size(), is( 2 ) );

	}
}
