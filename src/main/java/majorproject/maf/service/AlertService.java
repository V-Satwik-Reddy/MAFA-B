package majorproject.maf.service;

import majorproject.maf.dto.request.AlertRequestDto;
import majorproject.maf.dto.response.AlertResponseDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.Alert;
import majorproject.maf.model.StockPrice;
import majorproject.maf.model.enums.AlertCondition;
import majorproject.maf.model.enums.AlertStatus;
import majorproject.maf.repository.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final EmailService emailService;

    public AlertService(AlertRepository alertRepository, EmailService emailService) {
        this.alertRepository = alertRepository;
        this.emailService = emailService;
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
        alertResponseDto.setTriggeredAt(alert.getTriggeredAt());
        return alertResponseDto;
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
        alert.setTriggeredAt(null);
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

    public void checkAlerts(Map<String,StockPrice> stockPrices) {
        List<Alert> alerts=alertRepository.findAllByStatus(AlertStatus.ACTIVE);
        for(Alert alert:alerts){
            StockPrice stockPrice=stockPrices.get(alert.getSymbol());
            if((alert.getAlertCondition()== AlertCondition.ABOVE&&stockPrice.getClose()>alert.getTargetPrice())||
                alert.getAlertCondition()== AlertCondition.BELOW&&stockPrice.getClose()<alert.getTargetPrice()){
                alert.setStatus(AlertStatus.TRIGGERED);
                alert.setTriggeredAt(LocalDateTime.now());
                alertRepository.save(alert);
                emailService.sendAlertEmail(alert);
            }
        }
    }
}
