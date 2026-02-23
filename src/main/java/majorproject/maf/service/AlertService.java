package majorproject.maf.service;

import majorproject.maf.dto.request.AlertRequestDto;
import majorproject.maf.dto.response.AlertResponseDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.Alert;
import majorproject.maf.model.enums.AlertStatus;
import majorproject.maf.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public AlertResponseDto createAlert(UserDto user,AlertRequestDto alertRequestDto) {
        Alert alert = new Alert();
        alert.setUserEmail(user.getEmail());
        alert.setUserId(user.getId());
        alert.setSymbol(alertRequestDto.getSymbol());
        alert.setChannel(alertRequestDto.getChannel());
        alert.setAlertCondition(alertRequestDto.getCondition());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setTargetPrice(alertRequestDto.getTargetPrice());
        alertRepository.save(alert);
        return buildAlertResponse(alert);
    }

    public List<AlertResponseDto> getUserAlerts(Integer userId, AlertStatus status) {
        List<Alert> alerts;
        if(status==null){
            alerts=alertRepository.findByUserId(userId);
        }else{
            alerts=alertRepository.findByUserIdAndStatus(userId,status);
        }
        List<AlertResponseDto> alertResponseDtos=new ArrayList<>();
        for(Alert alert:alerts){
            alertResponseDtos.add(buildAlertResponse(alert));
        }
        return alertResponseDtos;
    }

    public AlertResponseDto deleteUserAlert(Integer userId,Long alertId) {
        Alert alert=alertRepository.findByUserIdAndId(userId,alertId);
        alert.setStatus(AlertStatus.CANCELLED);
        alertRepository.save(alert);
        return buildAlertResponse(alert);
    }

    private AlertResponseDto buildAlertResponse(Alert alert) {
        AlertResponseDto alertResponseDto=new AlertResponseDto();
        alertResponseDto.setId(alert.getId());
        alertResponseDto.setSymbol(alert.getSymbol());
        alertResponseDto.setStatus(alert.getStatus());
        alertResponseDto.setTargetPrice(alert.getTargetPrice());
        alertResponseDto.setCondition(alert.getAlertCondition());
        alertResponseDto.setCreatedAt(alert.getCreatedAt());
        alertResponseDto.setChannel(alert.getChannel());
        return alertResponseDto;
    }
}
