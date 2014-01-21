package controller.morra;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.dispatch.Mapper;
import akka.io.IO;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.async.DeferredResult;
import scala.Tuple2;
import scala.concurrent.Future;
import spray.can.Http;
import spray.http.HttpResponse;
import spray.httpx.RequestBuilding$;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * User: xudong
 * Date: 8/19/13
 * Time: 1:15 PM
 */
@Controller
public class ScalaMorraController {

    private ActorSystem system = ActorSystem.apply();

    private ActorRef io = IO.apply(Http.lookup(), system);

    @RequestMapping(value = "/scala/morra")
    @org.springframework.web.bind.annotation.ResponseBody
    public DeferredResult<String> morra(final @RequestParam("uids") String uidString) throws IOException {
        final long start = System.currentTimeMillis();

        final String[] uids = uidString.split(",");

        final DeferredResult<String> deferredResult = new DeferredResult<String>();

        List<Future<Tuple2<String, Integer>>> futures = new ArrayList<Future<Tuple2<String, Integer>>>();

        for (final String uid : uids) {
            Future<Object> ask = Patterns.ask(io, RequestBuilding$.MODULE$.Get().apply
                    ("http://127.0.0.1:8888/random?id=" + uid),
                    Timeout.apply(3, TimeUnit.SECONDS));

            futures.add(ask.map(new Mapper<Object, Tuple2<String, Integer>>() {
                @Override
                public Tuple2<String, Integer> apply(Object parameter) {
                    return new Tuple2<String, Integer>(uid, Integer.valueOf(((HttpResponse) parameter).entity().asString()));
                }
            }, system.dispatcher()));
        }

        Futures.sequence(futures, system.dispatcher()).map(new Mapper<Iterable<Tuple2<String, Integer>>, Tuple2<String, Integer>>() {
            @Override
            public Tuple2<String, Integer> apply(Iterable<Tuple2<String, Integer>> parameter) {
                String winner = null;
                Integer max = Integer.MIN_VALUE;

                for (Tuple2<String, Integer> guess : parameter) {
                    if (guess._2() > max) {
                        winner = guess._1();
                        max = guess._2();
                    }
                }

                long after = System.currentTimeMillis();

                System.out.println("morra " + uids.length +
                        " times spent " + (after - start));

                deferredResult.setResult(winner + " " + max);

                return new Tuple2<String, Integer>(winner, max);
            }
        }, system.dispatcher());

        return deferredResult;
    }
}
