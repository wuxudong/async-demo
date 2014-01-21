package controller.sum;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.dispatch.Mapper;
import akka.io.IO;
import akka.pattern.Patterns;
import akka.util.Timeout;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.async.DeferredResult;
import scala.Tuple2;
import scala.concurrent.Future;
import spray.can.Http;
import spray.http.HttpResponse;
import spray.httpx.RequestBuilding$;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


/**
 * User: xudong
 * Date: 8/19/13
 * Time: 1:15 PM
 */
@Controller
public class ScalaSumController {

    private ActorSystem system = ActorSystem.apply();

    private ActorRef io = IO.apply(Http.lookup(), system);

    @RequestMapping(value = "/scala/sum")
    @org.springframework.web.bind.annotation.ResponseBody
    public DeferredResult<String> sum() throws IOException {
        final long start = System.currentTimeMillis();

        final DeferredResult<String> deferredResult = new DeferredResult<String>();

        Future<Integer> p1Future = Patterns.ask(io, RequestBuilding$.MODULE$.Get().apply
                ("http://127.0.0.1:8888/random"),
                Timeout.apply(3, TimeUnit.SECONDS)).map(new Mapper<Object, Integer>() {
            @Override
            public Integer apply(Object parameter) {
                return Integer.valueOf(((HttpResponse) parameter).entity().asString());
            }
        }, system.dispatcher());


        Future<Tuple2<Integer, Integer>> p1p2Future = p1Future.flatMap(new Mapper<Integer, Future<Tuple2<Integer, Integer>>>() {

            @Override
            public Future<Tuple2<Integer, Integer>> apply(final Integer p1) {
                return Patterns.ask(io, RequestBuilding$.MODULE$.Get().apply
                        ("http://127.0.0.1:8888/random"),
                        Timeout.apply(3, TimeUnit.SECONDS)).map(new Mapper<Object, Tuple2<Integer, Integer>>() {
                    @Override
                    public Tuple2<Integer, Integer> apply(Object parameter) {
                        int p2 = Integer.valueOf(((HttpResponse) parameter).entity().asString());
                        return new Tuple2<Integer, Integer>(p1, p2);
                    }
                }, system.dispatcher());

            }

        }, system.dispatcher());

        p1p2Future.flatMap(new Mapper<Tuple2<Integer, Integer>, Future<Integer>>() {
            @Override
            public Future<Integer> apply(final Tuple2<Integer, Integer> tuple) {
                return Patterns.ask(io, RequestBuilding$.MODULE$.Get().apply
                        (String.format("http://127.0.0.1:8888/add?p1=%d&p2=%d", tuple._1(), tuple._2())),
                        Timeout.apply(3, TimeUnit.SECONDS)).map(new Mapper<Object, Integer>() {
                    @Override
                    public Integer apply(Object parameter) {
                        int sum = Integer.valueOf(((HttpResponse) parameter).entity().asString());
                        long after = System.currentTimeMillis();
                        System.out.println("sum  times spent " + (after - start));
                        deferredResult.setResult(String.format("%d + %d = %d", tuple._1(), tuple._2(), sum));
                        return sum;
                    }
                }, system.dispatcher());
            }

        }, system.dispatcher());

        return deferredResult;
    }
}
