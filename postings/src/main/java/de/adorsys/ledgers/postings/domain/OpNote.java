package de.adorsys.ledgers.postings.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@ToString
@AllArgsConstructor
public class OpNote {
	
	/*Id of this note*/
	@Id
	private String id;
	
	/*Id of the containing posting record.*/
	private String recId;

	/*This is the type of the note. A note can be a simple comment, a task, a reminder...*/
	private String type;
	
	/*This is the content of the note. Format might be dependent of the note type.*/
	private String content;
	
	/*Time of recording of this note.*/
	private String recTime;
	
	/*Prospective time of execution of this note.*/
	private String execTime;
	
	/*States if execution might occur before execution time.*/
	private Boolean prematureExc;
	
	/*States if repeated execution is allowed.*/
	private Boolean repeatedExec;
	
	/*Document the status of execution of this note.*/
	private String execStatus;

}
