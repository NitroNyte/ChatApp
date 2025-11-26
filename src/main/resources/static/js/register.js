/**
 * @file register.js
 * @description Handles the registration form validation.
 */

$(document).ready(function () {
  function showError(inputElement, message) {
    inputElement.next(".error-message").remove();

    inputElement.after(
      '<div class="error-message text-danger small mt-1">' + message + "</div>"
    );

    inputElement.addClass("is-invalid");
    inputElement.removeClass("is-valid");
  }

  function showSuccess(inputElement) {
    inputElement.next(".error-message").remove();
    inputElement.removeClass("is-invalid");
    inputElement.addClass("is-valid");
  }

  function validateName(){
    const nameInput = $("#name");
    const nameValue = nameInput.val().trim();

    if (nameValue === "") {
      showError(nameInput, "Name cannot be empty");
      return false;
    } else if (nameValue.length < 2) {
      showError(nameInput, "Name must be at least 2 characters");
      return false;
    } else {
      showSuccess(nameInput);
      return true;
    }
  }

  function validateSurname() {
    const surnameInput = $("#surname");
    const surnameValue = surnameInput.val().trim();

    if (surnameValue === "") {
      showError(surnameInput, "Surname cannot be empty");
      return false;
    } else if (surnameValue.length < 2) {
      showError(surnameInput, "Surname must be at least 2 characters");
      return false;
    } else {
      showSuccess(surnameInput);
      return true;
    }
  }

  function validateEmail() {
    const emailInput = $("#email");
    const emailValue = emailInput.val().trim();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (emailValue === "") {
      showError(emailInput, "Email cannot be empty");
      return false;
    } else if (!emailRegex.test(emailValue)) {
      showError(emailInput, "Please enter a valid email address");
      return false;
    } else {
      showSuccess(emailInput);
      return true;
    }
  }

  function validatePassword() {
    const passwordInput = $("#password");
    const passwordValue = passwordInput.val();

    if (passwordValue === "") {
      showError(passwordInput, "Password cannot be empty");
      return false;
    } else if (passwordValue.length < 8) {
      showError(passwordInput, "Password must be at least 8 characters");
      return false;
    } else {
      showSuccess(passwordInput);
      return true;
    }
  }

  $("#registerForm").submit(function (event) {
    const isNameValid = validateName();
    const isSurnameValid = validateSurname();
    const isEmailValid = validateEmail();
    const isPasswordValid = validatePassword();

    if (!isNameValid || !isSurnameValid || !isEmailValid || !isPasswordValid) {
      event.preventDefault();
      $("html, body").animate(
        {
          scrollTop: $(".is-invalid").first().offset().top - 100,
        },
        500
      );
    }
  });

  $("#name").on("blur", validateName);
  $("#surname").on("blur", validateSurname);
  $("#email").on("blur", validateEmail);
  $("#password").on("blur", validatePassword);

  $("#name, #surname, #email, #password").on("focus", function () {
    $(this).removeClass("is-invalid is-valid");
    $(this).next(".error-message").remove();
  });
});