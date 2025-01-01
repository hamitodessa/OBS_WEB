async function fetchWithSessionCheck(url, options = {}) {
    document.body.style.cursor = "wait";
    try {
        const response = await fetch(url, options);
        const responseText = await response.text();
        if (responseText.includes("<html")) {
            if (responseText.includes('<form') && responseText.includes('name="username"')) {
                window.location.href = "/login";
                return null;
            } else {
                throw new Error("Beklenmeyen bir HTML yanıtı alındı.");
            }
        }
        return JSON.parse(responseText);
    } catch (error) {
        throw error;
    } finally {
        document.body.style.cursor = "default";
    }
}