async function openurunkodlariModal(inputId, secondnerden,barkodurunkodu) {
  $('#urnsecondModal').modal('show');
  const modalError = document.getElementById("urnsecond-errorDiv");
  modalError.style.display = "none";
  modalError.innerText = "";
  document.body.style.cursor = "wait";
  try {
    const response = await fetchWithSessionCheck("modal/urunkodlari");
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    const data = response;
    const tableBody = document.getElementById("urnsecond-modalTableBody");
    tableBody.innerHTML = "";
    if (data.length === 0) {
      if (modalError) {
        modalError.style.display = "block";
        modalError.innerText = "Hiç veri bulunamadı.";
      }
      return;
    }
    tableBody.classList.add("table-row-height");
    data.forEach((row) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
                   <td>${row.Barkod || ""}</td>
                   <td>${row.Kodu || ""}</td>
                   <td>${row.Adi || ""}</td>
                   <td>${row.Sinif || ""}</td>
									 <td>${row.Birim || ""}</td>
									 <td>${row.Agirlik || ""}</td>
									 <td>${row.Mensei || ""}</td>
									 <td>${row.Ana_Grup || ""}</td>
									 <td>${row.Alt_Grup || ""}</td>
               `;
      tr.onclick = () => urnselectValue(inputId,row.Barkod , row.Kodu, secondnerden,barkodurunkodu);
      tableBody.appendChild(tr);
    });
  } catch (error) {
    const modalError = document.getElementById("urnsecond-errorDiv");
    modalError.style.display = "block";
    modalError.innerText = `Bir hata oluştu: ${error.message}`;
  } finally {
    document.body.style.cursor = "default";
  }
}

function stkfilterTable() {
  const searchValue = document.getElementById("modalSearch").value.toLowerCase();
  const rows = document.querySelectorAll("#modalTable tbody tr");
  rows.forEach((row) => {
    const rowText = Array.from(row.cells)
      .map((cell) => cell.textContent.toLowerCase())
      .join(" ");
    row.style.display = rowText.includes(searchValue) ? "" : "none";
  });
}

function urnselectValue(inputId, selectedBarkod, selectedKodu, secondnerden, barkodurunkodu) {
	const inputElementm = document.getElementById(inputId);
	document.getElementById("urnsecond-modalSearch").value = "";
	$('#urnsecondModal').modal('hide');

	if (inputElementm) {
		if (barkodurunkodu === "ukodukod") {
			inputElementm.value = selectedKodu;
		} else if (barkodurunkodu === "barkod") {
			inputElementm.value = selectedBarkod;
		}
	}
	if (secondnerden === "imalat") {
		urnaramaYap();
	}
	else if (secondnerden === "imalatsatir") {
		const event = new Event('change', { bubbles: true });
		inputElementm.dispatchEvent(event);
	}
  else if (secondnerden === "recetealt") {
    console.info("recetealt");
		const event = new Event('change', { bubbles: true });
		inputElementm.dispatchEvent(event);
	}
  else if (secondnerden === "recetesatir") {
    console.info("recetesatir");
		const event = new Event('change', { bubbles: true });
		inputElementm.dispatchEvent(event);
	}

}

