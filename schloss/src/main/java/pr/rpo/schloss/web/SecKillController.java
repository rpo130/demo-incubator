package pr.rpo.schloss.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

@Controller
@RequestMapping("/seckill")
public class SecKillController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private SubmissionPublisher<Integer> sp = new SubmissionPublisher<>();

    private int num = 1000;

    @GetMapping("")
    public String seckillPage() {
        return "secKill";
    }

    @RequestMapping(value = "/notify",produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public SseEmitter notifyPush() {
        SseEmitter sseEmitter = new SseEmitter(0l);

        Flow.Subscriber subscriber = new Flow.Subscriber() {
            private Flow.Subscription subscription;

            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                this.subscription.request(1);
            }

            @Override
            public void onNext(Object item) {
                try {
                    sseEmitter.send(item);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                this.subscription.request(1);
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        };

        sp.subscribe(subscriber);

        return sseEmitter;
    }

    @GetMapping("order")
    @ResponseBody
    public String order() {
        sp.submit(num);
        return "order -1 success:" + this.num;
    }

    private void decrease() {
        this.num--;
    }

    private void increase() {
        this.num++;
    }
}
