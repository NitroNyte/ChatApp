package xk.nitro.chat.dto;

import org.springframework.web.multipart.MultipartFile;

public record UserProfileDTO(
        UserDTO userEditInfo,
        MultipartFile newImageFile) {}
