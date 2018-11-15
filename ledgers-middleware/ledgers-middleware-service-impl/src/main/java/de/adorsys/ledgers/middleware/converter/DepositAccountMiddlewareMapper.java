package de.adorsys.ledgers.middleware.converter;

import java.util.List;

import org.mapstruct.Mapper;

import de.adorsys.ledgers.deposit.api.domain.DepositAccountBO;
import de.adorsys.ledgers.middleware.service.domain.account.DepositAccountTO;

@Mapper(componentModel = "spring", uses = CurrencyMiddlewareMapper.class)
public interface DepositAccountMiddlewareMapper {

    DepositAccountBO toDepositAccountBO(DepositAccountTO depositAccount);

    List<DepositAccountBO> toDepositAccountListBO(List<DepositAccountTO> list);

    DepositAccountTO toDepositAccountTO(DepositAccountBO depositAccount);

    List<DepositAccountTO> toDepositAccountListTO(List<DepositAccountBO> list);
}
