package majorproject.maf.model.user;


import jakarta.persistence.*;
import lombok.*;
import majorproject.maf.model.enums.EmploymentStatus;
import majorproject.maf.model.enums.Gender;
import majorproject.maf.model.enums.SalaryRange;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="user_profiles")
public class UserProfile {

    @Id
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")  // FK to user.id
    private User user;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender = Gender.PREFER_NOT_TO_SAY;
    private Double balance;
    @Column(unique = true)
    private String username;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private String jobTitle;
    private String companyName;
    private String industry;
    @Enumerated(EnumType.STRING)
    private EmploymentStatus employmentStatus = EmploymentStatus.UNEMPLOYED;
    @Enumerated(EnumType.STRING)
    private SalaryRange salaryRange = SalaryRange.BELOW_50K;

}

