package pooltable.PoolTable;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path="/pooltable")
public class MainController {

    @GetMapping(path="/dupa")
    public @ResponseBody String printDupa() {
        return "Dupa";
    }
}
