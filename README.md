
# MVC Streaming

| Feature                      | StreamingResponseBody                             | SseEmitter                                      | ResponseBodyEmitter                             | InputStreamResource                               |
 |------------------------------|--------------------------------------------------|------------------------------------------------|------------------------------------------------|--------------------------------------------------|
| **Purpose**                  | Stream data incrementally to the client          | Send Server-Sent Events (SSE) to clients      | Stream data to the client with support for async| Stream files or resources directly to the client  |
| **Use Cases**                | Large datasets, long-running processes            | Real-time updates, notifications                | Long-running processes, dynamic content         | Large files, dynamic content, file downloads      |
| **Memory Efficiency**        | Yes, does not load entire response into memory    | Yes, allows for incremental data without buffering| Yes, can handle large datasets without buffering | Yes, streams directly from an InputStream         |
| **Connection Handling**      | Non-blocking, allows other requests to be handled | Long-lived connections for real-time updates    | Can be asynchronous and allows for chunked data | Managed by Spring, auto-closed after response     |
| **Client Support**           | Any HTTP client that can handle streaming         | Browsers supporting SSE                          | Any HTTP client that can handle chunked responses| Any HTTP client                                   |
| **Typical Use in Web Apps**  | Sending large CSV/JSON files, data processing    | Live notifications (e.g., chat apps)           | Generating reports, sending logs                | File downloads, serving media                     |
| **Complexity**               | Simple to implement                               | Requires client-side handling of SSE            | More complex due to manual flushing              | Simple to implement                               |
 
# Client Server connection

| Feature                      | SseEmitter                                      | WebSocket                                      |
|------------------------------|------------------------------------------------|------------------------------------------------|
| **Purpose**                  | Send Server-Sent Events (SSE) to clients      | Full-duplex communication channel              |
| **Connection Type**          | Unidirectional (server to client)              | Bidirectional (client and server)             |
| **Use Cases**                | Real-time updates, notifications                | Interactive applications (e.g., chat apps, gaming) |
| **Protocol**                 | HTTP-based (SSE over HTTP)                     | WebSocket protocol                             |
| **Performance**              | Generally lower overhead for simple updates     | Higher performance for low-latency interaction |
| **Statefulness**             | Stateless connection (each request is independent)| Stateful connection (persistent connection)    |
| **Scalability**              | Easier to scale with HTTP load balancers       | Requires specific handling for scaling (sticky sessions, etc.) |
| **Browser Support**          | Native support in modern browsers               | Native support in modern browsers              |
| **Complexity**               | Simpler to implement and manage                 | More complex due to bi-directional communication |
| **Data Format**              | Text/event-stream (mostly text-based)          | Can support various formats (text, binary)    |

# Streaming data and files

| Feature                      | StreamingResponseBody                             | InputStreamResource                              |
|------------------------------|--------------------------------------------------|--------------------------------------------------|
| **Purpose**                  | Stream data incrementally to the client          | Stream files or resources directly to the client |
| **Connection Type**          | Non-blocking, can be asynchronous                | Typically used for blocking I/O operations       |
| **Use Cases**                | Large datasets, long-running processes            | Serving large files, file downloads               |
| **Memory Efficiency**        | Yes, streams data without loading it entirely into memory | Yes, streams data directly from an InputStream    |
| **Response Type**            | Written directly to the response output stream   | Served as a resource in the response body         |
| **Client Support**           | Any HTTP client that can handle streaming         | Any HTTP client that can download resources       |
| **Data Handling**            | Allows for dynamic and incremental data generation | Suitable for static file serving                  |
| **Complexity**               | Slightly more complex due to streaming logic      | Simpler to implement for file serving             |
| **Flushing Behavior**        | Flushes data in chunks while writing              | Data is sent in one go once the resource is read  |
| **Typical Use in Web Apps**  | Sending large CSV/JSON files, data processing     | Serving images, PDFs, or other static files      |

| Feature         | `ResponseBodyEmitter` | `SseEmitter` |
|---------------|-------------------|-------------|
| **Use Case** | General-purpose response streaming (e.g., JSON, plain text) | Server-Sent Events (SSE) specifically |
| **Content Type** | Not restricted, default is `application/octet-stream` | Automatically sets `text/event-stream` |
| **Event Formatting** | Sends raw data as-is | Formats messages in SSE format (`data: <message>\n\n`) |
| **Client Compatibility** | Requires custom client handling | Works natively with `EventSource` in JavaScript |
| **Connection Handling** | Standard HTTP response | SSE-specific features like reconnection support |

| Feature                 | `StreamingResponseBody` | `ResponseBodyEmitter` | `InputStreamResource` |
|-------------------------|------------------------|-----------------------|-----------------------|
| **Use Case**           | Large binary/text streaming | Asynchronous response streaming | Serving large files or input streams |
| **Streaming Mechanism** | Uses OutputStream for efficient byte streaming | Allows sending multiple async chunks | Wraps an InputStream for direct response |
| **Memory Efficiency**   | Very efficient, writes directly to OutputStream | Efficient but buffers chunks in memory | Efficient for file-based responses |
| **Client Compatibility** | Works well with large JSON responses | Good for real-time event-based streaming | Best for file downloads, may not be ideal for JSON |
| **Best For 100MB JSON?** | ✅ Yes, best choice | ❌ No, not optimal for large JSON | ❌ No, better for static files |

