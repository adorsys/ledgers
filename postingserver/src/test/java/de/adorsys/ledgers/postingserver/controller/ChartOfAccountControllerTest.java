package de.adorsys.ledgers.postingserver.controller;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseOperation;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;

import de.adorsys.ledgers.postingserver.PostingserverApplication;


@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(classes={PostingserverApplication.class}, webEnvironment=WebEnvironment.RANDOM_PORT)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,DbUnitTestExecutionListener.class})
@DatabaseSetup("ChartOfAccountControllerTest-db-entries.xml")
@DatabaseTearDown(value={"ChartOfAccountControllerTest-db-entries.xml"}, type=DatabaseOperation.DELETE_ALL)
public class ChartOfAccountControllerTest {
	
	@LocalServerPort
    private int port;
	
	private String urlPrefix;
	
	@Before
	public void before(){
		urlPrefix = "http://localhost:" + port;
	}
	
	@Test
	public void test_load_coa() {
		get(urlPrefix + "/coas/ci8k8bcdTrCsi-F3sT3i-g").then().assertThat().body("name", equalTo("CoA"));
	}
}
