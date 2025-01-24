let lastFocusedRow = null;

document.querySelector('table').addEventListener('focusin', (event) => {
    let currentRow = event.target.closest('tr'); 
    if (currentRow && currentRow !== lastFocusedRow) {
        console.log('Row changed:', currentRow);
        lastFocusedRow = currentRow; 
    }
});

async function fetchkoddepo() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.innerText = "";
    errorDiv.style.display = "none";
    rowCounter = 0;
    depolar = "";
    urnkodlar = "";
    try {
        const response = await fetchWithSessionCheck("stok/stkgeturndepo", {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
        });
        if (response.errorMessage) {
            throw new Error(responseBanka.errorMessage);
        }
        urnkodlar = response.urnkodlar || [];
        depolar = response.depolar || [];
        initializeRows();
    } catch (error) {
        errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
        errorDiv.style.display = "block";
    }
}

function initializeRows() {

    rowCounter = 0;
    for (let i = 0; i < 5; i++) {
        satirekle();
    }
}
function satirekle() {
    const table = document.getElementById("fatTable").getElementsByTagName("tbody")[0];
    const newRow = table.insertRow();
    incrementRowCounter();

    let ukoduoptionsHTML = urnkodlar.map(kod => `<option value="${kod.Kodu}">${kod.Kodu}</option>`).join("");
    newRow.innerHTML = `
		<td >
			<button id="bsatir_${rowCounter}" type="button" class="btn btn-secondary ml-2" onclick="satirsil(this)"><i class="fa fa-trash"></i></button>
		</td>
   		<td>
		    <div style="position: relative; width: 100%;">
		        <input class="form-control cins_bold" list="barkodOptions_${rowCounter}" maxlength="12" id="barkod_${rowCounter}" 
		            onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('barkod_${rowCounter}', 'fatsatir','barkodkod')" onchange="updateRowValues(this)">
		        <datalist id="barkodOptions_${rowCounter}"></datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		    </div>
		</td>

		<td>
		    <div style="position: relative; width: 100%;">
		        <input class="form-control cins_bold" list="ukoduOptions_${rowCounter}" maxlength="12" id="ukodu_${rowCounter}" 
		            onkeydown="focusNextCell(event, this)" ondblclick="openurunkodlariModal('ukodu_${rowCounter}', 'imalatsatir','ukodukod')" onchange="updateRowValues(this)">
		        <datalist id="ukoduOptions_${rowCounter}">${ukoduoptionsHTML}</datalist>
		        <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		    </div>
		</td>
		<td>
		<div style="position: relative; width: 100%;">
		    <select class="form-control" id="depo_${rowCounter}">
		        ${depolar.map(kod => `
		            <option value="${kod.DEPO}" >
		                ${kod.DEPO}
		            </option>
		        `).join('')}
		    </select>
		    <span style="position: absolute; top: 50%; right: 10px; transform: translateY(-50%); pointer-events: none;"> ▼ </span>
		</div>
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)" 
			  onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"
			  onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur3(this)"
			  onkeydown="focusNextCell(event, this)" value="${formatNumber3(0)}" style="text-align:right;">
		</td>
		<td>
			<label class="form-control" style="display: block;width:100%;height:100%;"><span>&nbsp;</span></label>
		</td>
		<td>
			<input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)" 
			onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;" >
		</td>
		<td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"  
		     onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"
		     onkeydown="focusNextRow(event, this)" value="" style="text-align:right;">
		</td>
	    `;
}
function handleBlur3(input) {
    input.value = formatNumber3(input.value);
    updateColumnTotal();
}
function handleBlur(input) {
    input.value = formatNumber2(input.value);
    updateColumnTotal();
}

function selectAllContent(element) {
  if (element && element.select) {
     element.select();
  }
}

function updateColumnTotal() {
  const rows = document.querySelectorAll('table tr');
  const totalTutarCell = document.getElementById("totalTutar");
	const totalMiktarCell =  document.getElementById("totalMiktar"); 
	const tevoran =  document.getElementById("tevoran"); 
	
  let total = 0;
	let totalmik = 0;
	let double_1 = 0 ;
	let double_2 = 0 ;
	let double_5 = 0 ;
	
  totalTutarCell.textContent = "0.00";
	totalMiktarCell.textContent = "0.000";
  rows.forEach(row => {
		const fiat = row.querySelector('td:nth-child(5) input');
    const iskonto = row.querySelector('td:nth-child(6) input');
		const miktar = row.querySelector('td:nth-child(7) input');
    const kdv = row.querySelector('td:nth-child(9) input');
    const tutar = row.querySelector('td:nth-child(10) input');

    if (fiat &&  miktar ) {
      const fia = parseLocaleNumber(fiat.value) || 0;
      const mik = parseLocaleNumber(miktar.value) || 0;
      const result = fia * mik;
			
      tutar.value = result.toLocaleString(undefined, {
        minimumFractionDigits: 2, maximumFractionDigits: 2
      });
			
			totalmik += mik;
      if (result > 0) {
        total += result;
				double_5 += tutar;
				    double_1 += (tutar * iskonto) / 100 ; 
				    double_2 += ( (tutar - (tutar * iskonto / 100) *  kdv) ) / 100 ; 
      }
    }
		
  });
	document.getElementById("urunadi").innerText = iskonto;
	document.getElementById("bakiye").innerText = double_5 - double_1;
	document.getElementById("kdv").innerText = double_2;
	
	///
  totalTutarCell.textContent = total.toLocaleString(undefined, {
    minimumFractionDigits: 2, maximumFractionDigits: 2
  });
	totalMiktarCell.textContent = totalmik.toLocaleString(undefined, {
	    minimumFractionDigits: 3, maximumFractionDigits: 3
	  });
		
 }

async function anagrpChanged(anagrpElement) {
  const anagrup = anagrpElement.value;
  const selectElement = document.getElementById("altgrp");
  const errorDiv = document.getElementById("errorDiv");
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

async function updateRowValues(inputElement) {
  const selectedValue = inputElement.value;
  const uygulananfiat = document.getElementById("uygulananfiat").value;
  const barkod = "";
	const gircikdeger = document.getElementById("gircik").value;
	const carikod = document.getElementById("carikod").value;
	
  document.body.style.cursor = "wait";
  errorDiv.style.display = "none";
  errorDiv.innerText = "";
  try {
    const response = await fetchWithSessionCheck("stok/urunoku", {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ ukodu: selectedValue, barkod: barkod, fiatlama: uygulananfiat, gircik: gircikdeger,ckod : carikod }),
    });
    if (response.errorMessage) {
      throw new Error(response.errorMessage);
    }
    const row = inputElement.closest('tr');
    const cells = row.querySelectorAll('td');
   
		const barkodCell = cells[1]?.querySelector('input');
    const fiatCell = cells[4]?.querySelector('input');
		const birimCell = cells[7];
    setLabelContent(birimCell, response.dto.birim);

		barkodCell.value = response.dto.barkod;
		fiatCell.value = formatNumber2(response.fiat);
		
		document.getElementById("urunadi").innerText = response.dto.adi;
		document.getElementById("anaalt").innerText = response.dto.anagrup + " / "+ response.dto.altgrup;
		
		const imgElement = document.getElementById("resimGoster");
		    if (response.dto.base64Resim && response.dto.base64Resim.trim() !== "") {
		      const base64String = 'data:image/jpeg;base64,' + response.dto.base64Resim.trim();
		      imgElement.src = base64String;
		      imgElement.style.display = "block";
		    } else {
		      imgElement.src = "";
		      imgElement.style.display = "none";
		    }
				
  } catch (error) {
    errorDiv.style.display = "block";
    errorDiv.innerText = error.message || "Beklenmeyen bir hata oluştu.";
  } finally {
    document.body.style.cursor = "default";
  }
}

function setLabelContent(cell, content) {
    const span = cell.querySelector('label span');
    if (span) {
        span.textContent = content ? content : '\u00A0';
    }
}
function focusNextRow(event, element) {
  if (event.key === "Enter") {
    event.preventDefault();
    const currentRow = element.closest('tr');
    const nextRow = currentRow.nextElementSibling;
    if (nextRow) {
      const secondInput = nextRow.querySelector("td:nth-child(3) input");
      if (secondInput) {
        secondInput.focus();
        secondInput.select();
      }
    } else {
      satirekle();
      const table = currentRow.parentElement;
      const newRow = table.lastElementChild;
      const secondInput = newRow.querySelector("td:nth-child(3) input");
      if (secondInput) {
        secondInput.focus();
        secondInput.select();
      }
    }
  }
}

function focusNextCell(event, element) {
    if (event.key === "Enter") {
        event.preventDefault();
        let currentCell = element.closest('td');
        let nextCell = currentCell.nextElementSibling;
        while (nextCell) {
            const focusableElement = nextCell.querySelector('input, select');
            if (focusableElement) {
                focusableElement.focus();
                if (focusableElement.select) {
                    focusableElement.select();
                }
                break;
            } else {
                nextCell = nextCell.nextElementSibling;
            }
        }
    }
}
