async function kurrapfetchTableData() {
  const hiddenFieldValue = document.getElementById("kurraporBilgi")?.value || "";
  const parsedValues = hiddenFieldValue.split(",");

  const startDate = parsedValues[0];
  const endDate   = parsedValues[1];
  const cins1     = parsedValues[2];
  const cins2     = parsedValues[3];

  const errorDiv = document.getElementById("errorDiv");
  errorDiv.style.display = "none";
  errorDiv.innerText = "";

  const kurraporDTO = { startDate, endDate, cins1, cins2 };

  const tableBody = document.getElementById("tableBody");
  tableBody.innerHTML = "";

  document.body.style.cursor = "wait";

  const yenileBtn = document.getElementById("yenileButton");
  if (yenileBtn) {
    yenileBtn.disabled = true;
    yenileBtn.innerText = "İşleniyor...";
  }

  try {
    const data = await fetchWithSessionCheck("kur/kurrapor", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(kurraporDTO),
    });

    if (data?.errorMessage) {
      throw new Error(data.errorMessage);
    }

    if (data.success && Array.isArray(data.data)) {
      data.data.forEach(item => {
        const row = document.createElement("tr");
        row.classList.add("table-row-height");
        row.innerHTML = `
          <td>${formatDate(item.Tarih)}</td>
          <td>${item.Kur || ""}</td>
          <td class="double-column">${formatNumber4(item.MA)}</td>
          <td class="double-column">${formatNumber4(item.MS)}</td>
          <td class="double-column">${formatNumber4(item.SA)}</td>
          <td class="double-column">${formatNumber4(item.SS)}</td>
          <td class="double-column">${formatNumber4(item.BA)}</td>
          <td class="double-column">${formatNumber4(item.BS)}</td>
        `;
        tableBody.appendChild(row);
      });
    } else {
      errorDiv.style.display = "block";
      errorDiv.innerText = data.errorMessage || "Bir hata oluştu.";
    }

  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Beklenmeyen hata.";
  } finally {
    document.body.style.cursor = "default";
    if (yenileBtn) {
      yenileBtn.disabled = false;
      yenileBtn.innerText = "Yenile";
    }
  }
}
