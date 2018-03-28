package net.aineuron.eagps.util;

import net.aineuron.eagps.model.database.RealmString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vit Veres on 14.08.2017
 * as a part of eagps project.
 */

public class FormatUtil {
	public static String formatEvent(List<RealmString> limits) {
		StringBuilder stringBuilder = new StringBuilder("");

		List<RealmString> filteredLimits = new ArrayList<>();
		for (RealmString rs : limits) {
			if (rs != null && !rs.getValue().isEmpty()) {
				filteredLimits.add(rs);
			}
		}

		for (int i = 0; i < filteredLimits.size(); i++) {
			RealmString realmString = filteredLimits.get(i);

			stringBuilder.append(realmString.getValue());
			if (i < filteredLimits.size() - 1) {
				stringBuilder.append(System.getProperty("line.separator"));
			}

		}

		return stringBuilder.toString();
	}

}
