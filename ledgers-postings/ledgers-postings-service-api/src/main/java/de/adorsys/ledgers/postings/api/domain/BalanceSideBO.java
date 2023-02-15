/*
 * Copyright (c) 2018-2023 adorsys GmbH and Co. KG
 * All rights are reserved.
 */

package de.adorsys.ledgers.postings.api.domain;

/**
 * The balance side describes the side of the balance where the account balance 
 * increases.
 * 
 * @author fpo
 *
 */
@SuppressWarnings("java:S115")
public enum BalanceSideBO {
	Dr,/*Indicates that the balance of this account increases in the debit.*/
	Cr,/*Indicates that the balance of this account increases in the credit*/
	DrCr;
}
