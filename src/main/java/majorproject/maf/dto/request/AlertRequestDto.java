package majorproject.maf.dto.request;

import lombok.*;
import majorproject.maf.model.enums.AlertCondition;
import majorproject.maf.model.enums.Channel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertRequestDto {

    private String symbol;

    private AlertCondition condition;

    private Double targetPrice;

    private Channel channel;

}
