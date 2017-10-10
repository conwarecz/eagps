package net.aineuron.eagps.model.transfer.tender;

import java.io.Serializable;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 09.10.2017.
 */

public class LimitationSerializable implements Serializable {
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