package net.aineuron.eagps.util;

import net.aineuron.eagps.model.database.RealmString;

import java.util.List;

/**
 * Created by Vit Veres on 14.08.2017
 * as a part of eagps project.
 */

public class FormatUtil {
	public static String formatEvent(List<RealmString> limits) {
		StringBuilder stringBuilder = new StringBuilder("");

		for (RealmString realmString : limits) {
			stringBuilder.append(realmString.getValue());
		}

		return stringBuilder.toString();
	}

}
