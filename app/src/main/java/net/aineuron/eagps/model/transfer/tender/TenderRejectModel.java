package net.aineuron.eagps.model.transfer.tender;

/**
 * Created by Petr Kresta, AiNeuron s.r.o. on 23.10.2017.
 */

public class TenderRejectModel {
    private Long entityId;
    private Long rejectReason;
    private String rejectComment;
    private String userName;

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(Long rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getRejectComment() {
        return rejectComment;
    }

    public void setRejectComment(String rejectComment) {
        this.rejectComment = rejectComment;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
