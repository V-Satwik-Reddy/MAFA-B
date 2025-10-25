package majorproject.maf.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class user {

    private final String id= UUID.randomUUID().toString();

    private String username;
    private String password;
    @NonNull private String email;

}
