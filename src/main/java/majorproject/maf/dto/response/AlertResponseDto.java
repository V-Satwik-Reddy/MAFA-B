package majorproject.maf.dto.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import majorproject.maf.model.enums.AlertCondition;
import majorproject.maf.model.enums.AlertStatus;
import majorproject.maf.model.enums.Channel;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlertResponseDto {

    private Long id;

    private String symbol;

    private AlertCondition condition;

    private Double targetPrice;

    private AlertStatus status;

    private Channel channel;

    private LocalDateTime createdAt;

}
