package de.adorsys.ledgers.mockbank.simple.data.test;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import de.adorsys.ledgers.mockbank.simple.data.test.api.Constants;

/**
 * 
 * Enable the Mock Bank Simple Data Test Beans registration.
 *
 * @author bwa
 *
 */
@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
@Import({
	MockBankSimpleDataTestConfiguration.class
})
@Profile(Constants.PROFILE_DATA_TEST)
public @interface EnableMockBankSimpleDataTest {

	@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
	@Target(value = {java.lang.annotation.ElementType.TYPE})
	@Documented
	@interface MockBankSimpleDataTestResource {

	}
}
