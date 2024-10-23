package xyz.sadiulhakim;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * | Feature                      | StreamingResponseBody                             | SseEmitter                                      | ResponseBodyEmitter                             | InputStreamResource                               |
 * |------------------------------|--------------------------------------------------|------------------------------------------------|------------------------------------------------|--------------------------------------------------|
 * | **Purpose**                  | Stream data incrementally to the client          | Send Server-Sent Events (SSE) to clients      | Stream data to the client with support for async| Stream files or resources directly to the client  |
 * | **Use Cases**                | Large datasets, long-running processes            | Real-time updates, notifications                | Long-running processes, dynamic content         | Large files, dynamic content, file downloads      |
 * | **Memory Efficiency**        | Yes, does not load entire response into memory    | Yes, allows for incremental data without buffering| Yes, can handle large datasets without buffering | Yes, streams directly from an InputStream         |
 * | **Connection Handling**      | Non-blocking, allows other requests to be handled | Long-lived connections for real-time updates    | Can be asynchronous and allows for chunked data | Managed by Spring, auto-closed after response     |
 * | **Client Support**           | Any HTTP client that can handle streaming         | Browsers supporting SSE                          | Any HTTP client that can handle chunked responses| Any HTTP client                                   |
 * | **Typical Use in Web Apps**  | Sending large CSV/JSON files, data processing    | Live notifications (e.g., chat apps)           | Generating reports, sending logs                | File downloads, serving media                     |
 * | **Complexity**               | Simple to implement                               | Requires client-side handling of SSE            | More complex due to manual flushing              | Simple to implement                               |
 */

@RestController
public class StreamController {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss a z");

    @GetMapping("/stream")
    public StreamingResponseBody streamingResponseBody() {
        return outputStream -> {
            try {
                for (int i = 0; i < 100; i++) {
                    String text = "Streaming text line " + (i + 1) + "\n";
                    outputStream.write(text.getBytes());
                    outputStream.flush(); // Ensure the data is sent immediately
                    Thread.sleep(100); // Simulate delay
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * | Feature               | `ResponseBodyEmitter`                          | `SseEmitter`                            |
     * |-----------------------|------------------------------------------------|-----------------------------------------|
     * | **Use Case**           | One-time streaming of large data or long-running task | Real-time event push to client          |
     * | **Communication**      | One-time, incremental HTTP response            | Persistent, continuous real-time updates |
     * | **Connection**         | Closed after response completion               | Open until explicitly closed            |
     * | **Protocol**           | Plain HTTP                                     | Server-Sent Events (SSE)                |
     * | **Browser Support**    | Custom handling on the client                  | Native `EventSource` API in browsers    |
     * | **Data Type**          | Arbitrary (HTML, JSON, binary)                 | Text/event-stream (SSE specific)        |
     * | **Bi-directional**     | No (but traditional HTTP can handle it)        | No (only server to client)              |
     * | **Backpressure**       | Not inherently supported                       | Managed efficiently for real-time small data |
     */

    /**
     * Suitable for general streaming of data from the server to the client.
     * Can be used for various types of responses (e.g., HTML, JSON, binary files).
     * One-time response: Once the data is fully sent, the connection is closed. Typically used for large files, long-running tasks, or a chunked response.
     * The client sends an HTTP request and gets data progressively, but once the entire response is sent, the connection is terminated.
     * <p>
     * When to use ResponseBodyEmitter:
     * When you need to stream a large amount of data that can be sent progressively (e.g., file download, report generation).
     * When you don’t need to keep the connection open after the data has been sent.
     * When you want to push a single response but want it to arrive incrementally (e.g., long-running HTTP request).
     */
    @GetMapping(value = "/stream-v2")
    public ResponseBodyEmitter streamData() {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();

        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    // Simulate data processing and streaming
                    String data = (i + 1) + ". Data: " + formatter.format(ZonedDateTime.now()) + "\n";
                    emitter.send(data, MediaType.TEXT_PLAIN); // Send data to the client

                    // Simulate delay for processing
                    TimeUnit.MILLISECONDS.sleep(100);
                }
                emitter.complete(); // Mark the emitter as complete
            } catch (Exception e) {
                emitter.completeWithError(e); // Handle errors
            } finally {
                emitter.complete();
            }
        });

        return emitter; // Return the emitter to the client
    }

    /**
     * Specifically for Server-Sent Events (SSE), a web standard that allows servers to push updates to the client continuously over an open connection.
     * The client subscribes to the server to receive real-time events.
     * Persistent connection: The connection remains open until explicitly closed, allowing the server to push multiple events over time. Ideal for real-time notifications, stock prices, or live updates.
     * Unidirectional: Data flows only from the server to the client.
     * <p>
     * When to use SseEmitter:
     * When you need real-time updates and the client needs to stay connected to receive continuous data from the server.
     * For live feeds, such as stock market updates, notifications, or live events.
     * When you need the connection to be long-lived and continuously send small bits of data over time.
     */
    @GetMapping(value = "/stream-v3")
    public SseEmitter streamSse() {
        SseEmitter emitter = new SseEmitter();

        executor.submit(() -> {
            try {
                for (int i = 0; i < 100; i++) {
                    // Simulate data processing and streaming
                    String data = (i + 1) + ". Data: " + formatter.format(ZonedDateTime.now()) + "\n";
                    emitter.send(data, MediaType.TEXT_PLAIN); // Send data to the client

                    // Simulate delay for processing
                    TimeUnit.MILLISECONDS.sleep(500);
                }
                emitter.complete(); // Mark the emitter as complete
            } catch (Exception e) {
                emitter.completeWithError(e); // Handle errors
            } finally {
                emitter.complete();
            }
        });

        return emitter; // Return the emitter to the client
    }

    /**
     * Overview of InputStreamResource
     * Purpose: InputStreamResource is a convenient way to represent a data source as an InputStream in Spring. It allows you to stream the content of large files directly to the client without loading the entire file into memory.
     * Use Case: It is particularly useful for sending large files (like images, videos, or large datasets) or generating dynamic content on-the-fly without the need to store it entirely in memory.
     *
     * Key Features
     * Streaming: Allows for the progressive reading of data from a resource (like a file) as it's being sent to the client. This is beneficial for handling large files that may not fit into memory.
     * Memory Efficiency: Since it streams the data directly from an InputStream, it minimizes memory usage, making it suitable for applications where memory resources are constrained.
     * Content-Type Support: You can easily set the content type and headers, allowing you to serve files with appropriate MIME types.
     *
     * When to Use InputStreamResource
     * Large Files: When you need to send large files (like media files, documents, or reports) to clients, and you want to avoid loading the entire file into memory.
     * Dynamic Content: When generating content dynamically, such as CSV reports or other formats, where data is produced incrementally.
     * File Downloads: Ideal for implementing download functionality in web applications, where files can be sent as an HTTP response.
     */
    @GetMapping("/file-stream")
    public ResponseEntity<InputStreamResource> streamFile() {
        ClassPathResource resource = new ClassPathResource("static/text.txt");
        try {
            InputStreamResource streamResource = new InputStreamResource(resource.getInputStream());

            // Set headers for the response
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=text.txt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // Set appropriate content type
                    .body(streamResource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
