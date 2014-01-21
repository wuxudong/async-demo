package controller.sum;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;

/**
 * User: xudong
 * Date: 8/19/13
 * Time: 1:15 PM
 */
@Controller
public class DeferredResultSumController {

    private AsyncRestTemplate asyncRestTemplate = new AsyncRestTemplate();

    @RequestMapping(value = "/deferred/sum")
    @org.springframework.web.bind.annotation.ResponseBody
    public DeferredResult<String> sum() throws IOException {

        final long start = System.currentTimeMillis();

        final DeferredResult<String> deferredResult = new DeferredResult<String>();
        asyncRestTemplate
                .getForEntity("http://127.0.0.1:8888/random", String.class).addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
            @Override
            public void onSuccess(ResponseEntity<String> result) {
                final int p1 = Integer.valueOf(result.getBody());

                asyncRestTemplate
                        .getForEntity("http://127.0.0.1:8888/random", String.class).addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
                    @Override
                    public void onSuccess(ResponseEntity<String> result) {
                        final int p2 = Integer.valueOf(result.getBody());

                        asyncRestTemplate.getForEntity(String.format("http://127.0.0.1:8888/add?p1=%d&p2=%d", p1,
                                p2), String.class).addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {
                            @Override
                            public void onSuccess(ResponseEntity<String> result) {
                                int sum = Integer.valueOf(result.getBody());
                                long after = System.currentTimeMillis();
                                System.out.println("sum  times spent " + (after - start));
                                deferredResult.setResult(String.format("%d + %d = %d", p1, p2, sum));
                            }

                            @Override
                            public void onFailure(Throwable t) {
                                t.printStackTrace();
                            }
                        });

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });

            }

            @Override
            public void onFailure(Throwable t) {
                t.printStackTrace();
            }
        });

        return deferredResult;
    }
}