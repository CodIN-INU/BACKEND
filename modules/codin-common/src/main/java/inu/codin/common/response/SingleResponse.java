package inu.codin.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> SingleResponse<T> success(T data) {
        return new SingleResponse<>(200, "Success", data);
    }

    public static <T> SingleResponse<T> success(String message, T data) {
        return new SingleResponse<>(200, message, data);
    }

    public static <T> SingleResponse<T> error(int code, String message) {
        return new SingleResponse<>(code, message, null);
    }

    public static <T> SingleResponse<T> error(String message) {
        return new SingleResponse<>(500, message, null);
    }
}
