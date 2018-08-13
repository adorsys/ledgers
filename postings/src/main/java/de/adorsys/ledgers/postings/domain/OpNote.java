package de.adorsys.ledgers.postings.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class OpNote {
	
	/*Id of this note*/
	@Id
	private String id;
	
	/*Id of the containing posting record.*/
	@Column(nullable=false, updatable=false)
	private String recId;

	/*This is the type of the note. A note can be a simple comment, a task, a reminder...*/
	private String type;
	
	/*This is the content of the note. Format might be dependent of the note type.*/
	private String content;
	
	/*Time of recording of this note.*/
	@Column(nullable=false, updatable=false)
	@CreatedDate
	private LocalDateTime recTime;
	
	/*Prospective time of execution of this note.*/
	private LocalDateTime execTime;
	
	/*States if execution might occur before execution time.*/
	private Boolean prematureExc;
	
	/*States if repeated execution is allowed.*/
	private Boolean repeatedExec;
	
	/*Document the status of execution of this note.*/
	private String execStatus;

}
