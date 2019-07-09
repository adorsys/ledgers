package de.adorsys.ledgers.middleware.test.client;

import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountStatusTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.UsageTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.client.rest.*;
import de.adorsys.ledgers.middleware.rest.exception.ConflictRestException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO.OWNER;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.CUSTOMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = LedgersClientApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("h2")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LedgersClientIT {
    @Autowired
    private AccountRestClient accountRestClient;
    @Autowired
    private UserMgmtRestClient userMgmtRestClient;
    @Autowired
    private AppMgmtRestClient appMgmtRestClient;
    @Autowired
    private AuthRequestInterceptor authHeader;
    @Autowired
    private DepositAccountInitService depositAccountInitService;
    @Autowired
    private UserMgmtStaffRestClient userMgmtStaffRestClient;
    @Autowired
    private AccountMgmtStaffRestClient accountMgmtStaffRestClient;

    private static final String BRANCH_LOGIN = "12345678";
    private static final String PIN = "12345";
    private static final String BANK_CODE = "76070024";

    private static final UserTO BRANCH = getUser(BRANCH_LOGIN, UserRoleTO.STAFF);
    private static UserTO USER_1 = getUser("1", CUSTOMER);
    private static UserTO USER_2 = getUser("2", CUSTOMER);
    private static AccountDetailsTO ACCOUNT_1 = getAccountDetails(USER_1.getLogin());
    private static AccountDetailsTO ACCOUNT_2 = getAccountDetails(USER_2.getLogin());

    @Before
    public void setUp() {
        depositAccountInitService.initConfigData();
    }

    @Test
    public void a_createAdmin() {
        UserTO adminUser = new UserTO("admin", "admin@ledgers.ldg", "12345");
        ResponseEntity<BearerTokenTO> response = appMgmtRestClient.admin(adminUser);
        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getAccess_token()).isNotBlank();
    }

    @Test
    public void b_createUserAndAccount() throws ConflictRestException {
        ResponseEntity<UserTO> user = userMgmtRestClient.register("francis.pouatcha", "fpo@mail.ledgers", "12345", CUSTOMER);
        AccountDetailsTO a = new AccountDetailsTO();
        a.setIban("DE69760700240340283600");
        a.setAccountStatus(AccountStatusTO.ENABLED);
        a.setCurrency(Currency.getInstance("EUR"));
        a.setProduct("Cash24");
        a.setAccountType(AccountTypeTO.CASH);
        a.setUsageType(UsageTypeTO.PRIV);
        a.setName("Francis Pouatcha");

        ResponseEntity<SCALoginResponseTO> response = userMgmtRestClient.authorise("francis.pouatcha", "12345", CUSTOMER);
        SCALoginResponseTO scaLoginResponseTO = response.getBody();
        BearerTokenTO token = scaLoginResponseTO.getBearerToken();

        authHeader.setAccessToken(token.getAccess_token());
        ResponseEntity<Void> createDepositAccountResponse = accountRestClient.createDepositAccount(a);
        Assert.assertTrue(OK.equals(createDepositAccountResponse.getStatusCode()));
        authHeader.setAccessToken(null);
    }

    @Test
    public void c_createBranch() {
        ResponseEntity<UserTO> responseBranchCreation = userMgmtStaffRestClient.register(BRANCH_LOGIN, BRANCH);
        assertThat(responseBranchCreation.getStatusCode()).isEqualTo(OK);
        assertThat(responseBranchCreation.getBody().getId()).isNotBlank();
    }

    @Test
    public void d_loginAsBranch() {
        ResponseEntity<SCALoginResponseTO> branchLogin = userMgmtStaffRestClient.login(new UserCredentialsTO(BRANCH.getLogin(), PIN, UserRoleTO.STAFF));
        assertThat(branchLogin.getStatusCode()).isEqualTo(OK);
        authHeader.setAccessToken(branchLogin.getBody().getBearerToken().getAccess_token());
    }

    @Test
    public void e_createTwoUsersAsBranch() {
        ResponseEntity<UserTO> user1Response = userMgmtStaffRestClient.createUser(USER_1);
        ResponseEntity<UserTO> user2Response = userMgmtStaffRestClient.createUser(USER_2);
        assertThat(user1Response.getStatusCode()).isEqualTo(OK);
        assertThat(user2Response.getStatusCode()).isEqualTo(OK);

        USER_1 = user1Response.getBody();
        USER_2 = user2Response.getBody();
        assertThat(USER_1.getId()).isNotBlank();
        assertThat(USER_2.getId()).isNotBlank();
        assertThat(user1Response.getBody().getUserRoles().contains(CUSTOMER)).isTrue();
        assertThat(user1Response.getBody().getUserRoles().contains(CUSTOMER)).isTrue();
    }

    @Test
    public void f_createAccountsForUsersAsBranch() {
        ResponseEntity<Void> accountResponse1 = accountMgmtStaffRestClient.createDepositAccountForUser(USER_1.getId(), ACCOUNT_1);
        ResponseEntity<Void> accountResponse2 = accountMgmtStaffRestClient.createDepositAccountForUser(USER_2.getId(), ACCOUNT_2);
        assertThat(accountResponse1.getStatusCode()).isEqualTo(OK);
        assertThat(accountResponse2.getStatusCode()).isEqualTo(OK);

        //Check Users Accesses and Branch Accesses are correct
        ResponseEntity<List<UserTO>> allBranchUsersResponse = userMgmtStaffRestClient.getBranchUsersByRoles(Collections.singletonList(CUSTOMER));
        checkUsersListAccesses(allBranchUsersResponse, OK, 2, 1);

        ResponseEntity<UserTO> branchResponse = userMgmtRestClient.getUser();
        checkUserResponse(branchResponse, OK, 2);
    }

    @Test
    public void g_addAccessToAccountOfAnotherUser() {
        //Add user2 access to account of user1
        ResponseEntity<Void> addAccessResponse = userMgmtStaffRestClient.updateAccountAccessForUser(USER_2.getId(), getAccountAccess(ACCOUNT_1, AccessTypeTO.DISPOSE, 30));
        assertThat(addAccessResponse.getStatusCode()).isEqualTo(OK);

        //Check user2 has 2 Accesses
        ResponseEntity<UserTO> user2Response = userMgmtStaffRestClient.getBranchUserById(USER_2.getId());
        checkUserResponse(user2Response, OK, 2);
    }

    @Test
    public void h_updateAccessOfUser() {
        //Update AccountAccess for user2 for account1
        AccountAccessTO modifiedAccess = getAccountAccess(ACCOUNT_1, OWNER, 80);
        ResponseEntity<Void> modifiedAccessResponse = userMgmtStaffRestClient.updateAccountAccessForUser(USER_2.getId(), modifiedAccess);
        assertThat(modifiedAccessResponse.getStatusCode()).isEqualTo(OK);

        //Check user2 still has 2 Accesses and access to account1 is modified to the one we set in previous call
        ResponseEntity<UserTO> user2Response = userMgmtStaffRestClient.getBranchUserById(USER_2.getId());
        checkUserResponse(user2Response, OK, 2);
        Optional<AccountAccessTO> accessFromResponse = user2Response.getBody().getAccountAccesses().stream().filter(a -> a.getIban().equals(ACCOUNT_1.getIban())).findFirst();
        assertThat(accessFromResponse.isPresent()).isTrue();
        assertThat(accessFromResponse.get()).isEqualToIgnoringGivenFields(modifiedAccess, "id");
        authHeader.setAccessToken(null);
    }

    private AccountAccessTO getAccountAccess(AccountDetailsTO account, AccessTypeTO accessType, int scaWeight) {
        AccountAccessTO access = new AccountAccessTO();
        access.setIban(account.getIban());
        access.setAccessType(accessType);
        access.setScaWeight(scaWeight);
        return access;
    }

    private void checkUsersListAccesses(ResponseEntity<List<UserTO>> allBranchUsersResponse, HttpStatus expectedStatus, int listSize, int qtyAccesses) {
        assertThat(allBranchUsersResponse.getStatusCode()).isEqualTo(expectedStatus);
        List<UserTO> usersList = allBranchUsersResponse.getBody();
        assertThat(usersList.size()).isEqualTo(listSize);
        assertThat(usersList.stream().allMatch(u -> u.getAccountAccesses().size() == qtyAccesses)).isTrue();
    }

    private void checkUserResponse(ResponseEntity<UserTO> response, HttpStatus expectedStatus, int qtyAccesses) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody().getAccountAccesses().size()).isEqualTo(qtyAccesses);
    }

    private static AccountDetailsTO getAccountDetails(String login) {
        AccountDetailsTO details = new AccountDetailsTO();
        details.setId(login);
        details.setIban(generateIban(login));
        details.setCurrency(Currency.getInstance("EUR"));
        details.setName(login);
        details.setAccountType(AccountTypeTO.CASH);
        details.setAccountStatus(AccountStatusTO.ENABLED);
        details.setUsageType(UsageTypeTO.PRIV);
        return details;
    }

    private static UserTO getUser(String login, UserRoleTO role) {
        UserTO user = new UserTO(login, login + "@eml.de", PIN);
        user.setUserRoles(Collections.singletonList(role));
        return user;
    }


    private static String generateIban(String singleDigitToEndIban) {
        if (isDigitsAndSize(singleDigitToEndIban, 1)) {
            BigInteger totalNr = new BigInteger(BANK_CODE + BRANCH_LOGIN + 0 + singleDigitToEndIban + "131400");
            String checkSum = String.valueOf(98 - totalNr.remainder(BigInteger.valueOf(97)).intValue());
            if (checkSum.length() < 2) {
                checkSum = "0" + checkSum;
            }
            return "DE" + checkSum + BANK_CODE + 0 + singleDigitToEndIban;
        }
        throw new IllegalArgumentException(String.format("Inappropriate data for IBAN creation %s", singleDigitToEndIban));
    }

    private static boolean isDigitsAndSize(String toCheck, int size) {
        String regex = "\\d+";
        return toCheck.matches(regex) && toCheck.length() == size;
    }
}
