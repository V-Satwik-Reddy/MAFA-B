package majorproject.maf.dto.response;
import lombok.*;
import majorproject.maf.model.CompanyMaster;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    private String symbol;
    private String name;

}
