const infoBox = document.getElementById("infoBox");

infoBox.addEventListener("mouseenter", () => {
    infoBox.classList.remove("afterHover");
    infoBox.classList.add("hover");
});

infoBox.addEventListener("mouseleave", () => {
    infoBox.classList.add("afterHover");
    infoBox.classList.remove("hover");
})