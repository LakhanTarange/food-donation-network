function loginUser(event) {
    event.preventDefault();

    document.getElementById("loginFeedback").textContent =
        "Login preview only. Account verification will work after " +
        "the backend is connected.";
}

function registerUser(event) {
    event.preventDefault();

    const name = document.getElementById("registerName").value.trim();
    const address = document.getElementById("registerAddress").value.trim();
    const password = document.getElementById("registerPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const feedback = document.getElementById("registerFeedback");

    if (!name || !address) {
        feedback.textContent = "Please enter your name and pickup address.";
        return;
    }

    if (password !== confirmPassword) {
        feedback.textContent = "Passwords do not match.";
        document.getElementById("confirmPassword").focus();
        return;
    }

    feedback.textContent =
        "Donor details validated. This is a form preview; " +
        "no account has been saved yet.";

    // Do not store passwords in browser storage.
    document.getElementById("registerPassword").value = "";
    document.getElementById("confirmPassword").value = "";
}