
loadSubjects();
async function loadSubjects() {
	document.body.style.cursor = "wait";
	const response = await fetch('/getSubjects', {
		method: 'GET',
		headers: {
			'Content-Type': 'application/json',
		},
	});
	if (response.ok) {
		const subjects = await response.json(); // Backend'den JSON verisi
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
                <h3>${subject.subjectTitle}</h3>
                <p>${subject.subjectDescription}</p>
                <button onclick="toggleCommentForm(${subject.subjectID})">Yorum Ekle</button>
                
                <!-- Form (Başlangıçta gizli) -->
                <div id="comment-form-${subject.subjectID}" class="comment-form" style="display: none; margin-top: 10px;">
                    <textarea placeholder="Yorum Yaz..." rows="3" style="width: 100%; padding: 10px;"></textarea>
                    <button onclick="submitComment(${subject.subjectID})" style="margin-top: 5px; padding: 10px; background-color: #6200ea; color: white; border: none; border-radius: 4px;">Yorum Kaydet</button>
                </div>
                
                <!-- Commits -->
                <div class="commits">
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
            </div>
        `;
		subjectSection.innerHTML += subjectElement;
	});
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
		alert('Please write a comment before submitting.');
		return;
	}
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
			},
		});
	} else {
		alert('Failed to add comment.');
	}
}


async function addSubject() {
	const title = document.querySelector('#subject-title').value;
	const description = document.querySelector('#subject-description').value;
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
			},
		});
	} else {
		alert('Failed to add subject.');
	}
}
