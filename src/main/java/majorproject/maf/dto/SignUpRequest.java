package majorproject.maf.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpRequest {
    @NotBlank
    private String username;
    @NotBlank private String password;

    @Email
    @NotBlank private String email;
}
