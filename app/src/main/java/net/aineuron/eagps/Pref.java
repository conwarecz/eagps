package net.aineuron.eagps;

import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * Created by Vit Veres on 16-May-17
 * as a part of Android-EAGPS project.
 */

@SharedPref(value = SharedPref.Scope.UNIQUE)
public interface Pref {
	@DefaultString("")
	String userName();

	@DefaultString("")
	String token();

	@DefaultLong(-1)
	long selectedCar();
}
