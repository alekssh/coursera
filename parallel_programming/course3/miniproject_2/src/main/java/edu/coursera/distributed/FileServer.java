package edu.coursera.distributed;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs     A proxy filesystem to serve files from. See the PCDPFilesystem
     *               class for more detailed documentation of its usage.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs)
            throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */
        while (true) {


            try (Socket clientSocket = socket.accept();
                 InputStream is = clientSocket.getInputStream();
                 OutputStream os = clientSocket.getOutputStream();
                 Reader reader = new BufferedReader(new InputStreamReader(is));
                 PrintWriter writer = new PrintWriter(os)

            ) {


                String request = ((BufferedReader) reader).readLine();

                String[] parsedRequest = request.split(" ");

                assert "GET".equals(parsedRequest[0]);

                String path = parsedRequest[1];


                String file = fs.readFile(new PCDPPath(path));

                if (file != null) {
                    writer.write("HTTP/1.0 200 OK\r\n");
                    writer.write("Server: FileServer\r\n\r\n");
                    writer.write(file);
                    writer.write("\r\n");
                } else {
                    writer.write("HTTP/1.0 404 Not Found\r\n");
                    writer.write("Server: FileServer\r\n\r\n");
                }

            }
        }
    }
}
