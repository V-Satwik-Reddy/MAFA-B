package majorproject.maf.service;

import majorproject.maf.dto.request.PreferenceRequest;
import majorproject.maf.dto.request.ProfileRequest;
import majorproject.maf.dto.response.*;
import majorproject.maf.exception.InvalidProfileDetailsException;
import majorproject.maf.model.*;
import majorproject.maf.model.enums.EmploymentStatus;
import majorproject.maf.model.enums.Gender;
import majorproject.maf.model.enums.SalaryRange;
import majorproject.maf.model.enums.UserStatus;
import majorproject.maf.repository.*;
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
        private final CompanyMasterRepository companyMasterRepository;
        private final SectorMasterRepository sectorMasterRepository;
    private final StockPriceRepository stockPriceRepository;

    public ProfileService(UserPreferencesRepository userPreferencesRepository, StockRepository stockRepo, UserRepository userRepo, UserProfileRepository userProfileRepository, CompanyMasterRepository companyMasterRepository, SectorMasterRepository sectorMasterRepository, StockPriceRepository stockPriceRepository) {
            this.companyMasterRepository = companyMasterRepository;
            this.sectorMasterRepository = sectorMasterRepository;
            this.userProfileRepository = userProfileRepository;
            this.stockRepo = stockRepo;
            this.userRepo = userRepo;
            this.userPreferencesRepository = userPreferencesRepository;
        this.stockPriceRepository = stockPriceRepository;
    }

        public void createProfile(ProfileRequest request, int userId) {
            try {
                User user = userRepo.getReferenceById(userId);
                UserProfile userProfile = buildProfile(new UserProfile(),request);
                userProfile.setUser(user);
                user.setPhone(request.getPhone());
                user.setStatus(UserStatus.ACTIVE);
                user.setPhoneVerified(true);
                userProfile.setBalance(0.0);
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
            fillPreferences(request, preferences);
        }

        public void updatePreferences(PreferenceRequest request, int id) {
            UserPreferences userPreferences= userPreferencesRepository.findByUserId(id);
            fillPreferences(request,userPreferences);
        }

        public PreferenceResponse getPreferences(UserDto u) {

            UserPreferences prefs = userPreferencesRepository.findFullPreferences(u.getId());

            Set<SectorDto> sectorIds = prefs.getSectors()
                    .stream()
                    .map(cp -> new SectorDto(cp.getSector().getId(),cp.getSector().getName()))
                    .collect(Collectors.toSet());

            Set<CompanyDto> companyIds = prefs.getCompanies()
                    .stream()
                    .map(cp-> new CompanyDto(cp.getCompany().getId(),cp.getCompany().getSymbol(),cp.getCompany().getName()))
                    .collect(Collectors.toSet());

            return new PreferenceResponse(
                    prefs.getInvestmentGoals(),
                    prefs.getRiskTolerance(),
                    prefs.getPreferredAsset(),
                    sectorIds,
                    companyIds
            );
        }

        private void fillPreferences(PreferenceRequest request, UserPreferences prefs) {

            prefs.setRiskTolerance(request.getRiskTolerance());
            prefs.setInvestmentGoals(request.getInvestmentGoals());
            prefs.setPreferredAsset(request.getPreferredAsset());

            // 🔥 BATCH FETCH (1 query each)
            List<SectorMaster> sectors = sectorMasterRepository.findByIdIn(request.getSectorIds());

            List<CompanyMaster> companies = companyMasterRepository.findByIdIn(request.getCompanyIds());

            // update sectors
            prefs.getSectors().clear();
            for (SectorMaster sector : sectors) {
                Sectors sp = new Sectors();
                sp.setUser(prefs);
                sp.setSector(sector);
                prefs.getSectors().add(sp);
            }

            // update companies
            prefs.getCompanies().clear();
            for (CompanyMaster company : companies) {
                Companies cp = new Companies();
                cp.setUser(prefs);
                cp.setCompany(company);
                prefs.getCompanies().add(cp);
            }
            userPreferencesRepository.save(prefs);
        }

        public boolean isUsernameAvailable(String username) {
            return userProfileRepository.findByUsername(username)==null;
        }

        public List<Share> getUserHoldings(int id) {
            List<Share> s=stockRepo.findByUserId(id).stream().map(
                    stock -> new Share(stock.getSymbol(), stock.getShares())
            ).toList();
            List<Double> prices= stockPriceRepository.batchFind(
                    s.stream().map(Share::getSymbol).toList()
            );
            for(int i=0;i<s.size();i++){
                s.get(i).setPrice(prices.get(i));
            }
            return s;
        }

        public void addBalance(int id, double amount) {
            userProfileRepository.creditBalance(id, amount);
        }

        public Double getBalance(int id) {
            UserProfile userProfile = userProfileRepository.findByUserId(id);
            return userProfile.getBalance();
        }

}
