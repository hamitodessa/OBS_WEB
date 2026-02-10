/* ==========================================================
   OBS.KERGIRIS - jQuery'siz tek dosya
   - window.OBS namespace
   - init() ile başlar
   - event handler'lar OBS.KERGIRIS.xxx şeklinde çağrılır
   ========================================================== */

(function(window, document) {
    window.OBS = window.OBS || {};
    const OBS = window.OBS;

    OBS.KERGIRIS = OBS.KERGIRIS || {};
    const K = OBS.KERGIRIS;

    /* ========================= helpers ========================= */
    K.byId = (id) => document.getElementById(id);

    K.setCursor = (mode) => { document.body.style.cursor = mode || "default"; };

    K.showError = (msg) => {
        const e = K.byId("errorDiv");
        if (!e) return;
        e.style.display = "block";
        e.innerText = msg || "Beklenmeyen bir hata oluştu.";
    };

    K.clearError = () => {
        const e = K.byId("errorDiv");
        if (!e) return;
        e.style.display = "none";
        e.innerText = "";
    };

    K.setBtnBusy = (btnId, busy, busyText, normalText) => {
        const btn = K.byId(btnId);
        if (!btn) return;
        btn.disabled = !!busy;
        if (busy && busyText != null) btn.textContent = busyText;
        if (!busy && normalText != null) btn.textContent = normalText;
    };

    K._num = (v) => {
        // parseLocaleNumber senin global fonksiyonun; varsa onu kullan
        if (typeof window.parseLocaleNumber === "function") return window.parseLocaleNumber(v);
        // fallback (basit)
        const s = (v ?? "").toString().replace(/\./g, "").replace(",", ".");
        const n = parseFloat(s);
        return isNaN(n) ? 0 : n;
    };

    K._fmt0 = (n) => (typeof window.formatNumber0 === "function" ? window.formatNumber0(n) : (Number(n) || 0).toFixed(0));
    K._fmt2 = (n) => (typeof window.formatNumber2 === "function" ? window.formatNumber2(n) : (Number(n) || 0).toFixed(2));
    K._fmt3 = (n) => (typeof window.formatNumber3 === "function" ? window.formatNumber3(n) : (Number(n) || 0).toFixed(3));

    /* ========================= state ========================= */
    K.rowCounter = 0;

    K.incrementRowCounter = () => { K.rowCounter++; };

    /* ========================= mask ========================= */
    K.applyMask = function() {
        const inputs = document.querySelectorAll("input[id^='ukodu_']");

        inputs.forEach((inp) => {
            // aynı input'a iki kez bağlanma
            if (inp.dataset.maskBound === "1") return;
            inp.dataset.maskBound = "1";

            // Inputmask tanımı
            Inputmask({
                mask: "AA-999-9999-9999",
                placeholder: "_",
                clearIncomplete: false,
                showMaskOnHover: false,
                showMaskOnFocus: true,
                definitions: {
                    "A": {
                        validator: "[A-Za-z0-9]",
                        casing: "upper"
                    }
                },
                // paste / manuel girişte format garanti
                onBeforeWrite: function(event, buffer, caretPos, opts) {
                    const raw = buffer.join("").replace(/[^A-Z0-9]/gi, "");
                    const formatted = OBS.KERGIRIS._formatUkodu(raw);
                    return {
                        refreshFromBuffer: true,
                        buffer: formatted.split(""),
                        caret: formatted.length
                    };
                }
            }).mask(inp);
        });
    };
    OBS.KERGIRIS._formatUkodu = function(val) {
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

    /* ========================= paket m3 ========================= */
    K.updatePaketM3 = () => {
        const rows = document.querySelectorAll("table tr");
        const paketMap = new Map();
        let paketadet = 0;

        rows.forEach((row) => {
            const paketnoCell = row.querySelector("td:nth-child(4) input");
            const m3Cell = row.querySelector("td:nth-child(6) label span");
            const paketm3Span = row.querySelector("td:nth-child(7) span");

            if (!paketnoCell || !m3Cell || !paketm3Span) return;

            const paketNo = (paketnoCell.value || "").trim();
            const m3Value = K._num((m3Cell.textContent || "").trim()) || 0;
            if (!paketNo) return;

            if (!paketMap.has(paketNo)) paketMap.set(paketNo, { total: 0, rows: [] });

            const entry = paketMap.get(paketNo);
            entry.total += m3Value;
            entry.rows.push(paketm3Span);
        });

        paketMap.forEach((data) => {
            const rowCount = data.rows.length;
            data.rows.forEach((cell, index) => {
                if (index === rowCount - 1) {
                    cell.textContent = data.total ? Number(data.total).toFixed(3) : "\u00A0";
                    paketadet += 1;
                } else {
                    cell.textContent = "\u00A0";
                }
            });
        });

        const el = K.byId("totalPakadet");
        if (el) el.textContent = "Paket:" + K._fmt0(paketadet);
    };

    /* ========================= rows ========================= */
    K.initializeRows = () => {
        K.rowCounter = 0;
        for (let i = 0;i < 5;i++) K.satirekle();
    };

    K.satirekle = () => {
        const table = K.byId("kerTable");
        if (!table) return;

        const tbody = table.getElementsByTagName("tbody")[0];
        if (!tbody) return;

        const rowCount = tbody.rows.length;
        if (rowCount === 250) {
            alert("En fazla 250 satır ekleyebilirsiniz.");
            return;
        }

        const newRow = tbody.insertRow();
        K.incrementRowCounter();

        newRow.innerHTML = `
      <td>
        <button id="bsatir_${K.rowCounter}" type="button"
          class="btn btn-secondary ml-2 ker-rowbtn"
          onclick="OBS.KERGIRIS.satirsil(this)">
          <i class="fa fa-trash"></i>
        </button>
      </td>

      <td>
        <input class="form-control ker-cell" maxlength="20" id="barkod_${K.rowCounter}"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)"
          onchange="OBS.KERGIRIS.updateRowValues(this)">
      </td>

      <td>
        <input class="form-control ker-cell" maxlength="16" id="ukodu_${K.rowCounter}"
          placeholder="XX-XXX-XXXX-XXXX"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)"
          onchange="OBS.KERGIRIS.updateValues(this)">
      </td>

      <td>
        <input class="form-control ker-cell" maxlength="16" id="pakno_${K.rowCounter}"
          onkeydown="if(event.key === 'Enter') OBS.KERGIRIS.paketkontrol(event,this)">
      </td>

      <td>
        <input class="form-control ker-cell ker-right"
          onfocus="OBS.KERGIRIS.selectAllContent(this)"
          onblur="OBS.KERGIRIS.handleBlur0(this)"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)"
          value="${K._fmt0(0)}">
      </td>

      <td>
        <label class="form-control ker-cell ker-label ker-right">
          <span>&nbsp;</span>
        </label>
      </td>

      <td>
        <label class="form-control ker-cell ker-label ker-right" style="color:darkgreen;">
          <span>&nbsp;</span>
        </label>
      </td>

      <td>
        <input class="form-control ker-cell"
          onfocus="OBS.KERGIRIS.selectAllContent(this)"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)">
      </td>

      <td>
        <input class="form-control ker-cell ker-right"
          onfocus="OBS.KERGIRIS.selectAllContent(this)"
          onblur="OBS.KERGIRIS.handleBlur(this)"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)"
          value="${K._fmt2(0)}">
      </td>

      <td>
        <input class="form-control ker-cell ker-right"
          onfocus="OBS.KERGIRIS.selectAllContent(this)"
          onblur="OBS.KERGIRIS.handleBlur(this)"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)"
          value="${K._fmt2(0)}">
      </td>

      <td>
        <input class="form-control ker-cell ker-right"
          onfocus="OBS.KERGIRIS.selectAllContent(this)"
          onblur="OBS.KERGIRIS.handleBlur(this)"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)"
          value="${K._fmt2(0)}">
      </td>

      <td>
        <input class="form-control ker-cell ker-right"
          onfocus="OBS.KERGIRIS.selectAllContent(this)"
          onblur="OBS.KERGIRIS.handleBlur(this)"
          onkeydown="OBS.KERGIRIS.focusNextCell(event, this)"
          value="${K._fmt2(0)}">
      </td>

      <td>
        <input class="form-control ker-cell"
          onfocus="OBS.KERGIRIS.selectAllContent(this)"
          onkeydown="OBS.KERGIRIS.focusNextRow(event, this)">
      </td>

      <!-- gizli kolonlar (senin mevcut yapın birebir) -->
      <td style="display:none;"></td>
      <td style="display:none;">1900-01-01 00:00:00.000</td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
      <td style="display:none;">0</td>
      <td style="display:none;">0</td>
      <td style="display:none;">0</td>
      <td style="display:none;">0</td>
      <td style="display:none;"></td>
      <td style="display:none;">0</td>
      <td style="display:none;"></td>
      <td style="display:none;"></td>
    `;

        K.applyMask();
    };

    K.satirsil = (button) => {
        const row = button?.closest("tr");
        if (row) row.remove();
        K.updateColumnTotal();
    };

    /* ========================= blur/format ========================= */
    K.handleBlur3 = (input) => {
        K.updateValues(input);
        input.value = K._fmt3(K._num(input.value));
        K.updateColumnTotal();
    };

    K.handleBlur = (input) => {
        K.updateValues(input);
        input.value = K._fmt2(K._num(input.value));
        K.updateColumnTotal();
    };

    K.handleBlur0 = (input) => {
        K.updateValues(input);
        input.value = K._fmt0(K._num(input.value));
        K.updateColumnTotal();
    };

    K.selectAllContent = (el) => { if (el && el.select) el.select(); };

    /* ========================= totals ========================= */
    K.updateColumnTotal = () => {
        const rows = document.querySelectorAll("table tr");

        const totalSatirCell = K.byId("totalSatir");
        const totalTutarCell = K.byId("totalTutar");
        const totalMiktarCell = K.byId("totalMiktar");
        const totalM3Cell = K.byId("totalM3");
        const totalPaketM3Cell = K.byId("totalPaketM3");
        const tevoran = K.byId("tevoran");

        let total = 0;
        let totalm3 = 0;
        let totalpakm3 = 0;
        let totalMiktar = 0;

        let double_1 = 0;
        let double_2 = 0;
        let double_5 = 0;
        let double_4 = 0;
        let totalsatir = 0;

        if (totalSatirCell) totalSatirCell.textContent = "0";
        if (totalTutarCell) totalTutarCell.textContent = "0.00";
        if (totalMiktarCell) totalMiktarCell.textContent = "0";
        if (totalM3Cell) totalM3Cell.textContent = "0.000";
        if (totalPaketM3Cell) totalPaketM3Cell.textContent = "0.000";

        rows.forEach((row) => {
            const ukoduInput = row.querySelector("td:nth-child(3) input");
            const mik = row.querySelector("td:nth-child(5) input");
            const m3 = row.querySelector("td:nth-child(6) label span");
            const pm3 = row.querySelector("td:nth-child(7) label span");
            const fiat = row.querySelector("td:nth-child(9) input");
            const iskonto = row.querySelector("td:nth-child(10) input");
            const kdvv = row.querySelector("td:nth-child(11) input");
            const tutar = row.querySelector("td:nth-child(12) input");

            if (ukoduInput && (ukoduInput.value || "").trim() !== "") totalsatir += 1;

            if (fiat && m3 && tutar) {
                const isk = K._num(iskonto?.value) || 0;
                const kdv = K._num(kdvv?.value) || 0;
                const fia = K._num(fiat.value) || 0;
                const m33 = K._num((m3.textContent || "").trim()) || 0;

                const result = fia * m33;

                tutar.value = K._fmt2(result);

                totalm3 += m33;
                totalpakm3 += K._num((pm3?.textContent || "").trim()) || 0;
                totalMiktar += mik?.value ? (K._num(mik.value) || 0) : 0;

                if (result > 0) {
                    total += result;
                    double_5 += result;
                    double_1 += (result * isk) / 100;
                    double_2 += ((result - (result * isk) / 100) * kdv) / 100;
                }
            }
        });

        const iskontoEl = K.byId("iskonto");
        const bakiyeEl = K.byId("bakiye");
        const kdvEl = K.byId("kdv");
        const tevedkdvEl = K.byId("tevedkdv");
        const tevdahtoptutEl = K.byId("tevdahtoptut");
        const beyedikdvEl = K.byId("beyedikdv");
        const tevhartoptutEl = K.byId("tevhartoptut");

        if (iskontoEl) iskontoEl.innerText = K._fmt2(double_1);
        if (bakiyeEl) bakiyeEl.innerText = K._fmt2(double_5 - double_1);
        if (kdvEl) kdvEl.innerText = K._fmt2(double_2);

        const tev = K._num(tevoran?.value) || 0;
        double_4 = tev;

        if (tevedkdvEl) tevedkdvEl.innerText = K._fmt2((double_2 / 10) * double_4);

        const double_0 = (double_5 - double_1) + double_2;

        if (tevdahtoptutEl) tevdahtoptutEl.innerText = K._fmt2(double_0);
        if (beyedikdvEl) beyedikdvEl.innerText = K._fmt2((double_2 - (double_2 / 10) * double_4));
        if (tevhartoptutEl) tevhartoptutEl.innerText = K._fmt2((double_5 - double_1) + (double_2 - (double_2 / 10) * double_4));

        if (totalTutarCell) totalTutarCell.textContent = total.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        if (totalMiktarCell) totalMiktarCell.textContent = totalMiktar.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
        if (totalM3Cell) totalM3Cell.textContent = totalm3.toLocaleString(undefined, { minimumFractionDigits: 3, maximumFractionDigits: 3 });
        if (totalPaketM3Cell) totalPaketM3Cell.textContent = totalpakm3.toLocaleString(undefined, { minimumFractionDigits: 3, maximumFractionDigits: 3 });
        if (totalSatirCell) totalSatirCell.textContent = totalsatir.toLocaleString(undefined, { minimumFractionDigits: 0, maximumFractionDigits: 0 });
    };

    /* ========================= anagrpChanged ========================= */
    K.anagrpChanged = async (anagrpElement) => {
        const anagrup = anagrpElement?.value || "";
        const selectElement = K.byId("altgrp");
        if (!selectElement) return;

        selectElement.innerHTML = "";

        if (anagrup === "") {
            selectElement.disabled = true;
            return;
        }

        K.setCursor("wait");
        K.clearError();

        try {
            const response = await window.fetchWithSessionCheck("kereste/altgrup", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ anagrup })
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            (response?.altKodlari || []).forEach((kod) => {
                const option = document.createElement("option");
                option.value = kod.ALT_GRUP;
                option.textContent = kod.ALT_GRUP;
                selectElement.appendChild(option);
            });

            selectElement.disabled = selectElement.options.length === 0;
        } catch (error) {
            selectElement.disabled = true;
            K.showError(error?.message || "Beklenmeyen bir hata oluştu.");
        } finally {
            K.setCursor("default");
        }
    };

    /* ========================= m3 hesap ========================= */
    K.hesapM3 = (ukodu, miktar) => {
        const token = (ukodu || "").split("-");
        if (token.length !== 4) return 0;

        const deger1 = token[1]?.trim();
        const deger2 = token[2]?.trim();
        const deger3 = token[3]?.trim();

        if (!deger1 || !deger2 || !deger3) return 0;

        return ((parseFloat(deger1) * parseFloat(deger2) * parseFloat(deger3)) * (Number(miktar) || 0)) / 1000000000;
    };

    K.hesaplaM3 = (inputElement) => {
        if (!inputElement) return;
        const row = inputElement.closest("tr");
        if (!row) return;

        const cells = row.querySelectorAll("td");
        const urkodCell = cells[2]?.querySelector("input")?.value?.trim();
        const miktarInput = cells[4]?.querySelector("input");
        if (!urkodCell || !miktarInput) return;

        const miktar = parseFloat(miktarInput.value) || 0;
        const m3 = K.hesapM3(urkodCell, miktar);

        const m3Cell = cells[5];
        if (m3Cell) {
            const span = m3Cell.querySelector("label span");
            if (span) span.textContent = Number(m3).toFixed(3);
        }
    };

    K.updateValues = async (inputElement) => {
        K.hesaplaM3(inputElement);
        K.updatePaketM3();
    };

    // Senin kodunda var; burada boş bırakıyorum (istersen doldururuz)
    K.updateRowValues = (inputElement) => {
        // barkod değişince vs. ne yapıyorsan burada
        // şimdilik sadece total hesap mantığı:
        K.updateColumnTotal();
    };

    /* ========================= focus navigation ========================= */
    K.focusNextRow = (event, element) => {
        if (event.key !== "Enter") return;
        event.preventDefault();

        const currentRow = element.closest("tr");
        const nextRow = currentRow?.nextElementSibling;

        if (nextRow) {
            const secondInput = nextRow.querySelector("td:nth-child(3) input");
            if (secondInput) { secondInput.focus(); secondInput.select?.(); }
            return;
        }

        K.satirekle();
        const tbody = currentRow?.parentElement;
        const newRow = tbody?.lastElementChild;
        const secondInput = newRow?.querySelector("td:nth-child(3) input");
        if (secondInput) { secondInput.focus(); secondInput.select?.(); }
    };

    K.focusNextCell = (event, element) => {
        if (event.key !== "Enter") return;
        event.preventDefault();

        let currentCell = element.closest("td");
        let nextCell = currentCell?.nextElementSibling;

        while (nextCell) {
            const focusable = nextCell.querySelector("input");
            if (focusable) {
                focusable.focus();
                focusable.select?.();
                break;
            }
            nextCell = nextCell.nextElementSibling;
        }
    };

    /* ========================= clearInputs ========================= */
    K.clearInputs = () => {
        const setText = (id, val) => { const el = K.byId(id); if (el) el.innerText = val; };
        const setVal = (id, val) => { const el = K.byId(id); if (el) el.value = val; };

        setText("iskonto", "0.00");
        setText("bakiye", "0.00");
        setText("kdv", "0.00");
        setText("tevedkdv", "0.00");
        setText("tevdahtoptut", "0.00");
        setText("beyedikdv", "0.00");
        setText("tevhartoptut", "0.00");

        setVal("tevoran", "0.00");
        setVal("ozelkod", "");
        setVal("anagrp", "");

        const altgrp = K.byId("altgrp");
        if (altgrp) { altgrp.innerHTML = ""; altgrp.disabled = true; }

        setVal("mensei", "");
        setVal("depo", "");
        setVal("nakliyeci", "");
        setVal("carikod", "");
        setVal("adreskod", "");

        setText("cariadilbl", "");
        setText("adresadilbl", "");

        const defaultDv = K.byId("defaultdvzcinsi")?.value || "TL";
        setVal("dovizcins", defaultDv);
        setVal("kur", "0.0000");

        setVal("not1", "");
        setVal("not2", "");
        setVal("not3", "");

        const fatmik = K.byId("fatmikyazdir");
        if (fatmik) fatmik.checked = false;

        setVal("a1", "");
        setVal("a2", "");

        const tbody = K.byId("tbody");
        if (tbody) tbody.innerHTML = "";

        K.rowCounter = 0;
        K.initializeRows();

        const setContent = (id, val) => { const el = K.byId(id); if (el) el.textContent = val; };
        setContent("totalSatir", K._fmt0(0));
        setContent("totalMiktar", K._fmt0(0));
        setContent("totalM3", K._fmt3(0));
        setContent("totalPaketM3", K._fmt3(0));
        setContent("totalTutar", K._fmt2(0));
        setContent("totalPakadet", "");
    };

    /* ========================= kerOku / sonfis / yenifis ========================= */
    K.kerOku = async () => {
        const fisno = (K.byId("fisno")?.value || "").trim();
        if (!fisno) return;

        K.setCursor("wait");

        try {
            const response = await window.fetchWithSessionCheck("kereste/kerOku", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ fisno, cins: "GIRIS" })
            });

            K.clearInputs();

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const data = response;
            const list = data?.data || [];
            if (!list.length) return;

            const table = K.byId("kerTable");
            const existingRows = table?.querySelectorAll("tbody tr") || [];

            if (list.length > existingRows.length) {
                const add = list.length - existingRows.length;
                for (let i = 0;i < add;i++) K.satirekle();
            }

            const rows = table.querySelectorAll("tbody tr");

            list.forEach((item, index) => {
                const cells = rows[index].cells;

                const barkodInput = cells[1]?.querySelector("input");
                if (barkodInput) barkodInput.value = item.Barkod || "";

                const urunKoduInput = cells[2]?.querySelector("input");
                if (urunKoduInput) urunKoduInput.value = item.Kodu || "";

                const paknoInput = cells[3]?.querySelector("input");
                if (paknoInput) paknoInput.value = item.Paket_No || "";

                const mikInput = cells[4]?.querySelector("input");
                if (mikInput) mikInput.value = K._fmt0(item.Miktar || 0);

                const m3Label = cells[5]?.querySelector("label span");
                if (m3Label) m3Label.textContent = K._fmt3(K.hesapM3(item.Kodu, item.Miktar));

                const pm3Label = cells[6]?.querySelector("label span");
                if (pm3Label) pm3Label.textContent = "";

                const konsInput = cells[7]?.querySelector("input");
                if (konsInput) konsInput.value = item.Konsimento || "";

                const fiatInput = cells[8]?.querySelector("input");
                if (fiatInput) fiatInput.value = K._fmt2(item.Fiat || 0);

                const iskInput = cells[9]?.querySelector("input");
                if (iskInput) iskInput.value = K._fmt2(item.Iskonto || 0);

                const kdvInput = cells[10]?.querySelector("input");
                if (kdvInput) kdvInput.value = K._fmt2(item.Kdv || 0);

                const tutarInput = cells[11]?.querySelector("input");
                if (tutarInput) tutarInput.value = K._fmt2(item.Tutar || 0);

                const izahatInput = cells[12]?.querySelector("input");
                if (izahatInput) izahatInput.value = item.Izahat || "";

                // gizli alanlar (metin)
                cells[13].innerText = item.Cikis_Evrak || "";
                cells[14].innerText = item.CTarih || "";
                cells[15].innerText = item.CKdv || "";
                cells[16].innerText = item.CDoviz || "";
                cells[17].innerText = item.CFiat || "";
                cells[18].innerText = item.CTutar || "";
                cells[19].innerText = item.CKur || "";
                cells[20].innerText = item.CCari_Firma || "";
                cells[21].innerText = item.CAdres_Firma || "";
                cells[22].innerText = item.CIskonto || "";
                cells[23].innerText = item.CTevkifat || "";
                cells[24].innerText = item.CAna_Grup || "";
                cells[25].innerText = item.CAlt_Grup || "";
                cells[26].innerText = item.CDepo || "";
                cells[27].innerText = item.COzel_Kod || "";
                cells[28].innerText = item.CIzahat || "";
                cells[29].innerText = item.CNakliyeci || "";
                cells[30].innerText = item.CUser || "";
                cells[31].innerText = item.CSatir || "";
            });

            // üst form alanları (ilk kaydı baz alıyorsun)
            const item = list[0];

            if (typeof window.formatdateSaatsiz === "function") {
                K.byId("fisTarih").value = window.formatdateSaatsiz(item.Tarih);
            }

            K.byId("anagrp").value = item.Ana_Grup || "";
            K.byId("kur").value = item.Kur;

            await K.anagrpChanged(K.byId("anagrp"));
            K.byId("altgrp").value = item.Alt_Grup || "";

            K.byId("ozelkod").value = item.Ozel_Kod || "";
            K.byId("mensei").value = item.Mensei || "";
            K.byId("depo").value = item.Depo || "";
            K.byId("nakliyeci").value = item.Nakliyeci || "";
            K.byId("tevoran").value = item.Tevkifat || "0";
            K.byId("carikod").value = item.Cari_Firma || "";
            K.byId("adreskod").value = item.Adres_Firma || "";
            K.byId("dovizcins").value = item.Doviz || "";

            K.byId("a1").value = data.a1;
            K.byId("a2").value = data.a2;
            K.byId("not1").value = data.dipnot?.[0] || "";
            K.byId("not2").value = data.dipnot?.[1] || "";
            K.byId("not3").value = data.dipnot?.[2] || "";

            K.updatePaketM3();
            K.updateColumnTotal();

            if (typeof window.hesapAdiOgren === "function") window.hesapAdiOgren(K.byId("carikod").value, "cariadilbl");
            if (typeof window.adrhesapAdiOgren === "function") window.adrhesapAdiOgren("adreskod", "adresadilbl");

            K.clearError();
        } catch (err) {
            K.showError(err?.message);
        } finally {
            K.setCursor("default");
        }
    };

    K.sonfis = async () => {
        K.setCursor("wait");
        try {
            const response = await window.fetchWithSessionCheck("kereste/sonfis", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ cins: "GIRIS" })
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const fisNoInput = K.byId("fisno");
            if (fisNoInput) fisNoInput.value = response.fisno;

            if (response?.fisNo === 0) {
                alert("Hata: Evrak numarası bulunamadı.");
                return;
            }

            if (response?.errorMessage) K.showError(response.errorMessage);
            else { K.clearError(); K.kerOku(); }
        } catch (err) {
            K.showError(err?.message);
        } finally {
            K.setCursor("default");
        }
    };

    K.yeniFis = async () => {
        K.clearError();
        K.clearInputs();
        K.setCursor("wait");

        try {
            const response = await window.fetchWithSessionCheck("kereste/yenifis", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ cins: "GIRIS" })
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            const fisNoInput = K.byId("fisno");
            if (fisNoInput) fisNoInput.value = response.fisno;
        } catch (err) {
            K.showError(err?.message);
        } finally {
            K.setCursor("default");
        }
    };

    /* ========================= paket kontrol ========================= */
    K.paketkontrol = async (event, input) => {
        const fisno = K.byId("fisno")?.value || "";

        K.clearError();

        const row = input?.closest("tr");
        if (!row) return;

        const cells = row.querySelectorAll("td");
        const konsCell = cells[7]?.querySelector("input")?.value?.trim() ?? "";
        if (!konsCell) {
            alert("Kontrol icin once Konsimento No giriniz!");
            return;
        }

        const pakno = input.value + "-" + konsCell;

        K.setCursor("wait");
        try {
            const response = await window.fetchWithSessionCheck("kereste/paket_oku", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ pno: pakno, cins: "GIRIS", fisno })
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            if (response?.mesaj) {
                K.setCursor("default");
                setTimeout(() => alert(response.mesaj), 100);
                return;
            }

            await K.updateValues(input);
            K.focusNextCell(event, input);
        } catch (err) {
            K.showError(err?.message);
        } finally {
            K.setCursor("default");
        }
    };

    /* ========================= format kontrol ========================= */
    K.kontrolFormat = (kod) => {
        const regex = /^[A-Z]{2}-\d{3}-\d{4}-\d{4}$/;
        return regex.test(kod);
    };

    K.kontrolEt = () => {
        const table = K.byId("kerTable")?.getElementsByTagName("tbody")[0];
        if (!table) return true;

        for (let row of table.rows) {
            // eski kod: row.cells[2].innerText -> input var, düzeltildi:
            const kod = row.cells[2]?.querySelector("input")?.value?.trim() || "";
            if (kod !== "" && !K.kontrolFormat(kod)) {
                alert(`Geçersiz kod bulundu: ${kod}`);
                return false;
            }
        }
        return true;
    };

    /* ========================= DTO hazırlama + table data ========================= */
    K.getTableData = () => {
        const table = K.byId("kerTable");
        const rows = table?.querySelectorAll("tbody tr") || [];
        const data = [];

        rows.forEach((row) => {
            const cells = row.querySelectorAll("td");
            const ukoduVal = cells[2]?.querySelector("input")?.value || "";

            if (ukoduVal.trim()) {
                data.push({
                    barkod: cells[1]?.querySelector("input")?.value || "",
                    ukodu: ukoduVal,
                    paketno: cells[3]?.querySelector("input")?.value || "",
                    miktar: K._num(cells[4]?.querySelector("input")?.value || 0),
                    konsimento: cells[7]?.querySelector("input")?.value || "",
                    fiat: K._num(cells[8]?.querySelector("input")?.value || 0),
                    iskonto: K._num(cells[9]?.querySelector("input")?.value || 0),
                    kdv: K._num(cells[10]?.querySelector("input")?.value || 0),
                    tutar: K._num(cells[11]?.querySelector("input")?.value || 0),
                    izahat: cells[12]?.querySelector("input")?.value || "",

                    cevrak: cells[13]?.textContent || "",
                    ctarih: cells[14]?.textContent || "1900-01-01 00:00:00.000",
                    ckdv: parseFloat((cells[15]?.textContent || "0").trim()) || 0.0,
                    cdoviz: cells[16]?.textContent || "",
                    cfiat: parseFloat((cells[17]?.textContent || "0").trim()) || 0.0,
                    ctutar: parseFloat((cells[18]?.textContent || "0").trim()) || 0.0,
                    ckur: parseFloat((cells[19]?.textContent || "0").trim()) || 0.0,
                    ccarifirma: cells[20]?.textContent || "",
                    cadresfirma: cells[21]?.textContent || "",
                    ciskonto: parseFloat((cells[22]?.textContent || "0").trim()) || 0.0,
                    ctevkifat: parseFloat((cells[23]?.textContent || "0").trim()) || 0.0,
                    canagrup: parseInt((cells[24]?.textContent || "0").trim(), 10) || 0,
                    caltgrup: parseInt((cells[25]?.textContent || "0").trim(), 10) || 0,
                    cdepo: parseInt((cells[26]?.textContent || "0").trim(), 10) || 0,
                    cozelkod: parseInt((cells[27]?.textContent || "0").trim(), 10) || 0,
                    cizahat: cells[28]?.textContent || "",
                    cnakliyeci: parseInt((cells[29]?.textContent || "0").trim(), 10) || 0,
                    cuser: cells[30]?.textContent || "",
                    csatir: parseInt((cells[31]?.textContent || "0").trim(), 10) || 0,

                    m3: K._num(cells[5]?.querySelector("label span")?.textContent || 0),
                    pakm3: K._num(cells[6]?.querySelector("label span")?.textContent || 0)
                });
            }
        });

        return data;
    };

    K.prepareureKayit = () => {
        const keresteDTO = {
            fisno: K.byId("fisno")?.value || "",
            tarih: K.byId("fisTarih")?.value || "",
            ozelkod: K.byId("ozelkod")?.value || "",
            anagrup: K.byId("anagrp")?.value || "",
            altgrup: K.byId("altgrp")?.value || "",
            depo: K.byId("depo")?.value || "",
            mensei: K.byId("mensei")?.value || "",
            nakliyeci: K.byId("nakliyeci")?.value || "",
            carikod: K.byId("carikod")?.value || "",
            adreskod: K.byId("adreskod")?.value || "",
            dvzcins: K.byId("dovizcins")?.value || "",
            kur: K._num(K.byId("kur")?.value) || 0.0,
            tevoran: K._num(K.byId("tevoran")?.value) || 0.0,
            not1: K.byId("not1")?.value || "",
            not2: K.byId("not2")?.value || "",
            not3: K.byId("not3")?.value || "",
            acik1: K.byId("a1")?.value || "",
            acik2: K.byId("a2")?.value || ""
        };

        const tableData = K.getTableData();
        return { keresteDTO, tableData };
    };

    /* ========================= kayit / yoket / download / mail ========================= */
    K.kerYoket = async () => {
        const fisno = (K.byId("fisno")?.value || "").trim();
        const table = K.byId("kerTable");
        const rows = table?.rows || [];

        if (!fisno || fisno === "0" || rows.length === 0) {
            alert("Geçerli bir evrak numarası giriniz.");
            return;
        }

        if (!confirm("Bu Fis silinecek ?")) return;

        K.setCursor("wait");
        K.setBtnBusy("kersilButton", true, "Siliniyor...", "Sil");

        try {
            const response = await window.fetchWithSessionCheck("kereste/fisYoket", {
                method: "POST",
                headers: { "Content-Type": "application/x-www-form-urlencoded" },
                body: new URLSearchParams({ fisno })
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            K.clearInputs();
            K.byId("fisno").value = "";
            K.clearError();
        } catch (err) {
            K.showError(err?.message);
        } finally {
            K.setCursor("default");
            K.setBtnBusy("kersilButton", false, "Siliniyor...", "Sil");
        }
    };

    K.kerKayit = async () => {
        const fisno = K.byId("fisno")?.value;
        const table = K.byId("kerTable");
        const rows = table?.rows || [];

        if (!fisno || fisno === "0" || rows.length === 0) {
            alert("Geçerli bir evrak numarası giriniz.");
            return;
        }

        if (!K.kontrolEt()) return;

        const payload = K.prepareureKayit();

        K.setCursor("wait");
        K.setBtnBusy("kerkaydetButton", true, "İşleniyor...", "Kaydet");
        K.clearError();

        try {
            const response = await window.fetchWithSessionCheck("kereste/girKayit", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            if (response?.errorMessage) throw new Error(response.errorMessage);

            K.clearInputs();
            K.byId("fisno").value = "";
            K.clearError();
        } catch (err) {
            K.showError(err?.message);
        } finally {
            K.setCursor("default");
            K.setBtnBusy("kerkaydetButton", false, "İşleniyor...", "Kaydet");
        }
    };

    K.cikisbilgiler = () => {
        const txt = (id) => (K.byId(id)?.innerText || "").trim();
        const tct = (id) => (K.byId(id)?.textContent || "").trim();

        return {
            totaltutar: txt("totalTutar"),
            totalmiktar: txt("totalMiktar"),
            totalm3: txt("totalM3"),
            totalpaketm3: txt("totalPaketM3"),
            paketsayi: tct("totalPakadet").replace("Paket:", "").trim() || "",

            iskonto: txt("iskonto"),
            bakiye: txt("bakiye"),
            kdv: txt("kdv"),
            tevedkdv: txt("tevedkdv"),
            tevdahtoptut: txt("tevdahtoptut"),
            beyedikdv: txt("beyedikdv"),
            tevhartoptut: txt("tevhartoptut")
        };
    };

    K.downloadgiris = async () => {
        const fisno = K.byId("fisno")?.value;
        const table = K.byId("kerTable");
        const rows = table?.rows || [];

        if (!fisno || fisno === "0" || rows.length === 0) {
            alert("Geçerli bir evrak numarasi giriniz.");
            return;
        }

        const dto = {
            ...K.prepareureKayit(),
            cikisbilgiDTO: K.cikisbilgiler()
        };

        K.setCursor("wait");
        K.setBtnBusy("girisdownloadButton", true, "İşleniyor...", "Yazdir");
        K.clearError();

        try {
            const response = await window.fetchWithSessionCheckForDownload("kereste/giris_download", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(dto)
            });

            if (response?.blob) {
                const disposition = response.headers.get("Content-Disposition");
                const fileName = disposition?.match(/filename="(.+)"/)?.[1] || "giris.pdf";

                const url = window.URL.createObjectURL(response.blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = fileName;
                document.body.appendChild(a);
                a.click();
                a.remove();
                window.URL.revokeObjectURL(url);
            } else {
                throw new Error("Dosya indirilemedi.");
            }
        } catch (err) {
            K.showError(err?.message);
        } finally {
            K.setCursor("default");
            K.setBtnBusy("girisdownloadButton", false, "İşleniyor...", "Yazdir");
        }
    };

    K.girismailAt = () => {
        localStorage.removeItem("tableData");
        localStorage.removeItem("grprapor");
        localStorage.removeItem("tablobaslik");

        const dto = {
            ...K.prepareureKayit(),
            cikisbilgiDTO: K.cikisbilgiler()
        };

        localStorage.setItem("keresteyazdirDTO", JSON.stringify(dto));

        const degerler = "kergiris";
        const url = `/send_email?degerler=${encodeURIComponent(degerler)}`;

        if (typeof window.mailsayfasiYukle === "function") {
            window.mailsayfasiYukle(url);
        } else {
            // fallback
            window.location.href = url;
        }
    };

    K.kercariIsle = async function() {
        const hesapKodu = K.byId("kerBilgi")?.value || "";
        const fisno = K.byId("fisno")?.value || "";

        const table = K.byId("kerTable");
        const rowsLen = table?.rows?.length || 0;

        if (!fisno || fisno === "0" || rowsLen === 0) {
            alert("Geçerli bir evrak numarası giriniz.");
            return;
        }

        K.clearError();
        document.body.style.cursor = "wait";
        K.setBtnBusy("carkayitButton", true, "İşleniyor...", "Cari Kaydet");

        const keresteDTO = {
            fisno: fisno,
            tarih: K.byId("fisTarih")?.value || "",
            carikod: K.byId("carikod")?.value || "",
            miktar: K.byId("totalM3")?.textContent || 0,
            tutar: parseLocaleNumber(K.byId("tevhartoptut")?.innerText || 0),
            karsihesapkodu: hesapKodu,
        };

        try {
            const response = await fetchWithSessionCheck("kereste/kergcariKayit", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(keresteDTO),
            });

            if (response?.errorMessage && response.errorMessage.trim() !== "") {
                throw new Error(response.errorMessage);
            }

            K.clearError();
        } catch (e) {
            K.showError(e?.message || "Beklenmeyen bir hata oluştu.");
        } finally {
            K.setBtnBusy("carkayitButton", false, "İşleniyor...", "Cari Kaydet");
            document.body.style.cursor = "default";
        }
    };


    /* ========================= init ========================= */
    K.init = () => {
        K.applyMask();
        // İstersen burada otomatik 5 satır oluştur:
        K.initializeRows();
    };


})(window, document);
