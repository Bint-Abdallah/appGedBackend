package org.example.appgedbackend.config;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    // Redirige toutes les routes non API vers index.html
    @RequestMapping(value = {
            "/{path:[^\\.]*}",
            "/**/{path:[^\\.]*}"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}
