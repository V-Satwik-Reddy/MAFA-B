package majorproject.maf.service;

import majorproject.maf.model.User;
import majorproject.maf.model.UserDto;
import majorproject.maf.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepo;
    @Autowired
    PasswordEncoder passEnc;

    public UserDto signUp(User user){
        User dbUser=userRepo.findByEmail(user.getEmail());
        if(dbUser!=null){
            return null;
        }
        String hashedPassword = passEnc.encode(user.getPassword());
        user.setPassword(hashedPassword);
        userRepo.save(user);

        UserDto udto=new UserDto(user.getUsername(),user.getEmail());
        return udto;
    }

    public UserDto login(User user){
        User dbUser=userRepo.findByEmail(user.getEmail());
        if(dbUser==null){
            return null;
        }
        boolean isCorrect=passEnc.matches(user.getPassword(), dbUser.getPassword());
        if(!isCorrect){
            return new UserDto();
        }
        UserDto udto=new UserDto(user.getUsername(),user.getEmail());
        return udto;
    }
}
