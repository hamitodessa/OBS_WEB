
loadSubjects();
async function loadSubjects() {
	const mesajsayi = document.querySelector('#mesajadet'); 
	mesajsayi.innerText = '';
	document.body.style.cursor = "wait";
	const response = await fetch('/getSubjects', {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json',
		},
	});
	if (response.ok) {
		const subjects = await response.json(); // Backend'den JSON verisi
		allSubjects = subjects;
		renderSubjects(subjects);
		document.body.style.cursor = "default";
	} else {
		document.body.style.cursor = "default";
	}
};


function renderSubjects(subjects) {
	if (!subjects || !Array.isArray(subjects)) {
		console.error("Geçersiz subject verisi:", subjects);
		return;
	}
	const subjectSection = document.querySelector('.subject-section');
	subjectSection.innerHTML = '';
	subjects.forEach((subject) => {
		const commits = subject.commits || [];
		const subjectElement = `
            <div class="subject">
                <h4 style="font-weight: bold;">${subject.subjectTitle}</h4>
                <p style="border: 1px solid #ddd; padding: 10px; border-radius: 4px;">${subject.subjectDescription}</p>
                <button onclick="toggleCommentForm(${subject.subjectID})">Yorum Ekle</button>

                <!-- Commits Göstermek/Gizlemek için Buton -->
                <button onclick="toggleCommits(${subject.subjectID})" style="margin-top: 10px;">Yorumları Göster</button>

                <!-- Yorumlar -->
                <div id="commits-${subject.subjectID}" class="commits" style="display: none; margin-top: 10px;border: 1px solid #ddd;">
                    ${commits
				.map(
					(commit) => `
                        <div class="commit">
                            <h4>By: ${commit.createdBy}</h4>
                            <p>${commit.commitText}</p>
                        </div>`
				)
				.join('')}
                </div>

                <!-- Yorum Formu -->
                <div id="comment-form-${subject.subjectID}" class="comment-form" style="display: none; margin-top: 10px;">
                    <textarea id="comment-text-${subject.subjectID}" placeholder="Yorum Yaz..." rows="3" style="width: 100%; padding: 10px;" maxlength="255"></textarea>
                    <button onclick="submitComment(${subject.subjectID})" style="margin-top: 5px; padding: 10px; background-color: #6200ea; color: white; border: none; border-radius: 4px;">Yorum Kaydet</button>
                </div>
            </div>
        `;
		subjectSection.innerHTML += subjectElement;
	});
}

function filterSubjects() {
	const searchTerm = document.getElementById('search-input').value.toLowerCase();
	if (searchTerm.trim() === '') {
		renderSubjects(allSubjects);
		return;
	}
	const filteredSubjects = allSubjects.filter((subject) => {
		const titleMatch = subject.subjectTitle.toLowerCase().includes(searchTerm);
		const descriptionMatch = subject.subjectDescription.toLowerCase().includes(searchTerm);
		return titleMatch || descriptionMatch;
	});
	if (filteredSubjects.length === 0) {
		document.querySelector('.subject-section').innerHTML = '<p>Sonuç bulunamadı.</p>';
	} else {
		renderSubjects(filteredSubjects);
	}
}

function toggleCommits(subjectId) {
	const commitsDiv = document.getElementById(`commits-${subjectId}`);
	if (!commitsDiv) {
		console.error(`Yorumlar için div bulunamadı: commits-${subjectId}`);
		return;
	}
	const isHidden = commitsDiv.style.display === 'none';
	commitsDiv.style.display = isHidden ? 'block' : 'none';
	const button = commitsDiv.previousElementSibling;
	button.textContent = isHidden ? 'Yorumları Gizle' : 'Yorumları Göster';
}


function toggleCommentForm(subjectId) {
	const form = document.getElementById(`comment-form-${subjectId}`);
	if (!form) {
		console.error(`Form with id comment-form-${subjectId} not found.`);
		return;
	}
	form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

async function submitComment(subjectId) {
	const commentText = document.getElementById(`comment-text-${subjectId}`).value;
	if (!commentText) {
		alert('Lütfen bir yorum yazın.');
		return;
	}
	const saveButton = document.querySelector(`#comment-form-${subjectId} button`);
	saveButton.textContent = "İşlem yapılıyor...";
	saveButton.disabled = true;
	document.body.style.cursor = "wait";
	try {
		const response = await fetch(`addComment/${subjectId}`, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ text: commentText }),
		});

		if (response.ok) {
			const url = "/forum";
			$.ajax({
				url: url,
				type: "GET",
				success: function(data) {
					if (data.includes('<form') && data.includes('name="username"')) {
						window.location.href = "/login";
					} else {
						$('#ara_content').html(data);
					}
				},
				error: function(xhr) {
					$('#ara_content').html('<h2>Bir hata oluştu: ' + xhr.statusText + '</h2>');
				},
				complete: function() {
					document.body.style.cursor = "default";
					saveButton.textContent = "Kaydet";
					saveButton.disabled = false;
				},
			});
		} else {
			alert('Yorum eklenemedi.');
		}
	} catch (error) {
		console.error(error.message);
	} finally {
		document.body.style.cursor = "default";
		saveButton.textContent = "Yorum Kaydet";
		saveButton.disabled = false;
	}
}

async function addSubject() {
	const title = document.querySelector('#subject-title').value;
	const description = document.querySelector('#subject-description').value;
	if (!title || !description) {
		alert('Lütfen hem başlık hem de açıklama alanlarını doldurun.');
		return;
	}
	const saveButton = document.getElementById('saveButton');
	saveButton.textContent = "İşlem yapılıyor...";
	saveButton.disabled = true;
	document.body.style.cursor = "wait";
	try {
		const response = await fetch('addSubject', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ title, description }),
		});
		if (response.ok) {
			const url = "/forum";
			$.ajax({
				url: url,
				type: "GET",
				success: function(data) {
					if (data.includes('<form') && data.includes('name="username"')) {
						window.location.href = "/login";
					} else {
						$('#ara_content').html(data);
					}
				},
				error: function(xhr) {
					$('#ara_content').html('<h2>Bir hata oluştu: ' + xhr.statusText + '</h2>');
				},
				complete: function() {
					document.body.style.cursor = "default";
					saveButton.textContent = "Kaydet";
					saveButton.disabled = false;
				},
			});
		} else {
			alert('Hata oluştu');
		}
	} catch (error) {
		console.error("Hata oluştu:", error);
	} finally {
		document.body.style.cursor = "default";
		saveButton.textContent = "Kaydet";
		saveButton.disabled = false;
	}
}