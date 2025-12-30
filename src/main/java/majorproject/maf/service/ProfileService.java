package majorproject.maf.service;

import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.exception.auth.UserNotFoundException;
import majorproject.maf.model.User;
import majorproject.maf.repository.StockRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

        private final UserRepository userRepo;
        private final StockRepository stockRepo;

        public ProfileService(UserRepository userRepo, StockRepository stockRepo) {
            this.stockRepo = stockRepo;
            this.userRepo = userRepo;
        }

        public UserDto getProfile(String email) {
            User user = userRepo.findByEmail(email);
            if(user == null) {
                throw new UserNotFoundException("No such user found with email: " + email);
            }
            return new UserDto(user.getUsername(), user.getEmail(),user.getPhone(),user.getBalance());
        }

        public UserDto updateProfile(UserDto userDto) {
        User existingUser = userRepo.findByEmail(userDto.getEmail());

        if (existingUser == null) {
            throw new UserNotFoundException("User not found with email: " + userDto.getEmail());
        }
        existingUser.setUsername(userDto.getUsername());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setBalance(userDto.getBalance());
        try {
            userRepo.save(existingUser);
        } catch (Exception e) {
            e.printStackTrace();
            Throwable root = e.getCause();
            while (root != null) {
                System.err.println("Root cause: " + root);
                root = root.getCause();
            }
        }

        return new UserDto(
                existingUser.getUsername(),
                existingUser.getEmail(),
                existingUser.getPhone(),
                existingUser.getBalance()
        );
    }

        public double getBalance(String email) {
        User user = userRepo.findByEmail(email);
        if(user == null) {
            throw new UserNotFoundException("No such user found with email: " + email);
        }
        return user.getBalance();
    }

        public List<Share> getUserHoldings(String email) {
            User user = userRepo.findByEmail(email);
            return stockRepo.findByUserId(user.getId()).stream().map(
                    stock -> new Share(stock.getSymbol(), stock.getShares())
            ).toList();
        }
}
