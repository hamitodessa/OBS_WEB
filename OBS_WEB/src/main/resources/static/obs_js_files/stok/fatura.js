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
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur3(this)" 
			  onkeydown="focusNextCell(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur3(this)"
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
		     onkeydown="focusNextRow(event, this)" value="${formatNumber2(0)}" style="text-align:right;">
		</td>
        <td>
		     <input class="form-control" onfocus="selectAllContent(this)" onblur="handleBlur(this)"
		     onkeydown="focusNextRow(event, this)" value="" style="text-align:right;">
		</td>
	    `;
}
