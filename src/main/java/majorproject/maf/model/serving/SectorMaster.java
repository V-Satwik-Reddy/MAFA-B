package majorproject.maf.model.serving;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sector_master")
@Getter @Setter
public class SectorMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;
}
