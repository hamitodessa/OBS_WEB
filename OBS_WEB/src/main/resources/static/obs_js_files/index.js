$(document).ready(function () {
	$('.changeLink').on('click', function (event) {
		event.preventDefault();
		document.getElementById("baslik").innerText = "";
		const url = $(this).attr('data-url');
		$('body').css('cursor', 'wait');
		$.ajax({
			url: url,
			type: "GET",
			success: function (data) {
				if (data.includes('<form') && data.includes('name="username"')) {
					window.location.href = "/login";
				} else {
					$('#ara_content').html(data);
					const action = urlActions[url];
					if (action) action();
				}
			},
			error: function (xhr) {
				if (xhr.status === 401) {
					window.location.href = "/login";
				} else {
					$('#ara_content').html('<h2>Bir hata oluştu: ' + xhr.status + ' - ' + xhr.statusText + '</h2>');
				}
			},
			complete: function () {
				$('body').css('cursor', 'default');
			}
		});
	});

	const urlActions = {
		"/cari/ekstre": cariBaslik,
		"/cari/mizan": cariBaslik,
		"/cari/ozelmizan": cariBaslik,
		"/cari/dvzcevirme": cariBaslik,
		"/cari/hspplngiris": () => {
			cariBaslik();
			hsparamaYap();
			document.getElementById("arama").value = "";
		},
		"/cari/dekont": cariBaslik,
		"/cari/tahsilat": () => {
			cariBaslik();
			fetchHesapKodlariOnce();
		},
		"/cari/tahsilatdegerleri": () => {
			cariBaslik();
			tahayarIlk();
		},
		"/cari/tahsilatrapor": cariBaslik,
		"/cari/hspplnliste": cariBaslik,
		"/cari/ornekhesapplani": cariBaslik,
		"/cari/carikoddegis": cariBaslik,
		"/user/userdetails": () => {
			detailoku();
		},
		"/user/userizinler": () => {
			adminbaglihesapoku();
		},
		"/adres/adresgiris": () => {
			adresBaslik();
			adraramaYap();
			document.getElementById("arama").value = "";
		},
		"/adres/etiketliste": adresBaslik,
		"/adres/etiketayar": adresBaslik,
		"/kur/kurgiris": () => {
			kuroku();
		},
		"/kambiyo/cekgiris": () => {
			kambiyoBaslik();
			fetchBankaSubeOnce();
		},
		"/kambiyo/cekcikis": () => {
			kambiyoBaslik();
			fetchcekno();
		},
		"/kambiyo/cektakip": kambiyoBaslik,
		"/kambiyo/cekrapor": kambiyoBaslik,
		"/stok/urunkart": () => {
			stokBaslik();
			urnaramaYap();
			document.getElementById("arama").value = "";
		},
		"/stok/uretim": () => {
			stokBaslik();
			fetchkoddepo();
		},
		"/stok/fatura": () => {
			stokBaslik();
			fetchkoddepo();
		},
	}
});