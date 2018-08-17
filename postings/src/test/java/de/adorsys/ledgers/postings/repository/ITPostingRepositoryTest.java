package de.adorsys.ledgers.postings.repository;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.domain.PostingType;
import de.adorsys.ledgers.postings.utils.Ids;
import de.adorsys.ledgers.postings.utils.RecordHashHelper;
import de.adorsys.ledgers.tests.PostingsApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=PostingsApplication.class)
public class ITPostingRepositoryTest {

	ObjectMapper om = new ObjectMapper();
	
	@Autowired
	private PostingRepository postingRepository;
	
	@Test
	public void test_posting_hash() throws NoSuchAlgorithmException, JsonProcessingException {
		Posting posting = Posting.builder()
			.id(Ids.id())
			.recordUser("recUser")
			.oprId("oprId")
			.oprDetails("oprDetails")
			.pstTime(LocalDateTime.now())
			.pstType(PostingType.BAL_STMT)
			.build();
		
		Posting saved = postingRepository.save(posting);
		
		String writeValueAsString = om.writeValueAsString(saved);
		
		Posting found = postingRepository.findById(posting.getId()).orElse(null);
		String recHash = found.getRecordHash();

		String writeValueAsString2 = om.writeValueAsString(found);
		Assert.assertEquals(writeValueAsString, writeValueAsString2);
		
		RecordHashHelper recordHashHelper = new RecordHashHelper();
		found.setRecordHash(null);
		String computedRecHash = recordHashHelper.computeRecHash(found);
		
		Assert.assertEquals(recHash, computedRecHash);
	}
}
