package de.adorsys.ledgers.postings.valuetypes;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostingHashInfo {

	private String id;

	private LocalDateTime recordTime;

	private String recordAntecedentId;

	private String recordAntecedentHash;

	private String recHash;
}
