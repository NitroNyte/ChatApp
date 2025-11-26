package xk.nitro.chat.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import xk.nitro.chat.model.User;

import jakarta.annotation.Nullable;
import net.coobird.thumbnailator.Thumbnails;

import xk.nitro.chat.dao.ProfilePictureDao;
import xk.nitro.chat.dao.UserDao;
import xk.nitro.chat.dto.UserDTO;
import xk.nitro.chat.model.ProfilePicture;

/**
 * A service that is used for setting, getting and saving a profile picture
 * 
 * I'm going to use the short form of the names following:
 * Pfp -> Profile picture
 */
@Service
public class ProfilePictureService {

    @Value("${app.image.width:100}")
    private int imageResizeWidth;

    @Value("${app.image.height:100}")
    private int imageResizeHeight;

    private static final Logger log = LoggerFactory.getLogger(ProfilePictureService.class);

    private final ResourceLoader resourceLoader;
    private final ProfilePictureDao profilePictureDao;
    private final UserDao userDao;

    public ProfilePictureService(@NonNull ResourceLoader resourceLoader, @NonNull ProfilePictureDao profilePictureDao,
            @NonNull UserDao userDao) {
        this.profilePictureDao = profilePictureDao;
        this.userDao = userDao;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Updates the users pfp in database, it saves the picture the user wants
     * 
     * @param userFormat           User dto where all the needed information is
     *                             saved
     * @param currentPictureFile   MultipartFile where the image is located
     * @param profilePictureFormat Pictures have a unique id, so we utilise that for
     *                             updating
     */
    public void setPersonalProfilePicture(UserDTO userFormat, MultipartFile currentPictureFile,
            Integer profilePictureId) {

        User currentUser = userDao.getReferenceById(userFormat.id());

        if (currentPictureFile.isEmpty() || currentPictureFile != null) {
            try {

                byte[] newImageByte = currentPictureFile.getBytes();
                ByteArrayOutputStream resizedImage = new ByteArrayOutputStream();

                Thumbnails.of(new ByteArrayInputStream(newImageByte))
                        .size(imageResizeWidth, imageResizeHeight)
                        .outputFormat("PNG")
                        .outputQuality(0.7F)
                        .toOutputStream(resizedImage);

                byte[] resizedBytes = resizedImage.toByteArray();

                ProfilePicture currentNewPicture = ProfilePicture.builder()
                        .id(profilePictureId)
                        .user(currentUser)
                        .imageName(currentPictureFile.getOriginalFilename())
                        .imageFormat(currentPictureFile.getContentType())
                        .imageData(resizedBytes).build();

                profilePictureDao.save(currentNewPicture);
            } catch (IOException e) {
                log.error("Something is wrong with the image and we could not save it", e);
            }
        }
    }

    /**
     * When user first logs in for the first time ever he gets a default pfp
     * 
     * @param currentUser logged in user
     */
    public void setDefaultProfilePicture(User currentUser) {

        if (userHasPfp(currentUser)) {
            log.info("User already has a profile picture, so we dont need another one");
            return;
        }

        try {
            byte[] defaultImageToByte;
            try (InputStream inputStreamImage = loadImage()) {
                defaultImageToByte = inputStreamImage.readAllBytes();
            }

            profilePictureDao.save(ProfilePicture.builder()
                    .user(currentUser)
                    .imageName("Default picture")
                    .imageFormat("png")
                    .imageData(defaultImageToByte).build());
        } catch (IOException e) {
            log.error("Default image could not be found/resolved", e);
        }
    }

    /**
     * Gets the pfp from database
     * 
     * @param currentLoggedInUser
     * @return Pfp entity/object
     */
    @NonNull
    public ProfilePicture getByUser(@NonNull User currentLoggedInUser) {

        ProfilePicture currentProfilePicture = profilePictureDao.findByUser(currentLoggedInUser).orElseThrow(
                () -> new IllegalArgumentException("Cannot find User with that specific profile picture in database"));
        log.info("Image retrived");
        return currentProfilePicture;
    }

    public InputStream loadImage() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/images/account.png");
        return resource.getInputStream();
    }

    public boolean userHasPfp(@Nullable User currentUser) {
        return profilePictureDao.existsByUser(currentUser);
    }

}
