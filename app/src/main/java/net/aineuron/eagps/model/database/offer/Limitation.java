package net.aineuron.eagps.model.database.offer;

/**
 * Created by Vit Veres on 31-May-17
 * as a part of Android-EAGPS project.
 */

public class Limitation {
	private boolean isExtendedDescription;
	private String limit;

	public boolean isExtendedDescription() {
		return isExtendedDescription;
	}

	public void setExtendedDescription(boolean extendedDescription) {
		isExtendedDescription = extendedDescription;
	}

	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}
}
