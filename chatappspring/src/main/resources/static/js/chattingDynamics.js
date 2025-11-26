const sendButton = document.getElementById("sendButton");
const sendButtonSvgImage = document.getElementById("sendButtonIcon");

sendButton.addEventListener("mousedown", () => {
    sendButtonSvgImage.style.fill = "black";
});

sendButton.addEventListener("mouseup", () => {
  sendButtonSvgImage.style.fill = "white"; 
});

sendButton.addEventListener("mouseleave", () => {
    sendButtonSvgImage.style.fill = "white";
})