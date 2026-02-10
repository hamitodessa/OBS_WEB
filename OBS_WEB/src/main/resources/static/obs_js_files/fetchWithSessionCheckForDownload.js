async function fetchWithSessionCheckForDownload(url, options = {}) {
    try {
        const response = await fetch(url, options);
        const contentType = response.headers.get("Content-Type");
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || `Beklenmeyen hata: ${response.status}`);
        }
        if (contentType) {
            if (contentType.includes("text/html")) {
                const responseText = await response.text();
                if (responseText.includes('<form') && responseText.includes('name="username"')) {
                    window.location.href = "/login";
                    return null;
                }
                throw new Error("Beklenmeyen bir HTML yanıtı alındı.");
            }
            if (contentType.includes("application")) {
                return {
                    blob: await response.blob(),
                    headers: response.headers,
                };
            }
            if (contentType.includes("application/json")) {
                return await response.json();
            }
        }
        throw new Error("Bilinmeyen içerik türü alındı.");
    } catch (error) {
        throw error;
    }
}