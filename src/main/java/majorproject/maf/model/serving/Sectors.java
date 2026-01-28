package majorproject.maf.model.serving;

import jakarta.persistence.*;
import lombok.*;
import majorproject.maf.model.user.UserPreferences;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sectors_preferences")
public class Sectors {
    @jakarta.persistence.Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserPreferences user;

    @ManyToOne(fetch = FetchType.LAZY)
    private SectorMaster sector;
}
