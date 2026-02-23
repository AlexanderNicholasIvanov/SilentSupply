package com.silentsupply.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards non-API, non-static routes to index.html for client-side routing.
 * Only active when the SPA has been built and placed in static resources.
 */
@Controller
public class SpaForwardController {

    /** Regex that matches a path segment without dots, excluding backend prefixes. */
    private static final String SEG = "(?!api$|ws$|swagger-ui$|api-docs$)[^\\.]*";

    /**
     * Forwards SPA routes to index.html for client-side routing.
     * Excludes backend paths (api, ws, swagger-ui, api-docs) and paths with file
     * extensions (.js, .css, etc.) so they are handled by their respective controllers.
     *
     * @return forward to index.html
     */
    @GetMapping(value = {
            "/{path:" + SEG + "}",
            "/{path1:" + SEG + "}/{path2:[^\\.]*}",
            "/{path1:" + SEG + "}/{path2:[^\\.]*}/{path3:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
