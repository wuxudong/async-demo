package controller.morra;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

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
public class DeferredResultMorraController {

    private AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();

    @RequestMapping(value = "/deferred/morra")
    @org.springframework.web.bind.annotation.ResponseBody
    public DeferredResult<String> morra(final @RequestParam("uids") String uidString) throws IOException {

        final long start = System.currentTimeMillis();

        final String[] uids = uidString.split(",");

        final AtomicInteger outstanding = new AtomicInteger(uids.length);

        final DeferredResult<String> deferredResult = new DeferredResult<String>();

        final Map<String, Integer> resultsSet = new ConcurrentHashMap<String,
                Integer>();

        for (final String uid : uids) {
            ListenableFuture<ResponseEntity<String>> futureEntity = asyncRestTemplate
                    .getForEntity("http://127.0.0.1:8888/random?id=" + uid, String.class);


            futureEntity.addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
                @Override
                public void onSuccess(ResponseEntity<String> result) {
                    resultsSet.put(uid, Integer.valueOf(result.getBody()));

                    if (outstanding.decrementAndGet() <= 0) {
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

                        deferredResult.setResult(winner + " " + max);
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                }
            });
        }


        return deferredResult;
    }
}