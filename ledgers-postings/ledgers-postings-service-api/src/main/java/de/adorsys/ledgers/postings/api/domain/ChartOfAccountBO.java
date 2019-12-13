package de.adorsys.ledgers.postings.api.domain;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * All accounts used by a company are defined in a chart of account.
 *
 * @author fpo
 */
@NoArgsConstructor
@EqualsAndHashCode
public class ChartOfAccountBO extends NamedBO {
    public ChartOfAccountBO(String name, String id, LocalDateTime created, String userDetails, String shortDesc, String longDesc) {
        super(name, id, created, userDetails, shortDesc, longDesc);
    }

    public ChartOfAccountBO(String ledgerName) {
        this.setUserDetails("System");
        this.setName(ledgerName);
        this.setShortDesc("COA: " + ledgerName);
    }
}
