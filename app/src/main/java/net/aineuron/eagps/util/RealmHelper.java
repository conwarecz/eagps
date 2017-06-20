package net.aineuron.eagps.util;

import net.aineuron.eagps.Appl;

import io.realm.Realm;

/**
 * Created by Vit Veres on 12/7/2016
 * as a part of project Android-ModuleGenerator.
 */

public class RealmHelper {
	public static Realm getDb() {
		return Realm.getInstance(Appl.dbConfig);
	}
}
