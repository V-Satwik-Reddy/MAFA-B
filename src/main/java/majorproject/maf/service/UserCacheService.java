package majorproject.maf.service;

import majorproject.maf.dto.response.UserDto;
import majorproject.maf.model.User;
import majorproject.maf.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class UserCacheService {

    private final UserRepository userRepo;
    public UserCacheService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @CachePut(value = "USERS_CACHE", key = "#dto.email")
    public UserDto cacheUser(UserDto dto) {
        return dto;
    }

    @Cacheable(value="USERS_CACHE", key="#email")
    public UserDto getCachedUser(String email) {
        User user= userRepo.findByEmail(email);
        return new UserDto(user.getId(), user.getEmail(),user.getPhone(),user.getStatus());
    }

    @CacheEvict(value="USERS_CACHE", key="#email")
    public void evictCachedUser(String email) {
    }
}
