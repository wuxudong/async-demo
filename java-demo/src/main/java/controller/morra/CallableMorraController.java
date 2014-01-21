package controller.morra;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * User: xudong
 * Date: 8/19/13
 * Time: 1:15 PM
 */
@Controller
public class CallableMorraController {
    private RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = "/callable/morra")
    @org.springframework.web.bind.annotation.ResponseBody
    public Callable<String> morra(final @RequestParam("uids") String uidString) throws IOException {

        final String[] uids = uidString.split(",");
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                long start = System.currentTimeMillis();

                String winner = null;
                Integer max = Integer.MIN_VALUE;
                for (String uid : uids) {
                    int guess = Integer.valueOf(restTemplate.getForObject("http://127.0.0.1:8888/random?id=" + uid,
                            String.class));
                    if (guess > max) {
                        winner = uid;
                        max = guess;
                    }
                }

                long after = System.currentTimeMillis();
                System.out.println("morra " + uids.length +
                        " times spent " + (after - start));

                return winner + " " + max;
            }
        };
    }
}
