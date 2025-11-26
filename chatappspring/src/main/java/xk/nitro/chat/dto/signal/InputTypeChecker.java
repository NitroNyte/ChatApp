package xk.nitro.chat.dto.signal;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SingularDataInput.class),
        @JsonSubTypes.Type(value = MultipleDataInput.class)
})
public interface InputTypeChecker {}
