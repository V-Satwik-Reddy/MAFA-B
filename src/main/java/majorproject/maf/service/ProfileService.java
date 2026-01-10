package majorproject.maf.service;

import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.exception.auth.UserNotFoundException;
import majorproject.maf.model.User;
import majorproject.maf.repository.StockRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

        private final UserRepository userRepo;
        private final StockRepository stockRepo;
        private final UserCacheService userCacheService;

        public ProfileService(UserRepository userRepo, StockRepository stockRepo, UserCacheService userCacheService) {
            this.userCacheService = userCacheService;
            this.stockRepo = stockRepo;
            this.userRepo = userRepo;
        }

        public UserDto getProfile(String email) {

            return userCacheService.getCachedUser(email);
        }

        public UserDto updateProfile(UserDto userDto) {
        User existingUser = userRepo.findByEmail(userDto.getEmail());
        existingUser.setUsername(userDto.getUsername());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setBalance(userDto.getBalance());
        try {
            userRepo.save(existingUser);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user profile: " + e.getMessage());
        }
        UserDto updatedUserDto = new UserDto(existingUser.getUsername(), existingUser.getEmail(), existingUser.getPhone(), existingUser.getBalance(), existingUser.getId());
        userCacheService.cacheUser(updatedUserDto);
        return updatedUserDto;
    }

        public double getBalance(String email) {
            return userCacheService.getCachedUser(email).getBalance();
    }

        public List<Share> getUserHoldings(String email) {
            UserDto user = userCacheService.getCachedUser(email);
            return stockRepo.findByUserId(user.getId()).stream().map(
                    stock -> new Share(stock.getSymbol(), stock.getShares())
            ).toList();
        }
}
