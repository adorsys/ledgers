package de.adorsys.ledgers.postings.impl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.postings.api.domain.LedgerAccountBO;
import de.adorsys.ledgers.postings.api.domain.LedgerBO;
import de.adorsys.ledgers.postings.api.exception.ChartOfAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerAccountNotFoundException;
import de.adorsys.ledgers.postings.api.exception.LedgerNotFoundException;
import de.adorsys.ledgers.postings.api.service.LedgerService;
import de.adorsys.ledgers.postings.db.domain.AccountCategory;
import de.adorsys.ledgers.postings.db.domain.BalanceSide;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.domain.Ledger;
import de.adorsys.ledgers.postings.db.domain.LedgerAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerAccountRepository;
import de.adorsys.ledgers.postings.db.repository.LedgerRepository;
import de.adorsys.ledgers.postings.db.utils.PostingRepositoryFunctions;
import de.adorsys.ledgers.postings.impl.converter.LedgerAccountMapper;
import de.adorsys.ledgers.postings.impl.converter.LedgerMapper;
import de.adorsys.ledgers.util.Ids;

@RunWith(MockitoJUnitRunner.class)
public class LedgerServiceImplTest {
	private static final LocalDateTime DATE_TIME = LocalDateTime.now();
	private static final String USER_NAME = "Mr. Jones";
	private static final ChartOfAccount COA = new ChartOfAccount(Ids.id(), DATE_TIME, USER_NAME,
			"Some short description", "Some long description", "COA");
	private static final Ledger LEDGER = new Ledger(Ids.id(), DATE_TIME, "User", "Some short description",
			"Some long description", "Ledger", COA);
	private static final LedgerAccount LEDGER_ACCOUNT = new LedgerAccount(Ids.id(), DATE_TIME, "User",
			"Some short description", "Some long description", USER_NAME, LEDGER, null, COA, BalanceSide.Cr,
			AccountCategory.AS);
	private static final LedgerMapper ledgerMapper = new LedgerMapper();
	private static final LedgerAccountMapper ledgerAccountMapper = new LedgerAccountMapper();

	@InjectMocks
	private LedgerService ledgerService = new LedgerServiceImpl(ledgerMapper, ledgerAccountMapper);

	@Mock
	private ChartOfAccountRepository chartOfAccountRepository;
	@Mock
	private LedgerRepository ledgerRepository;
	@Mock
	private LedgerAccountRepository ledgerAccountRepository;
	@Mock
	private Principal principal;

	@Test
	public void new_ledger_must_produce_id_created_user_copy_other_fields()
			throws LedgerNotFoundException, ChartOfAccountNotFoundException {
		when(chartOfAccountRepository.findById(COA.getId())).thenReturn(Optional.of(COA));
		when(principal.getName()).thenReturn(USER_NAME);
		when(ledgerRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		// When
		LedgerBO result = ledgerService.newLedger(ledgerMapper.toLedgerBO(LEDGER));
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isNotNull();
		assertThat(result.getCreated()).isNotNull();
		assertThat(result.getUserDetails()).isEqualTo(USER_NAME);
		assertThat(result.getName()).isEqualTo(LEDGER.getName());
		assertThat(result.getShortDesc()).isEqualTo(LEDGER.getShortDesc());
		assertThat(result.getLongDesc()).isEqualTo(LEDGER.getLongDesc());

		assertThat(result.getCoa()).isNotNull();
	}

	@Test
	public void new_ledgerAccount_must_produce_id_created_user_copy_other_fields() throws LedgerAccountNotFoundException, LedgerNotFoundException {
		when(ledgerAccountRepository.save(any())).thenAnswer(i -> i.getArgument(0));
		when(principal.getName()).thenReturn(USER_NAME);
		when(ledgerRepository.findById(any())).thenReturn(Optional.of(LEDGER));
		// When
		LedgerAccountBO result = ledgerService.newLedgerAccount(ledgerAccountMapper.toLedgerAccountBO(LEDGER_ACCOUNT));
		// Then
		assertThat(result).isNotNull();
		assertThat(result.getId()).isNotNull();
		assertThat(result.getCreated()).isNotNull();
		assertThat(result.getUserDetails()).isEqualTo(USER_NAME);
		assertThat(result.getName()).isEqualTo(LEDGER_ACCOUNT.getName());
		assertThat(result.getShortDesc()).isEqualTo(LEDGER_ACCOUNT.getShortDesc());
		assertThat(result.getLongDesc()).isEqualTo(LEDGER_ACCOUNT.getLongDesc());

		assertThat(result.getCoa()).isNotNull();
		assertThat(result.getLedger()).isNotNull();
	}

}
