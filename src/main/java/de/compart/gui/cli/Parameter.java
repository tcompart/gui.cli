package de.compart.gui.cli;

import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;
import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public interface Parameter<TYPE> {

	Maybe<String> getShortForm();
	
	Maybe<String> getLongForm();
	
	Maybe<String> getDescription();
	
	boolean isRequired();
	
	ParameterConfiguration needsArgument();
	
	Maybe<Class<TYPE>> getExpectedValueClass();
}
