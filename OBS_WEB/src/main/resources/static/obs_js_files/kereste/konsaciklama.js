async function konsacikyukle() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
    document.body.style.cursor = "wait";
    try {
        const data = await fetchWithSessionCheck("kereste/konsaciklamadoldur", {
            method: "GET",
        });
        if (data.errorMessage) {
            throw new Error(response.errorMessage);
        }
        console.info(data);
        data.data.forEach(item => {
            const row = document.createElement("tr");
            row.classList.add("table-row-height");
            row.innerHTML = `
                    <td>${item.KONS || ''}</td>
                    <td>${item.ACIKLAMA || ''}</td>
                `;
            row.addEventListener("click", () => setFormValues(row));
            tableBody.appendChild(row);
        });
				document.getElementById("kons").value = "";
				document.getElementById("aciklama").value = "";
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message;
    } finally {
        document.body.style.cursor = "default";
    }
}

async function saveKons() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = '';
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
    document.body.style.cursor = "wait";
    try {
        const kons = document.getElementById("kons").value;
        const aciklama = document.getElementById("aciklama").value;
        if (!kons) {
            alert("Konsimento alanı boş bırakılamaz.");
            return;
        }
        if (!aciklama) {
            alert("Açıklama alanı boş bırakılamaz.");
            return;
        }
        const response = await fetchWithSessionCheck("kereste/konskaydet", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ kons, aciklama }),
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        konsacikyukle();
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message || "Bir hata oluştu.";
    } finally {
		document.body.style.cursor = "default";
	}
}

async function deleteKons() {
	const confirmDelete = confirm("Bu Konsimento silinecek ?");
	    if (!confirmDelete) {
	      return;
	    }
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = '';
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
    document.body.style.cursor = "wait";
    try {
        const kons = document.getElementById("kons").value;
        if (!kons) {
            alert("Konsimento alani boş birakilamaz.");
            return;
        }
        const response = await fetchWithSessionCheck("kereste/konsdelete", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ kons}),
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        konsacikyukle();
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message || "Bir hata oluştu.";
    } finally {
		document.body.style.cursor = "default";
	}
}

function setFormValues(row) {
    const cells = row.cells;
    document.getElementById("kons").value = cells[0].textContent.trim() || "";
    document.getElementById("aciklama").value = cells[1].textContent.trim() || "";
}