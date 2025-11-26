const db = new Dexie("PersonalDatabase");

db.version(4).stores({
    message: '++id, [senderId+receiverId], content, timeCreated, status',
    oneTimePreKey: '++id, currentUserId',
    signedPreKey: '++id, currentUserId, signedPreKeyRecord',
    identityKey: '++id, currentUserId, identityKeyRecord',
    kyberPreKey: '++id, currentUserId, kyberPreKeyRecord',
    chatSession: '++id, [senderId+receiverId], sessionRecord'
});

function addMessage(current_user, current_friend, content, time_created) {
    db.message.add({
        senderId: current_user,
        receiverId: current_friend,
        content: content,
        timeCreated: time_created,
        status: "SENT"
    });
}

function addOtpk(currentUserId, otpkRecord) {
    db.oneTimePreKey.add({
        currentUserId: currentUserId,
        otpkRecord: otpkRecord
    });
}

function addSingedPreKey(currentUserId, spkRecord) {
    db.signedPreKey.add({
        currentUserId: currentUserId,
        signedPreKeyRecord: spkRecord
    });
}

function addIdentityKey(currentUserId, identityKeyRecord) {
    db.identityKey.add({
        currentUserId: currentUserId,
        identityKeyRecord: identityKeyRecord
    });
}

function addKyberPreKey(currentUserId, kyberPreKeyRecord) {
    db.kyberPreKey.add({
        currentUserId: currentUserId,
        kyberPreKeyRecord: kyberPreKeyRecord
    });
}

function addSessionRecord(currentUserId, friendId, sessionRecord) {
    db.chatSession.add({
        senderId: currentUserId,
        receiverId: friendId,
        sessionRecord: sessionRecord
    });
}

function updateSessionRecord(senderId, receiverId, sessionRecord) {
    db.chatSession
        .where("senderId")
        .equals(senderId)
        .and(chatSession => chatSession.receiverId === receiverId)
        .modify(chatSession => {
            chatSession.sessionRecord = sessionRecord;
        });

    console.log("Updated session record");
}


function addMessagesInBulk(listOfMessages) {
    db.message.bulkAdd(listOfMessages);
}

async function getUniqueMessages(current_user, current_friend) {
    try {
        return (await db.message
            .where("[senderId+receiverId]")
            .equals([current_user, current_friend])
            .or(["senderId+receiverId"])
            .equals([current_friend, current_user])
            .toArray()).map(({id, ...rest}) => rest);
    } catch (error) {
        console.error('Error fetching messages:', error);
        return [];
    }
}

async function getOtpkRecords(currentUserId) {
    const result = await db.oneTimePreKey
        .where("currentUserId")
        .equals(currentUserId)
        .toArray();

    return result.map(r => r.otpkRecord);
}


async function getSpkRecord(currentUserId) {
    const result = await db.signedPreKey
        .where("currentUserId")
        .equals(currentUserId)
        .first();

    return result?.signedPreKeyRecord;
}

async function getIdKey(currentUserId) {
    const result = await db.identityKey
        .where("currentUserId")
        .equals(currentUserId)
        .first();

    return result?.identityKeyRecord;
}

async function getKyberPreKey(currentUserId) {
    const result = await db.kyberPreKey
        .where("currentUserId")
        .equals(currentUserId)
        .first();

    return result?.kyberPreKeyRecord;
}

async function getSessionRecord(senderId, receiverId) {
    const result = await db.chatSession
        .where('[senderId+receiverId]')
        .equals([senderId, receiverId])
        .first();

    return result?.sessionRecord;
}

async function sessionRecordExists(senderId, receiverId) {
    const record = await db.chatSession
        .where("[senderId+receiverId]")
        .equals([senderId, receiverId])
        .first();

    console.log("Session record exists: " + !!record);
    return !!record;
}





