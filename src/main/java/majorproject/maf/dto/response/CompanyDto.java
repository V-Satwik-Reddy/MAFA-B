package majorproject.maf.dto.response;

import lombok.*;
import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String symbol;
    private String name;
    private SectorDto sector;
}
