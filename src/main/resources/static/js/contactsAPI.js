const form = document.querySelector("#addFriendForm");

form.addEventListener("submit", async (event) => {
  event.preventDefault();

  const formData = new FormData(form);

  try {
    const response = await fetch("/times2/contact/addContact", {
      method: "POST",
      body: formData,
    });

    const result = await response.text();

    if (!response.ok) {
      throw new Error(result);
    }

    document.querySelector("#smallTextInfo").textContent = result;
  } catch (error) {
    document.querySelector("#smallTextInfo").textContent = error.message;
  }
});
