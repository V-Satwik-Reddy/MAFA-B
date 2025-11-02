package majorproject.maf.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private String token;
    private boolean success;
    private String message;
    private T data;

    public ApiResponse(boolean b, String userFound, T user) {
        this.success = b;
        this.message = userFound;
        this.data =  user;
    }
}
