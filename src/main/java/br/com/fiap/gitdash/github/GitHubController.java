package br.com.fiap.gitdash.github;
 
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import java.util.List;
 
@Controller
public class GitHubController {
 
    private final GitHubService gitHubService;
    private final RestTemplate restTemplate;
 
    public GitHubController(GitHubService gitHubService, RestTemplate restTemplate) {
        this.gitHubService = gitHubService;
        this.restTemplate = restTemplate;
    }
 
    @GetMapping("/")
    public String getUserInfo(Model model, @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient) {
        String tokenValue = authorizedClient.getAccessToken().getTokenValue();
 
        // Obter informações do usuário
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + tokenValue);
        HttpEntity<String> entity = new HttpEntity<>(headers);
 
        ResponseEntity<String> userResponse = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                entity,
                String.class
        );
 
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode userNode = mapper.readTree(userResponse.getBody());
 
            model.addAttribute("name", userNode.path("name").asText());
            model.addAttribute("avatar_url", userNode.path("avatar_url").asText());
            model.addAttribute("html_url", userNode.path("html_url").asText());
 
        } catch (Exception e) {
            e.printStackTrace();
        }
 
        // Obter repositórios
        List<RepositoryInfo> repos = gitHubService.getUserRepositories(tokenValue);
        model.addAttribute("repos", repos);
 
        return "user";
    }
}
 