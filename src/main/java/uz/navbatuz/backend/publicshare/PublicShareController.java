package uz.navbatuz.backend.publicshare;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import uz.navbatuz.backend.provider.model.Provider;
import uz.navbatuz.backend.provider.repository.ProviderRepository;
import uz.navbatuz.backend.service.model.ServiceEntity;
import uz.navbatuz.backend.service.repository.ServiceRepository;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class PublicShareController {

    private final ProviderRepository providerRepo;
    private final ServiceRepository  serviceRepo;

    // --------- SHORT PATHS: /p/{id} and /s/{id} → redirect to /public/... (cleaner for scraping) ---------
    @GetMapping("/p/{id}")
    public ResponseEntity<Void> shortProvider(@PathVariable String id) {
        return ResponseEntity.status(302).location(URI.create("/public/p/" + id)).build();
    }

    @GetMapping("/s/{id}")
    public ResponseEntity<Void> shortService(@PathVariable String id) {
        return ResponseEntity.status(302).location(URI.create("/public/s/" + id)).build();
    }

    // --------- OG PAGES (web scrapers) + instant redirect into SPA ---------
    @GetMapping(value = "/public/p/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> providerOg(@PathVariable String id) {
        Provider p = providerRepo.findById(parse(id))
                .orElse(null);

        String title = (p != null ? safe(p.getName()) : "Provider");
        String desc  = (p != null ? safe(Optional.ofNullable(p.getDescription()).orElse("Visit on Birzum")) : "Visit on Birzum");
        String image = (p != null && p.getLogoUrl() != null) ? abs(p.getLogoUrl()) : abs("/assets/og/default_provider.png");
        String target = "/#/provider/" + id;  // IMPORTANT: your Flutter route

        String html = ogHtml(
                title,
                desc,
                image,
                target
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=600")
                .body(html);
    }

    @GetMapping(value = "/public/s/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> serviceOg(@PathVariable String id) {
        ServiceEntity s = serviceRepo.findById(parse(id)).orElse(null);

        String title = (s != null ? safe(s.getName()) : "Service");
        String desc  = (s != null ? safe(Optional.ofNullable(s.getDescription()).orElse("Book on Birzum")) : "Book on Birzum");
        String image = (s != null && s.getImageUrl() != null) ? abs(s.getImageUrl()) : abs("/assets/og/default_service.png");
        String target = "/#/service/" + id;   // If you deep-link to service page

        String html = ogHtml(
                title,
                desc,
                image,
                target
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=600")
                .body(html);
    }

    /* ================= helpers ================= */

    private static UUID parse(String s) {
        try { return UUID.fromString(s); } catch (Exception e) { return null; }
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;")
                .replace(">","&gt;").replace("\"","&quot;");
    }

    // Build absolute URL for images if client saved a relative path like /uploads/...
    private static String abs(String url) {
        if (url == null || url.isBlank()) return "";
        if (url.startsWith("http://") || url.startsWith("https://")) return url;
        // point at your public site (web app) or API that serves /uploads
        return "https://www.birzum.app" + (url.startsWith("/") ? url : ("/" + url));
    }

    private static String ogHtml(String title, String desc, String image, String target) {
        StringBuilder b = new StringBuilder();
        b.append("<!doctype html><html lang=\"en\"><head>")
                .append("<meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(title).append("</title>")
                // OpenGraph / Twitter
                .append("<meta property=\"og:type\" content=\"website\"/>")
                .append("<meta property=\"og:title\" content=\"").append(title).append("\"/>")
                .append("<meta property=\"og:description\" content=\"").append(desc).append("\"/>")
                .append("<meta property=\"og:image\" content=\"").append(image).append("\"/>")
                .append("<meta property=\"og:url\" content=\"https://www.birzum.app").append(target).append("\"/>")
                .append("<meta name=\"twitter:card\" content=\"summary_large_image\"/>")
                .append("<meta name=\"twitter:title\" content=\"").append(title).append("\"/>")
                .append("<meta name=\"twitter:description\" content=\"").append(desc).append("\"/>")
                .append("<meta name=\"twitter:image\" content=\"").append(image).append("\"/>")
                // Immediate redirect for humans (scrapers ignore)
                .append("<meta http-equiv=\"refresh\" content=\"0;url=").append(target).append("\">")
                .append("</head><body>")
                .append("<noscript><a href=\"").append(target).append("\">Open</a></noscript>")
                .append("<script>location.replace('").append(escapeForJs(target)).append("');</script>")
                .append("</body></html>");
        return b.toString();
    }

    private static String escapeForJs(String s) {
        return s.replace("\\", "\\\\").replace("'", "\\'");
    }
}
