currentPage = 0;
totalPages = 0;
pageSize = 500;

function setDisabled(el, yes) { el.disabled = !!yes; }

function updatePaginationUI() {
  const first = document.getElementById("ilksayfa");
  const prev  = document.getElementById("oncekisayfa");
  const next  = document.getElementById("sonrakisayfa");
  const last  = document.getElementById("sonsayfa");

  const noData = totalPages === 0;

  setDisabled(first, noData || currentPage <= 0);
  setDisabled(prev,  noData || currentPage <= 0);

  // Sonraki/Son: son sayfadaysa ya da hiç veri yoksa kapat
  setDisabled(next, noData || currentPage >= totalPages - 1);
  setDisabled(last, noData || currentPage >= totalPages - 1);
}

function ilksayfa()      { if (currentPage > 0)           lograpor(0); }
function oncekisayfa()   { if (currentPage > 0)           lograpor(currentPage - 1); }
function sonrakisayfa()  { if (currentPage < totalPages-1) lograpor(currentPage + 1); }
function sonsayfa()      { if (totalPages > 0)            lograpor(totalPages - 1); }

async function toplampagesize() {
  const errorDiv = document.getElementById("errorDiv");
  try {
    errorDiv.style.display = "none";
    errorDiv.innerText = "";

    const modul = document.getElementById("user_modul").value;
    const startDate = document.getElementById("startDate").value;
    const endDate = document.getElementById("endDate").value;
	const aciklama = document.getElementById("aciklama").value;
    const resp = await fetchWithSessionCheck("user/logsize", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ modul, startDate, endDate ,aciklama})
    });

    const totalRecords = resp.totalRecords ?? 0;
    totalPages = Math.ceil(totalRecords / pageSize);
    if (totalPages < 0) totalPages = 0;
  } catch (err) {
    errorDiv.style.display = "block";
    errorDiv.innerText = err?.message || err || "Beklenmeyen hata.";
    totalPages = 0;
  } finally {
    updatePaginationUI();
  }
}

async function logdoldur() {
  document.body.style.cursor = "wait";
  await toplampagesize();       // <-- ÖNEMLİ: bekle
  await lograpor(0);
  document.body.style.cursor = "default";
}

async function lograpor(page) {
  const modul = document.getElementById("user_modul").value;
  const startDate = document.getElementById("startDate").value;
  const endDate = document.getElementById("endDate").value;
  const aciklama = document.getElementById("aciklama").value;

  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";

  currentPage = page;

  try {
    const response = await fetchWithSessionCheck("user/loglistele", {
      method: 'POST',
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ modul, startDate, endDate, aciklama, page, pageSize })
    });

    if (response.errorMessage) throw new Error(response.errorMessage);

    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";

    if (response.success) {
      response.data.forEach(item => {
        const row = document.createElement("tr");
        row.classList.add("table-row-height");
        row.innerHTML = `
          <td>${item.TARIH || ''}</td>
          <td>${item.MESAJ || ''}</td>
          <td>${item.EVRAK || ''}</td>
          <td>${item.USER_NAME || ''}</td>
        `;
        tableBody.appendChild(row);
      });
    }
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    updatePaginationUI(); // <-- her yüklemeden sonra güncelle
  }
}
