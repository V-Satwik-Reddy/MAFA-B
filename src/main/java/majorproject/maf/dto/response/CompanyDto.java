package majorproject.maf.dto.response;
import lombok.*;
import majorproject.maf.model.CompanyMaster;

@Getter
@Setter
@NoArgsConstructor
public class CompanyDto {
    private Long id;
    private String symbol;
    private String name;

    public CompanyDto(CompanyMaster company) {
        this.id = company.getId();
        this.symbol = company.getSymbol();
        this.name = company.getName();
    }
}
