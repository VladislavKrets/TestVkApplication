package com.vkfriends.task;

import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.UserAuthResponse;
import com.vk.api.sdk.objects.account.UserSettings;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Controller
public class Controller {
    private final static int APP_ID = 5811050;
    private final static String CLIENT_SECRET = "knSc6wK92p9T0wYN3Uzs";
    private final static String REDIRECT_URI = "http://185.247.117.223:8080";
    private final static String BASE_URL = "https://oauth.vk.com/authorize?" +
            "client_id=5811050" +
            "&display=page" +
            "&redirect_uri=" + REDIRECT_URI + "/vkauth" +
            "&scope=friends,status,offline" +
            "&response_type=code" +
            "&v=5.103";

    private String token;
    private TransportClient transportClient;
    private VkApiClient vk;
    private int id;

    @GetMapping("/")
    public String authenticate(@CookieValue(value = "token", defaultValue = "") String token,
                               @CookieValue(value = "id", defaultValue = "") String id) {
        if (!token.isEmpty()) {
            this.token = token;
            this.id = Integer.parseInt(id);
            return "redirect:/friends";
        }

        return "auth";
    }

    @PostMapping("/")
    public RedirectView postAuthenticate() {
        return new RedirectView(BASE_URL);
    }

    @GetMapping("/friends")
    public String friends(Model model) {
        if (token == null) return "redirect:/";
        if (transportClient == null || vk == null) {
            transportClient = HttpTransportClient.getInstance();
            vk = new VkApiClient(transportClient);
        }
        UserActor actor = new UserActor(id, token);
        try {
            UserXtrCounters userXtrCountersProfile = vk.users().get(actor).userIds(String.valueOf(id)).execute().get(0);
            StringBuilder name = new StringBuilder();
            name.append(userXtrCountersProfile.getFirstName()).append(" ").append(userXtrCountersProfile.getLastName());
            model.addAttribute("profile_name", name.toString());
            name = new StringBuilder();
            List<String> list = vk.friends().get(actor).count(5).count(5).execute().getItems()
                    .stream().map(String::valueOf).collect(Collectors.toList());
            List<UserXtrCounters> userXtrCountersList = vk.users().get(actor).userIds(list).execute();


            for (UserXtrCounters userXtrCounters : userXtrCountersList) {
                name.append("<br>").append(userXtrCounters.getFirstName()).append(" ").append(userXtrCounters.getLastName());
            }

            model.addAttribute("name", name.toString());
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
        return "friends";
    }

    @GetMapping("/vkauth")
    public String vkAuth(@RequestParam String code, HttpServletResponse response) {
        transportClient = HttpTransportClient.getInstance();
        vk = new VkApiClient(transportClient);
        try {
            UserAuthResponse authResponse = vk.oAuth()
                    .userAuthorizationCodeFlow(APP_ID, CLIENT_SECRET, REDIRECT_URI + "/vkauth", code)
                    .execute();
            token = authResponse.getAccessToken();
            Cookie cookie = new Cookie("token", token);
            response.addCookie(cookie);
            id = authResponse.getUserId();
            cookie = new Cookie("id", String.valueOf(id));
            response.addCookie(cookie);
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
        return "redirect:/friends";
    }

    @PostMapping("/friends")
    public String exit(HttpServletResponse response){
        Cookie cookie = new Cookie("token", "");
        response.addCookie(cookie);
        cookie = new Cookie("id", "");
        response.addCookie(cookie);
        token = null;
        id = 0;
        return "redirect:/";
    }
}
