package de.adorsys.ledgers.postings.impl.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import de.adorsys.ledgers.postings.api.domain.ChartOfAccountBO;
import de.adorsys.ledgers.postings.api.service.ChartOfAccountService;
import de.adorsys.ledgers.postings.db.domain.ChartOfAccount;
import de.adorsys.ledgers.postings.db.repository.ChartOfAccountRepository;
import de.adorsys.ledgers.postings.impl.converter.ChartOfAccountMapper;


@RunWith(MockitoJUnitRunner.class)
public class ChartOfAccountServiceImplTest {
    private static final String USER_NAME = "TestName";
    private static final ChartOfAccountMapper chartOfAccountMapper = new ChartOfAccountMapper();
	
    @InjectMocks
    private ChartOfAccountService chartOfAccountService = new ChartOfAccountServiceImpl(chartOfAccountMapper);
    @Mock
    private ChartOfAccountRepository chartOfAccountRepo;
    @Mock
    private Principal principal;

    @Test
    public void new_coa_must_produce_id_created_user_and_copy_name_shortdesc_longdesc() {
    	ChartOfAccount coa = new ChartOfAccount(null, null, null, "shortDesc", "longDesc", "coaName");
    			
        when(principal.getName()).thenReturn(USER_NAME);
        when(chartOfAccountRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        //When
        ChartOfAccountBO chartOfAccountBO = chartOfAccountMapper.toChartOfAccountBO(coa);
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
