package de.adorsys.ledgers.middleware.test.client;

//import de.adorsys.ledgers.deposit.api.service.DepositAccountInitService;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountStatusTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountTypeTO;
import de.adorsys.ledgers.middleware.api.domain.account.UsageTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.*;
import de.adorsys.ledgers.middleware.client.rest.*;
import de.adorsys.ledgers.util.domain.CustomPageImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static de.adorsys.ledgers.middleware.api.domain.um.AccessTypeTO.OWNER;
import static de.adorsys.ledgers.middleware.api.domain.um.UserRoleTO.CUSTOMER;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.OK;


//@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = LedgersClientApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT) //TODO Remove or refactor this!
//@ActiveProfiles("h2")
//@TestMethodOrder(MethodOrderer.Alphanumeric.class)
//@Disabled("Use WebSecurityConfigKeycloak")
class LedgersClientIT {

   /* @Autowired TODO REMOVE ME AS ILLEGAL! RestClient should not depend on services!
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
    private static UserTO USER_1 = getUser("01", CUSTOMER);
    private static UserTO USER_2 = getUser("02", CUSTOMER);
    private static AccountDetailsTO ACCOUNT_1 = getAccountDetails(USER_1.getLogin());
    private static AccountDetailsTO ACCOUNT_2 = getAccountDetails(USER_2.getLogin());

    @BeforeEach
    void setUp() {
        depositAccountInitService.initConfigData();
    }

    @Test
    void a_createAdmin() {
        // Given
        UserTO adminUser = new UserTO("admin", "admin@ledgers.ldg", "12345");

        // When
        ResponseEntity<Void> response = appMgmtRestClient.admin(adminUser);

        // Then
        assertEquals(OK, response.getStatusCode());
        //assertFalse(response.getBody().getAccess_token().isEmpty());
    }

    @Test
    void b_createUserAndAccount() {
        userMgmtRestClient.register("francis.pouatcha", "fpo@mail.ledgers", "12345", CUSTOMER);
        AccountDetailsTO a = new AccountDetailsTO();
        a.setIban("DE69760700240340283600");
        a.setAccountStatus(AccountStatusTO.ENABLED);
        a.setCurrency(Currency.getInstance("EUR"));
        a.setProduct("Cash24");
        a.setAccountType(AccountTypeTO.CASH);
        a.setUsageType(UsageTypeTO.PRIV);
        a.setName("Francis Pouatcha");

        *//*ResponseEntity<SCALoginResponseTO> response = userMgmtRestClient.authorise("francis.pouatcha", "12345", CUSTOMER);
        SCALoginResponseTO scaLoginResponseTO = response.getBody();
        BearerTokenTO token = scaLoginResponseTO.getBearerToken();*//* //TODO FIX ME!!!

        //authHeader.setAccessToken(token.getAccess_token());
        //ResponseEntity<Void> createDepositAccountResponse = accountRestClient.createDepositAccount(a);
        //assertEquals(OK, createDepositAccountResponse.getStatusCode());
        authHeader.setAccessToken(null);
    }

    @Test
    void c_createBranch() {
        // When
        ResponseEntity<UserTO> responseBranchCreation = userMgmtStaffRestClient.register(BRANCH_LOGIN, BRANCH);

        // Then
        assertEquals(OK, responseBranchCreation.getStatusCode());
        assertFalse(responseBranchCreation.getBody().getId().isEmpty());
    }

    *//*@Test
    void d_loginAsBranch() {
        // When
        ResponseEntity<SCALoginResponseTO> branchLogin = userMgmtStaffRestClient.login(new UserCredentialsTO(BRANCH.getLogin(), PIN, UserRoleTO.STAFF));
        assertEquals(OK, branchLogin.getStatusCode());
        authHeader.setAccessToken(branchLogin.getBody().getBearerToken().getAccess_token());
    }*//*

    @Test
    void e_createTwoUsersAsBranch() {
        // When
        ResponseEntity<UserTO> user1Response = userMgmtStaffRestClient.createUser(USER_1);
        ResponseEntity<UserTO> user2Response = userMgmtStaffRestClient.createUser(USER_2);
        assertEquals(OK, user1Response.getStatusCode());
        assertEquals(OK, user2Response.getStatusCode());

        USER_1 = user1Response.getBody();
        USER_2 = user2Response.getBody();

        // Then
        assertFalse(USER_1.getId().isEmpty());
        assertFalse(USER_2.getId().isEmpty());
        assertTrue(user1Response.getBody().getUserRoles().contains(CUSTOMER));
        assertTrue(user1Response.getBody().getUserRoles().contains(CUSTOMER));
    }

    @Test
    void f_createAccountsForUsersAsBranch() {
        // Given
        ResponseEntity<Void> accountResponse1 = accountMgmtStaffRestClient.createDepositAccountForUser(USER_1.getId(), ACCOUNT_1);
        ResponseEntity<Void> accountResponse2 = accountMgmtStaffRestClient.createDepositAccountForUser(USER_2.getId(), ACCOUNT_2);
        assertEquals(OK, accountResponse1.getStatusCode());
        assertEquals(OK, accountResponse2.getStatusCode());

        // When
        //Check Users Accesses and Branch Accesses are correct
        ResponseEntity<CustomPageImpl<UserTO>> allBranchUsersResponse = userMgmtStaffRestClient.getBranchUsersByRoles(Collections.singletonList(CUSTOMER), "", false, 0, Integer.MAX_VALUE);
        checkUsersListAccesses(allBranchUsersResponse, OK, 2, 1);

        ResponseEntity<UserTO> branchResponse = userMgmtRestClient.getUser();
        checkUserResponse(branchResponse, OK, 2);

        //refresh accounts
        List<AccountDetailsTO> accounts = accountMgmtStaffRestClient.getListOfAccounts().getBody();
        ACCOUNT_1 = accounts.get(0);
        ACCOUNT_2 = accounts.get(1);
    }

    @Test
    void g_addAccessToAccountOfAnotherUser() {
        //Add user2 access to account of user1
        ResponseEntity<Void> addAccessResponse = userMgmtStaffRestClient.updateAccountAccessForUser(USER_2.getId(), getAccountAccess(ACCOUNT_1, AccessTypeTO.DISPOSE, 30));
        assertEquals(OK, addAccessResponse.getStatusCode());

        //Check user2 has 2 Accesses
        ResponseEntity<UserTO> user2Response = userMgmtStaffRestClient.getBranchUserById(USER_2.getId());
        checkUserResponse(user2Response, OK, 2);
    }

    @Test
    void h_updateAccessOfUser() {
        // Given
        //Update AccountAccess for user2 for account1
        AccountAccessTO modifiedAccess = getAccountAccess(ACCOUNT_1, OWNER, 80);
        ResponseEntity<Void> modifiedAccessResponse = userMgmtStaffRestClient.updateAccountAccessForUser(USER_2.getId(), modifiedAccess);
        assertEquals(OK, modifiedAccessResponse.getStatusCode());

        // When
        //Check user2 still has 2 Accesses and access to account1 is modified to the one we set in previous call
        ResponseEntity<UserTO> user2Response = userMgmtStaffRestClient.getBranchUserById(USER_2.getId());
        checkUserResponse(user2Response, OK, 2);
        Optional<AccountAccessTO> accessFromResponse = user2Response.getBody().getAccountAccesses().stream().filter(a -> a.getIban().equals(ACCOUNT_1.getIban())).findFirst();

        // Then
        assertTrue(accessFromResponse.isPresent());

        AccountAccessTO actual = accessFromResponse.get();
        modifiedAccess.setId(actual.getId());

        assertEquals(modifiedAccess, actual);
        authHeader.setAccessToken(null);
    }

    private AccountAccessTO getAccountAccess(AccountDetailsTO account, AccessTypeTO accessType, int scaWeight) {
        AccountAccessTO access = new AccountAccessTO();
        access.setIban(account.getIban());
        access.setCurrency(Currency.getInstance("EUR"));
        access.setAccessType(accessType);
        access.setScaWeight(scaWeight);
        access.setAccountId(account.getId());
        return access;
    }

    private void checkUsersListAccesses(ResponseEntity<CustomPageImpl<UserTO>> allBranchUsersResponse, HttpStatus expectedStatus, int listSize, int qtyAccesses) {
        assertEquals(expectedStatus, allBranchUsersResponse.getStatusCode());
        CustomPageImpl<UserTO> usersList = allBranchUsersResponse.getBody();
        assertEquals(listSize, usersList.getTotalElements());
        assertTrue(usersList.getContent().stream().allMatch(u -> u.getAccountAccesses().size() == qtyAccesses));
    }

    private void checkUserResponse(ResponseEntity<UserTO> response, HttpStatus expectedStatus, int qtyAccesses) {
        assertEquals(expectedStatus, response.getStatusCode());
        assertEquals(qtyAccesses, response.getBody().getAccountAccesses().size());
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
        user.setId(login);
        user.setUserRoles(Collections.singletonList(role));
        return user;
    }

    private static String generateIban(String ibanEnding) {
        if (isDigitsAndSize(BRANCH_LOGIN, 8) && isDigitsAndSize(ibanEnding, 2)) {
            BigInteger totalNr = new BigInteger(LedgersClientIT.BANK_CODE + BRANCH_LOGIN + ibanEnding + "131400");
            String checkSum = String.format("%02d", 98 - totalNr.remainder(BigInteger.valueOf(97)).intValue());
            return "DE" + checkSum + LedgersClientIT.BANK_CODE + BRANCH_LOGIN + ibanEnding;
        }
        throw new IllegalArgumentException(String.format("Inappropriate data for IBAN creation %s %s", BRANCH_LOGIN, ibanEnding));
    }

    private static boolean isDigitsAndSize(String toCheck, int size) {
        String regex = "\\d+";
        return toCheck.matches(regex) && toCheck.length() == size;
    }*/
}
