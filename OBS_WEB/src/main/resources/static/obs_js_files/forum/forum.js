/* =========================
   OBS.FORUM
   ========================= */
window.OBS = window.OBS || {};
OBS.FORUM = OBS.FORUM || {};

(() => {
  const F = OBS.FORUM;

  /* ---------- state ---------- */
  F.allSubjects = [];

  /* ---------- helpers ---------- */
  F.byId = (id) => document.getElementById(id);

  F.setCursor = (c) => { document.body.style.cursor = c || "default"; };

  F.errClear = () => {
    const e = F.byId("errorDiv");
    if (!e) return;
    e.style.display = "none";
    e.innerText = "";
  };

  F.errShow = (msg) => {
    const e = F.byId("errorDiv");
    if (!e) return;
    e.style.display = "block";
    e.innerText = msg || "Bir hata oluştu.";
  };

  F.setBtnBusy = (btn, busyText, isBusy) => {
    if (!btn) return;
    if (isBusy) {
      btn.dataset.oldText = btn.textContent;
      btn.textContent = busyText || "İşleniyor...";
      btn.disabled = true;
    } else {
      btn.textContent = btn.dataset.oldText || btn.textContent;
      btn.disabled = false;
      delete btn.dataset.oldText;
    }
  };

  /* =========================================================
     INIT
     ========================================================= */
  F.init = async function () {
    await F.loadSubjects();
  };

  /* =========================================================
     LOAD
     ========================================================= */
  F.loadSubjects = async function () {
    const mesajsayi = document.querySelector("#mesajadet");
    if (mesajsayi) mesajsayi.innerText = "";

    F.errClear();
    F.setCursor("wait");

    // küçük bir UI nefesi
    await new Promise(r => setTimeout(r, 30));

    try {
      const response = await fetchWithSessionCheck("/getSubjects", {
        method: "GET",
        headers: { "Content-Type": "application/json" },
      });

      if (!response) return;
      if (response.errorMessage) throw new Error(response.errorMessage);

      if (response.success) {
        const subjects = Array.isArray(response.subjects) ? response.subjects : [];
        F.allSubjects = subjects;
        F.renderSubjects(subjects);

        // istersen burada mesaj sayısını bas
        //if (mesajsayi) mesajsayi.innerText = String(subjects.length);
      } else {
        F.errShow("Bir hata oluştu.");
      }
    } catch (error) {
      F.errShow(error.message || "Bir hata oluştu. Daha sonra tekrar deneyin.");
    } finally {
      F.setCursor("default");
    }
  };

  /* =========================================================
     RENDER
     ========================================================= */
  F.renderSubjects = function (subjects) {
    if (!subjects || !Array.isArray(subjects)) return;

    const subjectSection = document.querySelector(".subject-section");
    if (!subjectSection) return;

    subjectSection.innerHTML = "";

    subjects.forEach((subject) => {
      const commits = Array.isArray(subject.commits) ? subject.commits : [];

      const subjectElement = `
        <div class="subject">
          <h4>${subject.subjectTitle ?? ""}</h4>
          <p>${subject.subjectDescription ?? ""}</p>

          <button type="button" onclick="OBS.FORUM.toggleCommentForm(${subject.subjectID})">
            Yorum Ekle
          </button>

          <button type="button" id="btn-commits-${subject.subjectID}"
                  onclick="OBS.FORUM.toggleCommits(${subject.subjectID})">
            Yorumları Göster
          </button>

          <div id="commits-${subject.subjectID}" class="commits" style="display:none;">
            ${commits.map((c) => `
              <div class="commit">
                <h4>By: ${c.createdBy ?? ""}</h4>
                <p>${c.commitText ?? ""}</p>
              </div>
            `).join("")}
          </div>

          <div id="comment-form-${subject.subjectID}" class="comment-form"
               style="display:none; margin-top:10px;">
            <textarea class="form-control"
                      id="comment-text-${subject.subjectID}"
                      placeholder="Yorum Yaz..."
                      rows="3"
                      maxlength="255"></textarea>

            <button type="button" onclick="OBS.FORUM.submitComment(${subject.subjectID})">
              Yorum Kaydet
            </button>
          </div>
        </div>
      `;

      subjectSection.insertAdjacentHTML("beforeend", subjectElement);
    });
  };

  /* =========================================================
     FILTER
     ========================================================= */
  F.filterSubjects = function () {
    const input = F.byId("search-input");
    const searchTerm = (input?.value || "").toLowerCase();

    if (!searchTerm.trim()) {
      F.renderSubjects(F.allSubjects);
      return;
    }

    const filtered = F.allSubjects.filter((s) => {
      const t = (s.subjectTitle || "").toLowerCase();
      const d = (s.subjectDescription || "").toLowerCase();
      return t.includes(searchTerm) || d.includes(searchTerm);
    });

    const subjectSection = document.querySelector(".subject-section");
    if (!subjectSection) return;

    if (!filtered.length) subjectSection.innerHTML = "<p>Sonuç bulunamadı.</p>";
    else F.renderSubjects(filtered);
  };

  /* =========================================================
     TOGGLES
     ========================================================= */
  F.toggleCommits = function (subjectId) {
    const commitsDiv = F.byId(`commits-${subjectId}`);
    if (!commitsDiv) return;

    const isHidden = (commitsDiv.style.display === "none" || commitsDiv.style.display === "");
    commitsDiv.style.display = isHidden ? "block" : "none";

    const btn = F.byId(`btn-commits-${subjectId}`);
    if (btn) btn.textContent = isHidden ? "Yorumları Gizle" : "Yorumları Göster";
  };

  F.toggleCommentForm = function (subjectId) {
    const form = F.byId(`comment-form-${subjectId}`);
    if (!form) return;

    const isHidden = (form.style.display === "none" || form.style.display === "");
    form.style.display = isHidden ? "block" : "none";
  };

  /* =========================================================
     SUBMIT COMMENT (no ajax reload, just refresh subjects)
     ========================================================= */
  F.submitComment = async function (subjectId) {
    const ta = F.byId(`comment-text-${subjectId}`);
    const commentText = (ta?.value || "").trim();

    if (!commentText) {
      alert("Lütfen bir yorum yazın.");
      return;
    }

    F.errClear();
    F.setCursor("wait");

    const saveButton = document.querySelector(`#comment-form-${subjectId} button`);
    F.setBtnBusy(saveButton, "İşlem yapılıyor...", true);

    try {
      const response = await fetchWithSessionCheck(`addComment/${subjectId}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: commentText }),
      });

      if (!response) return;
      if (response.errorMessage) throw new Error(response.errorMessage);

      if (response.success) {
        // textarea temizle + form kapat
        if (ta) ta.value = "";
        const form = F.byId(`comment-form-${subjectId}`);
        if (form) form.style.display = "none";

        // sadece listeyi yenile (sayfayı değil)
        await F.loadSubjects();
      } else {
        F.errShow("Bir hata oluştu.");
      }
    } catch (error) {
      F.errShow(error.message || "Bir hata oluştu. Daha sonra tekrar deneyin.");
    } finally {
      F.setCursor("default");
      F.setBtnBusy(saveButton, "", false);
      if (saveButton) saveButton.textContent = "Yorum Kaydet";
    }
  };

  /* =========================================================
     ADD SUBJECT (no ajax reload, just refresh subjects)
     ========================================================= */
  F.addSubject = async function () {
    const titleEl = F.byId("subject-title");
    const descEl = F.byId("subject-description");

    const title = (titleEl?.value || "").trim();
    const description = (descEl?.value || "").trim();

    if (!title || !description) {
      alert("Lütfen hem başlık hem de açıklama alanlarını doldurun.");
      return;
    }

    F.errClear();
    F.setCursor("wait");

    const saveButton = F.byId("saveButton");
    F.setBtnBusy(saveButton, "İşlem yapılıyor...", true);

    try {
      const response = await fetchWithSessionCheck("addSubject", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ title, description }),
      });

      if (!response) return;
      if (response.errorMessage) throw new Error(response.errorMessage);

      if (response.success) {
        // inputları temizle
        if (titleEl) titleEl.value = "";
        if (descEl) descEl.value = "";

        await F.loadSubjects();
      } else {
        F.errShow("Bir hata oluştu.");
      }
    } catch (error) {
      F.errShow(error.message || "Bir hata oluştu. Daha sonra tekrar deneyin.");
    } finally {
      F.setCursor("default");
      F.setBtnBusy(saveButton, "", false);
      if (saveButton) saveButton.textContent = "Kaydet";
    }
  };

})();
