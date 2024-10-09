package xyz.sadiulhakim;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class StreamController {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @GetMapping("/stream")
    public StreamingResponseBody streamingResponseBody() {
        return outputStream -> {
            try {
                for (int i = 0; i < 10; i++) {
                    String text = "Streaming text line " + (i + 1) + "\n";
                    outputStream.write(text.getBytes());
                    outputStream.flush(); // Ensure the data is sent immediately
                    Thread.sleep(1000); // Simulate delay
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    @GetMapping(value = "/stream-v2")
    public ResponseBodyEmitter streamData() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    // Simulate data processing and streaming
                    String data = "Data: " + System.currentTimeMillis();
                    emitter.send(data); // Send data to the client

                    // Simulate delay for processing
                    TimeUnit.MILLISECONDS.sleep(100);
                }
                emitter.complete(); // Mark the emitter as complete
            } catch (Exception e) {
                emitter.completeWithError(e); // Handle errors
            }
        });

        return emitter; // Return the emitter to the client
    }
}
