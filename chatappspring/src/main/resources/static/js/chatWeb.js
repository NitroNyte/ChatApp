let socket = null;
let stompClient = null;
let historyMessages = null;

const friendId = parseInt(window.location.pathname.split("/").pop());
const currentUserId = parseInt(document.getElementById("currentUserId").value);
$(document).ready(function () {
    connect().then().catch();

    $("#msgTypeBox").on("keypress", (e) => {
        if (e.which === 13) {
            const message = $("#msgTypeBox").val().trim();
            if (!message) {
                return;
            }
            sendMessage();
            $("#msgTypeBox").val("");
        }
    });

    $("#sendButton").on("click", function () {
        const message = $("#msgTypeBox").val().trim();
        if (!message) return;

        sendMessage();
        $("#msgTypeBox").val("").focus();
    });

});

async function connect() {
    socket = new SockJS("/chat-websocket?friendId=" + friendId);
    stompClient = Stomp.over(socket);

    let userMessages = await getUniqueMessages(currentUserId, friendId);
    let sessionExists = await sessionRecordExists(currentUserId, friendId);


    stompClient.connect({friendId: friendId}, async function (frame) {
        sendUserKeys().then().catch((err) => {
            console.log("The keys cannot be sent: ", err);
        });

        stompClient.subscribe("/user/queue/cipherReady", function (message) {
            if (message.body === "SESSION_CIPHER_READY") {
                sendFriendsMessages(userMessages);

                historyMessages = stompClient.subscribe(
                    "/user/queue/history",
                    function (message) {
                        let decryptedMessagesInOrder = JSON.parse(message.body).inOrderDecryptedMessages;
                        let decryptedMessagesToSave = JSON.parse(message.body).decryptedMessages;

                        decryptedMessagesInOrder.forEach((message) => {
                            showMessageHistoryFormat(message);
                        });

                        addMessagesInBulk(decryptedMessagesToSave);

                        decryptedMessagesInOrder = [];
                        decryptedMessagesToSave = [];

                        historyMessages.unsubscribe();
                        historyMessages = null;
                    }
                );
            }
        });

        stompClient.subscribe("/user/queue/messages", function (message) {
            showMessage(message);
            sendInfoToServerAboutSeen();
        });

        stompClient.subscribe("/user/queue/sessionState", function (message) {
            let sessionRecordObject = JSON.parse(message.body).data;
            console.log(sessionRecordObject);
            if (sessionExists) {
                console.log("Updating session");
                updateSessionRecord(currentUserId, friendId, sessionRecordObject);
            } else {
                console.log("Session does not exist so we create a new one!");
                addSessionRecord(currentUserId, friendId, sessionRecordObject);
            }
        })


    });
}

function showMessage(message) {
    let currentMessage = JSON.parse(message.body);
    if (currentMessage.senderId === currentUserId) {
        $("#chatArea").append(
            "<div class='sender'>" +
            "<div class='senderBoxMessage'>" +
            "<div class='senderBoxSentMessage'>" +
            "<p>" +
            currentMessage.content +
            "</p>" +
            "</div>" +
            "<div class='senderBoxName'>" +
            "</div>" +
            "</div>" +
            "</div>"
        );

    } else {
        $("#chatArea").append(
            "<div class='contact'>" +
            "<div class='contactBoxMessage'>" +
            "<div class='contactBoxName'>" +
            "<h6 style='color:gray; font-style:italic; padding-left: 10px'>Name Of friend</h6>" +
            "</div>" +
            "<div class='contactBoxSentMessage'>" +
            "<p>" +
            currentMessage.content +
            "</p>" +
            "</div>" +
            "</div>" +
            "</div>"
        );
    }
}

function showMessageHistoryFormat(currentMessage) {
    if (currentMessage.senderId == currentUserId) {
        $("#chatArea").append(
            "<div class='sender'>" +
            "<div class='senderBoxMessage'>" +
            "<div class='senderBoxSentMessage'>" +
            "<p>" +
            currentMessage.content +
            "</p>" +
            "</div>" +
            "<div class='senderBoxName'>" +
            "</div>" +
            "</div>" +
            "</div>"
        );
    } else {
        $("#chatArea").append(
            "<div class='contact'>" +
            "<div class='contactBoxMessage'>" +
            "<div class='contactBoxName'>" +
            "<h6 style='color:gray; font-style:italic; padding-left: 10px'>Name Of friend</h6>" +
            "</div>" +
            "<div class='contactBoxSentMessage'>" +
            "<p>" +
            currentMessage.content +
            "</p>" +
            "</div>" +
            "</div>" +
            "</div>"
        );
    }
}

function sendMessage() {
    let content = $("#msgTypeBox").val();
    addMessage(currentUserId, friendId, content, Date.now());
    stompClient.send(
        "/ws/message",
        {},
        JSON.stringify({content: content, receiverId: friendId})
    );
}

function sendInfoToServerAboutSeen() {
    stompClient.send("/ws/message/seen", {}, friendId);
}

async function sendUserKeys() {
    const otpk = await getOtpkRecords(currentUserId);
    const signedPreKey = await getSpkRecord(currentUserId);
    const identityKey = await getIdKey(currentUserId);
    const kyberPreKey = await getKyberPreKey(currentUserId);
    const sessionRecord = await getSessionRecord(currentUserId, friendId);

    await stompClient.send("/ws/user/userKeys", {}, JSON.stringify({
            key: "IDENTITY_KEY",
            dataObject: {
                type: "SINGULAR_DATA_INPUT",
                data: identityKey
            }
        }
    ));

    await stompClient.send("/ws/user/userKeys", {}, JSON.stringify({
            key: "SIGNED_PRE_KEY",
            dataObject: {
                type: "SINGULAR_DATA_INPUT",
                data: signedPreKey
            }

        }
    ));

    await stompClient.send("/ws/user/userKeys", {}, JSON.stringify({
            key: "KYBER_PRE_KEY",
            dataObject: {
                type: "SINGULAR_DATA_INPUT",
                data: kyberPreKey
            }
        }
    ));

    await stompClient.send("/ws/user/userKeys", {}, JSON.stringify({
            key: "ONE_TIME_PRE_KEYS",
            dataObject: {
                type: "MULTIPLE_DATA_INPUT",
                data: otpk
            }
        }
    ));

    await stompClient.send("/ws/user/userKeys", {}, JSON.stringify({
            key: "SESSION_RECORD",
            dataObject: {
                type: "SINGULAR_DATA_INPUT",
                data: sessionRecord
            }
        }
    ));

}


function sendFriendsMessages(userMessages) {
    stompClient.send("/ws/message/history", {}, JSON.stringify({
        friendId: friendId,
        messages: userMessages
    }));
}

function messageSessionCipher() {
    stompClient.send("/ws/user/createSessionCipher", {}, {});
}