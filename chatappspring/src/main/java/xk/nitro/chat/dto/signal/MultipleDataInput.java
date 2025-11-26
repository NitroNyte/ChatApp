package xk.nitro.chat.dto.signal;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.List;

@JsonTypeName("MULTIPLE_DATA_INPUT")
public record MultipleDataInput(
        List<byte[]> data
) implements InputTypeChecker {}
