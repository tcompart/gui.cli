package de.compart.gui.cli;

import java.io.File;

import de.uni_leipzig.asv.clarin.common.tuple.Maybe;

public class ClassCastHelperFactory {

	protected ClassCastHelperFactory() {
		// TODO Auto-generated constructor stub
	}
	
	public static <TYPE> Maybe<TYPE> castObject(final Class<TYPE> classToBeCastTo, final String originalInputString) {
		
		if (classToBeCastTo.isAssignableFrom(File.class)) {
			return Maybe.just(classToBeCastTo.cast(new File(originalInputString)));
		} else if (classToBeCastTo.isAssignableFrom(String.class)) {
			return Maybe.just(classToBeCastTo.cast(originalInputString));
		} else if (classToBeCastTo.isAssignableFrom(Integer.class)) {
			return Maybe.just(classToBeCastTo.cast(Integer.parseInt(originalInputString)));
		}
		
		
		return Maybe.nothing();
	}

}
