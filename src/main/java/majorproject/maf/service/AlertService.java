package majorproject.maf.service;

import majorproject.maf.dto.request.AlertRequestDto;
import majorproject.maf.dto.response.AlertResponseDto;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.exception.ResourseNotFoundException;
import majorproject.maf.model.Alert;
import majorproject.maf.model.StockPrice;
import majorproject.maf.model.enums.AlertCondition;
import majorproject.maf.model.enums.AlertStatus;
import majorproject.maf.repository.AlertRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public AlertService(AlertRepository alertRepository, EmailService emailService, UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
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
        alert.setUser(userRepository.getReferenceById(user.getId()));
        alert.setSymbol(alertRequestDto.getSymbol());
        alert.setChannel(alertRequestDto.getChannel());
        alert.setAlertCondition(alertRequestDto.getCondition());
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setTargetPrice(alertRequestDto.getTargetPrice());
        alert.setTriggeredAt(null);
        alertRepository.save(alert);
        return buildAlertResponse(alert);
    }

    public List<AlertResponseDto> getUserAlerts(Integer userId, AlertStatus status,int page, Integer size) {
        List<Alert> alerts;
        if(size!=null&&status!=null){
            PageRequest pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
            alerts=alertRepository.findByUserIdAndStatus(userId,status,pageable);
        }else if(size!=null){
            PageRequest pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());
            alerts=alertRepository.findByUserId(userId,pageable);
        }else if(status != null){
            alerts=alertRepository.findByUserIdAndStatus(userId,status);
        }else{
            alerts=alertRepository.findByUserId(userId);
        }
        List<AlertResponseDto> alertResponseDTOs=new ArrayList<>();
        for(Alert alert:alerts){
            alertResponseDTOs.add(buildAlertResponse(alert));
        }
        return alertResponseDTOs;
    }

    public AlertResponseDto deleteUserAlert(Integer userId,Long alertId) {
        Alert alert=alertRepository.findByUserIdAndId(userId,alertId);
        if(alert==null){
            throw new ResourseNotFoundException("Alert not found with id "+alertId);
        }
        if(alert.getStatus()==AlertStatus.TRIGGERED){
            throw new IllegalStateException("Cannot cancel an already triggered alert");
        }
        alert.setStatus(AlertStatus.CANCELLED);
        alertRepository.save(alert);
        return buildAlertResponse(alert);
    }

    @Async
    public void checkAlerts(Map<String,StockPrice> stockPrices) {
        List<Alert> alerts=alertRepository.findAllByStatus(AlertStatus.ACTIVE);
        for(Alert alert:alerts){
            StockPrice stockPrice=stockPrices.get(alert.getSymbol());
            if((alert.getAlertCondition()== AlertCondition.ABOVE&&stockPrice.getClose()>alert.getTargetPrice())||
            (alert.getAlertCondition()== AlertCondition.BELOW&&stockPrice.getClose()<alert.getTargetPrice())){
                alert.setStatus(AlertStatus.TRIGGERED);
                alert.setTriggeredAt(LocalDateTime.now());
                alertRepository.save(alert);
                emailService.sendAlertEmail(alert);
            }
        }
    }
}
