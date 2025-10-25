package majorproject.maf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {

    @Id
    @Column(updatable = false, nullable = false)
    private final String id= UUID.randomUUID().toString();

    private String username;
    private String password;
    @NonNull private String email;

}
