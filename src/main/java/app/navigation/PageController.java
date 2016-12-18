package app.navigation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {
	@RequestMapping("/")
	public String index() {
		return "index";
	}

	@RequestMapping("/test/")
	public String dbTest() {
		return "test/index";
	}
}
