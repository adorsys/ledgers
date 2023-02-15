/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.impl.converter;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import org.mapstruct.Mapper;

@Mapper
public interface ChartOfAccountMapper {

    ChartOfAccountBO toChartOfAccountBO(ChartOfAccount coa);

    ChartOfAccount toChartOfAccount(ChartOfAccountBO coa);
}
