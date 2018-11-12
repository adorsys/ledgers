package de.adorsys.ledgers.postings.api.domain;

import java.time.LocalDateTime;

/**
 * All accounts used by a company are defined in a chart of account.
 *
 * @author fpo
 */
public class ChartOfAccountBO extends NamedBO {
    public ChartOfAccountBO() {
    }

    public ChartOfAccountBO(String name, String id, LocalDateTime created, String userDetails, String shortDesc, String longDesc) {
        super(name, id, created, userDetails, shortDesc, longDesc);
    }
}
