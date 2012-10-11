package de.compart.gui.cli;

import de.compart.common.Maybe;
import de.compart.gui.cli.ParameterFactory.ParameterConfiguration;

public interface Parameter<TYPE> {

	String getShortForm();

	String getLongForm();

	Maybe<String> getDescription();

	boolean isRequired();

	ParameterConfiguration isArgumentRequired();
}