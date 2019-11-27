package de.adorsys.ledgers.deposit.api.service.mappers;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.domain.PostingBO;
import de.adorsys.ledgers.postings.api.domain.PostingLineBO;
import de.adorsys.ledgers.util.Ids;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface PostingMapper {
    @Mapping(target = "oprId", expression = "java( id())")
    @Mapping(target = "id", expression = "java(null)")
    @Mapping(target = "recordUser", source = "userName")
    @Mapping(target = "oprTime", source = "pstTime")
    @Mapping(target = "pstTime", source = "pstTime")
    @Mapping(target = "valTime", source = "pstTime")
    @Mapping(target = "oprSrc", source = "paymentId")
    @Mapping(target = "ledger", source = "ledger")
    @Mapping(target = "pstType", expression = "java(de.adorsys.ledgers.postings.api.domain.PostingTypeBO.BUSI_TX)")
    @Mapping(target = "pstStatus", expression = "java(de.adorsys.ledgers.postings.api.domain.PostingStatusBO.POSTED)")
    PostingBO buildPosting(LocalDateTime pstTime, String paymentId, String oprDetails, LedgerBO ledger, String userName);

    @Mapping(target = "id", source = "lineId")
    @Mapping(target = "account", source = "ledgerAccount")
    @Mapping(target = "details", source = "lineDetails")
    PostingLineBO buildPostingLine(String lineDetails, LedgerAccountBO ledgerAccount, BigDecimal debitAmount, BigDecimal creditAmount, String subOprSrcId, String lineId);

    @SuppressWarnings("PMD.ShortMethodName")
    default String id() {
        return Ids.id();
    }
}
