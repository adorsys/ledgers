package de.adorsys.ledgers.postings.impl.service;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.impl.converter.ChartOfAccountMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.security.Principal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ChartOfAccountServiceImplTest {
    private static final String USER_NAME = "TestName";
    private static final ChartOfAccountMapper mapper = new ChartOfAccountMapper();

    @InjectMocks
    private ChartOfAccountServiceImpl chartOfAccountService;
    @Mock
    private ChartOfAccountRepository chartOfAccountRepo;
    @Mock
    private Principal principal;
    @Mock
    ChartOfAccountMapper chartOfAccountMapper;

    @Test
    public void new_coa_must_produce_id_created_user_and_copy_name_shortdesc_longdesc() {
        ChartOfAccount coa = new ChartOfAccount("id", LocalDateTime.now(), "TestName", "shortDesc", "longDesc", "coaName");

        when(chartOfAccountRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(chartOfAccountMapper.toChartOfAccountBO(any())).thenReturn(mapper.toChartOfAccountBO(coa));
        //When
        ChartOfAccountBO chartOfAccountBO = mapper.toChartOfAccountBO(coa);
        ChartOfAccountBO result = chartOfAccountService.newChartOfAccount(chartOfAccountBO);
        //Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getUserDetails()).isEqualTo(USER_NAME);
        assertThat(result.getName()).isEqualTo(coa.getName());
        assertThat(result.getShortDesc()).isEqualTo(coa.getShortDesc());
        assertThat(result.getLongDesc()).isEqualTo(coa.getLongDesc());
    }
}
