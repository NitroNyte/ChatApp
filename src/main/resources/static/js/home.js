saveUserKeys().then().catch();

async function saveUserKeys() {
    try {
        const userKeyResponse = await fetch("/times2/api/user/keys");

        if (!userKeyResponse.ok) {
            throw new Error("Failed to fetch user keys!");
        }

        const userKeysObject = await userKeyResponse.json();


        if (userKeysObject.userKeys.preKeyRecordList) {
            userKeysObject.userKeys.preKeyRecordList.forEach(userKey => {
                addOtpk(userKeysObject.userId, userKey);
            });
        }
        if (userKeysObject.userKeys.kyberPreKeyRecord) {
            addKyberPreKey(userKeysObject.userId, userKeysObject.userKeys.kyberPreKeyRecord);
        }
        if (userKeysObject.userKeys.signedPreKeyRecord) {
            addSingedPreKey(userKeysObject.userId, userKeysObject.userKeys.signedPreKeyRecord);
        }
        if (userKeysObject.userKeys.identityKeyPair) {
            addIdentityKey(userKeysObject.userId, userKeysObject.userKeys.identityKeyPair);
        }
        console.log("userKeysObject", userKeysObject);
    } catch (error) {
        console.error(error);
        return null;
    }
}
