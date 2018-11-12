package de.adorsys.ledgers.postings.api.domain;

import java.time.LocalDateTime;

public class LedgerBO extends NamedBO {

    /*The attached chart of account.*/
    private ChartOfAccountBO coa;

    public ChartOfAccountBO getCoa() {
        return coa;
    }

    public void setCoa(ChartOfAccountBO coa) {
        this.coa = coa;
    }

    public LedgerBO() {
    }

    public LedgerBO(ChartOfAccountBO coa) {
        this.coa = coa;
    }

    public LedgerBO(String name, String id, LocalDateTime created, String userDetails, String shortDesc, String longDesc, ChartOfAccountBO coa) {
        super(name, id, created, userDetails, shortDesc, longDesc);
        this.coa = coa;
    }
}
