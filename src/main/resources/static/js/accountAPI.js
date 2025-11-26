$(document).ready(function () {
    loadProfileImage().then().catch();
});


async function loadProfileImage() {
    try {
        const pictureResponse = await fetch("/times2/account/user/profileImage");

        if (!pictureResponse.ok) {
            throw new Error("Failed to fetch image");
        }

        const result = await pictureResponse.blob();

        const imageUrl = URL.createObjectURL(result);

        document.getElementById("ProfilePic").src = imageUrl;
    } catch (error) {
        console.error(error);
    }
}
