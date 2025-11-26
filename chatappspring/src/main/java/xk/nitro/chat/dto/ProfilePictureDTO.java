package xk.nitro.chat.dto;

public record ProfilePictureDTO(
        Integer profilePictureId,
        Integer userId,
        String imageName,
        byte[] imageData) {}
