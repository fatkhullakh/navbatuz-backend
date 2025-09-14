package uz.navbatuz.backend.publicshare;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
public class PublicShortRedirects {

    @GetMapping("/p/{id}")
    public RedirectView provider(@PathVariable String id) {
        return new RedirectView("/public/p/" + id);
    }

    @GetMapping("/s/{id}")
    public RedirectView service(@PathVariable String id) {
        return new RedirectView("/public/s/" + id);
    }
}
