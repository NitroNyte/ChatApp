const addFriendBtn = document.getElementById("friend");
const closeAddFriendBtn = document.getElementById("closePopUp");
const popUpBox = document.getElementById("popUp");
const popUpBoxInner = document.getElementById("popUpBox-inner");

const notifyBell = document.getElementById("notifications");
const closeNotifyBox = document.getElementById("closePopUpNotification");
const notifyBox = document.getElementById("popUpNotifications");

addFriendBtn.addEventListener("click", () => {
  popUpBox.classList.add("open");
});

closeAddFriendBtn.addEventListener("click", () => {
  popUpBox.classList.remove("open");
});

closeNotifyBox.addEventListener("click", () => {
  notifyBox.classList.remove("open");
});

notifyBell.addEventListener("click", () => {
  notifyBox.classList.add("open");
});

popUpBoxInner.addEventListener("mouseenter", () => {
  popUpBoxInner.classList.remove("afterHover");
  popUpBoxInner.classList.add("hover");
});

popUpBoxInner.addEventListener("mouseleave", () => {
  popUpBoxInner.classList.add("afterHover");
  popUpBoxInner.classList.remove("hover");
});

//The contact box functions
const contactBox = document.getElementById("friendsTabDesign");

contactBox.addEventListener("mouseenter", () => {
  contactBox.classList.remove("afterHover");
  contactBox.classList.add("hover");
});

contactBox.addEventListener("mouseleave", () => {
  contactBox.classList.add("afterHover");
  contactBox.classList.remove("hover");
});
