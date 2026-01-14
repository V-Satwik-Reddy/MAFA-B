package majorproject.maf.service;

import majorproject.maf.dto.request.ProfileRequest;
import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.exception.InvalidProfileDetailsException;
import majorproject.maf.model.UserProfile;
import majorproject.maf.model.enums.EmploymentStatus;
import majorproject.maf.model.enums.Gender;
import majorproject.maf.model.enums.SalaryRange;
import majorproject.maf.repository.StockRepository;
import majorproject.maf.repository.UserProfileRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

        private final UserProfileRepository userProfileRepository;
        private final StockRepository stockRepo;
        private final UserCacheService userCacheService;
        private final UserRepository userRepo;
        public ProfileService(StockRepository stockRepo, UserCacheService userCacheService, UserRepository userRepo, UserProfileRepository userProfileRepository) {
            this.userProfileRepository = userProfileRepository;
            this.userCacheService = userCacheService;
            this.stockRepo = stockRepo;
            this.userRepo = userRepo;
        }

        public ProfileRequest getProfile(int usedId) {
            UserProfile userProfile = userProfileRepository.findByUserId(usedId);
            return new ProfileRequest(userProfile.getFirstName(), userProfile.getLastName(),
                    userProfile.getDateOfBirth(), userProfile.getGender().toString(), userProfile.getUsername(),
                    userProfile.getAddressLine1(), userProfile.getAddressLine2(), userProfile.getCity(),
                    userProfile.getState(), userProfile.getPostalCode(), userProfile.getCountry(),
                    userProfile.getJobTitle(), userProfile.getCompanyName(), userProfile.getIndustry(),
                    userProfile.getEmploymentStatus().toString(), userProfile.getSalaryRange().toString()
            );
        }

        public List<Share> getUserHoldings(String email) {
            UserDto user = userCacheService.getCachedUser(email);
            return stockRepo.findByUserId(user.getId()).stream().map(
                    stock -> new Share(stock.getSymbol(), stock.getShares())
            ).toList();
        }

        public void createProfile(ProfileRequest request, int userId) {
            try {
                UserProfile userProfile = verifyAndBuildProfile(request, userId);
                userProfileRepository.save(userProfile);
            }catch(Exception ex) {
                throw new InvalidProfileDetailsException("Profile creation failed: " + ex.getMessage());
            }
    }

        private UserProfile verifyAndBuildProfile(ProfileRequest request, int userId) {
            Gender gender= switch (request.getGender().toLowerCase()) {
                case "male" -> Gender.MALE;
                case "female" -> Gender.FEMALE;
                default -> Gender.PREFER_NOT_TO_SAY;
            };
            EmploymentStatus employmentStatus= switch (request.getEmploymentStatus().toLowerCase()) {
                case "employed" -> EmploymentStatus.EMPLOYED;
                case "self-employed" -> EmploymentStatus.SELF_EMPLOYED;
                case "student" -> EmploymentStatus.STUDENT;
                case "retired" -> EmploymentStatus.RETIRED;
                default -> EmploymentStatus.UNEMPLOYED;
            };
            SalaryRange salaryRange= switch (request.getSalaryRange().toLowerCase()) {
                case "50k_100k" -> SalaryRange.BETWEEN_50K_100K;
                case "100k_150k" -> SalaryRange.BETWEEN_100K_200K;
                case "150k_200k" -> SalaryRange.ABOVE_200K;
                default -> SalaryRange.BELOW_50K;
            };
        return new UserProfile(userId,userRepo.findById(userId),request.getFirstName(), request.getLastName(), request.getDateOfBirth(), gender, request.getUsername(),
                request.getAddressLine1(), request.getAddressLine2(), request.getCity(), request.getState(), request.getPostalCode(), request.getCountry(),
                request.getJobTitle(), request.getCompanyName(), request.getIndustry(), employmentStatus, salaryRange
                );
    }
}
