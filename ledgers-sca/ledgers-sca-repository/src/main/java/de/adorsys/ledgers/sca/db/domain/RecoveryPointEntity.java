package de.adorsys.ledgers.sca.db.domain;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "recovery_point")
public class RecoveryPointEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recovery_point_generator")
    @SequenceGenerator(name = "recovery_point_generator", sequenceName = "recovery_point_id_seq", allocationSize = 1)
    private Long id;
    private String description;
    @CreationTimestamp
    private LocalDateTime rollBackTime;
    private String branchId;
}
