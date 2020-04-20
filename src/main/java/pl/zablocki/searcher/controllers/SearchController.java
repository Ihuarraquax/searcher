package pl.zablocki.searcher.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.zablocki.searcher.dto.PageDto;
import pl.zablocki.searcher.services.SearcherService;

import java.util.List;

@Controller
public class SearchController {

    private final SearcherService searcherService;

    public SearchController(SearcherService searcherService) {
        this.searcherService = searcherService;
    }

    @GetMapping(path = "/")
    public String index(){
        return "index";
    }

    @PostMapping()
    public String processAdding(@RequestParam String url){

        searcherService.add(url);
        return "index";
    }

    @PostMapping(path = "/search")
    public String processSearching(@RequestParam String phrase, Model model){

        List<PageDto> pages = searcherService.search(phrase);
        model.addAttribute("pages", pages);
        return "index";
    }
}
