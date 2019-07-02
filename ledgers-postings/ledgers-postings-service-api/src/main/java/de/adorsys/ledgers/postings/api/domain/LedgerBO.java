package de.adorsys.ledgers.postings.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LedgerBO extends NamedBO {

    /*The attached chart of account.*/
    private ChartOfAccountBO coa;

    public LedgerBO() {
    }

    public LedgerBO(String name, String id, LocalDateTime created, String userDetails, String shortDesc, String longDesc, ChartOfAccountBO coa) {
        super(name, id, created, userDetails, shortDesc, longDesc);
        this.coa = coa;
    }
}
