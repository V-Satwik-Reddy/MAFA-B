package majorproject.maf.dto.response;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Profile {

    private String email;
    @Column(unique = true)
    private String username;
    private String phone;
    private Double balance;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;


    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private String jobTitle;
    private String companyName;
    private String industry;
    private String employmentStatus;
    private String salaryRange;

}