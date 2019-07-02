package de.adorsys.ledgers.postings.db.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters.LocalDateTimeConverter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@EqualsAndHashCode
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
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime recTime;
	
	/*Prospective time of execution of this note.*/
	@Convert(converter=LocalDateTimeConverter.class)
	private LocalDateTime execTime;
	
	/*States if execution might occur before execution time.*/
	private Boolean prematureExc;
	
	/*States if repeated execution is allowed.*/
	private Boolean repeatedExec;
	
	/*Document the status of execution of this note.*/
	private String execStatus;

	public OpNote() {
		super();
	}

	public OpNote(String id, String recId, String type, String content, LocalDateTime recTime, LocalDateTime execTime,
			Boolean prematureExc, Boolean repeatedExec, String execStatus) {
		super();
		this.id = id;
		this.recId = recId;
		this.type = type;
		this.content = content;
		this.recTime = recTime;
		this.execTime = execTime;
		this.prematureExc = prematureExc;
		this.repeatedExec = repeatedExec;
		this.execStatus = execStatus;
	}

	@Override
	public String toString() {
		return "OpNote [id=" + id + ", recId=" + recId + ", type=" + type + ", content=" + content + ", recTime="
				+ recTime + ", execTime=" + execTime + ", prematureExc=" + prematureExc + ", repeatedExec="
				+ repeatedExec + ", execStatus=" + execStatus + "]";
	}
}
