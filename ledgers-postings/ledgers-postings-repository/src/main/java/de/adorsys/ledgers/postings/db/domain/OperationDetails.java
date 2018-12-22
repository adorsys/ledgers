package de.adorsys.ledgers.postings.db.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.UUID;

@Entity
public class OperationDetails {
    @Id
    private String id;
    @Lob
    private String opDetails;

    public OperationDetails() {
    }

    public OperationDetails(String opDetails) {
        this.id = UUID.randomUUID().toString();
        this.opDetails = opDetails;
    }

    public OperationDetails(String id, String opDetails) {
        this.id = id;
        this.opDetails = opDetails;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOpDetails() {
        return opDetails;
    }

    public void setOpDetails(String opDetails) {
        this.opDetails = opDetails;
    }
}
