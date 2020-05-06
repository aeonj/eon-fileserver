package eon.hg.fileserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/")
    public String init() {
        return "toIndex";
    }

    @RequestMapping("/list")
    public String list() {
        return "toList";
    }

    @RequestMapping("/login")
    public String login() {
        return "toLogin";
    }

    @RequestMapping("/index")
    public String index() {
        return "toIndex";
    }
}
