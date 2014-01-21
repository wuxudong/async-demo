package controller.morra;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.AsyncRestTemplate;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * User: xudong
 * Date: 8/19/13
 * Time: 1:15 PM
 */
@Controller
public class AsyncMorraController {
    public static final String RESULT = "result";

    private AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();

    @RequestMapping(value = "/async/morra")
    @org.springframework.web.bind.annotation.ResponseBody
    public void morra(final @RequestParam("uids") String uidString,
                      HttpServletRequest request, final HttpServletResponse httpServletResponse) throws
            IOException {
        final long start = System.currentTimeMillis();

        final String[] uids = uidString.split(",");

        final AtomicInteger outstanding = new AtomicInteger(uids.length);

        if (request.getAttribute(RESULT) == null) {
            final Map<String, Integer> resultsSet = new ConcurrentHashMap<String, Integer>();
            request.setAttribute(RESULT, resultsSet);

            final AsyncContext async = request.startAsync();
            async.setTimeout(10000);


            for (final String uid : uids) {
                ListenableFuture<ResponseEntity<String>> futureEntity = asyncRestTemplate
                        .getForEntity("http://127.0.0.1:8888/random?id=" + uid, String.class);


                futureEntity.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
                    @Override
                    public void onSuccess(ResponseEntity<String> result) {
                        resultsSet.put(uid, Integer.valueOf(result.getBody()));

                        if (outstanding.decrementAndGet() <= 0) {
                            try {
                                String winner = null;
                                Integer max = Integer.MIN_VALUE;

                                for (Map.Entry<String, Integer> entry : resultsSet.entrySet()) {

                                    if (entry.getValue() > max) {
                                        winner = entry.getKey();
                                        max = entry.getValue();
                                    }
                                }

                                long after = System.currentTimeMillis();

                                System.out.println("morra " + uids.length +
                                        " times spent " + (after - start));

                                httpServletResponse.getWriter().write(winner + " " + max);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            async.dispatch();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }
    }
}
