package de.adorsys.ledgers.postingserver.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.service.PostingService;

@RestController
public class PostingController {
	
	@Autowired
	private PostingService postingService;
	
	/**
	 * Creates a new Posting.
	 * 
	 * - If there is another posting with the same operation id
	 * 	- The new posting can only be stored is the oldest is not part of a closed accounting period.
	 * 	- A posting time can not be older than a closed accounting period. 
	 * 
	 * @param posting
	 * @return
	 */
	@PostMapping(path = "/postings")
	public ResponseEntity<Posting> newPosting(Posting posting){
		Posting newPosting = postingService.newPosting(posting);
		return ResponseEntity.ok(newPosting);
	}
	
	/**
	 * Listing all postings associated with this operation id.
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(path = "postings", params={"oprId"})
	public ResponseEntity<List<Posting>> findPostingsByOperationId(@RequestParam(required=true, name="oprId")String oprId){
		List<Posting> list = postingService.findPostingsByOperationId(oprId);
		return ResponseEntity.ok(list);
	}
}
