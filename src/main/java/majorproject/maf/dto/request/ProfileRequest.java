package majorproject.maf.dto.request;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;
    private String phone;
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

    @Override
    public String toString() {
        return "ProfileRequest{" +
                "addressLine1='" + addressLine1 + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", username='" + username + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", country='" + country + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", companyName='" + companyName + '\'' +
                ", industry='" + industry + '\'' +
                ", employmentStatus='" + employmentStatus + '\'' +
                ", salaryRange='" + salaryRange + '\'' +
                '}';
    }

    private String employmentStatus;
    private String salaryRange;

}