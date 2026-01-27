/* =========================================================
   OBS.KODDEGIS (2026 CAM) – JQUERY YOK / TEK DOSYA
   - Eski kodun %100 aynı fonksiyonları
   - Input mask: Inputmask varsa onu kullanır, yoksa vanilla uygular
   ========================================================= */

window.OBS = window.OBS || {};
OBS.KODDEGIS = OBS.KODDEGIS || {};

(function (M) {
  "use strict";

  /* ---------------- helpers ---------------- */
  M.byId = (id) => document.getElementById(id);

  M._showError = function (msg) {
    const errorDiv = M.byId("errorDiv");
    if (!errorDiv) return;
    errorDiv.style.display = "block";
    errorDiv.innerText = msg || "Beklenmeyen bir hata oluştu.";
  };

  M._clearError = function () {
    const errorDiv = M.byId("errorDiv");
    if (!errorDiv) return;
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
  };


	 /* Local script tag: <script src="/obs_js_files/inputmask/inputmask.min.js"></script> */
	 /* format: AA-999-9999-9999 */
	 M._formatUkodu = function (val) {
	   const raw = (val || "")
	     .toUpperCase()
	     .replace(/[^A-Z0-9]/g, "");

	   const a = raw.slice(0, 2);
	   const b = raw.slice(2, 5);
	   const c = raw.slice(5, 9);
	   const d = raw.slice(9, 13);

	   let out = a;
	   if (b.length) out += "-" + b;
	   if (c.length) out += "-" + c;
	   if (d.length) out += "-" + d;

	   return out;
	 };

	 M.applyMask = function () {
	   const koduInput = M.byId("kodu");
	   if (!koduInput) return;

	   // aynı input'a iki kez bağlanma
	   if (koduInput.dataset.maskBound === "1") return;
	   koduInput.dataset.maskBound = "1";

	   // default değer
	   if (!koduInput.value || koduInput.value.trim() === "") {
	     koduInput.value = "00-000-0000-0000";
	   }

	   // LOCAL Inputmask yüklüyse kullan
	   if (window.Inputmask && typeof window.Inputmask === "function") {
	     try {
	       if (koduInput.inputmask && typeof koduInput.inputmask.remove === "function") {
	         koduInput.inputmask.remove();
	       }
	     } catch (e) {}

	     window.Inputmask({
	       mask: "AA-999-9999-9999",
	       placeholder: "_",      // senin eski sistem
	       clearIncomplete: false,
	       showMaskOnHover: false,
	       showMaskOnFocus: true,

	       definitions: {
	         A: { validator: "[A-Za-z0-9]", casing: "upper" }
	       },

	       // paste / manuel girişte format garanti
	       onBeforeWrite: function (event, buffer) {
	         const raw = (buffer || []).join("").replace(/[^A-Z0-9]/gi, "");
	         const formatted = M._formatUkodu(raw);
	         return {
	           refreshFromBuffer: true,
	           buffer: formatted.split(""),
	           caret: formatted.length
	         };
	       }
	     }).mask(koduInput);

	     // boş bırakırlarsa defaulta dön
	     koduInput.addEventListener("blur", () => {
	       if (!koduInput.value || koduInput.value.trim() === "") {
	         koduInput.value = "00-000-0000-0000";
	       } else {
	         
	         koduInput.value = M._formatUkodu(koduInput.value);
	       }
	     });

	    
	     koduInput.value = M._formatUkodu(koduInput.value);
	     return;
	   }

	   
	 };



  /* =========================================================
     ORIJINAL FONKSIYONLAR – EKSİKSİZ
     ========================================================= */

  M.toggleCheckboxes = function (source) {
    const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]');
    checkboxes.forEach((checkbox) => {
      checkbox.checked = source.checked;
    });
    M.satirsay();
  };

  M.satirsay = function () {
    const checkboxes = document.querySelectorAll('tbody input[type="checkbox"]:checked');
    let totalsatir = checkboxes.length;
    M.byId("totalSatir").innerText = formatNumber0(totalsatir);
  };

  M.fetchTable = async function () {
    const errorDiv = M.byId("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    document.body.style.cursor = "wait";
    const tableBody = M.byId("tableBody");
    tableBody.innerHTML = "";

    const kons = M.byId("kons").value;
    const kodu = M.byId("kodu").value;
    const evrak = M.byId("gevrak").value;
    const pakno = M.byId("pakno").value;

    if (
      (!kons || kons.trim() === "") &&
      (!evrak || evrak.trim() === "") &&
      (!pakno || pakno.trim() === "") &&
      (kodu === "00-000-0000-0000")
    ) {
      document.body.style.cursor = "default";
      return;
    }

    M.byId("totalSatir").innerText = "0";

    try {
      const response = await fetchWithSessionCheck("kereste/koddegisload", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ pakno, kons, kodu, evrak }),
      });

      if (response.errorMessage) {
        throw new Error(response.errorMessage);
      }

      const data = response;

      data.data.forEach((rowData) => {
        const row = document.createElement("tr");
        row.classList.add("table-row-height");
        row.innerHTML = `
          <td style="width: 40px;"><input type="checkbox" onclick="OBS.KODDEGIS.satirsay()"></td>
          <td>${rowData.Evrak_No}</td>
          <td>${rowData.Barkod}</td>
          <td>${rowData.Kodu}</td>
          <td>${rowData.Paket_No}</td>
          <td>${rowData.Konsimento}</td>
          <td class="double-column">${formatNumber0(rowData.Miktar)}</td>
          <td class="double-column">${formatNumber3(rowData.m3)}</td>
          <td>${formatDate(rowData.Tarih)}</td>
          <td class="double-column">${formatNumber2(rowData.Kdv)}</td>
          <td>${rowData.Doviz}</td>
          <td class="double-column">${formatNumber2(rowData.Fiat)}</td>
          <td class="double-column">${formatNumber2(rowData.Tutar)}</td>
          <td class="double-column">${formatNumber2(rowData.Kur)}</td>
          <td>${rowData.Cari_Firma}</td>
          <td>${rowData.Adres_Firma}</td>
          <td class="double-column">${formatNumber2(rowData.Iskonto)}</td>
          <td class="double-column">${formatNumber2(rowData.Tevkifat)}</td>
          <td>${rowData.Ana_Grup}</td>
          <td>${rowData.Alt_Grup}</td>
          <td>${rowData.Mensei}</td>
          <td style="display: none;">${rowData.Satir}</td>
        `;
        tableBody.appendChild(row);
      });
    } catch (error) {
      errorDiv.style.display = "block";
      errorDiv.innerText = error; // senin orijinalin böyleydi (message değil)
    } finally {
      document.body.style.cursor = "default";
    }
  };

  M.kodKaydet = async function () {
    const errorDiv = M.byId("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";

    const ykod = M.byId("ykod").value.trim();
    const kodadi = M.byId("kodadi").innerText.trim();

    if (!ykod || !kodadi) {
      document.body.style.cursor = "default";
      return;
    }

    const selectedData = [];
    document.querySelectorAll("#tableBody tr").forEach((tr) => {
      const checkbox = tr.querySelector('td input[type="checkbox"]');
      if (checkbox && checkbox.checked) {
        const cells = tr.querySelectorAll("td");
        selectedData.push({
          kodu: ykod,
          paket: cells[4]?.textContent.trim(),
          kons: cells[5]?.textContent.trim(),
          satir: parseInt(cells[21]?.textContent.trim(), 10) || 0
        });
      }
    });

    if (selectedData.length === 0) {
      document.body.style.cursor = "default";
      alert("Lütfen en az bir satır seçin.");
      return;
    }

    try {
      const response = await fetchWithSessionCheck("kereste/ykodkaydet", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ selectedRows: selectedData }),
      });

      if (response.errorMessage) {
        throw new Error(response.errorMessage);
      }
    } catch (error) {
      errorDiv.style.display = "block";
      errorDiv.innerText = error; // senin orijinalin böyleydi
    } finally {
      const tableBody = M.byId("tableBody");
      tableBody.innerHTML = "";
      M.byId("totalSatir").innerText = "0";
      M.clearinput();
      document.body.style.cursor = "default";
    }
  };

  M.ykonsKaydet = async function () {
    const errorDiv = M.byId("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    document.body.style.cursor = "wait";

    const ykons = M.byId("ykons").value.trim();
    const konsadi = M.byId("konsadi").innerText.trim();

    if (!ykons || !konsadi) {
      document.body.style.cursor = "default";
      return;
    }

    const selectedData = [];
    document.querySelectorAll("#tableBody tr").forEach((tr) => {
      const checkbox = tr.querySelector('td input[type="checkbox"]');
      if (checkbox && checkbox.checked) {
        const cells = tr.querySelectorAll("td");
        selectedData.push({
          ykons: ykons,
          kons: cells[5]?.textContent.trim(),
          satir: parseInt(cells[21]?.textContent.trim(), 10) || 0
        });
      }
    });

    if (selectedData.length === 0) {
      document.body.style.cursor = "default";
      alert("Lütfen en az bir satır seçin.");
      return;
    }

    try {
      const response = await fetchWithSessionCheck("kereste/ykonskaydet", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ selectedRows: selectedData }),
      });

      if (response.errorMessage) {
        throw new Error(response.errorMessage);
      }
    } catch (error) {
      errorDiv.style.display = "block";
      errorDiv.innerText = error; // senin orijinalin böyleydi
    } finally {
      const tableBody = M.byId("tableBody");
      tableBody.innerHTML = "";
      M.byId("totalSatir").innerText = "0";
      M.clearinput();
      document.body.style.cursor = "default";
    }
  };

  M.kodadi = async function () {
    document.body.style.cursor = "wait";
    const ukod = M.byId("ykod").value;
    M.byId("kodadi").innerText = "";

    try {
      const response = await fetchWithSessionCheck("kereste/kodadi", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ kod: ukod }),
      });

      if (response.errorMessage) {
        throw new Error(response.errorMessage);
      }
      M.byId("kodadi").innerText = response.urunAdi;
    } catch (error) {
      const errorDiv = M.byId("errorDiv");
      errorDiv.style.display = "block";
      errorDiv.innerText = error.message;
    } finally {
      document.body.style.cursor = "default";
    }
  };

  M.konsadi = async function () {
    document.body.style.cursor = "wait";
    const kons = M.byId("ykons").value;
    M.byId("konsadi").innerText = "";

    try {
      const response = await fetchWithSessionCheck("kereste/konsadi", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({ kons: kons }),
      });

      if (response.errorMessage) {
        throw new Error(response.errorMessage);
      }
      M.byId("konsadi").innerText = response.konsAdi;
    } catch (error) {
      const errorDiv = M.byId("errorDiv");
      errorDiv.style.display = "block";
      errorDiv.innerText = error.message;
    } finally {
      document.body.style.cursor = "default";
    }
  };

  M.clearinput = function () {
    M.byId("ykod").value = "";
    M.byId("kodadi").innerText = "";

    M.byId("ykons").value = "";
    M.byId("konsadi").innerText = "";

    const kons = (M.byId("kons").value = "");
    const kodu = (M.byId("kodu").value = "00-000-0000-0000");
    const evrak = (M.byId("gevrak").value = "");
    const pakno = (M.byId("pakno").value = "");
  };

  /* ---------------- init ---------------- */
  M.init = function () {
	OBS.KODDEGIS.exports();
    M.applyMask();

    // eski $(document).ready yerine DOMContentLoaded
    // ayrıca istersen "kodu" inputunda enter basınca fetchTable yapabiliriz, şimdilik dokunmadım.
  };

  /* ---------------- exports ---------------- */
  // HTML inline onclick’ler için güvenli çağrılar
  M.exports = function () {
    // bu fonksiyonları dışarı görünür yap
    OBS.KODDEGIS.toggleCheckboxes = M.toggleCheckboxes;
    OBS.KODDEGIS.satirsay = M.satirsay;
    OBS.KODDEGIS.fetchTable = M.fetchTable;
    OBS.KODDEGIS.kodKaydet = M.kodKaydet;
    OBS.KODDEGIS.ykonsKaydet = M.ykonsKaydet;
    OBS.KODDEGIS.kodadi = M.kodadi;
    OBS.KODDEGIS.konsadi = M.konsadi;
    OBS.KODDEGIS.clearinput = M.clearinput;
    OBS.KODDEGIS.init = M.init;
    OBS.KODDEGIS.applyMask = M.applyMask;
  };

})(OBS.KODDEGIS);

