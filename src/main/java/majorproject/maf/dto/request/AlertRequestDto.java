package majorproject.maf.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.enums.AlertCondition;
import majorproject.maf.model.enums.Channel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlertRequestDto {

    private String symbol;

    private AlertCondition condition;

    private Double targetPrice;

    private Channel channel;

}
