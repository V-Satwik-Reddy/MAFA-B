package majorproject.maf.service;

import majorproject.maf.dto.UserDto;
import majorproject.maf.exception.auth.UserNotFoundException;
import majorproject.maf.model.User;
import majorproject.maf.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileService {

        private final UserRepository userRepo;

        public ProfileService(UserRepository userRepo) {
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
        // Don't modify email or password unless explicitly updated
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
}
