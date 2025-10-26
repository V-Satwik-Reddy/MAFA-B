package majorproject.maf.service;

import majorproject.maf.dto.UserDto;
import majorproject.maf.exception.auth.UserNotFoundException;
import majorproject.maf.model.User;
import majorproject.maf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
            return new UserDto(user.getUsername(), user.getEmail());
        }

        public List<UserDto> getUsers() {
            List<User> users = userRepo.findAll();
            List<UserDto> userDtos = new ArrayList<>();
            for(User user : users) {
                userDtos.add(new UserDto(user.getUsername(), user.getEmail()));
            }
            return userDtos;
        }
}
