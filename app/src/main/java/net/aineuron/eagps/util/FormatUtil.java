package net.aineuron.eagps.util;

import android.support.annotation.NonNull;

import net.aineuron.eagps.model.database.RealmString;
import net.aineuron.eagps.model.database.order.Address;

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

	// Building up addresses from what we have
	@NonNull
	public static String formatDestinationAddress(Address destinationAddress, String workshopName) {
		String addressResult = "";
		if (destinationAddress != null) {
			if (workshopName != null) {
				addressResult += workshopName;
			}
			if (destinationAddress.getAddress().getStreet() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getStreet();
			}
			if (destinationAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getCity();
			}
			if (destinationAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += destinationAddress.getAddress().getZipCode();
			}
		}
		return addressResult;
	}


	@NonNull
	public static String formatClientAddress(Address clientAddress, String clientLocationComment) {
		String addressResult = "";
		if (clientAddress != null) {
			if (clientAddress.getAddress().getStreet() != null) {
				addressResult += clientAddress.getAddress().getStreet();
			}
			if (clientAddress.getAddress().getCity() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getCity();
			}
			if (clientAddress.getAddress().getZipCode() != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientAddress.getAddress().getZipCode();
			}
			if (clientLocationComment != null) {
				if (addressResult.length() > 0) {
					addressResult += ", ";
				}
				addressResult += clientLocationComment;
			}
		}
		return addressResult;
	}

}
