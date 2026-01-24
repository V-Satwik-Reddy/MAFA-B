package majorproject.maf.service;

import majorproject.maf.dto.request.PreferenceRequest;
import majorproject.maf.dto.request.ProfileRequest;
import majorproject.maf.dto.response.Profile;
import majorproject.maf.dto.response.Share;
import majorproject.maf.dto.response.UserDto;
import majorproject.maf.exception.InvalidProfileDetailsException;
import majorproject.maf.model.*;
import majorproject.maf.model.enums.EmploymentStatus;
import majorproject.maf.model.enums.Gender;
import majorproject.maf.model.enums.SalaryRange;
import majorproject.maf.model.enums.UserStatus;
import majorproject.maf.repository.StockRepository;
import majorproject.maf.repository.UserPreferencesRepository;
import majorproject.maf.repository.UserProfileRepository;
import majorproject.maf.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileService {

        private final UserProfileRepository userProfileRepository;
        private final StockRepository stockRepo;
        private final UserRepository userRepo;
        private final UserPreferencesRepository userPreferencesRepository;

        public ProfileService(UserPreferencesRepository userPreferencesRepository,StockRepository stockRepo, UserRepository userRepo, UserProfileRepository userProfileRepository) {
            this.userProfileRepository = userProfileRepository;
            this.stockRepo = stockRepo;
            this.userRepo = userRepo;
            this.userPreferencesRepository = userPreferencesRepository;
        }

        public void createProfile(ProfileRequest request, int userId) {
            try {
                User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
                UserProfile userProfile = buildProfile(new UserProfile(),request);
                userProfile.setUser(user);
                user.setUserProfile(userProfile); // sync both sides
                user.setPhone(request.getPhone());
                user.setStatus(UserStatus.ACTIVE);
                user.setPhoneVerified(true);
                userProfile.setBalance(0.0);
                userProfileRepository.save(userProfile);
            }catch(Exception ex) {
                throw new InvalidProfileDetailsException("Profile creation failed: " + ex.getMessage());
            }
        }

        public void updateProfile(ProfileRequest request, int userId) {
            UserProfile existingProfile = userProfileRepository.findByUserId(userId);
            UserProfile userProfile = buildProfile(existingProfile, request);
            userProfileRepository.save(userProfile);
        }

        public Profile getProfile(UserDto userDto) {
            int userId = userDto.getId();
            UserProfile userProfile = userProfileRepository.findByUserId(userId);
            return new Profile(userDto.getEmail(), userProfile.getUsername(),userDto.getPhone(),userProfile.getBalance(), userProfile.getFirstName(), userProfile.getLastName(),
                    userProfile.getDateOfBirth(), userProfile.getGender().toString(),
                    userProfile.getAddressLine1(), userProfile.getAddressLine2(), userProfile.getCity(),
                    userProfile.getState(), userProfile.getPostalCode(), userProfile.getCountry(),
                    userProfile.getJobTitle(), userProfile.getCompanyName(), userProfile.getIndustry(),
                    userProfile.getEmploymentStatus().toString(), userProfile.getSalaryRange().toString()
            );
        }

        private UserProfile buildProfile(UserProfile profile, ProfileRequest request) {
            Gender gender = switch (request.getGender().toLowerCase()) {
                case "male" -> Gender.MALE;
                case "female" -> Gender.FEMALE;
                default -> Gender.PREFER_NOT_TO_SAY;
            };

            EmploymentStatus employmentStatus = switch (request.getEmploymentStatus().toLowerCase()) {
                case "employed" -> EmploymentStatus.EMPLOYED;
                case "self-employed" -> EmploymentStatus.SELF_EMPLOYED;
                case "student" -> EmploymentStatus.STUDENT;
                case "retired" -> EmploymentStatus.RETIRED;
                default -> EmploymentStatus.UNEMPLOYED;
            };

            SalaryRange salaryRange = switch (request.getSalaryRange().toLowerCase()) {
                case "50k_100k" -> SalaryRange.BETWEEN_50K_100K;
                case "100k_150k" -> SalaryRange.BETWEEN_100K_200K;
                case "150k_200k" -> SalaryRange.ABOVE_200K;
                default -> SalaryRange.BELOW_50K;
            };

            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setDateOfBirth(request.getDateOfBirth());
            profile.setGender(gender);
            profile.setUsername(request.getUsername());

            profile.setAddressLine1(request.getAddressLine1());
            profile.setAddressLine2(request.getAddressLine2());
            profile.setCity(request.getCity());
            profile.setState(request.getState());
            profile.setPostalCode(request.getPostalCode());
            profile.setCountry(request.getCountry());

            profile.setJobTitle(request.getJobTitle());
            profile.setCompanyName(request.getCompanyName());
            profile.setIndustry(request.getIndustry());
            profile.setEmploymentStatus(employmentStatus);
            profile.setSalaryRange(salaryRange);

            return profile;
        }

        public void createPreferences(PreferenceRequest request, int id) {
            User user = userRepo.getReferenceById(id);
            UserPreferences preferences = new UserPreferences();
            preferences.setUser(user);
            user.setUserPreferences(preferences);
            fillPreferences(request, preferences);
        }

        public void updatePreferences(PreferenceRequest request, int id) {
            UserPreferences userPreferences= userPreferencesRepository.findByUserId(id);
            fillPreferences(request,userPreferences);
        }

        public PreferenceRequest getPreferences(UserDto u) {
            int userId = u.getId();
            UserPreferences userPreferences = userPreferencesRepository.findFullPreferences(userId);
            Set<String> sectors = userPreferences.getSectors()
                    .stream()
                    .map(Sectors::getSectorName)
                    .collect(Collectors.toSet());
            Set<String> companies = userPreferences.getCompanies()
                    .stream()
                    .map(Companies::getCompanyName)
                    .collect(Collectors.toSet());

            return new PreferenceRequest(
                    userPreferences.getInvestmentGoals(),
                    userPreferences.getRiskTolerance(),
                    userPreferences.getPreferredAsset(),
                    sectors,
                    companies
            );
        }

        private void fillPreferences(PreferenceRequest request,UserPreferences userPreferences) {
            userPreferences.setRiskTolerance(request.getRiskTolerance());
            userPreferences.setInvestmentGoals(request.getInvestmentGoals());
            userPreferences.setPreferredAsset(request.getPreferredAsset());

            Set<Sectors> sectors = userPreferences.getSectors();
            sectors.clear();
            for(String sectorName : request.getSectors()) {
                Sectors sector = new Sectors();
                sector.setUser(userPreferences);
                sector.setSectorName(sectorName);
                sectors.add(sector);
            }
            userPreferences.setSectors(sectors);

            Set<Companies> companies = userPreferences.getCompanies();
            companies.clear();
            for(String companyName : request.getCompanies()) {
                Companies company = new Companies();
                company.setUser(userPreferences);
                company.setCompanyName(companyName);
                companies.add(company);
            }
            userPreferences.setCompanies(companies);
            userPreferencesRepository.save(userPreferences);
        }

        public boolean isUsernameAvailable(String username) {
            return userProfileRepository.findByUsername(username)==null;
        }

        public List<Share> getUserHoldings(int id) {
            return stockRepo.findByUserId(id).stream().map(
                    stock -> new Share(stock.getSymbol(), stock.getShares())
            ).toList();
        }

        public void addBalance(int id, double amount) {
            userProfileRepository.creditBalance(id, amount);
        }

        public Double getBalance(int id) {
            UserProfile userProfile = userProfileRepository.findByUserId(id);
            return userProfile.getBalance();
        }
}
