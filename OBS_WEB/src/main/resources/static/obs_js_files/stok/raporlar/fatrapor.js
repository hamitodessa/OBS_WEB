async function anagrpChanged(anagrpElement) {
  const anagrup = anagrpElement.value;
  const errorDiv = document.getElementById("errorDiv");
  const selectElement = document.getElementById("altgrp");
  selectElement.innerHTML = '';
  if (anagrup === "") {
    selectElement.disabled = true;
    return;
  }
  document.body.style.cursor = "wait";
  errorDiv.style.display = "none";
  errorDiv.innerText = "";
  try {
    const response = await fetchWithSessionCheck("stok/altgrup", {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ anagrup: anagrup }),
    });
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    response.altKodlari.forEach(kod => {
      const option = document.createElement("option");
      option.value = kod.ALT_GRUP;
      option.textContent = kod.ALT_GRUP;
      selectElement.appendChild(option);
    });
    selectElement.disabled = selectElement.options.length === 0;
  } catch (error) {
    selectElement.disabled = true;
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    document.body.style.cursor = "default";
  }
}

async function fatfetchTableData() {
	const hiddenFieldValue = $('#fatrapBilgi').val();
	const parsedValues = hiddenFieldValue.split(",");
	const fatraporDTO = {
		fatno1: parsedValues[0],
		fatno22: parsedValues[1],
		anagrp: parsedValues[2],
		tar1: parsedValues[3],
		tar2: parsedValues[4],
		altgrp: parsedValues[5],
		ckod1: parsedValues[6],
		ckod2: parsedValues[7],
		depo: parsedValues[8],
		adr1: parsedValues[9],
		adr2: parsedValues[10],
		turu: parsedValues[11],
		ukod1: parsedValues[12],
		ukod2: parsedValues[13],
		tev1: parsedValues[14],
		tev2: parsedValues[15],
		okod1: parsedValues[16],
		okod2: parsedValues[17],
		gruplama: parsedValues[18],
		dvz1: parsedValues[19],
		dvz2: parsedValues[20],
		caradr: parsedValues[21],
			};
      const errorDiv = document.getElementById("errorDiv");
      document.body.style.cursor = "wait";
      const $yenileButton = $('#fatrapyenileButton');
      $yenileButton.prop('disabled', true).text('İşleniyor...');
      const mainTableBody = document.getElementById("mainTableBody");
      mainTableBody.innerHTML = "";
    
      try {
        const response = await fetchWithSessionCheck("stok/fatrapdoldur", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(fatraporDTO),
        });
        data = response;
        if (data.success) {
          data.data.forEach(rowData => {
            const row = document.createElement('tr');
            row.classList.add('expandable');
            row.classList.add("table-row-height");
            row.innerHTML = `
              <td class="toggle-button">+</td>
                    <td>${rowData.Fatura_No || ''}</td>
                    <td>${rowData.Hareket || ''}</td>
                    <td>${formatDate(rowData.Tarih)}</td>
                    <td>${rowData.Cari_Firma || ''}</td>
                    <td>${rowData.Adres_Firma || ''}</td>
                    <td>${rowData.Doviz || ''}</td>
                    <td class="double-column">${formatNumber3(rowData.Miktar)}</td>
                    <td class="double-column">${formatNumber3(rowData.Tutar)}</td>
                    <td class="double-column">${formatNumber2(rowData.Iskontolu_Tutar)}</td>
                `;
            mainTableBody.appendChild(row);
            const detailsRow = document.createElement('tr');
            detailsRow.classList.add('details-row');
            detailsRow.innerHTML = `<td colspan="12"></td>`;
            mainTableBody.appendChild(detailsRow);
           
              row.addEventListener('click', async () => {
                const toggleButton = row.querySelector('.toggle-button');
                const isVisible = detailsRow.style.display === 'table-row';
                detailsRow.style.display = isVisible ? 'none' : 'table-row';
                toggleButton.textContent = isVisible ? '+' : '-';
                document.body.style.cursor = "wait";
                if (!isVisible) {
                  try {
                    const details = await fetchDetails(rowData.Fatura_No, rowData.Hareket);
                    const data = details.data;
                    let detailsTable = `
                                <table class="details-table table table-bordered table-hover">
                                    <thead class="thead-dark">
                                        <tr>
                                            <th>Kodu</th>
                                            <th>Adi</th>
                                            <th>Miktar</th>
                                            <th>Birim</th>
                                            <th style="text-align: right;">Fiat</th>
                                            <th>Doviz</th>
                                            <th style="text-align: right;">Tutar</th>
                                            <th style="text-align: right;">Iskonto</th>
                                            <th style="text-align: right;">Iskonto_Tutar</th>
                                            <th style="text-align: right;">Iskontolu_Tutar</th>
                                            <th style="text-align: right;">Kdv</th>
                                            <th style="text-align: right;">Kdv Tutar</th>
                                            <th style="text-align: right;">Tevkifat</th>
                                            <th style="text-align: right;">Tev Edi.Kdv.</th>
                                            <th style="text-align: right;">Tev Dah Top. Tut.</th>
                                            <th style="text-align: right;">Beyan Edilen Kdv</th>
                                            <th style="text-align: right;">Tev Har TopTutar</th>
                                            <th>Ana Grup</th>
                                            <th>Alt Grup</th>
                                            <th>Depo</th>
                                            <th>Ozel Kod</th>
                                            <th>Izahat</th>
                                            <th>Hareket</th>
                                            <th>User</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                            `;
                    data.forEach(item => {
                      detailsTable += `
                                    <tr>
                                        <td>${item.Kodu || ''}</td>
                                        <td>${item.Adi || ''}</td>
                                        <td style="text-align: right;">${formatNumber3(item.Miktar)}</td>
                                        <td>${item.Birim || ''}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Fiat)}</td>
                                        <td>${item.Doviz || ''}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskonto)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskonto_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Iskontolu_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Kdv)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Kdv_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tevkifat)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tev_Edilen_KDV)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tev_Dah_Top_Tutar)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Beyan_Edilen_KDV)}</td>
                                        <td style="text-align: right;">${formatNumber2(item.Tev_Har_Top_Tutar)}</td>
                                        <td >${item.Ana_Grup}</td>
                                        <td >${item.Alt_Grup}</td>
                                        <td >${item.Depo}</td>
                                        <td >${item.Ozel_Kod}</td>
                                        <td >${item.Izahat}</td>
                                        <td >${item.Hareket}</td>
                                        <td >${item.USER}</td>
                                    </tr>
                                `;
                              });
                    detailsTable += `
                                    </tbody>
                                </table>
                            `;
                    detailsRow.children[0].classList.add("table-row-height");
                    detailsRow.children[0].innerHTML = detailsTable;
                    document.body.style.cursor = "default";
                  } catch (error) {
                    detailsRow.children[0].innerHTML = `
                                <strong>Hata:</strong> Detay bilgileri alınamadı.
                            `;
                    document.body.style.cursor = "default";
                  }
                }
                document.body.style.cursor = "default";
              });
           
          });
        } else {
          errorDiv.style.display = "block";
          errorDiv.innerText = data.errorMessage || "Bir hata oluştu.";
        }
        document.body.style.cursor = "default";
      } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error;
      } finally {
        $yenileButton.prop('disabled', false).text('Yenile');
        document.body.style.cursor = "default";
      }
    }