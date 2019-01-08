package de.adorsys.ledgers.postings.db.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import de.adorsys.ledgers.util.Ids;

@Entity
public class OperationDetails {
    @Id
    private String id;
    @Lob
    private String opDetails;

    public OperationDetails() {
    }

    public OperationDetails(String opDetails) {
        this.id = Ids.id();
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
