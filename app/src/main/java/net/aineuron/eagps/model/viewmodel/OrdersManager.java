package net.aineuron.eagps.model.viewmodel;

import net.aineuron.eagps.Pref_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * Created by Vit Veres on 05-Jun-17
 * as a part of Android-EAGPS project.
 */

@EBean(scope = EBean.Scope.Singleton)
public class OrdersManager {

	@Pref
	Pref_ pref;


}
