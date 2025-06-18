package inu.codin.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ListResponse<T> {
    private int code;
    private String message;
    private List<T> data;

    public static <T> ListResponse<T> success(List<T> data) {
        return new ListResponse<>(200, "Success", data);
    }

    public static <T> ListResponse<T> success(String message, List<T> data) {
        return new ListResponse<>(200, message, data);
    }

    public static <T> ListResponse<T> error(int code, String message) {
        return new ListResponse<>(code, message, null);
    }

    public static <T> ListResponse<T> error(String message) {
        return new ListResponse<>(500, message, null);
    }
}
