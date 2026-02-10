/* =========================
   PASSWORD CHANGE (VANILLA)
   Namespace: OBS.PWD
   ========================= */
window.OBS ||= {};
OBS.PWD ||= {};

OBS.PWD.init = function() {

    /* -------- submit (delegation) -------- */
    document.removeEventListener("submit", OBS.PWD._onSubmit, true);
    OBS.PWD._onSubmit = async function(event) {
        const form = event.target;
        if (!form || form.id !== "passwordForm2026") return;

        event.preventDefault();

        const btn = document.getElementById("pwdSubmitBtn");
        const spin = document.getElementById("pwdSpin");
        const text = document.getElementById("pwdBtnText");
        const msg = document.getElementById("pwdMessage");

        const showMsg = (t, type) => {
            msg.classList.remove("success", "error", "visible");
            msg.classList.add(type === "success" ? "success" : "error");
            msg.innerText = t || (type === "success" ? "Başarılı" : "Hata");
            void msg.offsetWidth; // animasyon reset
            msg.classList.add("visible");
        };

        msg.classList.remove("success", "error", "visible");
        msg.innerText = "";

        const newPwd = (document.getElementById("newPassword")?.value || "").trim();
        if (newPwd.length < 8) {
            showMsg("Yeni şifre en az 8 karakter olmalı.", "error");
            return;
        }

        btn.disabled = true;
        spin.style.display = "inline-block";
        text.innerText = "Güncelleniyor...";

        try {
            const formData = new URLSearchParams(new FormData(form));

            const result = await fetchWithSessionCheck("user/user_pwd_change", {
                method: "POST",
                body: formData,
                headers: { "Content-Type": "application/x-www-form-urlencoded" }
            });

            if (result?.errorMessage) {
                showMsg(result.errorMessage, "error");
            } else {
                showMsg(result?.message || "Şifre güncellendi.", "success");
                document.getElementById("oldPassword").value = "";
                document.getElementById("newPassword").value = "";
            }

        } catch (e) {
            showMsg(e?.message || "Beklenmeyen bir hata oluştu.", "error");
        } finally {
            btn.disabled = false;
            spin.style.display = "none";
            text.innerText = "Şifre Güncelle";
        }
    };

    document.addEventListener("submit", OBS.PWD._onSubmit, true);

    /* -------- show / hide password -------- */
    document.removeEventListener("click", OBS.PWD._onEyeClick, true);
    OBS.PWD._onEyeClick = function(event) {
        const eye = event.target.closest(".pwd-eye");
        if (!eye) return;

        const id = eye.dataset.toggle;
        const input = document.getElementById(id);
        if (!input) return;

        const isPwd = input.type === "password";
        input.type = isPwd ? "text" : "password";
        eye.innerText = isPwd ? "🙈" : "👁";
    };

    document.addEventListener("click", OBS.PWD._onEyeClick, true);
};

/* -------- sayfadan çıkınca -------- */
OBS.PWD.destroy = function() {
    document.removeEventListener("submit", OBS.PWD._onSubmit, true);
    document.removeEventListener("click", OBS.PWD._onEyeClick, true);
};
