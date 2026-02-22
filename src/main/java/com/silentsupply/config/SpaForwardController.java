package com.silentsupply.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards non-API, non-static routes to index.html for client-side routing.
 * Only active when the SPA has been built and placed in static resources.
 */
@Controller
public class SpaForwardController {

    /**
     * Forwards all non-API paths to the SPA's index.html.
     * This allows React Router to handle client-side routes like /messages, /login, etc.
     * Paths with file extensions (e.g., .js, .css) are excluded so static assets load normally.
     *
     * @return forward to index.html
     */
    @GetMapping(value = {
            "/{path:[^\\.]*}",
            "/{path1:[^\\.]*}/{path2:[^\\.]*}",
            "/{path1:[^\\.]*}/{path2:[^\\.]*}/{path3:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
