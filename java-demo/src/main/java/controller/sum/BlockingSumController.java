package controller.sum;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

/**
 * User: xudong
 * Date: 8/19/13
 * Time: 1:15 PM
 */
@Controller
public class BlockingSumController {

    private RestTemplate restTemplate = new RestTemplate();

    @RequestMapping(value = "/blocking/sum")
    @org.springframework.web.bind.annotation.ResponseBody
    public String sum() throws IOException {
        long start = System.currentTimeMillis();

        int add1 = Integer.valueOf(restTemplate.getForObject("http://127.0.0.1:8888/random",
                String.class));
        int add2 = Integer.valueOf(restTemplate.getForObject("http://127.0.0.1:8888/random",
                String.class));

        int sum = Integer.valueOf(restTemplate.getForObject(String.format("http://127.0.0.1:8888/add?p1=%d&p2=%d",
                add1, add2),
                String.class));

        long after = System.currentTimeMillis();
        System.out.println("sum  times spent " + (after - start));

        return String.format("%d + %d = %d", add1, add2, sum);
    }
}
