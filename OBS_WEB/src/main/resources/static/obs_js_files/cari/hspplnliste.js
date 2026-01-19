function aramaYap() {
  const input = document.getElementById("arama");
  const table = document.getElementById("myTable");
  if (!input || !table) return;

  const filter = input.value.trim().toLowerCase();
  const rows = table.tBodies[0]?.rows || table.rows;

  for (let i = 0; i < rows.length; i++) {
    const cells = rows[i].cells;
    let show = false;

    // sadece 1. ve 2. sütun
    if (cells.length >= 2) {
      const col1 = cells[0].textContent.toLowerCase();
      const col2 = cells[1].textContent.toLowerCase();
      show = col1.includes(filter) || col2.includes(filter);
    }

    rows[i].style.display = show ? "" : "none";
  }
}