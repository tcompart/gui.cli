package de.compart.gui.cli.util;

import de.compart.common.Maybe;
import de.compart.common.reflect.ReflectionUtil;
import de.compart.gui.cli.Parameter;
import de.compart.gui.cli.ParameterFactory;

import java.io.File;
import java.util.*;

public class ClassCastHelperFactory {

	@SuppressWarnings( "unchecked" )
	public static <TYPE> Maybe<TYPE> getValueOutOfParameter( final Parameter<TYPE> parameter, final String... originalInputString ) {

		final List<Class<?>> classList = ReflectionUtil.getTypeArguments( Parameter.class, parameter.getClass() );
		if ( classList.isEmpty() ) {
			return Maybe.nothing();
		}
		final Class<TYPE> classToBeCastTo = ( Class<TYPE> ) classList.get( 0 );
		return getValueOutOfParameter( classToBeCastTo, parameter.isArgumentRequired(), originalInputString );
	}

	@SuppressWarnings( "unchecked" )
	public static <TYPE> Maybe<TYPE> getValueOutOfParameter( final Class<TYPE> classToBeCastTo, final ParameterFactory.ParameterConfiguration parameterConfiguration, final String... originalInputString ) {
		if ( classToBeCastTo == null ) {
			return ( Maybe<TYPE> ) Maybe.just( ( Object.class.cast( originalInputString ) ) );
		}

		switch ( parameterConfiguration ) {
			case NO_ARGUMENTS:
				return ( Maybe<TYPE> ) Maybe.just( Boolean.TRUE );
			case ONE_ARGUMENT:
				final String originalStringAlone = originalInputString[ 0 ];
				if ( classToBeCastTo.isAssignableFrom( File.class ) ) {
					return Maybe.just( classToBeCastTo.cast( new File( originalStringAlone ) ) );
				} else if ( classToBeCastTo.isAssignableFrom( Integer.class ) ) {
					return Maybe.just( classToBeCastTo.cast( Integer.parseInt( originalStringAlone ) ) );
				}
				return Maybe.just( classToBeCastTo.cast( originalStringAlone ) );
			case MANY_ARGUMENTS:
				if ( classToBeCastTo.isAssignableFrom( Set.class ) ) {
					return Maybe.just( classToBeCastTo.cast( new HashSet<String>() {{
						this.addAll( Arrays.asList( originalInputString ) );
					}} ) );
				}
				return Maybe.just( classToBeCastTo.cast( new ArrayList<String>() {{
					this.addAll( Arrays.asList( originalInputString ) );
				}} ) );
		}
		return Maybe.nothing();
	}
}