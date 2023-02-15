/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

export class Amount {
  /** ISO 4217 currency code */
  currency: string;
  amount: number; // BigDecimal
}
