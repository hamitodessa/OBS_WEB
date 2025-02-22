async function kodacikyukle() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = "";
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";

    document.body.style.cursor = "wait";
    try {
        const data = await fetchWithSessionCheck("kereste/kodaciklamadoldur", {
            method: "GET",

        });
        if (data.errorMessage) {
            throw new Error(response.errorMessage);
        }
        data.data.forEach(item => {
            const row = document.createElement("tr");
            row.classList.add("table-row-height");
            row.innerHTML = `
                    <td>${item.KOD || ''}</td>
                    <td>${item.ACIKLAMA || ''}</td>
                `;
            row.addEventListener("click", () => setFormValues(row));
            tableBody.appendChild(row);

        });
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message;
    } finally {
        document.body.style.cursor = "default";
    }
}

async function saveKod() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = '';
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
    document.body.style.cursor = "wait";
    try {
        const kod = document.getElementById("kod").value;
        const aciklama = document.getElementById("aciklama").value;
        if (!kod) {
            alert("Kod alanı boş bırakılamaz.");
            return;
        }
        if (!aciklama) {
            alert("Açıklama alanı boş bırakılamaz.");
            return;
        }
        const response = await fetchWithSessionCheck("kereste/kodkaydet", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ kod, aciklama }),
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        kodacikyukle();
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message || "Bir hata oluştu.";
    } finally {
		document.body.style.cursor = "default";
	}
}

async function deleteKod() {
    const errorDiv = document.getElementById("errorDiv");
    errorDiv.style.display = "none";
    errorDiv.innerText = '';
    const tableBody = document.getElementById("tableBody");
    tableBody.innerHTML = "";
    document.body.style.cursor = "wait";
    try {
        const kod = document.getElementById("kod").value;
        if (!kod) {
            alert("Kod alani boş birakilamaz.");
            return;
        }
        if (!aciklama) {
            alert("Açiklama alani boş birakilamaz.");
            return;
        }
        const response = await fetchWithSessionCheck("kereste/koddelete", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ kod}),
        });
        if (response.errorMessage) {
            throw new Error(response.errorMessage);
        }
        kodacikyukle();
    } catch (error) {
        errorDiv.style.display = "block";
        errorDiv.innerText = error.message || "Bir hata oluştu.";
    } finally {
		document.body.style.cursor = "default";
	}
}

function setFormValues(row) {
    const cells = row.cells;
    document.getElementById("kod").value = cells[0].textContent.trim() || "";
    document.getElementById("aciklama").value = cells[1].textContent.trim() || "";
}