package de.adorsys.ledgers.postingserver.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.adorsys.ledgers.postings.domain.Posting;
import de.adorsys.ledgers.postings.exception.NotFoundException;
import de.adorsys.ledgers.postings.service.PostingService;

@Api(description = "Posting Controller. Handles postings (journal entries).")
@RestController
public class PostingController {
	
	@Autowired
	private PostingService postingService;
	
	/**
	 * @param posting
	 * @return
	 */
    @ApiOperation(value = "Creates a new Posting.",
            notes = "- If there is another posting with the same operation id\n" +
                    "- The new posting can only be stored is the oldest is not part of a closed accounting period.\n" +
                    "- A posting time can not be older than a closed accounting period. ")
	@PostMapping(path = "/postings")
	public ResponseEntity<Posting> newPosting(Posting posting){
		Posting newPosting;
		try {
			newPosting = postingService.newPosting(posting);
		} catch (NotFoundException e) {
			throw new ResourceNotFoundException(e.getMessage());
		}
		return ResponseEntity.ok(newPosting);
	}
	
	/**
	 * @param id
	 * @return
	 */
    @ApiOperation(value = "Listing all postings associated with this operation id.")
	@GetMapping(path = "postings", params={"oprId"})
	public ResponseEntity<List<Posting>> findPostingsByOperationId(@RequestParam(required=true, name="oprId")String oprId){
		List<Posting> list = postingService.findPostingsByOperationId(oprId);
		return ResponseEntity.ok(list);
	}
}
