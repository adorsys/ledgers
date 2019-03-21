package de.adorsys.ledgers.data.upload.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ControllerTest {
   /* private static final Logger logger = LoggerFactory.getLogger(ControllerTest.class);
    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    private RestTemplate tmpl = new RestTemplate();
    private final String testDataFile = "TestData.yml";
    private final String login = "testLogin";
    private final String pin = "12345";
    private DataPayload updateDataRequest = null;
    private List<AccountDetailsTO> accountsAtLedgers;*/

    @Test
    public void none(){}
    /*public void generateTestData() throws IOException {
        ResponseEntity<UserTO> register = register();
        ResponseEntity<SCALoginResponseTO> login = login();
        ResponseEntity<Resource> generateData = generateData(login.getBody().getBearerToken().getAccess_token());
        assertThat(generateData.getStatusCode(), is(HttpStatus.OK));
        DataPayload dataPayload = objectMapper.readValue(generateData.getBody().getInputStream(), DataPayload.class);
        assertThat(dataPayload.getAccounts().size(),is(51));
        assertThat(dataPayload.getUsers().size(),is(35));
    }

    private ResponseEntity<UserTO> register() {
        String url = "http://localhost:8088/staff-access/users/register";
        Map<String, String> uriParams = new HashMap<>();
        uriParams.put("branch", "111");
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                                               .queryParam("branch", "00000000");
        URI uri = builder.buildAndExpand(uriParams).toUri();
        UserTO user = new UserTO("111", "email", "111");
        user.setId("111");
        user.setUserRoles(Collections.singletonList(UserRoleTO.STAFF));
        try {
            return tmpl.postForEntity(uri, user, UserTO.class);
        } catch (RestClientException e){
            return ResponseEntity.ok(user);
        }
    }

    private ResponseEntity<SCALoginResponseTO> login() {
        String url = "http://localhost:8088/staff-access/users/login";
        UserCredentialsTO credentials = new UserCredentialsTO("111", "111", UserRoleTO.STAFF);
        return tmpl.postForEntity(url, credentials, SCALoginResponseTO.class);
    }

    private ResponseEntity<Resource> generateData(String token) {
        MultiValueMap<String, String> headers = getHeaders(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return tmpl.exchange("http://localhost:8088/staff-access/generate", HttpMethod.GET, request, Resource.class);
    }

    private MultiValueMap<String, String> getHeaders(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");
        return headers;
    }*/

    /*@Test
    public void test() {
        tmpl.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        //Read file
        MultipartFile file = getFile();
        assertThat("Assert byte array is not null", file != null, is(true));
        //Parse byte Array
        updateDataRequest = mapToRequestData(file);
        assertThat("Assert parsing is successful", updateDataRequest != null, is(true));
        //Create TPP
        UserTO user = createTpp();
        //Login TPP
        String token = loginTpp(user);
        assertThat("Check if TPP is logged in", token == null, is(false));
        //Send Update Request to Ledgers
        sendUpdateRequest(token, file);
        //Check if data is in Ledgers and is consistent
        checkData(token);
    }

    private void checkData(String token) {
        assertThat("Assert Users are present", checkIfUsersAreCreated(token), is(true));
        assertThat("Assert accounts are present", checkIfAccountsAreCreated(token), is(true));
        assertThat("Assert balances are correct", checkIfBalancesAreUpToDate(token), is(true));
    }

    private boolean checkIfBalancesAreUpToDate(String token) {
        return updateDataRequest.getBalancesList().stream()
                       .allMatch(b -> accountsAtLedgers.stream()
                                              .filter(a -> a.getIban().equals(b.getIban())).findFirst()
                                              .map(a -> isBalanceUpToDate(token, a.getId(), b))
                                              .orElse(false));
    }

    private boolean isBalanceUpToDate(String token, String accountId, AccountBalance balance) {
        MultiValueMap<String, String> headers = getHeaders(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<AccountDetailsTO> exchange = tmpl.exchange("http://localhost:8088/staff-access/accounts/" + accountId, HttpMethod.GET, request, AccountDetailsTO.class);
            assertThat("getBalances() is OK", exchange.getStatusCodeValue() == 200, is(true));
            return exchange.getBody().getBalances().get(0).getAmount().getAmount().compareTo(balance.getAmount()) == 0;

        } catch (RestClientException e) {
            logger.error("Could not get Accounts list");
            return false;
        }
    }

    private boolean checkIfUsersAreCreated(String token) {
        MultiValueMap<String, String> headers = getHeaders(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<UserTO>> reference = new ParameterizedTypeReference<List<UserTO>>() {
        };
        List<UserTO> users = null;
        try {
            ResponseEntity<List<UserTO>> exchange = tmpl.exchange("http://localhost:8088/staff-access/users?roles=CUSTOMER", HttpMethod.GET, request, reference);
            assertThat("getUsers() is OK", exchange.getStatusCodeValue() == 200, is(true));
            users = exchange.getBody();
        } catch (RestClientException e) {
            logger.error("Could not get Accounts list");
            users = Collections.emptyList();
        }
        final List<UserTO> usrs = users;

        //Check if all Users are created
        return updateDataRequest.getUsers()
                       .stream().anyMatch(user -> !isMissingUser(user, usrs));
    }

    private boolean isMissingUser(UserTO user, List<UserTO> usrs) {
        return usrs.stream()
                       .noneMatch(u -> u.getLogin().equals(user.getLogin()));
    }

    private boolean checkIfAccountsAreCreated(String token) {
        MultiValueMap<String, String> headers = getHeaders(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ParameterizedTypeReference<List<AccountDetailsTO>> reference = new ParameterizedTypeReference<List<AccountDetailsTO>>() {
        };
        List<AccountDetailsTO> accounts = null;
        try {
            ResponseEntity<List<AccountDetailsTO>> exchange = tmpl.exchange("http://localhost:8088/staff-access/accounts", HttpMethod.GET, request, reference);
            assertThat("getListOfAccounts() is OK", exchange.getStatusCodeValue() == 200, is(true));
            accounts = exchange.getBody();
        } catch (RestClientException e) {
            logger.error("Could not get Accounts list");
            accounts = Collections.emptyList();
        }
        final List<AccountDetailsTO> accs = accounts;
        accountsAtLedgers = accounts;
        //Check if all Accounts are created
        return updateDataRequest.getAccounts().stream()
                       .anyMatch(account -> !isMissingAccount(account, accs));
    }

    private boolean isMissingAccount(AccountDetailsTO account, List<AccountDetailsTO> accs) {
        return accs.stream()
                       .noneMatch(a -> a.getIban().equals(account.getIban()));
    }

    private void sendUpdateRequest(String token, MultipartFile requestBody) {
        MultiValueMap<String, String> headers = getHeaders(token);
        HttpEntity<MultipartFile> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Void> response = tmpl.postForEntity("http://localhost:8088/staff-access/upload", request, Void.class);
        assertThat("Update request result is OK", response.getStatusCode() == HttpStatus.OK, is(true));
    }

    private MultiValueMap<String, String> getHeaders(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", "application/json");
        return headers;
    }

    private String loginTpp(UserTO user) {
        try {
            UserCredentialsTO credentials = new UserCredentialsTO(user.getLogin(), user.getPin(), user.getUserRoles().stream().findFirst().get());
            return tmpl.postForEntity("http://localhost:8088/staff-access/users/login", credentials, SCALoginResponseTO.class).getBody().getBearerToken().getAccess_token();
        } catch (RestClientException e) {
            logger.error("An error occurred during login: {}", user.getLogin(), e.getLocalizedMessage());
            return null;
        }
    }

    private MultipartFile getFile() {
        InputStream resource = ClassLoader.getSystemResourceAsStream(testDataFile);
        assertThat("Read file is OK", resource != null, is(true));
        try {
         return new CommonsMultipartFile(new DiskFileItem("name", "yml",false, testDataFile,100000000,new InputStreamResource(resource).getFile()));
            //IOUtils.toByteArray(resource);
        } catch (IOException e) {
            logger.error("Could not read file, message is: \n ", e.getLocalizedMessage());
            return null;
        }
    }

    private UserTO createTpp() {
        String branch = "TestBranch";
        String url = "http://localhost:8088/staff-access/users/register?branch=" + branch;

        UserTO user = new UserTO(login, "test@email.com", pin);
        user.setUserRoles(Collections.singleton(UserRoleTO.STAFF));
        ResponseEntity<UserTO> responseEntity = null;
        try {
            responseEntity = tmpl.postForEntity(url, user, UserTO.class);
        } catch (RestClientException e) {
            if (e.getMessage().contains("occupied")) {
                logger.error("Branch is already created");
                return user;
            }
            logger.error(e.getLocalizedMessage());
            //TODO add check here if no connection
        }
        return Optional.ofNullable(responseEntity)
                       .map(HttpEntity::getBody)
                       .orElse(user);
    }

    private DataPayload mapToRequestData(MultipartFile file) {
        try {
            return objectMapper.readValue(file.getInputStream(), DataPayload.class);
        } catch (IOException e) {
            logger.error("Could not parse data from byte array");
            return null;
        }*/
}
