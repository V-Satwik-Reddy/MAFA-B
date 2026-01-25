package majorproject.maf.dto.response;

import lombok.*;
import majorproject.maf.model.SectorMaster;

@Getter
@Setter
@AllArgsConstructor
public class SectorDto {
    private Long id;
    private String name;

    public SectorDto(SectorMaster sector) {
        this.id = sector.getId();
        this.name = sector.getName();
    }
}
