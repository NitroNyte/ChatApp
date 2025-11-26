package xk.nitro.chat.dto.signal;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("SINGULAR_DATA_INPUT")
public record SingularDataInput(
        byte[] data
) implements InputTypeChecker {
}
