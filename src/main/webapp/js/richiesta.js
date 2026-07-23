fetch("CaptchaServlet")
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            document.getElementById("domandaCaptcha").textContent = data.domanda;
        })
        .catch(function () {
            document.getElementById("domandaCaptcha").textContent =
                    "Errore nel caricamento del captcha";
        });