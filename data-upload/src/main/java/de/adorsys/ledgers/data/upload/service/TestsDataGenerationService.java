package de.adorsys.ledgers.data.upload.service;

import de.adorsys.ledgers.data.upload.model.AccountBalance;
import de.adorsys.ledgers.data.upload.model.DataPayload;
import de.adorsys.ledgers.data.upload.utils.IbanGenerator;
import de.adorsys.ledgers.middleware.api.domain.account.AccountDetailsTO;
import de.adorsys.ledgers.middleware.api.domain.account.AccountReferenceTO;
import de.adorsys.ledgers.middleware.api.domain.general.AddressTO;
import de.adorsys.ledgers.middleware.api.domain.payment.*;
import de.adorsys.ledgers.middleware.api.domain.um.UserTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.BULK;
import static de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO.SINGLE;
import static java.util.function.Function.identity;

@Service
public class TestsDataGenerationService {
    private static final String TEST_CREDITOR_IBAN = "DE68370400440000000000";
    private Random random = new Random();

    public DataPayload generateData(DataPayload data, String branch, boolean generatePayments) {
        Map<String, AccountDetailsTO> detailsMap = getNotNullList(data.getAccounts()).stream()
                                                           .map(a -> generateDetails(a, branch))
                                                           .collect(Collectors.toMap(this::getLastTwoSymbols, identity()));
        data.setAccounts(new ArrayList<>(detailsMap.values()));
        List<AccountBalance> balances = getNotNullList(data.getBalancesList()).stream()
                                                .map(b -> generateBalances(b, branch, detailsMap))
                                                .collect(Collectors.toList());
        data.setBalancesList(balances);
        List<UserTO> users = getNotNullList(data.getUsers()).stream()
                                     .map(u -> generateUsers(u, branch, detailsMap))
                                     .collect(Collectors.toList());
        data.setUsers(users);
        data.setGeneratePayments(generatePayments);
        data.setBranch(branch);
        return data;
    }

    private <T> List<T> getNotNullList(List<T> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }

    private String getLastTwoSymbols(AccountDetailsTO a) {
        return a.getIban()
                       .substring(a.getIban().length() - 2);
    }

    private AccountBalance generateBalances(AccountBalance balance, String branch, Map<String, AccountDetailsTO> detailsMap) {
        String iban = getGeneratedIbanOrNew(balance.getIban(), branch, detailsMap);
        balance.setIban(iban);
        return balance;
    }

    private AccountDetailsTO generateDetails(AccountDetailsTO details, String branch) {
        String iban = generateIban(branch, details.getIban());
        details.setIban(iban);
        return details;
    }

    private UserTO generateUsers(UserTO user, String branch, Map<String, AccountDetailsTO> detailsMap) {
        user.setId(addBranchPrefix(branch, user.getId()));
        user.setEmail(addBranchPrefix(branch, user.getEmail()));
        user.setLogin(addBranchPrefix(branch, user.getLogin()));
        user.getScaUserData()
                .forEach(d -> d.setMethodValue(addBranchPrefix(branch, d.getMethodValue())));
        user.getAccountAccesses()
                .forEach(a -> a.setIban(getGeneratedIbanOrNew(a.getIban(), branch, detailsMap)));
        return user;
    }

    private String addBranchPrefix(String branch, String concatObj) {
        return branch + "_" + concatObj;
    }

    private String generateIban(String branch, String iban) {
        return IbanGenerator.generateIban(branch, iban);
    }

    private String getGeneratedIbanOrNew(String iban, String branch, Map<String, AccountDetailsTO> detailsMap) {
        return detailsMap.containsKey(iban)
                       ? detailsMap.get(iban).getIban()
                       : generateIban(branch, iban);
    }

    public Map<PaymentTypeTO, Object> generatePayments(AccountBalance balance, String branch) {
        EnumMap<PaymentTypeTO, Object> map = new EnumMap<>(PaymentTypeTO.class);
        map.put(SINGLE, generateSinglePayment(balance, branch));
        map.put(BULK, generateBulkPayment(balance, branch));
        return map;
    }

    private BulkPaymentTO generateBulkPayment(AccountBalance balance, String branch) {
        return new BulkPaymentTO(
                null,
                false,
                generateReference(balance.getIban(), balance.getCurrency()),
                LocalDate.now(),
                TransactionStatusTO.RCVD,
                Arrays.asList(generateSinglePayment(balance, branch), generateSinglePayment(balance, branch)),
                PaymentProductTO.INSTANT_SEPA
        );
    }

    private SinglePaymentTO generateSinglePayment(AccountBalance balance, String branch) {
        String endToEndId = generateEndToEndId(branch);
        return new SinglePaymentTO(
                null,
                endToEndId,
                generateReference(balance.getIban(), balance.getCurrency()),
                generateAmount(balance),
                generateReference(TEST_CREDITOR_IBAN, balance.getCurrency()),
                "adorsys GmbH & CO KG",
                "adorsys GmbH & CO KG",
                getTestCreditorAddress(),
                null,
                TransactionStatusTO.RCVD,
                PaymentProductTO.INSTANT_SEPA,
                LocalDate.now(),
                null
        );
    }

    private AmountTO generateAmount(AccountBalance balance) {
        AmountTO amount = new AmountTO();
        amount.setCurrency(balance.getCurrency());
        int balanceAmount = balance.getAmount().intValue();
        int maxAmount = balanceAmount < 3
                                ? 3
                                : balanceAmount / 3;
        int rand = random.nextInt(maxAmount - 1) + 1;
        amount.setAmount(BigDecimal.valueOf(rand));
        return amount;
    }

    private AddressTO getTestCreditorAddress() {
        return new AddressTO("Fürther Str.", "246a", "Nürnberg", "90429", "Germany");
    }

    private AccountReferenceTO generateReference(String iban, Currency currency) {
        AccountReferenceTO reference = new AccountReferenceTO();
        reference.setIban(iban);
        reference.setCurrency(currency);
        return reference;
    }

    private String generateEndToEndId(String branchId) {
        return String.join("_", branchId, String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) + random.nextInt(9), String.valueOf(ThreadLocalRandom.current().nextLong(10000, 99999)));
    }
}
