package majorproject.maf.model.serving;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_master")
@Getter @Setter
public class CompanyMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String symbol;   //

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id")
    private SectorMaster sector;
}

