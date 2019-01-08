import {Amount} from "./amount.model";

export class Account {
  id: string;
  /** International Bank Account Number */
  iban: string;
  /** Basic Bank Account Number */
  bban: string;
  /** Primary Account Number */
  pan: string;
  maskedPan: string;
  /** Mobile Subscriber Integrated Services Digital Number */
  msisdn: string;
  /** ISO 4217 currency code */
  currency: string;
  name: string;
  product: string;
  accountType: AccountType;
  accountStatus: AccountStatus;
  bic: string;
  linkedAccounts: string;
  usageType: UsageType;
  details: string;
  balances: AccountBalance[];
}

export enum AccountType {
  CACC = "Current",  // Account used to post debits and credits when no specific account has been nominated
  CASH = "Cash Payment",  // Account used for the payment of cash
  CHAR = "Charges",  // Account used for charges if different from the account for payment
  CISH = "Cash Income",  // Account used for payment of income if different from the current cash account
  COMM = "Commission",  // Account used for commission if different from the account for payment
  CPAC = "Clearing Participant Settlement Account",  // Account used to post settlement debit and credit entries on behalf of a designated Clearing Participant
  LLSV = "Limited Liquidity Savings Account",  // Account used for savings with special interest and withdrawal terms
  LOAN = "Loan",  // Account used for loans
  MGLD = "Marginal Lending",  // Account used for a marginal lending facility
  MOMA = "Money Market ",  // Account used for money markets if different from the cash account
  NREX = "Non Resident External",  // Account used for non-resident external
  ODFT = "Overdraft",  // Account is used for overdrafts
  ONDP = "Overnight Deposit",  // Account used for overnight deposits
  OTHR = "Other Account",  // Account not otherwise specified
  SACC = "Settlement",  // Account used to post debit and credit entries, as a result of transactions cleared and settled through a specific clearing and settlement system
  SLRY = "Salary",  // Accounts used for salary payments
  SVGS = "Savings",  // Account used for savings
  TAXE = "Tax",  // Account used for taxes if different from the account for payment
  TRAN = "Transacting Account",  // A transacting account is the most basic type of bank account that you can get. The main difference between transaction and cheque accounts is that you usually do not get a cheque book with your transacting account and neither are you offered an overdraft facility
  TRAS = "Cash Trading",  // Account used for trading if different from the current cash account
}

export enum AccountStatus {
  ENABLED = "ENABLED",
  DELETED = "DELETED",
  BLOCKED = "BLOCKED",
}

export enum UsageType {
  PRIV = "PRIV",
  ORGA = "ORGA",
}

class AccountBalance {
  amount: Amount;
  balanceType: BalanceType;
  lastChange: Date; // LocalDateTime;
  referenceDate: Date; // LocalDate;
  lastCommittedTransaction: string;
}

export enum BalanceType {
  CLOSING_BOOKED = "CLOSING_BOOKED",
  EXPECTED = "EXPECTED",
  AUTHORISED = "AUTHORISED",
  OPENING_BOOKED = "OPENING_BOOKED",
  INTERIM_AVAILABLE = "INTERIM_AVAILABLE",
  FORWARD_AVAILABLE = "FORWARD_AVAILABLE",
  NONINVOICED = "NONINVOICED",
  AVAILABLE = "AVAILABLE",
}
