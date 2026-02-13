async function fetchWithSessionCheckForm(url) {
    document.body.style.cursor = "wait";
    try {
        const res = await fetch(url, {
            method: "GET",
            credentials: "same-origin",
            headers: { "X-Requested-With": "XMLHttpRequest" },
            cache: "no-store"
        });
        if (res.status === 401) {
            window.location.href = "/login";
            return null;
        }
        const text = await res.text();
        if (text.includes("<form") && text.includes('name="username"')) {
            window.location.href = "/login";
            return null;
        }
        if (!res.ok) {
            throw new Error(`${res.status} ${res.statusText}`);
        }
        return text;
    } finally {
        document.body.style.cursor = "default";
    }
}